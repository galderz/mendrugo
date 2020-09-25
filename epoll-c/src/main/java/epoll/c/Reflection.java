package epoll.c;

import java.lang.reflect.Executable;
import java.util.function.Consumer;

public class Reflection
{
    final Consumer<CheckedSupplier<Executable>> registerMethod;

    private Reflection(Consumer<CheckedSupplier<Executable>> registerMethod)
    {
        this.registerMethod = registerMethod;
    }

    public static Reflection of(Consumer<Executable> registerMethod)
    {
        Consumer<CheckedSupplier<Executable>> checkedRegisterMethod = m ->
        {
            try
            {
                registerMethod.accept(m.get());
            }
            catch (Throwable t)
            {
                throw new RuntimeException(t);
            }
        };

        return new Reflection(checkedRegisterMethod);
    }

    @FunctionalInterface
    interface CheckedSupplier<T>
    {
        T get() throws Throwable;
    }
}
