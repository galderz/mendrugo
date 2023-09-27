package org.acme.command.extension.deployment;

import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.Gizmo;
import io.quarkus.gizmo.MethodCreator;
import jakarta.enterprise.context.ApplicationScoped;

class CommandExtensionProcessor
{
    private static final String FEATURE = "command-extension";

    @BuildStep
    FeatureBuildItem feature()
    {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void generateCommand(BuildProducer<GeneratedBeanBuildItem> generatedBeanClasses)
    {
        System.out.println("Generating command...");

        final ClassOutput beanOutput = new GeneratedBeanGizmoAdaptor(generatedBeanClasses);

        final ClassCreator command = ClassCreator.builder()
            .classOutput(beanOutput)
            .className("org.acme.command.generated.GeneratedCommand")
            .interfaces("org.acme.command.main.Command") // todo share class
            .build();
        command.addAnnotation(ApplicationScoped.class);

        final MethodCreator run = command.getMethodCreator("run", void.class);
        Gizmo.systemOutPrintln(run, run.load("A generated command"));
        run.returnValue(null);
        run.close();

        command.close();
    }
}
