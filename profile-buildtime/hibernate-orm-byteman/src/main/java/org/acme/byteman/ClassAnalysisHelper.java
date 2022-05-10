package org.acme.byteman;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

public class ClassAnalysisHelper extends Helper
{
    public ClassAnalysisHelper(Rule rule)
    {
        super(rule);
    }

    public void trackClass(Class<?> clazz)
    {
        System.out.println("Track " + clazz);
        System.out.println(formatStack());
    }
}
