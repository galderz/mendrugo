package org.example.random;

import java.security.SecureRandom;

public class Randoms
{
    final SecureRandom secure;

    public Randoms(SecureRandom secure)
    {
        this.secure = secure;
    }
}
