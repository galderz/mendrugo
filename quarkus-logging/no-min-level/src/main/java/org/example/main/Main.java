package org.example.main;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.example.house.House;

@QuarkusMain
public class Main implements QuarkusApplication
{
    @Override
    public int run(String... args)
    {
        assert false;
        House.house();
        return 0;
    }
}
