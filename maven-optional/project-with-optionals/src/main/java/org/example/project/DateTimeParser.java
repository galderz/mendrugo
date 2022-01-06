package org.example.project;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class DateTimeParser
{
    public ZonedDateTime parse(String text)
    {
        // Example: "Europe/Paris,2015-05-05,10:15:30"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("z,yyyy-MM-dd,HH:mm:ss");
        return ZonedDateTime.parse(text, formatter);
    }
}
