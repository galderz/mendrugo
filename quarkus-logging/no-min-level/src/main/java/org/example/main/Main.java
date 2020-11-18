package org.example.main;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.example.iron.Iron;

@QuarkusMain
public class Main implements QuarkusApplication
{
    @Override
    public int run(String... args)
    {
        assert false;
        Iron.iron();
        return 0;
    }
}
