package org.example.main.graal;

import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.TargetClass;
import org.example.optional.NumberParser;

@TargetClass(className = "org.example.project.Parser")
final class Target_org_example_project_Parser
{
    @Delete
    NumberParser numberParser;
}

