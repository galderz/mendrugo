package jmh.basics;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.openjdk.jmh.runner.options.TimeValue;

@AutomaticFeature
public class Reflections implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        try
        {
            registerConverter(Integer.class);
            registerConverter(String.class);
            registerConverter(Boolean.class);
            registerConverter(TimeValue.class);

            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL0")
                    .getDeclaredField("markerBegin")
            );

            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL4")
                    .getDeclaredField("markerEnd")
            );

            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("type")
            );
            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("count")
            );
            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("timeValue")
            );
            RuntimeReflection.register(
                Class.forName("org.openjdk.jmh.infra.IterationParamsL2")
                    .getDeclaredField("batchSize")
            );
        }
        catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void registerConverter(Class<?> argumentType) throws NoSuchMethodException
    {
        try
        {
            RuntimeReflection.register(argumentType.getDeclaredMethod( "valueOf", String.class ));
        } catch (NoSuchMethodException e)
        {
            RuntimeReflection.register(argumentType.getConstructor(String.class ));
        }
    }
}
