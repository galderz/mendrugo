package org.acme.common;

public record Whale(
    double swimSpeed
    , String color
) implements Mammal {}
