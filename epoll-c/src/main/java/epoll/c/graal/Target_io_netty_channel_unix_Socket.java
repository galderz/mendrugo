package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import com.oracle.svm.jni.JNIThreadLocalEnvironment;
import epoll.c.graal.c.NettyUnixSocket;
import org.graalvm.word.WordFactory;

import java.io.IOException;

@TargetClass(className = "io.netty.channel.unix.Socket")
final class Target_io_netty_channel_unix_Socket
{
    @Substitute
    private static boolean isIPv6Preferred()
    {
        return NettyUnixSocket.isIPv6Preferred(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static boolean isIPv6(int fd)
    {
        return NettyUnixSocket.isIPv6(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int shutdown(int fd, boolean read, boolean write)
    {
        return NettyUnixSocket.shutdown(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, read, write);
    }

    @Substitute
    private static int connect(int fd, boolean ipv6, byte[] address, int scopeId, int port)
    {
        return NettyUnixSocket.connect(
            JNIThreadLocalEnvironment.getAddress()
            , WordFactory.nullPointer()
            , fd
            , ipv6
            , JNIObjectHandles.createLocal(address)
            , scopeId
            , port
        );
    }

//    @Substitute private static int connectDomainSocket(int fd, byte[] path) {}

    @Substitute
    private static int finishConnect(int fd)
    {
        return NettyUnixSocket.finishConnect(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

//    @Substitute private static int disconnect(int fd, boolean ipv6) {}

    @Substitute
    private static int bind(int fd, boolean ipv6, byte[] address, int scopeId, int port)
    {
        return NettyUnixSocket.bind(
            JNIThreadLocalEnvironment.getAddress()
            , WordFactory.nullPointer()
            , fd
            , ipv6
            , JNIObjectHandles.createLocal(address)
            , scopeId
            , port
        );
    }

//    @Substitute private static int bindDomainSocket(int fd, byte[] path) {}

    @Substitute
    private static int listen(int fd, int backlog)
    {
        return NettyUnixSocket.listen(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, backlog);
    }

    @Substitute
    private static int accept(int fd, byte[] addr)
    {
        return NettyUnixSocket.accept(
            JNIThreadLocalEnvironment.getAddress()
            , WordFactory.nullPointer()
            , fd
            , JNIObjectHandles.createLocal(addr)
        );
    }

    @Substitute
    private static byte[] remoteAddress(int fd)
    {
        return JNIObjectHandles.getObject(
            NettyUnixSocket.remoteAddress(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd)
        );
    }

    @Substitute
    private static byte[] localAddress(int fd)
    {
        return JNIObjectHandles.getObject(
            NettyUnixSocket.localAddress(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd)
        );
    }

//    @Substitute private static int sendTo {}
//    @Substitute private static int sendToAddress {}
//    @Substitute private static int sendToAddresses {}
//    @Substitute private static DatagramSocketAddress recvFrom {}
//    @Substitute private static DatagramSocketAddress recvFromAddress {}
//    @Substitute private static int recvFd(int fd) {}
//    @Substitute private static int sendFd(int socketFd, int fd) {}

    @Substitute
    private static int newSocketStreamFd(boolean ipv6)
    {
        return NettyUnixSocket.newSocketStreamFd(WordFactory.nullPointer(), WordFactory.nullPointer(), ipv6);
    }

//    @Substitute private static int newSocketDgramFd(boolean ipv6) {}
//    @Substitute private static int newSocketDomainFd(boolean ipv6) {}
//    @Substitute private static int isReuseAddress(int fd) throws IOException {}
//    @Substitute private static int isReusePort(int fd) throws IOException {}
//    @Substitute private static int getReceiveBufferSize(int fd) throws IOException {}

    @Substitute
    private static int getSendBufferSize(int fd)
    {
        return NettyUnixSocket.getSendBufferSize(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

//    @Substitute private static int isKeepAlive(int fd) throws IOException {}
//    @Substitute private static int isTcpNoDelay(int fd) throws IOException {}
//    @Substitute private static int isBroadcast(int fd) throws IOException {}

    @Substitute
    private static int getSoLinger(int fd) throws IOException
    {
        return NettyUnixSocket.getSoLinger(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

//    @Substitute private static int getSoError(int fd) throws IOException {}
//    @Substitute private static int getTrafficClass(int fd, boolean ipv6) throws IOException {}

    @Substitute
    private static void setReuseAddress(int fd, int reuseAddress)
    {
        NettyUnixSocket.setReuseAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, reuseAddress);
    }

//    @Substitute private static void setReusePort(int fd, int reuseAddress) throws IOException {}
//    @Substitute private static void setKeepAlive(int fd, int keepAlive) throws IOException {}
//    @Substitute private static void setReceiveBufferSize(int fd, int receiveBufferSize) throws IOException {}
//    @Substitute private static void setSendBufferSize(int fd, int sendBufferSize) throws IOException {}

    @Substitute
    private static void setTcpNoDelay(int fd, int tcpNoDelay)
    {
        NettyUnixSocket.setTcpNoDelay(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpNoDelay);
    }

//    @Substitute private static void setSoLinger(int fd, int soLinger) throws IOException {}
//    @Substitute private static void setBroadcast(int fd, int broadcast) throws IOException {}
//    @Substitute private static void setTrafficClass(int fd, boolean ipv6, int trafficClass) throws IOException {}

    @Substitute
    private static void initialize(boolean ipv4Preferred)
    {
        NettyUnixSocket.initialize(WordFactory.nullPointer(), WordFactory.nullPointer(), ipv4Preferred);
    }
}
