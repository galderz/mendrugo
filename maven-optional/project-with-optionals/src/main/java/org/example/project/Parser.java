package org.example.project;

import org.example.optional.NumberParser;

public class Parser
{
    private NumberParser numberParser;
    private DateTimeParser dateTimeParser;

    public Object parse(String text)
    {
        final char ch = text.charAt(0);
        if (Character.isDigit(ch))
        {
            numberParser = new NumberParser();
            return numberParser.parseInt(text);
        }
        else if (Character.isAlphabetic(ch))
        {
            dateTimeParser = new DateTimeParser();
            return dateTimeParser.parse(text);
        }
        else
        {
            throw new IllegalArgumentException("Don't know how to parse: " + text);
        }
    }
}
