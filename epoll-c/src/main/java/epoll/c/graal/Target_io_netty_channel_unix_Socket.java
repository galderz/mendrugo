package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import com.oracle.svm.jni.JNIThreadLocalEnvironment;
import epoll.c.graal.c.NettyUnixSocket;
import io.netty.channel.unix.DatagramSocketAddress;
import org.graalvm.word.WordFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

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

    @Substitute
    private static int connectDomainSocket(int fd, byte[] path)
    {
        return NettyUnixSocket.connectDomainSocket(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, JNIObjectHandles.createLocal(path));
    }

    @Substitute
    private static int finishConnect(int fd)
    {
        return NettyUnixSocket.finishConnect(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int disconnect(int fd, boolean ipv6)
    {
        return NettyUnixSocket.disconnect(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6);
    }

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

    @Substitute
    private static int bindDomainSocket(int fd, byte[] path)
    {
        return NettyUnixSocket.bindDomainSocket(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, JNIObjectHandles.createLocal(path));
    }

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

    @Substitute
    private static int sendTo(int fd, boolean ipv6, ByteBuffer buf, int pos, int limit, byte[] address, int scopeId, int port)
    {
        return NettyUnixSocket.sendTo(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(buf), pos, limit, JNIObjectHandles.createLocal(address), scopeId, port);
    }

    @Substitute
    private static int sendToAddress(int fd, boolean ipv6, long memoryAddress, int pos, int limit, byte[] address, int scopeId, int port)
    {
        return NettyUnixSocket.sendToAddress(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, memoryAddress, pos, limit, JNIObjectHandles.createLocal(address), scopeId, port);
    }

    @Substitute
    private static int sendToAddresses(int fd, boolean ipv6, long memoryAddress, int length, byte[] address, int scopeId, int port)
    {
        return NettyUnixSocket.sendToAddresses(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, memoryAddress, length, JNIObjectHandles.createLocal(address), scopeId, port);
    }

    @Substitute
    private static DatagramSocketAddress recvFrom(int fd, ByteBuffer buf, int pos, int limit) throws IOException
    {
        return JNIObjectHandles.getObject(NettyUnixSocket.recvFrom(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, buf, pos, limit));
    }

    @Substitute
    private static DatagramSocketAddress recvFromAddress(int fd, long memoryAddress, int pos, int limit) throws IOException
    {
        return JNIObjectHandles.getObject(NettyUnixSocket.recvFromAddress(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, memoryAddress, pos, limit));
    }

    @Substitute
    private static int recvFd(int fd)
    {
        return NettyUnixSocket.recvFd(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int sendFd(int socketFd, int fd)
    {
        return NettyUnixSocket.sendFd(WordFactory.nullPointer(), WordFactory.nullPointer(), socketFd, fd);
    }

    @Substitute
    private static int newSocketStreamFd(boolean ipv6)
    {
        return NettyUnixSocket.newSocketStreamFd(WordFactory.nullPointer(), WordFactory.nullPointer(), ipv6);
    }

    @Substitute
    private static int newSocketDgramFd(boolean ipv6)
    {
        return NettyUnixSocket.newSocketDgramFd(WordFactory.nullPointer(), WordFactory.nullPointer(), ipv6);
    }

    @Substitute
    private static int newSocketDomainFd()
    {
        return NettyUnixSocket.newSocketDomainFd(WordFactory.nullPointer(), WordFactory.nullPointer());
    }

    @Substitute
    private static int isReuseAddress(int fd) throws IOException
    {
        return NettyUnixSocket.isReuseAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isReusePort(int fd) throws IOException
    {
        return NettyUnixSocket.isReusePort(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getReceiveBufferSize(int fd) throws IOException
    {
        return NettyUnixSocket.getReceiveBufferSize(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getSendBufferSize(int fd)
    {
        return NettyUnixSocket.getSendBufferSize(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isKeepAlive(int fd) throws IOException
    {
        return NettyUnixSocket.isKeepAlive(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isTcpNoDelay(int fd) throws IOException
    {
        return NettyUnixSocket.isTcpNoDelay(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isBroadcast(int fd) throws IOException
    {
        return NettyUnixSocket.isBroadcast(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getSoLinger(int fd) throws IOException
    {
        return NettyUnixSocket.getSoLinger(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getSoError(int fd) throws IOException
    {
        return NettyUnixSocket.getSoError(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTrafficClass(int fd, boolean ipv6) throws IOException
    {
        return NettyUnixSocket.getTrafficClass(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6);
    }

    @Substitute
    private static void setReuseAddress(int fd, int reuseAddress)
    {
        NettyUnixSocket.setReuseAddress(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, reuseAddress);
    }

    @Substitute
    private static void setReusePort(int fd, int reuseAddress) throws IOException
    {
        NettyUnixSocket.setReusePort(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, reuseAddress);
    }

    @Substitute
    private static void setKeepAlive(int fd, int keepAlive) throws IOException
    {
        NettyUnixSocket.setKeepAlive(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, keepAlive);
    }

    @Substitute
    private static void setReceiveBufferSize(int fd, int receiveBufferSize) throws IOException
    {
        NettyUnixSocket.setReceiveBufferSize(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, receiveBufferSize);
    }

    @Substitute
    private static void setSendBufferSize(int fd, int sendBufferSize) throws IOException
    {
        NettyUnixSocket.setSendBufferSize(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, sendBufferSize);
    }

    @Substitute
    private static void setTcpNoDelay(int fd, int tcpNoDelay)
    {
        NettyUnixSocket.setTcpNoDelay(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpNoDelay);
    }

    @Substitute
    private static void setSoLinger(int fd, int soLinger) throws IOException
    {
        NettyUnixSocket.setSoLinger(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, soLinger);
    }

    @Substitute
    private static void setBroadcast(int fd, int broadcast) throws IOException
    {
        NettyUnixSocket.setBroadcast(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, broadcast);
    }

    @Substitute
    private static void setTrafficClass(int fd, boolean ipv6, int trafficClass) throws IOException
    {
        NettyUnixSocket.setTrafficClass(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6, trafficClass);
    }

    @Substitute
    private static void initialize(boolean ipv4Preferred)
    {
        NettyUnixSocket.initialize(WordFactory.nullPointer(), WordFactory.nullPointer(), ipv4Preferred);
    }
}
