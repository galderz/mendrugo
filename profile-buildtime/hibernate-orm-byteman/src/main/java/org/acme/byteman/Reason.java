package org.acme.byteman;

record Reason(int iteration, Class<?> clazz, String reason) {}