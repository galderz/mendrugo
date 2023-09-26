package org.acme.greeting.extension.deployment;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.GeneratedClassGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import io.quarkus.gizmo.ClassOutput;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

class GreetingExtensionProcessor
{
    private static final String FEATURE = "fibula-extension";

    @BuildStep
    FeatureBuildItem feature()
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void generateAndCompile(BuildProducer<GeneratedClassBuildItem> generatedClasses)
    {
        final JavaFile javaFile = greetingJavaFile();
        writeTo(javaFile, System.out);
    }

    JavaFile greetingJavaFile()
    {
        MethodSpec main = MethodSpec.methodBuilder("main")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(String[].class, "args")
            .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
            .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(main)
            .build();

        return JavaFile.builder("com.example.helloworld", helloWorld)
            .build();
    }

    private void writeTo(JavaFile javaFile, PrintStream out)
    {
        try
        {
            javaFile.writeTo(out);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

}
