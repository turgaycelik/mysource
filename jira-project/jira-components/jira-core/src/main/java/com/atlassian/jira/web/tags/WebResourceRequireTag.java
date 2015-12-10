package com.atlassian.jira.web.tags;

import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.text.StrTokenizer;
import webwork.view.taglib.WebWorkTagSupport;

import javax.servlet.jsp.JspException;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;

/**
 * Represents a JSP {@link javax.servlet.jsp.tagext.Tag} that is able to indicate a list of required web-resources for
 * the current page.
 *
 * @since v5.0
 */
public class WebResourceRequireTag extends WebWorkTagSupport
{
    private String modules;
    private String contexts;

    /**
     * Gets the comma delimited list of web-resource modules to require for the current page.
     *
     * @return A comma delimited list of web-resource modules to require for the current page.
     */
    public String getModules()
    {
        return modules;
    }

    public void setModules(final String modules)
    {
        this.modules = modules;
    }

    /**
     * Gets the comma delimited list of web-resource contexts that define resources required for the current page.
     *
     * @return A comma delimited list of web-resource contexts that define resources required for the current page.
     */
    public String getContexts()
    {
        return contexts;
    }

    public void setContexts(final String contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public int doEndTag() throws JspException
    {
        includeResourcesDefinedInModules();
        includeResourcesDefinedInContexts();
        return super.doEndTag();
    }

    private void includeResourcesDefinedInModules()
    {
        for (final StrTokenizer csvTokenizer = StrTokenizer.getCSVInstance(modules); csvTokenizer.hasNext();)
        {
            final String aRequiredWebResourceModule = csvTokenizer.nextToken();
            getWebResourceManager().requireResource(aRequiredWebResourceModule);
        }
    }

    private void includeResourcesDefinedInContexts()
    {
        for (final StrTokenizer csvTokenizer = StrTokenizer.getCSVInstance(contexts); csvTokenizer.hasNext();)
        {
            final String aRequiredWebResourceContext = csvTokenizer.nextToken();
            getWebResourceManager().requireResourcesForContext(aRequiredWebResourceContext);
        }
    }

    @VisibleForTesting
    protected WebResourceManager getWebResourceManager()
    {
        return getComponent(WebResourceManager.class);
    }
}
