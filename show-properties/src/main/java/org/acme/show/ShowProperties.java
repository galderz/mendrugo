package org.acme.show;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.Properties;

@QuarkusMain
public class ShowProperties implements QuarkusApplication
{
    private static final String INDENT = "    ";
    private static final String PROP_SETTINGS = "Property settings:";

    @Override
    public int run(String... args) throws Exception
    {
        Properties p = System.getProperties();
        System.out.println(PROP_SETTINGS);
        for (String key : p.stringPropertyNames().stream().sorted().toList())
        {
            printPropertyValue(key, p.getProperty(key));
        }
        System.out.println();
        return 0;
    }

    private static void printPropertyValue(String key, String value)
    {
        System.out.print(INDENT + key + " = ");
        if (key.equals("line.separator"))
        {
            for (byte b : value.getBytes())
            {
                switch (b)
                {
                    case 0xd:
                        System.out.print("\\r ");
                        break;
                    case 0xa:
                        System.out.print("\\n ");
                        break;
                    default:
                        // print any bizarre line separators in hex, but really
                        // shouldn't happen.
                        System.out.printf("0x%02X", b & 0xff);
                        break;
                }
            }
            System.out.println();
            return;
        }
        if (!isPath(key))
        {
            System.out.println(value);
            return;
        }
        String[] values = value.split(System.getProperty("path.separator"));
        boolean first = true;
        for (String s : values)
        {
            if (first)
            { // first line treated specially
                System.out.println(s);
                first = false;
            }
            else
            { // following lines prefix with indents
                System.out.println(INDENT + INDENT + s);
            }
        }
    }

    private static boolean isPath(String key)
    {
        return key.endsWith(".dirs") || key.endsWith(".path");
    }
}
