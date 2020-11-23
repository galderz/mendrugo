///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.2
//DEPS com.squareup:javapoet:1.13.0

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Command(
    description = "Generate java source files and GraalVM/Mandrel reflection registrations"
    , mixinStandardHelpOptions = true
    , name = "reflection-poet"
)
public class reflection_poet implements Callable<Integer>
{
    public static final String PACKAGE_NAME = "com.example.reflection";

    @Option(
        description = "Number of fields per type"
        , names = {"-f", "--number-fields"}
    )
    private int numFields = 10;

    @Option(
        description = "Generated sources directory"
        , names = {"-g", "--generated-sources-directory"}
    )
    private Path generatedSourcesDirectory = Path.of("target", "generated-sources");

    @Override
    public Integer call() throws Exception
    {
        generateFruits();
        // generateReflectionFeature();
        generateMain();
        return 0;
    }

    void generateMain() throws IOException
    {
        MethodSpec assertFruits = MethodSpec.methodBuilder("assertFruits")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("assertFruit()")
            .build();

        MethodSpec assertFruit = generateAssertFruit();

        MethodSpec main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println($S)", System.class, "Check asserts enabled...")
            .addStatement("$1L.Asserts.check()", PACKAGE_NAME)
            .addStatement("$T.out.println($S)", System.class, "Run assertions...")
            .addStatement("assertFruits()")
            .addStatement("$T.out.println($S)", System.class, "Assertions successfully passed")
            .build();

        TypeSpec type = TypeSpec.classBuilder("Main")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
            .addMethod(assertFruits)
            .addMethod(assertFruit)
            .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type)
            .build();

        javaFile.writeTo(generatedSourcesDirectory);
    }

    MethodSpec generateAssertFruit()
    {
        return MethodSpec.methodBuilder("assertFruit")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("Class<?> clazz = Class.forName($S)", String.format("%s.%s", PACKAGE_NAME, "Fruit0"))
            .addStatement("assert \"Fruit0\".equals(clazz.getSimpleName())")
            .beginControlFlow("for (int i = $L; i < $L; i++)", 0, numFields)
            .addStatement("String fieldName = $S + i", "attr_")
            .addStatement("assert fieldName.equals(clazz.getField(fieldName).getName())")
            .addStatement("String getterName = $S + i", "getAttr")
            .addStatement("assert getterName.equals(clazz.getMethod(getterName).getName())")
            .addStatement("String setterName = $S + i", "setAttr")
            .addStatement("assert setterName.equals(clazz.getMethod(setterName, String.class).getName())")
            .endControlFlow()
            .build();
    }

    void generateFruits() throws Exception
    {
        final var fields = IntStream.range(0, numFields)
            .mapToObj(reflection_poet::toField)
            .collect(Collectors.toList());

        final var getters = IntStream.range(0, numFields)
            .mapToObj(reflection_poet::toGetter)
            .collect(Collectors.toList());

        final var setters = IntStream.range(0, numFields)
            .mapToObj(reflection_poet::toSetter)
            .collect(Collectors.toList());

        final var typeBuilder = TypeSpec.classBuilder("Fruit0")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        fields.forEach(typeBuilder::addField);
        getters.forEach(typeBuilder::addMethod);
        setters.forEach(typeBuilder::addMethod);

        final var type = typeBuilder.build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type)
            .build();

        javaFile.writeTo(generatedSourcesDirectory);
    }

    static MethodSpec toSetter(int fieldNumber)
    {
        final var builder = MethodSpec.methodBuilder(
            String.format("setAttr%d", fieldNumber)
        );

        return builder
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(String.class, "name")
            .addStatement("this.attr_$L = name", fieldNumber)
            .build();
    }


    static MethodSpec toGetter(int fieldNumber)
    {
        final var builder = MethodSpec.methodBuilder(
            String.format("getAttr%d", fieldNumber)
        );

        return builder
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return attr_$L", fieldNumber)
            .build();
    }

    static FieldSpec toField(int fieldNumber)
    {
        final var builder = FieldSpec.builder(
            String.class
            , String.format("attr_%d", fieldNumber)
        );

        return builder
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new reflection_poet()).execute(args);
        System.exit(exitCode);
    }
}
