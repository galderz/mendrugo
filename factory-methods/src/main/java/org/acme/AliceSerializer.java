package org.acme;

public class AliceSerializer implements Serializer
{
    @Override
    public byte[] serialize(Object obj)
    {
        return new byte[]{1, 2, 3};
    }
}
