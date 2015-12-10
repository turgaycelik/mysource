package com.atlassian.jira.web.tags;

import javax.servlet.jsp.JspException;

import com.atlassian.plugin.webresource.WebResourceManager;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Holds the unit tests for {@link WebResourceRequireTag}
 *
 * @since v5.0
 */
public class TestWebResourceRequireTag
{
    private WebResourceManager mockWebResourceManager = mock(WebResourceManager.class);

    @Test
    public void isAbleToSpecifyOneRequiredResourceModule() throws JspException
    {
        final WebResourceRequireTag resourceRequireTag = createWebResourceRequireTag();
        resourceRequireTag.setModules("a.test.resource");

        resourceRequireTag.doEndTag();

        verify(mockWebResourceManager).requireResource("a.test.resource");
    }

    @Test
    public void isAbleToSpecifyMoreThanOneRequiredResourceModuleSeparatedByCommas() throws JspException
    {
        final WebResourceRequireTag resourceRequireTag = createWebResourceRequireTag();
        resourceRequireTag.setModules("test.resource.module.1, test.resource.module.2");

        resourceRequireTag.doEndTag();

        verify(mockWebResourceManager).requireResource("test.resource.module.1");
        verify(mockWebResourceManager).requireResource("test.resource.module.2");
    }

    @Test
    public void isAbleToSpecifyOneRequiredResourceContext() throws JspException
    {
        final WebResourceRequireTag resourceRequireTag = createWebResourceRequireTag();
        resourceRequireTag.setContexts("a.test.context");

        resourceRequireTag.doEndTag();

        verify(mockWebResourceManager).requireResourcesForContext("a.test.context");
    }

    @Test
    public void isAbleToSpecifyMoreThanOneRequiredResourceContextSeparatedByCommas() throws JspException
    {
        final WebResourceRequireTag resourceRequireTag = createWebResourceRequireTag();
        resourceRequireTag.setContexts("test.resource.context.1, test.resource.context.2");

        resourceRequireTag.doEndTag();

        verify(mockWebResourceManager).requireResourcesForContext("test.resource.context.1");
        verify(mockWebResourceManager).requireResourcesForContext("test.resource.context.2");
    }

    private WebResourceRequireTag createWebResourceRequireTag()
    {
        return new WebResourceRequireTag()
            {
                @Override
                protected WebResourceManager getWebResourceManager()
                {
                    return mockWebResourceManager;
                }
            };
    }
}
