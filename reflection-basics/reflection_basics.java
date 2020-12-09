import java.util.ArrayList;

class reflection_basics
{
    public static void main(String[] args) throws Exception
    {
        needEnabledAsserts();
        invokeReflectMethodViaBakedClass();
        invokeReflectMethodViaParamClass(ArrayList.class);
        System.out.println("Assertions passed");
    }

    private static void invokeReflectMethodViaParamClass(Class<?> clazz) throws Exception
    {
        final var method = clazz.getMethod("clone");

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(method.invoke(list));
    }

    private static void invokeReflectMethodViaBakedClass() throws Exception
    {
        final var method = ArrayList.class.getMethod("clone");

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        assert list.equals(list.clone());
        assert list.equals(method.invoke(list));
    }

    private static void needEnabledAsserts()
    {
        boolean enabled = false;
        //noinspection AssertWithSideEffects
        assert enabled = true;
        //noinspection ConstantConditions
        if (!enabled)
        {
            throw new AssertionError("assert not enabled");
        }
    }
}