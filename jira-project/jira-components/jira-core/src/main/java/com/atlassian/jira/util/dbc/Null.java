package com.atlassian.jira.util.dbc;

/**
 * Utility class with checks for nullness. Prefer {@link Assertions}.
 * 
 * @since v3.11
 */
public final class Null
{
    public static void not(final String name, final Object notNull) /* sheepishly */throws IllegalArgumentException
    {
        Assertions.notNull(name, notNull);
    }

    private Null()
    {}
}