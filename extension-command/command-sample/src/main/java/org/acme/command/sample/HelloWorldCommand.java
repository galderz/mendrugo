package org.acme.command.sample;

import org.acme.command.annotations.WithCommand;

public class HelloWorldCommand
{
    @WithCommand
    public void firstCommand()
    {
        System.out.println("Executing first annotated command!");
    }

    @WithCommand
    public void secondCommand()
    {
        System.out.println("Executing second annotated command!");
    }
}
