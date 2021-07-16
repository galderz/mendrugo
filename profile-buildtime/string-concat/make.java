///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.2
//DEPS com.squareup:javapoet:1.13.0

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Command(name = "make", mixinStandardHelpOptions = true, version = "make 0.1",
    description = "make made with jbang")
class make implements Callable<Integer>
{
    public static final String PACKAGE_NAME = "com.example.stringconcat";

    @Option(
        description = "Number of aggregate types"
        , names = {"-a", "--number-aggregate-types"}
    )
    private int numAggregates = 5;

    @Option(
        description = "Number of types"
        , names = {"-t", "--number-types"}
    )
    private int numTypes = 25;

    @Option(
        description = "Number of fields per type"
        , names = {"-f", "--number-fields"}
    )
    private int numFields = 5;

    @Option(
        description = "Generated sources directory"
        , names = {"-g", "--generated-sources-directory"}
    )
    private Path generatedSourcesDirectory = Path.of("target", "generated-sources");

    public static void main(String... args)
    {
        int exitCode = new CommandLine(new make()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()
    {
        final List<TypeSpec> types = makeTypes(numTypes, numFields);
        final List<TypeSpec> aggregators = makeAggregators(numAggregates, types);
        final TypeSpec main = makeMain(aggregators);

        types.addAll(aggregators);
        types.add(main);

        types.stream()
            .map(type -> JavaFile.builder(PACKAGE_NAME, type).build())
            .forEach(javaFile ->
                    CheckedConsumer.<Path>of(javaFile::writeTo).unchecked()
                        .accept(generatedSourcesDirectory)
            );

        return 0;
    }

    private TypeSpec makeMain(List<TypeSpec> aggregators)
    {
        MethodSpec.Builder main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(String[].class, "args");

        aggregators
            .forEach(aggregator ->
                main.addStatement("$T.out.println(new $L())", System.class, aggregator.name)
            );

        return TypeSpec.classBuilder("Main")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main.build())
            .build();
    }

    private List<TypeSpec> makeTypes(int numTypes, int numFields)
    {
        final List<FieldSpec> fields = IntStream.range(0, numFields)
            .mapToObj(i ->
                FieldSpec
                    .builder(String.class, String.format("attr_%d", i))
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
            .collect(Collectors.toList());

        final MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class);

        toString.addCode("return \"Fruit{\"\n");
        fields.forEach(field -> toString.addCode(String.format("+ \" %s=\" + %s\n", field.name, field.name)));
        toString.addStatement("+ \"}\"");

        return IntStream.range(0, numTypes)
            .mapToObj(i ->
                TypeSpec
                    .classBuilder("Fruit" + i)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            )
            .peek(typeBuilder -> typeBuilder.addFields(fields))
            .peek(typeBuilder -> typeBuilder.addMethod(toString.build()))
            .map(TypeSpec.Builder::build)
            .collect(Collectors.toList());
    }

    private List<TypeSpec> makeAggregators(int numAggregates, List<TypeSpec> typeSpecs)
    {
        final List<FieldSpec> fields = typeSpecs.stream()
            .map(type ->
                FieldSpec
                    .builder(ClassName.get(PACKAGE_NAME, type.name), type.name.toLowerCase(Locale.ROOT))
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
            .collect(Collectors.toList());

        final List<MethodSpec> toStringMethods = IntStream.range(0, numAggregates)
            .mapToObj(i ->
                MethodSpec.methodBuilder("toString")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addCode("return \"Basket" + i + "{\"\n")
                    .addCode(toStringFields(numTypes - i, fields).build())
                    .addCode("+ \"}\";")
                    .build()
            )
            .collect(Collectors.toList());

        return IntStream.range(0, numAggregates)
            .mapToObj(i ->
                TypeSpec.classBuilder("Basket" + i)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(toStringMethods.get(i))
                    .addFields(fields)
                    .build()
            )
            .collect(Collectors.toList());
    }

    private CodeBlock.Builder toStringFields(int numStringFields, List<FieldSpec> fields)
    {
        return IntStream.range(0, numStringFields)
            .mapToObj(i -> String.format("+ \" %s=\" + %s\n", fields.get(i).name, fields.get(i).name))
            .collect(
                CodeBlock::builder
                , CodeBlock.Builder::add
                , (b1, b2) -> {}
            );
    }

    public interface CheckedConsumer<T>
    {
        void accept(T t) throws Throwable;

        static <T> CheckedConsumer<T> of(CheckedConsumer<T> methodReference) {
            return methodReference;
        }

        default Consumer<T> unchecked() {
            return t -> {
                try {
                    accept(t);
                } catch(Throwable x) {
                    sneakyThrow(x);
                }
            };
        }

        static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }
}
