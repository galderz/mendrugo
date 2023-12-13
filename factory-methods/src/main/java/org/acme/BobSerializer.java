package org.acme;

public class BobSerializer implements Serializer
{
    @Override
    public byte[] serialize(Object obj)
    {
        return new byte[]{10, 20, 30};
    }
}
