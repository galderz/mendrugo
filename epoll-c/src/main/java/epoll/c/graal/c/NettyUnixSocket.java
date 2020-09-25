package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.word.WordBase;

@CLibrary(value = "netty-unix-common", requireStatic = true)
public class NettyUnixSocket
{
    @CFunction(value = "io_netty_unix_socket_isIPv6Preferred", transition = CFunction.Transition.NO_TRANSITION)
    public static native boolean isIPv6Preferred(WordBase jnienv, WordBase clazz);

    @CFunction(value = "io_netty_unix_socket_isIPv6", transition = CFunction.Transition.NO_TRANSITION)
    public static native boolean isIPv6(WordBase jnienv, WordBase clazz, int fd);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int shutdown(WordBase jnienv, WordBase clazz, int fd, boolean read, boolean write);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int connect(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, byte[] address, int scopeId, int port);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int connectDomainSocket(WordBase jnienv, WordBase clazz, int fd, byte[] path);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int finishConnect(WordBase jnienv, WordBase clazz, int fd);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int disconnect(WordBase jnienv, WordBase clazz, int fd, boolean ipv6);

    @CFunction(value = "io_netty_unix_socket_bind", transition = CFunction.Transition.TO_NATIVE)
    public static native int bind(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle address, int scopeId, int port);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int bindDomainSocket(WordBase jnienv, WordBase clazz, int fd, byte[] path);

    @CFunction(value = "io_netty_unix_socket_listen", transition = CFunction.Transition.NO_TRANSITION)
    public static native int listen(WordBase jnienv, WordBase clazz, int fd, int backlog);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int accept(WordBase jnienv, WordBase clazz, int fd, byte[] addr);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native byte[] remoteAddressWordBase jnienv, WordBase clazz, (int fd);

    @CFunction(value = "io_netty_unix_socket_localAddress", transition = CFunction.Transition.TO_NATIVE)
    public static native JNIObjectHandle localAddress(WordBase jnienv, WordBase clazz, int fd);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int sendTo(WordBase jnienv, WordBase clazz,
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int sendToAddress(WordBase jnienv, WordBase clazz, 
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int sendToAddresses(WordBase jnienv, WordBase clazz, 
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native DatagramSocketAddress recvFrom(WordBase jnienv, WordBase clazz, 
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native DatagramSocketAddress recvFromAddress(WordBase jnienv, WordBase clazz, 
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int recvFd(WordBase jnienv, WordBase clazz, int fd);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int sendFd(WordBase jnienv, WordBase clazz, int socketFd, int fd);

    @CFunction(value = "io_netty_unix_socket_newSocketStreamFd", transition = CFunction.Transition.NO_TRANSITION)
    public static native int newSocketStreamFd(WordBase jnienv, WordBase clazz, boolean ipv6);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int newSocketDgramFd(WordBase jnienv, WordBase clazz, boolean ipv6);
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int newSocketDomainFd();WordBase jnienv, WordBase clazz,
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int isReuseAddress(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int isReusePort(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int getReceiveBufferSize(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_unix_socket_getSendBufferSize", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getSendBufferSize(WordBase jnienv, WordBase clazz, int fd);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int isKeepAlive(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int isTcpNoDelay(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int isBroadcast(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int getSoLinger(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int getSoError(WordBase jnienv, WordBase clazz, int fd) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native int getTrafficClass(WordBase jnienv, WordBase clazz, int fd, boolean ipv6) throws IOException;

    @CFunction(value = "io_netty_unix_socket_setReuseAddress", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setReuseAddress(WordBase jnienv, WordBase clazz, int fd, int reuseAddress);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setReusePort(WordBase jnienv, WordBase clazz, int fd, int reuseAddress) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setKeepAlive(WordBase jnienv, WordBase clazz, int fd, int keepAlive) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setReceiveBufferSize(WordBase jnienv, WordBase clazz, int fd, int receiveBufferSize) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setSendBufferSize(WordBase jnienv, WordBase clazz, int fd, int sendBufferSize) throws IOException;

    @CFunction(value = "io_netty_unix_socket_setTcpNoDelay", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpNoDelay(WordBase jnienv, WordBase clazz, int fd, int tcpNoDelay);

//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setSoLinger(WordBase jnienv, WordBase clazz, int fd, int soLinger) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setBroadcast(WordBase jnienv, WordBase clazz, int fd, int broadcast) throws IOException;
//    @CFunction(value = "io_netty_unix_socket_", transition = CFunction.Transition.NO_TRANSITION) public static native void setTrafficClass(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, int trafficClass) throws IOException;

    @CFunction(value = "io_netty_unix_socket_initialize", transition = CFunction.Transition.NO_TRANSITION)
    public static native void initialize(WordBase jnienv, WordBase clazz, boolean ipv4Preferred);
}
