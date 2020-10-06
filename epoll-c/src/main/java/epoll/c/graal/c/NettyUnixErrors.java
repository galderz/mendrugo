package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixErrors
{
    @CFunction(value = "io_netty_unix_errors_errnoENOENT", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoENOENT(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoEBADF", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoEBADF(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoEPIPE", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoEPIPE(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoECONNRESET", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoECONNRESET(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoENOTCONN", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoENOTCONN(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoEAGAIN", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoEAGAIN(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoEWOULDBLOCK", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoEWOULDBLOCK(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errnoEINPROGRESS", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errnoEINPROGRESS(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errorECONNREFUSED", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errorECONNREFUSED(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errorEISCONN", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errorEISCONN(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errorEALREADY", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errorEALREADY(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_errorENETUNREACH", transition = CFunction.Transition.NO_TRANSITION)
    public static native int errorENETUNREACH(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_errors_strError", transition = CFunction.Transition.TO_NATIVE)
    public static native JNIObjectHandle strError(WordBase jnienv, WordBase clazz, int err);
}
