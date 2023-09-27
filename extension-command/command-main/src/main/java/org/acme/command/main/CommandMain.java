package org.acme.command.main;

import io.quarkus.arc.All;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import java.util.List;

@QuarkusMain
public class CommandMain implements QuarkusApplication
{
    @Inject
    @All
    List<Command> commands;

    @Override
    public int run(String... args)
    {
        System.out.println("Hello " + args[0]);
        commands.forEach(Command::run);
        return 0;
    }
}
