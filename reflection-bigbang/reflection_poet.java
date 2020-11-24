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
    public static final String PACKAGE_HOSTED = "org.graalvm.nativeimage.hosted";

    @Override
    public Integer call() throws Exception
    {
        generateFruits();
        generateReflectionFeature();
        generateMain();
        return 0;
    }

    void generateReflectionFeature() throws IOException
    {
        final var registerFruits = registerFruits();
        final var registerFruit = registerFruit();
        final var registerFruitFieldsAndMethods = registerFruitFieldsAndMethods();

        final var beforeAnalysis = beforeAnalysis();

        TypeSpec type = TypeSpec.classBuilder("ReflectionFeature")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ClassName.get(PACKAGE_HOSTED, "Feature"))
            .addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "AutomaticFeature"))
            .addMethod(beforeAnalysis)
            .addMethod(registerFruits)
            .addMethod(registerFruit)
            .addMethod(registerFruitFieldsAndMethods)
            .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type).build();
        javaFile.writeTo(generatedSourcesDirectory);
    }

    private MethodSpec beforeAnalysis()
    {
        return MethodSpec.methodBuilder("beforeAnalysis")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ClassName.get(PACKAGE_HOSTED, "Feature.BeforeAnalysisAccess"), "access")
            .beginControlFlow("try")
            .addStatement("registerFruits()")
            .nextControlFlow("catch ($T e)", Exception.class)
            .addStatement("throw new $T(e)", RuntimeException.class)
            .endControlFlow()
            .build();
    }

    MethodSpec registerFruits()
    {
        return MethodSpec.methodBuilder("registerFruits")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .beginControlFlow("for (int i = $L; i < $L; i++)", 0, numClasses)
            .addStatement("registerFruit(i)")
            .endControlFlow()
            .build();
    }

    MethodSpec registerFruit()
    {
        return MethodSpec.methodBuilder("registerFruit")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(int.class, "index")
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("$T.out.println(\"Register fruit \" + index)", System.class)
            .addStatement("Class<?> clazz = Class.forName(\"$L.Fruit\" + index)", PACKAGE_NAME)
            .addStatement("$L.RuntimeReflection.register(clazz)", PACKAGE_HOSTED)
            .addStatement("registerFruitFieldsAndMethods(clazz)")
            .build();
    }

    MethodSpec registerFruitFieldsAndMethods()
    {
        return MethodSpec.methodBuilder("registerFruitFieldsAndMethods")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Class.class, "clazz")
            .addException(Exception.class)
            .returns(void.class)
            .beginControlFlow("for (int i = $L; i < $L; i++)", 0, numFields)
            .addStatement("String fieldName = $S + i", "attr_")
            .addStatement("$L.RuntimeReflection.register(clazz.getField(fieldName))", PACKAGE_HOSTED)
            .addStatement("String getterName = $S + i", "getAttr")
            .addStatement("$L.RuntimeReflection.register(clazz.getMethod(getterName))", PACKAGE_HOSTED)
            .addStatement("String setterName = $S + i", "setAttr")
            .addStatement("$L.RuntimeReflection.register(clazz.getMethod(setterName, String.class))", PACKAGE_HOSTED)
            .endControlFlow()
            .build();
    }

    void generateMain() throws IOException
    {
        final var assertFruits = assertFruits();
        final var assertFruit = assertFruit();
        final var assertFruitField = assertFruitFieldAndMethods();

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
            .addMethod(assertFruitField)
            .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type).build();
        javaFile.writeTo(generatedSourcesDirectory);
    }

    MethodSpec assertFruits()
    {
        return MethodSpec.methodBuilder("assertFruits")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .beginControlFlow("for (int i = $L; i < $L; i++)", 0, numClasses)
            .addStatement("assertFruit(i)")
            .endControlFlow()
            .build();
    }

    MethodSpec assertFruit()
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
            .addStatement("assertFruitFieldAndMethods(clazz, i)")
            .endControlFlow()
            .build();
    }

    MethodSpec assertFruitFieldAndMethods()
    {
        return MethodSpec.methodBuilder("assertFruitFieldAndMethods")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(Class.class, "clazz")
            .addParameter(int.class, "index")
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("String fieldName = $S + index", "attr_")
            .addStatement("assert fieldName.equals(clazz.getField(fieldName).getName())")
            .addStatement("String getterName = $S + index", "getAttr")
            .addStatement("assert getterName.equals(clazz.getMethod(getterName).getName())")
            .addStatement("String setterName = $S + index", "setAttr")
            .addStatement("assert setterName.equals(clazz.getMethod(setterName, String.class).getName())")
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
