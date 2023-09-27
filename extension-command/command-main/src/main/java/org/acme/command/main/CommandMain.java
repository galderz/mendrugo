package org.acme.command.main;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain
public class CommandMain implements QuarkusApplication
{
    @Inject
    Command command;

    @Override
    public int run(String... args)
    {
        System.out.println("Hello " + args[0]);
        command.run();
        return 0;
    }
}
