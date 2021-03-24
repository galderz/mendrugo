package org.example.random;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.security.SecureRandom;

@TargetClass(className = "org.example.random.Utils")
final class Target_org_example_random_Utils {
    @Delete
    static SecureRandom testRandom;
}

class FeaturesAndSubstitutions
{
}
