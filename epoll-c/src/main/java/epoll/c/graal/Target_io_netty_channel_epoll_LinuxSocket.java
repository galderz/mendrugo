package epoll.c.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.jni.JNIObjectHandles;
import com.oracle.svm.jni.JNIThreadLocalEnvironment;
import epoll.c.graal.c.NettyEpollSocket;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.unix.PeerCredentials;
import org.graalvm.word.WordFactory;

import java.io.IOException;

@TargetClass(className = "io.netty.channel.epoll.LinuxSocket")
final class Target_io_netty_channel_epoll_LinuxSocket
{
    @Substitute
    private static void joinGroup(int fd, boolean ipv6, byte[] group, byte[] interfaceAddress, int scopeId, int interfaceIndex) throws IOException
    {
        NettyEpollSocket.joinGroup(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(group), JNIObjectHandles.createLocal(interfaceAddress), scopeId, interfaceIndex);
    }

    @Substitute
    private static void joinSsmGroup(int fd, boolean ipv6, byte[] group, byte[] interfaceAddress, int scopeId, int interfaceIndex, byte[] source) throws IOException
    {
        NettyEpollSocket.joinSsmGroup(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(group), JNIObjectHandles.createLocal(interfaceAddress), scopeId, interfaceIndex, JNIObjectHandles.createLocal(source));
    }

    @Substitute
    private static void leaveGroup(int fd, boolean ipv6, byte[] group, byte[] interfaceAddress, int scopeId, int interfaceIndex) throws IOException
    {
        NettyEpollSocket.leaveGroup(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(group), JNIObjectHandles.createLocal(interfaceAddress), scopeId, interfaceIndex);
    }

    @Substitute
    private static void leaveSsmGroup(int fd, boolean ipv6, byte[] group, byte[] interfaceAddress, int scopeId, int interfaceIndex, byte[] source) throws IOException
    {
        NettyEpollSocket.leaveSsmGroup(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(group), JNIObjectHandles.createLocal(interfaceAddress), scopeId, interfaceIndex, JNIObjectHandles.createLocal(source));
    }

