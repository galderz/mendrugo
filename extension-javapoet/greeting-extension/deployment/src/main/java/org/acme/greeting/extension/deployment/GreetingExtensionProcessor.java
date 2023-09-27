package org.acme.greeting.extension.deployment;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
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
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import org.acme.compiler.InMemoryCompiler;
import org.acme.compiler.InMemoryFileManager;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

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
    void generateAndCompile(
        BuildProducer<GeneratedClassBuildItem> generatedClasses
        , BuildProducer<GeneratedBeanBuildItem> generatedBeanClasses
    )
    {
        final JavaFile helloWorldJavaFile = greetingJavaFile();
        writeTo(helloWorldJavaFile, System.out);
        final byte[] helloWorldClassBytes = InMemoryCompiler.compileAsBytes(helloWorldJavaFile);
        final GeneratedClassBuildItem classItem = new GeneratedClassBuildItem(true, getJavaClassName(helloWorldJavaFile), helloWorldClassBytes);
        generatedClasses.produce(classItem);

        final JavaFile appLifecycleBeanJavaFile = appLifecycleBeanJavaFile();
        writeTo(appLifecycleBeanJavaFile, System.out);
        final byte[] appLifecycleBeanClassBytes = InMemoryCompiler.compileAsBytes(appLifecycleBeanJavaFile);
        final GeneratedBeanBuildItem beanItem = new GeneratedBeanBuildItem(getJavaClassName(appLifecycleBeanJavaFile), appLifecycleBeanClassBytes);
        generatedBeanClasses.produce(beanItem);
    }

    private static String getJavaClassName(JavaFile java)
    {
        String name = java.packageName;
        name = name.isEmpty() ? java.typeSpec.name : name + "." + java.typeSpec.name;
        return name;
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

    JavaFile appLifecycleBeanJavaFile()
    {
        MethodSpec onStart = MethodSpec.methodBuilder("onStart")
            .returns(void.class)
            .addParameter(
                ParameterSpec.builder(StartupEvent.class, "ev")
                    .addAnnotation(Observes.class)
                    .build()
            )
            .addStatement("LOGGER.info($S)", "The application is starting...")
            .build();

        TypeSpec appLifecycleBean = TypeSpec.classBuilder("AppLifecycleBean")
            .addField(
                FieldSpec.builder(Logger.class, "LOGGER")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.getLogger($S)", Logger.class, "ListenerBean")
                    .build()
            )
            .addModifiers(Modifier.PUBLIC)
            .addMethod(onStart)
            .build();

        return JavaFile.builder("org.acme.lifecycle", appLifecycleBean)
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
