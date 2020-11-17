package org.example.main;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.example.car.Car;
import org.example.house.House;

@QuarkusMain
public class Main implements QuarkusApplication
{
    @Override
    public int run(String... args)
    {
        House.house();
        // Car.car();
        return 0;
    }
}
