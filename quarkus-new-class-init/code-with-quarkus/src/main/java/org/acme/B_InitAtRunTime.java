package org.acme;

public class B_InitAtRunTime {
    static int b = 100;

    static {
        A_InitAtBuildTime.a = A_InitAtBuildTime.a + 1;
    }
}
