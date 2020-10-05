package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import com.oracle.svm.jni.JNIThreadLocalEnvironment;
import epoll.c.graal.c.NettyUnixErrors;
import org.graalvm.word.WordFactory;

@TargetClass(className = "io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods")
final class Target_io_netty_channel_unix_ErrorsStaticallyReferencedJniMethods
{
    @Substitute
    private static int errnoENOENT()
    {
        return NettyUnixErrors.errnoENOENT(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoEBADF()
    {
        return NettyUnixErrors.errnoEBADF(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoEPIPE()
    {
        return NettyUnixErrors.errnoEPIPE(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoECONNRESET()
    {
        return NettyUnixErrors.errnoECONNRESET(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoENOTCONN()
    {
        return NettyUnixErrors.errnoENOTCONN(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoEAGAIN()
    {
        return NettyUnixErrors.errnoEAGAIN(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoEWOULDBLOCK()
    {
        return NettyUnixErrors.errnoEWOULDBLOCK(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errnoEINPROGRESS()
    {
        return NettyUnixErrors.errnoEINPROGRESS(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errorECONNREFUSED()
    {
        return NettyUnixErrors.errorECONNREFUSED(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errorEISCONN()
    {
        return NettyUnixErrors.errorEISCONN(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errorEALREADY()
    {
        return NettyUnixErrors.errorEALREADY(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int errorENETUNREACH()
    {
        return NettyUnixErrors.errorENETUNREACH(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static String strError(int err)
    {
        return JNIObjectHandles.getObject(
            NettyUnixErrors.strError(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), err)
        );
    }
}
