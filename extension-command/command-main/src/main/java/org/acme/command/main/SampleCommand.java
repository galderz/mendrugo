package org.acme.command.main;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SampleCommand implements Command
{
    @Override
    public void run()
    {
        System.out.println("A sample command");
    }
}
