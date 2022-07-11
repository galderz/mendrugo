package org.example;

public class B_InitAtRunTime
{
    static int b = 123;

    static {
        A_InitAtBuildTime.a = A_InitAtBuildTime.a + 1;
    }
}
