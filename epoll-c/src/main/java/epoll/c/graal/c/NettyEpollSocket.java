package epoll.c.graal.c;

import com.oracle.svm.jni.nativeapi.JNIObjectHandle;
import io.netty.channel.unix.PeerCredentials;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.word.WordBase;

import java.io.IOException;

public class NettyEpollSocket
{
    @CFunction(value = "io_netty_epoll_linuxsocket_joinGroup", transition = CFunction.Transition.TO_NATIVE)
    public static native void joinGroup(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle group, JNIObjectHandle interfaceAddress, int scopeId, int interfaceIndex) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_joinSsmGroup", transition = CFunction.Transition.TO_NATIVE)
    public static native void joinSsmGroup(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle group, JNIObjectHandle interfaceAddress, int scopeId, int interfaceIndex, JNIObjectHandle source) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_leaveGroup", transition = CFunction.Transition.TO_NATIVE)
    public static native void leaveGroup(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle group, JNIObjectHandle interfaceAddress, int scopeId, int interfaceIndex) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_leaveSsmGroup", transition = CFunction.Transition.TO_NATIVE)
    public static native void leaveSsmGroup(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle group, JNIObjectHandle interfaceAddress, int scopeId, int interfaceIndex, JNIObjectHandle source) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_sendFile", transition = CFunction.Transition.TO_NATIVE)
    public static native long sendFile(WordBase jnienv, WordBase clazz, int socketFd, JNIObjectHandle src, long baseOffset, long offset, long length) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpDeferAccept", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpDeferAccept(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isTcpQuickAck", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isTcpQuickAck(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isTcpCork", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isTcpCork(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getSoBusyPoll", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getSoBusyPoll(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpNotSentLowAt", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpNotSentLowAt(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpKeepIdle", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpKeepIdle(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpKeepIntvl", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpKeepIntvl(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpKeepCnt", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpKeepCnt(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpUserTimeout", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTcpUserTimeout(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTimeToLive", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getTimeToLive(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isIpFreeBind", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isIpFreeBind(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isIpTransparent", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isIpTransparent(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isIpRecvOrigDestAddr", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isIpRecvOrigDestAddr(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getTcpInfo", transition = CFunction.Transition.NO_TRANSITION)
    public static native void getTcpInfo(WordBase jnienv, WordBase clazz, int fd, long[] array) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_PeerCredentials", transition = CFunction.Transition.NO_TRANSITION)
    public static native PeerCredentials getPeerCredentials(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_isTcpFastOpenConnect", transition = CFunction.Transition.NO_TRANSITION)
    public static native int isTcpFastOpenConnect(WordBase jnienv, WordBase clazz, int fd) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpDeferAccept", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpDeferAccept(WordBase jnienv, WordBase clazz, int fd, int deferAccept) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpQuickAck", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpQuickAck(WordBase jnienv, WordBase clazz, int fd, int quickAck) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpCork", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpCork(WordBase jnienv, WordBase clazz, int fd, int tcpCork) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setSoBusyPoll", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setSoBusyPoll(WordBase jnienv, WordBase clazz, int fd, int loopMicros) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpNotSentLowAt", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpNotSentLowAt(WordBase jnienv, WordBase clazz, int fd, int tcpNotSentLowAt) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpFastOpen", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpFastOpen(WordBase jnienv, WordBase clazz, int fd, int tcpFastopenBacklog) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpFastOpenConnect", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpFastOpenConnect(WordBase jnienv, WordBase clazz, int fd, int tcpFastOpenConnect) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpKeepIdle", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpKeepIdle(WordBase jnienv, WordBase clazz, int fd, int seconds) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpKeepIntvl", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpKeepIntvl(WordBase jnienv, WordBase clazz, int fd, int seconds) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpKeepCnt", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpKeepCnt(WordBase jnienv, WordBase clazz, int fd, int probes) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpUserTimeout", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTcpUserTimeout(WordBase jnienv, WordBase clazz, int fd, int milliseconds)throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setIpFreeBind", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setIpFreeBind(WordBase jnienv, WordBase clazz, int fd, int freeBind) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setIpTransparent", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setIpTransparent(WordBase jnienv, WordBase clazz, int fd, int transparent) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setIpRecvOrigDestAddr", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setIpRecvOrigDestAddr(WordBase jnienv, WordBase clazz, int fd, int transparent) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTcpMd5Sig", transition = CFunction.Transition.TO_NATIVE)
    public static native void setTcpMd5Sig(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle address, int scopeId, JNIObjectHandle key) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setInterface", transition = CFunction.Transition.TO_NATIVE)
    public static native void setInterface(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, JNIObjectHandle interfaceAddress, int scopeId, int networkInterfaceIndex) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_getInterface", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getInterface(WordBase jnienv, WordBase clazz, int fd, boolean ipv6);

    @CFunction(value = "io_netty_epoll_linuxsocket_getIpMulticastLoop", transition = CFunction.Transition.NO_TRANSITION)
    public static native int getIpMulticastLoop(WordBase jnienv, WordBase clazz, int fd, boolean ipv6) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setIpMulticastLoop", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setIpMulticastLoop(WordBase jnienv, WordBase clazz, int fd, boolean ipv6, int enabled) throws IOException;

    @CFunction(value = "io_netty_epoll_linuxsocket_setTimeToLive", transition = CFunction.Transition.NO_TRANSITION)
    public static native void setTimeToLive(WordBase jnienv, WordBase clazz, int fd, int ttl) throws IOException;
}
