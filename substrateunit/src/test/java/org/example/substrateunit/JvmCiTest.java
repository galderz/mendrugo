package org.example.substrateunit;

import jdk.vm.ci.services.Services;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JvmCiTest
{
    @Test
    void testJvmCiVersion()
    {
        assertThat(Services.getSavedProperties().get("java.specification.version"), equalTo("11"));
    }
}
