/*
 * Copyright (c) 2002-2004 All rights reserved.
 */
package com.atlassian.jira.util.dbc;

/**
 * Utility class with checks for nullness. Prefer {@link Assertions}.
 * 
 * @since v3.11
 * @deprecated Use {@link Assertions#notNull(String,Object)} instead. Since v6.0.
 */
@Deprecated
public final class Null
{
    /**
     * @deprecated Use {@link Assertions#notNull(String,Object)} instead. Since v6.0.
     */
    @Deprecated
    public static void not(final String name, final Object notNull) /* sheepishly */throws IllegalArgumentException
    {
        Assertions.notNull(name, notNull);
    }

    private Null()
    {}
}