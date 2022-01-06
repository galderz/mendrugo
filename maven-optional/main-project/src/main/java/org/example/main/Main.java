package org.example.main;

import org.example.project.Parser;

public class Main
{
    public static void main(String[] args)
    {
        final Parser parser = new Parser();
        System.out.println("Parse OK: " + parser.parse(args[0]));
    }
}
