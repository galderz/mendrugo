package org.acme.common;

public record Elephant(
    int hornLength
    , String continent
) implements Mammal {}
