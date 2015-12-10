package com.atlassian.jira.plugin.navigation;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.web.util.ProductVersionDataBeanProvider;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.conditions.NeverDisplayCondition;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.webresource.api.assembler.PageBuilderService;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link HeaderFooterRendering}
 *
 * HeaderFooterRendering may be a ghetto, but it's pretty important that JIRA doesn't get stabbed while walking through that neighbourhood.
 *
 * @since v6.1
 */
@RunWith (JUnit4.class)
public class HeaderFooterRenderingTest
{
    private final ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);
    private final PageBuilderService pageBuilderService = mock(PageBuilderService.class);
    private final WebInterfaceManager webInterfaceManager = mock(WebInterfaceManager.class);
    private final ProductVersionDataBeanProvider productVersionDataBeanProvider = mock(ProductVersionDataBeanProvider.class);
    private final WebResourceUrlProvider webResourceUrlProvider = mock(WebResourceUrlProvider.class);

    private final HeaderFooterRendering headerFooterRendering = spy(new HeaderFooterRendering(applicationProperties, pluginAccessor, pageBuilderService, webInterfaceManager, productVersionDataBeanProvider, webResourceUrlProvider));

    @Test
    public void testRenderHeaderHonoursCondition() throws IOException
    {
        // If we're not careful, a null Condition returned from the TopNavigationModuleDescriptor may cause a NPE.
        // This has bitten us in the past.
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PluggableTopNavigation topNavModule = mock(PluggableTopNavigation.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(new NeverDisplayCondition());
        when(badTopNav.getModule()).thenReturn(topNavModule);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(topNavModule, never()).getHtml(request);
    }

    @Test
    public void testRenderHeaderWithTopNavNullCondition() throws IOException
    {
        // If we're not careful, a null Condition returned from the TopNavigationModuleDescriptor may cause a NPE.
        // This has bitten us in the past.
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(null);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(badTopNav, atLeastOnce()).getCondition();
    }

    @Test
    public void testRenderHeaderWithTopNavNullModule() throws IOException
    {
        // This is now getting into paranoid territory
        // Defensive against getModule() returning null, which I don't think should happen normally
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(new AlwaysDisplayCondition());
        when(badTopNav.getModule()).thenReturn(null);
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(badTopNav, atLeastOnce()).getModule();
    }

    @Test
    public void testRenderHeaderWithConditionShouldDisplayThrowingExceptions() throws IOException
    {
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Condition badCondition = mock(Condition.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(badCondition);
        when(badCondition.shouldDisplay(anyMap())).thenThrow(new RuntimeException());
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(badCondition, atLeastOnce()).shouldDisplay(anyMap());
    }

    @Test
    public void testRenderHeaderWithPluginParseExceptionThrown() throws IOException
    {
        // TopNavigationModuleDescriptor.getModule may throw a RuntimeException.
        // There would be atlassian-util-concurrent exceptions wrapping the PluginParseException in practice,
        // but these are a pain to work out the chain of (InitializationExceptions and ExecutionExceptions) for little benefit
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(new AlwaysDisplayCondition());
        when(badTopNav.getModule()).thenThrow(new PluginParseException());
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(badTopNav, atLeastOnce()).getModule();
    }

    @Test
    public void testRenderHeaderWithGetHtmlThrowingExceptions() throws IOException
    {
        // TopNavigationModuleDescriptor.getModule may throw a RuntimeException.
        // There would be atlassian-util-concurrent exceptions wrapping the PluginParseException in practice,
        // but these are a pain to work out the chain of (InitializationExceptions and ExecutionExceptions) for little benefit
        JspWriter writer = mock(JspWriter.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PluggableTopNavigation topNavModule = mock(PluggableTopNavigation.class);

        TopNavigationModuleDescriptor badTopNav = mock(TopNavigationModuleDescriptor.class);
        when(badTopNav.getCondition()).thenReturn(new AlwaysDisplayCondition());
        when(badTopNav.getModule()).thenReturn(topNavModule);
        when(topNavModule.getHtml(request)).thenThrow(new RuntimeException());

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class)).thenReturn(Lists.newArrayList(badTopNav));

        headerFooterRendering.includeTopNavigation(writer, request, "bogus.section", Collections.<String, Object>emptyMap());

        verify(topNavModule, atLeastOnce()).getHtml(request);
    }
}