    @Substitute
    private static long sendFile(int socketFd, DefaultFileRegion src, long baseOffset, long offset, long length) throws IOException
    {
        return NettyEpollSocket.sendFile(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), socketFd, JNIObjectHandles.createLocal(src), baseOffset, offset, length);
    }

    @Substitute
    private static int getTcpDeferAccept(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpDeferAccept(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isTcpQuickAck(int fd) throws IOException
    {
        return NettyEpollSocket.isTcpQuickAck(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isTcpCork(int fd) throws IOException
    {
        return NettyEpollSocket.isTcpCork(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getSoBusyPoll(int fd) throws IOException
    {
        return NettyEpollSocket.getSoBusyPoll(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTcpNotSentLowAt(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpNotSentLowAt(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTcpKeepIdle(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpKeepIdle(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTcpKeepIntvl(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpKeepIntvl(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTcpKeepCnt(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpKeepCnt(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTcpUserTimeout(int fd) throws IOException
    {
        return NettyEpollSocket.getTcpUserTimeout(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int getTimeToLive(int fd) throws IOException
    {
        return NettyEpollSocket.getTimeToLive(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isIpFreeBind(int fd) throws IOException
    {
        return NettyEpollSocket.isIpFreeBind(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isIpTransparent(int fd) throws IOException
    {
        return NettyEpollSocket.isIpTransparent(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isIpRecvOrigDestAddr(int fd) throws IOException
    {
        return NettyEpollSocket.isIpRecvOrigDestAddr(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static void getTcpInfo(int fd, long[] array) throws IOException
    {
        NettyEpollSocket.getTcpInfo(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, array);
    }

    @Substitute
    private static PeerCredentials getPeerCredentials(int fd) throws IOException
    {
        return NettyEpollSocket.getPeerCredentials(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static int isTcpFastOpenConnect(int fd) throws IOException
    {
        return NettyEpollSocket.isTcpFastOpenConnect(WordFactory.nullPointer(), WordFactory.nullPointer(), fd);
    }

    @Substitute
    private static void setTcpDeferAccept(int fd, int deferAccept) throws IOException
    {
        NettyEpollSocket.setTcpDeferAccept(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, deferAccept);
    }

    @Substitute
    private static void setTcpQuickAck(int fd, int quickAck) throws IOException
    {
        NettyEpollSocket.setTcpQuickAck(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, quickAck);
    }

    @Substitute
    private static void setTcpCork(int fd, int tcpCork) throws IOException
    {
        NettyEpollSocket.setTcpCork(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpCork);
    }

    @Substitute
    private static void setSoBusyPoll(int fd, int loopMicros) throws IOException
    {
        NettyEpollSocket.setSoBusyPoll(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, loopMicros);
    }

    @Substitute
    private static void setTcpNotSentLowAt(int fd, int tcpNotSentLowAt) throws IOException
    {
        NettyEpollSocket.setTcpNotSentLowAt(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpNotSentLowAt);
    }

    @Substitute
    private static void setTcpFastOpen(int fd, int tcpFastopenBacklog) throws IOException
    {
        NettyEpollSocket.setTcpFastOpen(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpFastopenBacklog);
    }

    @Substitute
    private static void setTcpFastOpenConnect(int fd, int tcpFastOpenConnect) throws IOException
    {
        NettyEpollSocket.setTcpFastOpenConnect(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, tcpFastOpenConnect);
    }

    @Substitute
    private static void setTcpKeepIdle(int fd, int seconds) throws IOException
    {
        NettyEpollSocket.setTcpKeepIdle(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, seconds);
    }

    @Substitute
    private static void setTcpKeepIntvl(int fd, int seconds) throws IOException
    {
        NettyEpollSocket.setTcpKeepIntvl(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, seconds);
    }

    @Substitute
    private static void setTcpKeepCnt(int fd, int probes) throws IOException
    {
        NettyEpollSocket.setTcpKeepCnt(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, probes);
    }

    @Substitute
    private static void setTcpUserTimeout(int fd, int milliseconds)throws IOException
    {
        NettyEpollSocket.setTcpUserTimeout(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, milliseconds);
    }

    @Substitute
    private static void setIpFreeBind(int fd, int freeBind) throws IOException
    {
        NettyEpollSocket.setIpFreeBind(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, freeBind);
    }

    @Substitute
    private static void setIpTransparent(int fd, int transparent) throws IOException
    {
        NettyEpollSocket.setIpTransparent(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, transparent);
    }

    @Substitute
    private static void setIpRecvOrigDestAddr(int fd, int transparent) throws IOException
    {
        NettyEpollSocket.setIpRecvOrigDestAddr(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, transparent);
    }

    @Substitute
    private static void setTcpMd5Sig(int fd, boolean ipv6, byte[] address, int scopeId, byte[] key) throws IOException
    {
        NettyEpollSocket.setTcpMd5Sig(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(address), scopeId, JNIObjectHandles.createLocal(key));
    }

    @Substitute
    private static void setInterface(int fd, boolean ipv6, byte[] interfaceAddress, int scopeId, int networkInterfaceIndex) throws IOException
    {
        NettyEpollSocket.setInterface(JNIThreadLocalEnvironment.getAddress(), WordFactory.nullPointer(), fd, ipv6, JNIObjectHandles.createLocal(interfaceAddress), scopeId, networkInterfaceIndex);
    }

    @Substitute
    private static int getInterface(int fd, boolean ipv6)
    {
         return NettyEpollSocket.getInterface(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6);
    }

    @Substitute
    private static int getIpMulticastLoop(int fd, boolean ipv6) throws IOException
    {
        return NettyEpollSocket.getIpMulticastLoop(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6);
    }

    @Substitute
    private static void setIpMulticastLoop(int fd, boolean ipv6, int enabled) throws IOException
    {
        NettyEpollSocket.setIpMulticastLoop(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ipv6, enabled);
    }

    @Substitute
    private static void setTimeToLive(int fd, int ttl) throws IOException
    {
        NettyEpollSocket.setTimeToLive(WordFactory.nullPointer(), WordFactory.nullPointer(), fd, ttl);
    }
}
