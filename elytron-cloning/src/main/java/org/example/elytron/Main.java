package org.example.elytron;

import org.wildfly.security.credential.KeyPairCredential;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        needEnabledAsserts();

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024);
        KeyPair keyPair = kpGen.generateKeyPair();

        final var credential = new KeyPairCredential(keyPair);
        final var cloned = credential.clone();
        assert credential.equals(cloned);
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
