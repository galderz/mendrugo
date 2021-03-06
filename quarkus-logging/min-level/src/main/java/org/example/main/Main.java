package org.example.main;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.example.table.Table;

@QuarkusMain
public class Main implements QuarkusApplication
{
    public static final String MIN_LEVEL = "INFO";

    @Override
    public int run(String... args)
    {
        Asserts.check();
        Table.table();
        return 0;
    }
}
