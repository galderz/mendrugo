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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
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
        description = "Number of classes"
        , names = {"-c", "--number-classes"}
    )
    private int numClasses = 10;

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

//    void generateReflectionFeature() throws IOException
//    {
//        final var packageHosted = "org.graalvm.nativeimage.hosted";
//
//        final var methodBuilder = MethodSpec.methodBuilder("beforeAnalysis")
//            .addModifiers(Modifier.PUBLIC)
//            .addParameter(ClassName.get(packageHosted, "BeforeAnalysisAccess"), "access");
//
//        methodBuilder
//            .beginControlFlow("try")
//            .addStatement("$L.$L()", packageHosted)
//            .nextControlFlow("catch ($T e)", Exception.class)
//            .addStatement("throw new $T(e)", RuntimeException.class)
//            .endControlFlow();
//
//        TypeSpec type = TypeSpec.classBuilder("ReflectionFeature")
//            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//            .superclass(ClassName.get(packageHosted, "Feature"))
//            .addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "AutomaticFeature"))
////            .addMethod(main)
////            .addMethod(assertFruits)
////            .addMethod(assertFruit)
//            .build();
//
//        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type).build();
//        javaFile.writeTo(generatedSourcesDirectory);
//    }

    void generateMain() throws IOException
    {
        final var assertFruitsBuilder = MethodSpec.methodBuilder("assertFruits")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class);

        IntStream.range(0, numClasses)
            .forEach(index -> assertFruitsBuilder.addStatement("assertFruit($L)", index));

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
            .addMethod(assertFruitsBuilder.build())
            .addMethod(assertFruit)
            .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type).build();
        javaFile.writeTo(generatedSourcesDirectory);
    }

    MethodSpec generateAssertFruit()
    {
        return MethodSpec.methodBuilder("assertFruit")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(int.class, "index")
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("$T.out.println(\"Assert fruit \" + index)", System.class)
            .addStatement("Class<?> clazz = Class.forName(\"$L.Fruit\" + index)", PACKAGE_NAME)
            .addStatement("assert (\"Fruit\" + index).equals(clazz.getSimpleName())")
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

    void generateFruits()
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

        IntStream.range(0, numClasses)
            .mapToObj(reflection_poet::toType)
            .map(reflection_poet.addMembers(fields, getters, setters))
            .map(reflection_poet::toJavaFile)
            .forEach(this::writeJavaFile);
    }

    void writeJavaFile(JavaFile javaFile)
    {
        try
        {
            javaFile.writeTo(generatedSourcesDirectory);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    static JavaFile toJavaFile(TypeSpec type)
    {
        return JavaFile.builder(PACKAGE_NAME, type).build();
    }

    static Function<TypeSpec.Builder, TypeSpec> addMembers(List<FieldSpec> fields, List<MethodSpec> getters, List<MethodSpec> setters)
    {
        return typeBuilder ->
        {
            fields.forEach(typeBuilder::addField);
            getters.forEach(typeBuilder::addMethod);
            setters.forEach(typeBuilder::addMethod);
            return typeBuilder.build();
        };
    }

    static TypeSpec.Builder toType(int index)
    {
        return TypeSpec.classBuilder("Fruit" + index)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
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
