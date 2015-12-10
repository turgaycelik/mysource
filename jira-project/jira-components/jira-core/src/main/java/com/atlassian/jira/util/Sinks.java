package com.atlassian.jira.util;

import com.atlassian.jira.util.dbc.Assertions;

public class Sinks
{
    public static <T> Consumer<T> nullChecker()
    {
        return new Consumer<T>()
        {
            public void consume(final T element)
            {
                Assertions.notNull("element", element);
            }
        };
    }

    ///CLOVER:OFF
    private Sinks()
    {
        throw new AssertionError("cannot instantiate");
    }
    ///CLOVER:ON
}
