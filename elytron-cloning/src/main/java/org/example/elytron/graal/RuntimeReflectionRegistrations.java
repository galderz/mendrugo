package org.example.elytron.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.example.elytron.Main;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import javax.crypto.SecretKey;
import java.lang.reflect.UndeclaredThrowableException;

@AutomaticFeature
public class RuntimeReflectionRegistrations implements Feature
{
    public void beforeAnalysis(BeforeAnalysisAccess access)
    {
        try
        {
            RuntimeReflection.register(Main.CopyConstructorSecretKey.class.getDeclaredConstructor(SecretKey.class));
            RuntimeReflection.register(Main.CopyConstructorSecretKey.class.getDeclaredMethod("destroy"));
            RuntimeReflection.register(Main.CloneMethodSecretKey.class.getDeclaredMethod("clone"));
            RuntimeReflection.register(Main.CloneMethodSecretKey.class.getDeclaredMethod("destroy"));
        }
        catch (NoSuchMethodException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }
}
