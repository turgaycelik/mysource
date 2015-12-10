package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

/**
 * Implementation of JqlFunctionModuleDescriptor.
 *
 * @since v4.3
 */
public class JqlFunctionModuleDescriptorImpl extends AbstractJiraModuleDescriptor<JqlFunction> implements JqlFunctionModuleDescriptor
{
    private volatile String fName;

    private volatile boolean isList = false;

    public JqlFunctionModuleDescriptorImpl(final JiraAuthenticationContext authenticationContext,
            final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        final Element fNameElement = element.element("fname");
        if (fNameElement != null)
        {
            fName = StringUtils.trimToNull(fNameElement.getText());
        }

        // if <list> is not specified, default to false
        final Element listElement = element.element("list");
        if (listElement != null)
        {
            isList = Boolean.parseBoolean(listElement.getTextTrim());
        }
    }

    @Override
    public String getFunctionName()
    {
        return fName;
    }

    @Override
    public boolean isList()
    {
        return isList;
    }

    @Override
    public boolean equals(Object obj)
    {
        return new ModuleDescriptors.EqualsBuilder(this).isEqualsTo(obj);
    }

    @Override
    public int hashCode()
    {
        return new ModuleDescriptors.HashCodeBuilder(this).toHashCode();
    }
}
