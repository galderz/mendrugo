package org.acme.command.sample;

import org.acme.command.annotations.WithCommand;

public class HelloWorldCommand
{
    @WithCommand
    public void helloWorld()
    {
        System.out.println("Annotated hello world!");
    }
}
