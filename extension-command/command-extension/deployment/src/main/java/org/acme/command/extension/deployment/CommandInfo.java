package org.acme.command.extension.deployment;

import org.jboss.jandex.MethodInfo;

import java.util.ArrayList;
import java.util.List;

public record CommandInfo(List<MethodInfo> methods)
{
    public static final class Builder
    {
        final List<MethodInfo> methods = new ArrayList<>();

        void withMethod(MethodInfo methodInfo)
        {
            methods.add(methodInfo);
        }

        CommandInfo build()
        {
            return new CommandInfo(methods);
        }
    }
}