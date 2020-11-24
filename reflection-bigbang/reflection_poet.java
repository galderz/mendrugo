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
    public static final String PACKAGE_HOSTED = "org.graalvm.nativeimage.hosted";
    public static final String RUNTIME_REFLECTION = String.format("%s.RuntimeReflection", PACKAGE_HOSTED);

    @Option(
        description = "Class prefix"
        , names = {"-p", "--class-prefix"}
    )
    private String classPrefix = "Fruit";

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
        generateReflectionFeature();
        generateMain();
        return 0;
    }

    void generateReflectionFeature() throws IOException
    {
        final var beforeAnalysis = beforeAnalysis();

        TypeSpec type = TypeSpec.classBuilder("ReflectionFeature")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(ClassName.get(PACKAGE_HOSTED, "Feature"))
            .addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "AutomaticFeature"))
            .addMethod(beforeAnalysis)
            .addMethod(registerReflection())
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
            .addStatement("registerReflection()")
            .nextControlFlow("catch ($T e)", Exception.class)
            .addStatement("throw new $T(e)", RuntimeException.class)
            .endControlFlow()
            .build();
    }

    void generateMain() throws IOException
    {
        MethodSpec main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println($S)", System.class, "Check asserts enabled...")
            .addStatement("$1L.Asserts.check()", PACKAGE_NAME)
            .addStatement("$T.out.println($S)", System.class, "Run assertions...")
            .addStatement("assertReflection()")
            .addStatement("$T.out.println($S)", System.class, "Assertions successfully passed")
            .build();

        TypeSpec type = TypeSpec.classBuilder("Main")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
            .addMethod(assertReflection())
            .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, type).build();
        javaFile.writeTo(generatedSourcesDirectory);
    }

    MethodSpec assertReflection()
    {
        return forAll("assertReflection"
            , CodeBlock.of("assert ($S + classIndex).equals(clazz.getSimpleName())", classPrefix)
            , CodeBlock.of("assert ($1S + fieldIndex).equals(clazz.getField($1S + fieldIndex).getName())", "attr_")
            , CodeBlock.of("assert ($1S + fieldIndex).equals(clazz.getMethod($1S + fieldIndex).getName())", "getAttr")
            , CodeBlock.of("assert ($1S + fieldIndex).equals(clazz.getMethod($1S + fieldIndex, String.class).getName())", "setAttr")
        );
    }

    MethodSpec registerReflection()
    {
        return forAll("registerReflection"
            , CodeBlock.of("$L.register(clazz)", RUNTIME_REFLECTION)
            , CodeBlock.of("$L.register(clazz.getField($S + fieldIndex))", RUNTIME_REFLECTION, "attr_")
            , CodeBlock.of("$L.register(clazz.getMethod($S + fieldIndex))", RUNTIME_REFLECTION, "getAttr")
            , CodeBlock.of("$L.register(clazz.getMethod($S + fieldIndex, String.class))", RUNTIME_REFLECTION, "setAttr")
        );
    }

    private MethodSpec forAll(String name, CodeBlock perClass, CodeBlock perField, CodeBlock perGetter, CodeBlock perSetter)
    {
        return MethodSpec.methodBuilder(name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addException(Exception.class)
            .returns(void.class)
            .addStatement("$T.out.println($S)", System.class, String.format("%s for %d classes and %d fields each", name, numClasses, numFields))
            .beginControlFlow("for (int classIndex = $L; classIndex < $L; classIndex++)", 0, numClasses)
            .addStatement("Class<?> clazz = Class.forName(\"$L.$L\" + classIndex)", PACKAGE_NAME, classPrefix)
            .addStatement(perClass)
            .beginControlFlow("for (int fieldIndex = $L; fieldIndex < $L; fieldIndex++)", 0, numFields)
            .addStatement(perField)
            .addStatement(perGetter)
            .addStatement(perSetter)
            .endControlFlow()
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
