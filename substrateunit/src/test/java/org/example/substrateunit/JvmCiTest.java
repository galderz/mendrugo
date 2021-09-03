package org.example.substrateunit;

import jdk.vm.ci.services.Services;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

// Keep all methods as public to avoid the need to open up modules.
// More details: https://stackoverflow.com/a/53462763/186429
public class JvmCiTest
{
    @Test
    public void testJvmCiVersion()
    {
        assertThat(Services.getSavedProperties().get("java.specification.version"), equalTo("11"));
    }
}
