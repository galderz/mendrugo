///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.2
//DEPS com.squareup:javapoet:1.13.0

import com.squareup.javapoet.ClassName;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Command(name = "make", mixinStandardHelpOptions = true, version = "make 0.1",
    description = "make made with jbang")
class make implements Callable<Integer>
{
    public static final String PACKAGE_NAME = "com.example.stringconcat";

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
        final TypeSpec aggregator = makeAggregator(types);
        final TypeSpec main = makeMain(aggregator);

        types.add(aggregator);
        types.add(main);

        types.stream()
            .map(type -> JavaFile.builder(PACKAGE_NAME, type).build())
            .forEach(javaFile ->
                    CheckedConsumer.<Path>of(javaFile::writeTo).unchecked()
                        .accept(generatedSourcesDirectory)
            );

        return 0;
    }

    private TypeSpec makeMain(TypeSpec aggregator)
    {
        MethodSpec main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println(new $L())", System.class, aggregator.name)
            .build();

        return TypeSpec.classBuilder("Main")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
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

        return IntStream.range(0, numTypes)
            .mapToObj(i ->
                TypeSpec
                    .classBuilder("Fruit" + i)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL))
            .peek(typeBuilder -> typeBuilder.addFields(fields))
            .map(TypeSpec.Builder::build)
            .collect(Collectors.toList());
    }

    private TypeSpec makeAggregator(List<TypeSpec> typeSpecs)
    {
        final List<FieldSpec> fields = typeSpecs.stream()
            .map(type ->
                FieldSpec
                    .builder(ClassName.get(PACKAGE_NAME, type.name), type.name.toLowerCase(Locale.ROOT))
                    .addModifiers(Modifier.PUBLIC)
                    .build()
            )
            .collect(Collectors.toList());

        final MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class);

        toString.addCode("return \"Basket{\"\n");
        fields.forEach(field -> toString.addCode(String.format("+ \" %s=\" + %s\n", field.name, field.name)));
        toString.addStatement("+ \"}\"");

        return TypeSpec.classBuilder("Basket")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(toString.build())
            .addFields(fields)
            .build();
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
