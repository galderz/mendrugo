package epoll.jni.run.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "io.netty.util.internal.NativeLibraryLoader")
final class Target_io_netty_util_internal_NativeLibraryLoader
{

    // This method can trick GraalVM into thinking that Classloader#defineClass is getting called
    @Substitute
    static Class<?> tryToLoadClass(final ClassLoader loader, final Class<?> helper)
        throws ClassNotFoundException {
        return Class.forName(helper.getName(), false, loader);
    }

}
