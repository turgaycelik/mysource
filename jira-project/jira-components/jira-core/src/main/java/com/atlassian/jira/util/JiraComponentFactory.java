package com.atlassian.jira.util;

import java.util.Arrays;

/**
 * Default implementation of {@link com.atlassian.jira.util.ComponentFactory} that uses {@link JiraUtils#loadComponent(Class, java.util.Collection)} and
 * {@link JiraUtils#loadComponent(Class)}.
 *
 * @since v4.0
 */
public final class JiraComponentFactory implements ComponentFactory
{
    private static final JiraComponentFactory INSTANCE = new JiraComponentFactory();

    private JiraComponentFactory()
    {
    }

    public <T> T createObject(final Class<T> type, final Object... arguments)
    {
        if (arguments.length == 0)
        {
            return JiraUtils.loadComponent(type);
        }
        else
        {
            return JiraUtils.loadComponent(type, Arrays.asList(arguments));
        }
    }

    public <T> T createObject(final Class<T> type)
    {
        return JiraUtils.loadComponent(type);
    }

    @Override
    public String toString()
    {
        return "Jira Component Factory";
    }

    public static JiraComponentFactory getInstance()
    {
        return INSTANCE;
    }
}
