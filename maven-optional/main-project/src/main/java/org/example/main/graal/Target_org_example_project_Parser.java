package org.example.main.graal;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className = "org.example.project.Parser")
final class Target_org_example_project_Parser
{
    @Delete
    Target_org_example_optional_NumberParser numberParser;
}

