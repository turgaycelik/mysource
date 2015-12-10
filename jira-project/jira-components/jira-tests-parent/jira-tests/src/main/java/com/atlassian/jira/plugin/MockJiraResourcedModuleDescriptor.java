package com.atlassian.jira.plugin;

import com.atlassian.jira.mock.plugin.MockModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @since v6.2.3
 */
public abstract class MockJiraResourcedModuleDescriptor<T> extends MockModuleDescriptor<T>
        implements JiraResourcedModuleDescriptor<T>
{
    public MockJiraResourcedModuleDescriptor(final Class<T> type)
    {
        super(type);
    }

    public MockJiraResourcedModuleDescriptor(final Class<T> type, Plugin plugin, String key)
    {
        super(type, plugin, key);
    }

    @Override
    public I18nHelper getI18nBean()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getHtml(final String resourceName)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getHtml(final String resourceName, final Map<String, ?> startingParams)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void writeHtml(final String resourceName, final Map<String, ?> startingParams, final Writer writer)
            throws IOException
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
