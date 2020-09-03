package epoll;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class NettyJNI
{
    static void registerJNI(Effects effects)
    {
        defaultFileRegion(effects);
        nativeDatagramPacket(effects);
        peerCredentials(effects);
        datagramSocket(effects);
        effects.registerClass.accept(classForName("io.netty.channel.ChannelException"));
    }

    private static void defaultFileRegion(Effects effects)
    {
        final Class<?> clazz = classForName("io.netty.channel.DefaultFileRegion");
        effects.registerClass.accept(clazz);
        effects.registerField.accept(privateField("file", clazz));
        effects.registerField.accept(privateField("transferred", clazz));
    }

    private static void nativeDatagramPacket(Effects effects)
    {
        final Class<?> clazz = classForName("io.netty.channel.epoll.NativeDatagramPacketArray$NativeDatagramPacket");
        effects.registerClass.accept(clazz);
        effects.registerField.accept(privateField("addr", clazz));
        effects.registerField.accept(privateField("addrLen", clazz));
        effects.registerField.accept(privateField("scopeId", clazz));
        effects.registerField.accept(privateField("port", clazz));
        effects.registerField.accept(privateField("memoryAddress", clazz));
        effects.registerField.accept(privateField("count", clazz));
    }

    private static void peerCredentials(Effects effects)
    {
        final Class<?> clazz = classForName("io.netty.channel.unix.PeerCredentials");
        effects.registerClass.accept(clazz);
        effects.registerMethod.accept(
            classGetDeclaredConstructor(clazz, int.class, int.class, int[].class)
        );
    }

    private static void datagramSocket(Effects effects)
    {
        final Class<?> clazz = classForName("io.netty.channel.unix.DatagramSocketAddress");
        effects.registerClass.accept(clazz);
        effects.registerMethod.accept(
            classGetDeclaredConstructor(clazz, byte[].class, int.class, int.class, int.class, clazz)
        );
    }

    private static Field privateField(String fieldName, Class<?> clazz)
    {
        final var fileField = classGetDeclaredField(fieldName, clazz);
        fileField.setAccessible(true);
        return fileField;
    }

    private static Executable classGetDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes)
    {
        try
        {
            final Constructor<?> ctor = clazz.getDeclaredConstructor(parameterTypes);
            ctor.setAccessible(true);
            return ctor;
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Field classGetDeclaredField(String fieldName, Class<?> clazz)
    {
        try
        {
            return clazz.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> classForName(String className)
    {
        try
        {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    final static class Effects
    {
        final Consumer<Class<?>> registerClass;
        final Consumer<Field> registerField;
        final Consumer<Executable> registerMethod;

        Effects(Consumer<Class<?>> registerClass, Consumer<Field> registerField, Consumer<Executable> registerMethod) {
            this.registerClass = registerClass;
            this.registerField = registerField;
            this.registerMethod = registerMethod;
        }
    }

    public static void main(String[] args)
    {
        final var effects = new Effects(c -> {}, f -> {}, m -> {});
        registerJNI(effects);
    }
}
