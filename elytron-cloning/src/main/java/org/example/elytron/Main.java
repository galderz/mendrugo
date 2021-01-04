package org.example.elytron;

import org.wildfly.security.credential.SecretKeyCredential;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Key;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.function.UnaryOperator;

import static java.security.AccessController.doPrivileged;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        needEnabledAsserts();

        // findReflectionCopyConstructor();
        cloneWithCopyConstructor();

        // findReflectionCloneMethod();
        cloneWithCloneMethod();

        // findReflectionNoCopyCtorOrCloneMethod();
        cloneBestGuess();
    }

    private static void findReflectionCopyConstructor()
    {
        final var secretKey = new CopyConstructorSecretKey("blehbleh");
        final var ctorOp = checkForCopyCtor(CopyConstructorSecretKey.class, SecretKey.class);
        final var copied = ctorOp.apply(secretKey);
        assert copied instanceof CopyConstructorSecretKey;
        assert ((CopyConstructorSecretKey) copied).calledCopyConstructor;
    }

    private static UnaryOperator<Key> checkForCopyCtor(final Class<?> declType, final Class<?> paramType) {
        final Constructor<?> constructor = doPrivileged((PrivilegedAction<Constructor<?>>) () ->
        {
            try
            {
                return declType.getDeclaredConstructor(paramType);
            }
            catch (NoSuchMethodException e)
            {
                System.out.printf("Copy ctor in %s for parameter %s not found%n", declType, paramType);
                return null;
            }
        });
        return constructor == null ? null : produceOp(constructor);
    }

    private static UnaryOperator<Key> produceOp(final Constructor<?> constructor)
    {
        return original ->
        {
            try
            {
                return (Key) constructor.newInstance(original);
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

    private static void cloneWithCopyConstructor()
    {
        final var secretKey = new CopyConstructorSecretKey("blehbleh");
        final var credential = new SecretKeyCredential(secretKey);
        System.out.println("Call SecretKeyCredential.clone()");
        final var cloned = credential.clone();
        assert cloned.getSecretKey() instanceof CopyConstructorSecretKey;
        assert ((CopyConstructorSecretKey) cloned.getSecretKey()).calledCopyConstructor;
    }

    private static void findReflectionCloneMethod()
    {
        final var secretKey = new CloneMethodSecretKey(getPasswordKey("blehbleh"));
        final var cloneOp = checkForCloneMethod(CloneMethodSecretKey.class, SecretKey.class);
        final var cloned = cloneOp.apply(secretKey);
        assert cloned instanceof CloneMethodSecretKey;
        assert secretKey.calledClone;
    }

    private static UnaryOperator<Key> checkForCloneMethod(final Class<?> declType, final Class<?> returnType)
    {
        final Method method = doPrivileged((PrivilegedAction<Method>) () ->
        {
            try
            {
                final var cloneMethod = declType.getDeclaredMethod("clone");
                if (cloneMethod.getReturnType() == returnType)
                    return cloneMethod;

                return null;
            }
            catch (NoSuchMethodException e)
            {
                return null;
            }
        });

        return method == null ? null : produceOp(method);
    }

    private static UnaryOperator<Key> produceOp(final Method method)
    {
        return original ->
        {
            try
            {
                return (Key) method.invoke(original);
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

    public static final class CopyConstructorSecretKey implements SecretKey
    {
        final SecretKey key;
        final boolean calledCopyConstructor;

        public CopyConstructorSecretKey(String password)
        {
            this.key = getPasswordKey(password);
            this.calledCopyConstructor = false;
        }

        public CopyConstructorSecretKey(SecretKey key)
        {
            this.key = key;
            this.calledCopyConstructor = true;
        }

        @Override
        public void destroy()
        {
            // Required to force copy constructor to be looked up
        }

        @Override
        public String getAlgorithm()
        {
            return key.getAlgorithm();
        }

        @Override
        public String getFormat()
        {
            return key.getFormat();
        }

        @Override
        public byte[] getEncoded()
        {
            return key.getEncoded();
        }
    }

    private static void cloneWithCloneMethod() throws Exception
    {
        final var secretKey = new CloneMethodSecretKey(getPasswordKey("blehbleh"));
        final var credential = new SecretKeyCredential(secretKey);
        final var cloned = credential.clone();
        assert cloned.getSecretKey() instanceof CloneMethodSecretKey;
        assert secretKey.calledClone;
    }

    public static final class CloneMethodSecretKey implements SecretKey
    {
        final SecretKey key;
        boolean calledClone;

        public CloneMethodSecretKey(SecretKey key)
        {
            this.key = key;
        }

        @Override
        public SecretKey clone()
        {
            calledClone = true;
            return this;
        }

        @Override
        public void destroy()
        {
            // Required to force clone method to be looked up
        }

        @Override
        public String getAlgorithm()
        {
            return key.getAlgorithm();
        }

        @Override
        public String getFormat()
        {
            return key.getFormat();
        }

        @Override
        public byte[] getEncoded()
        {
            return key.getEncoded();
        }
    }

    private static void findReflectionNoCopyCtorOrCloneMethod()
    {
        final var secretKey = getPasswordKey("blahblah");
        final var cloneOp = checkForCloneMethod(secretKey.getClass(), PBEKey.class);
        assert cloneOp == null;
        final var copyCtorOp = checkForCloneMethod(secretKey.getClass(), PBEKey.class);
        assert copyCtorOp == null;
    }

    private static void cloneBestGuess() throws Exception
    {
        final var secretKey = getPasswordKey("blahblah");
        final var credential = new SecretKeyCredential(secretKey);
        final var cloned = credential.clone();
        assert credential.equals(cloned);
    }

    private static SecretKey getPasswordKey(String password)
    {
        int keyLength = 256;
        int saltLength = keyLength / 8;
        int iterations = 65536;
        byte[] salt = new SecureRandom().generateSeed(saltLength);
        PBEKeySpec passwordKeySpec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory secretKeyFactory = null;
        try
        {
            secretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndTripleDES");
            return secretKeyFactory.generateSecret(passwordKeySpec);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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
