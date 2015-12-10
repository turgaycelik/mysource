package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.ExampleGenerator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This simple function returns an example date/time formatted with the given pattern.
 * To be used for generating an example of the output for a given pattern.
 *
 * @since v5.2
 */
public class ExampleDateFormatOutputGenerator implements ExampleGenerator
{
    private static final long EXAMPLE_DATE = 1179892547906l; //Wed May 23 13:55:47 EST 2007

    public String generate(Object pattern)
    {
        if (!(pattern instanceof String))
        {
            return null;
        }
        try
        {
        	DateTimeFormatter formatter = DateTimeFormat.forPattern((String) pattern);
        	return formatter.print(EXAMPLE_DATE);
        }
        catch (IllegalArgumentException iae)
        {
        	return null;
        }
    }
}


