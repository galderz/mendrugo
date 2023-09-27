package org.acme.command.extension.deployment;

import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.Gizmo;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.command.annotations.WithCommand;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

class CommandExtensionProcessor
{
    private static final String FEATURE = "command-extension";
    private static final DotName WITH_COMMAND = DotName.createSimple(WithCommand.class.getName());

    @BuildStep
    FeatureBuildItem feature()
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void generateCommand(
        BuildProducer<GeneratedBeanBuildItem> generatedBeanClasses
        , CombinedIndexBuildItem index
    )
    {
        System.out.println("Generating command...");

        MethodInfo methodInfo = null;
        for (AnnotationInstance ann : index.getIndex().getAnnotations(WITH_COMMAND))
        {
            methodInfo = ann.target().asMethod();
            System.out.println(methodInfo);
        }

        if (methodInfo != null)
        {
            final ClassOutput beanOutput = new GeneratedBeanGizmoAdaptor(generatedBeanClasses);

            final ClassCreator command = ClassCreator.builder()
                .classOutput(beanOutput)
                .className("org.acme.command.generated.GeneratedCommand")
                .interfaces("org.acme.command.main.Command") // todo share class
                .build();
            command.addAnnotation(ApplicationScoped.class);

            final MethodCreator run = command.getMethodCreator("run", void.class);
            Gizmo.systemOutPrintln(run, run.load("A generated command"));

            final ClassInfo classInfo = methodInfo.declaringClass();
            final String typeName = classInfo.name().toString();
            final String typeDescriptor = "L" + typeName.replace('.', '/') + ";";
            final AssignableResultHandle variable = run.createVariable(typeDescriptor);
            run.assign(variable, run.newInstance(MethodDescriptor.ofConstructor(classInfo.name().toString())));
            run.invokeVirtualMethod(MethodDescriptor.of(methodInfo), variable);

            run.returnValue(null);
            run.close();

            command.close();
        }
    }
}
