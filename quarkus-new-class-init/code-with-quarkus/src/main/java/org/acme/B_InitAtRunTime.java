package org.acme;

public class B_InitAtRunTime {
    static int b = 100;

    static {
        System.out.println("static {} for B_InitAtRunTime");
        A_InitAtBuildTime.a = A_InitAtBuildTime.a + 1;
    }
}
