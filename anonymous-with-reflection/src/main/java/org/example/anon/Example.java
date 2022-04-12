package org.example.anon;

import org.example.anon.java.io.NewObjectStreamClass;
import org.example.anon.java.io.ObjectStreamClass;

import java.util.*;
import java.util.stream.*;

public class Example
{
    public static void main(String[] args) throws Exception
    {
        System.out.println(
            "[ObjectStreamClass.Caches] Local descs is " + System.identityHashCode(ObjectStreamClass.Caches.localDescs)
        );
        System.out.println(
            "[ObjectStreamClass.Caches] Reflectors is " + System.identityHashCode(ObjectStreamClass.Caches.reflectors)
        );

        System.out.println(
            "[NewObjectStreamClass.Caches] Local descs is " + System.identityHashCode(NewObjectStreamClass.Caches.localDescs)
        );
        System.out.println(
            "[NewObjectStreamClass.Caches] Reflectors is " + System.identityHashCode(NewObjectStreamClass.Caches.reflectors)
        );
    }
}
