///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA_OPTIONS -ea
//DEPS org.graalvm.nativeimage:svm:21.2.0-SNAPSHOT

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.function.UnaryOperator;

import static java.security.AccessController.doPrivileged;

class example
{
    public static void main(String... args)
    {
        needEnabledAsserts();

        final var list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        final var otherList = new ArrayList<>();
        list.add(1);
        list.add(2);

        final var cloned = list.clone();
        assert list.equals(cloned);
        assert !otherList.equals(cloned);

        final var string = "helloworld";
        assert string.equals(new String(string));
        assert !string.equals(new String("hello"));

        final var cloneMethod = checkForCloneMethod(ArrayList.class, null);
        assert list.equals(cloneMethod.apply(list));
        assert !otherList.equals(cloneMethod.apply(list));

//        final var copyCtor = checkForCopyCtor(String.class, String.class);
//        assert string.equals(copyCtor.apply(string));
//        assert !string.equals(copyCtor.apply("hello"));
    }

    private static UnaryOperator<Object> checkForCloneMethod(final Class<?> declType, final Class<?> returnType) {
        final var method = doPrivileged((PrivilegedAction<Method>) () ->
        {
            try
            {
                return declType.getMethod("clone");
                // return ArrayList.class.getMethod("clone");
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        });
        return method == null ? null : obj ->
        {
            try
            {
                return method.invoke(obj);
            }
            catch (RuntimeException | Error e)
            {
                throw e;
            }
            catch (Throwable throwable)
            {
                throw new UndeclaredThrowableException(throwable);
            }
        };
    }


//    private static UnaryOperator<Object> checkForCloneMethod(final Class<?> declType, final Class<?> returnType) {
//        final var method = doPrivileged((PrivilegedAction<Method>) () ->
//        {
//            try
//            {
//                return declType.getMethod("clone");
//            }
//            catch (NoSuchMethodException e)
//            {
//                return null;
//            }
//        });
//
//        return method == null ? null : obj ->
//        {
//            try
//            {
//                return method.invoke(obj);
//            }
//            catch (RuntimeException | Error e)
//            {
//                throw e;
//            }
//            catch (Throwable throwable)
//            {
//                throw new UndeclaredThrowableException(throwable);
//            }
//        };
//    }
//
//    private static UnaryOperator<Object> checkForCopyCtor(final Class<?> declType, final Class<?> paramType)
//    {
//        final var constructor = doPrivileged((PrivilegedAction<Constructor>) () ->
//        {
//            try
//            {
//                return declType.getConstructor(paramType);
//            }
//            catch (NoSuchMethodException e)
//            {
//                return null;
//            }
//        });
//
//        return constructor == null ? null : param ->
//        {
//            try
//            {
//                return constructor.newInstance(param);
//            }
//            catch (RuntimeException | Error e)
//            {
//                throw e;
//            }
//            catch (Throwable throwable)
//            {
//                throw new UndeclaredThrowableException(throwable);
//            }
//        };
//    }

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

    @AutomaticFeature
    private static final class Reflection implements Feature
    {
        public void beforeAnalysis(BeforeAnalysisAccess access)
        {
            System.out.println("Register for reflection");
//            RuntimeReflection.register(ArrayList.class);
//            RuntimeReflection.register(Class.class);

            try
            {
                RuntimeReflection.register(ArrayList.class.getMethod("clone"));
                // RuntimeReflection.register(String.class.getConstructor(String.class));
                // RuntimeReflection.register(String.class);
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
