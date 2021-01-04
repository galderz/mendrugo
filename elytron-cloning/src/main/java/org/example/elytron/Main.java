package org.example.elytron;

import org.wildfly.security.credential.SecretKeyCredential;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        needEnabledAsserts();
        cloneWithCopyConstructor();
        cloneWithCloneMethod();
        cloneBestGuess();
    }

    private static void cloneWithCopyConstructor()
    {
        final var secretKey = new CopyConstructorSecretKey("blehbleh");
        final var credential = new SecretKeyCredential(secretKey);
        final var cloned = credential.clone();
        assert cloned.getSecretKey() instanceof CopyConstructorSecretKey;
        assert ((CopyConstructorSecretKey) cloned.getSecretKey()).calledCopyConstructor;
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

    private static void cloneBestGuess() throws Exception
    {
        // Option 1. Try with a key that has no clone or copy constructor, but it's checked
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
