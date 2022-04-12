package org.example.anon.java.io;

abstract class ClassCache<T>
{
    protected abstract T computeValue(Class<?> cl);
}
