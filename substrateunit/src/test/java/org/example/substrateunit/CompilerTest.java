package org.example.substrateunit;

import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CompilerTest
{
    @Test
    public void testJavaSpecificationVersion()
    {
        assertThat(JavaVersionUtil.JAVA_SPEC, equalTo(11));
    }
}
