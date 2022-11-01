///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.5.0
//DEPS com.squareup:javapoet:1.13.0

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

@Command(name = "hugeclass", mixinStandardHelpOptions = true, version = "hugeclass 0.1",
    description = "hugeclass made with jbang")
class hugeclass implements Callable<Integer>
{

    @Parameters(index = "0", description = "Number of fields to generate", defaultValue = "10")
    private int numFields;

    @Parameters(index = "1", description = "Size of each field", defaultValue = "10")
    private int fieldSize;

    public static void main(String... args)
    {
        int exitCode = new CommandLine(new hugeclass()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception
    {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("Huge")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec randomBytes = MethodSpec.methodBuilder("randomBytes")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .returns(byte[].class)
            .addStatement("byte[] b = new byte[$L]", fieldSize)
            .addStatement("new $T().nextBytes(b)", Random.class)
            .addStatement("return b")
            .build();
        typeBuilder.addMethod(randomBytes);

        for (int i = 0; i < numFields; i++)
        {
            FieldSpec field = FieldSpec.builder(byte[].class, "b" + i)
                .addModifiers(Modifier.PRIVATE)
                .initializer("randomBytes()")
                .build();
            typeBuilder.addField(field);
        }

        final MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("toString")
            .addAnnotation(Override.class)
            .returns(String.class)
            .addModifiers(Modifier.PUBLIC)
            .addCode("""
                return "Huge{" +
                """)
            .addCode("""
                "b0=" + $T.toString(b0) +
                """, Arrays.class);
        for (int i = 1; i < numFields; i++)
        {
            toStringBuilder.addCode("""
                ",b$L=" + $T.toString(b$L) +
                """, i, Arrays.class, i);
        }
        toStringBuilder.addStatement("'}'");
        typeBuilder.addMethod(toStringBuilder.build());

        final MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override.class)
            .returns(int.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("""
                int result = Arrays.hashCode(b0)
                """);
        for (int i = 1; i < numFields; i++)
        {
            hashCodeBuilder.addStatement("""
                result = 31 * result + $T.hashCode(b$L)
                """, Arrays.class, i);
        }
        hashCodeBuilder.addStatement("return result");
        typeBuilder.addMethod(hashCodeBuilder.build());

        JavaFile javaFile = JavaFile.builder("jfr.oldobject.gen", typeBuilder.build()).build();
        javaFile.writeTo(Path.of("src/main/java"));

        return 0;
    }
}
