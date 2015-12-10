package com.atlassian.jira.plugin;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.ModuleDescriptor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link ModuleDescriptor} for Plugin Points declared in JIRA.
 *
 * @since v5.0
 */
public interface JiraResourcedModuleDescriptor<T> extends ModuleDescriptor<T>
{
    I18nHelper getI18nBean();
    
    public String getHtml(final String resourceName);

    public String getHtml(final String resourceName, final Map<String, ?> startingParams);

    public void writeHtml(final String resourceName, final Map<String, ?> startingParams, final Writer writer) throws IOException;
}
