package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.plugin.issuelink.IssueLinkRenderer;
import com.atlassian.jira.plugin.issuelink.IssueLinkRendererModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since v6.2
 */
public class RemoteIssueLinkUtilsTest
{
    @Rule
    public final InitMockitoMocks initMocks = new InitMockitoMocks(this);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    PluginAccessor pluginAccessor;

    @Mock
    IssueLinkRendererModuleDescriptor defaultRenderer;
    @Mock
    IssueLinkRendererModuleDescriptor goodDescriptorRenderer;
    @Mock
    IssueLinkRendererModuleDescriptor badDescriptorRenderer;

    @Mock
    IssueLinkRenderer renderer;

    final RemoteIssueLink link = new RemoteIssueLink(1L, 1L, "", "", "", "", "", "", "", false, "", "", "", "application", "Application" );

    @Before
    public void setUp() throws Exception
    {
        Mockito.when(defaultRenderer.isDefaultHandler()).thenReturn(true);
        Mockito.when(badDescriptorRenderer.isDefaultHandler()).thenThrow(new RuntimeException("Misbehaving plugin exception"));

        Mockito.when(goodDescriptorRenderer.handlesApplicationType("application")).thenReturn(true);
        Mockito.when(badDescriptorRenderer.handlesApplicationType(Mockito.anyString())).thenThrow(new RuntimeException("Misbehaving plugin exception"));

        Mockito.when(pluginAccessor.getEnabledModuleDescriptorsByClass(IssueLinkRendererModuleDescriptor.class)).thenReturn(
                ImmutableList.of(badDescriptorRenderer, goodDescriptorRenderer, defaultRenderer));

        Mockito.when(goodDescriptorRenderer.getModule()).thenReturn(renderer);
    }

    @Test
    public void testGetIssueLinkRendererModuleDescriptorSurvivesPluginThrowingExceptions() throws Exception
    {
        Assert.assertSame(defaultRenderer, RemoteIssueLinkUtils.getIssueLinkRendererModuleDescriptor(pluginAccessor, null));
        Assert.assertSame(goodDescriptorRenderer, RemoteIssueLinkUtils.getIssueLinkRendererModuleDescriptor(pluginAccessor, "application"));
    }

    @Test
    public void testConvertToIssueLinkContexts() throws Exception
    {
        Mockito.when(renderer.shouldDisplay(Mockito.<RemoteIssueLink>any())).thenReturn(true);
        Mockito.when(renderer.requiresAsyncLoading(link)).thenReturn(true);
        Mockito.when(goodDescriptorRenderer.getInitialHtml(link)).thenReturn("valid html");

        final Map<String,List<IssueLinkContext>> contextMap = RemoteIssueLinkUtils.convertToIssueLinkContexts(ImmutableList.of(link), 1L, "", new MockI18nHelper(), pluginAccessor);

        Assert.assertEquals(1, contextMap.size());
        final List<IssueLinkContext> contexts = contextMap.values().iterator().next();
        final IssueLinkContext context = Iterables.getOnlyElement(contexts);
        Assert.assertTrue(context.isRequiresAsyncLoading());
        Assert.assertEquals("valid html", context.getHtml());
    }

    @Test
    public void testConvertToIssueLinkContextsSurvivesPluginThrowingExceptionFromGetInitialHtml() throws Exception
    {
        Mockito.when(renderer.shouldDisplay(Mockito.<RemoteIssueLink>any())).thenReturn(true);
        Mockito.when(goodDescriptorRenderer.getInitialHtml(Mockito.<RemoteIssueLink>any())).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        final Map<String,List<IssueLinkContext>> contexts = RemoteIssueLinkUtils.convertToIssueLinkContexts(ImmutableList.of(link), 1L, "", new MockI18nHelper(), pluginAccessor);
        Assert.assertEquals(Collections.<String,List<IssueLinkContext>>emptyMap(), contexts);
    }

    @Test
    public void testConvertToIssueLinkContextsSurvivesPluginThrowingExceptionFromRequiresAsyncLoading() throws Exception
    {
        Mockito.when(renderer.shouldDisplay(Mockito.<RemoteIssueLink>any())).thenReturn(true);
        Mockito.when(renderer.requiresAsyncLoading(Mockito.<RemoteIssueLink>any())).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        final Map<String,List<IssueLinkContext>> contexts = RemoteIssueLinkUtils.convertToIssueLinkContexts(ImmutableList.of(link), 1L, "", new MockI18nHelper(), pluginAccessor);
        Assert.assertEquals(Collections.<String,List<IssueLinkContext>>emptyMap(), contexts);
    }

    @Test
    public void testConvertToIssueLinkContextsSurvivesPluginThrowingExceptionFromShouldDisplay() throws Exception
    {
        Mockito.when(renderer.shouldDisplay(Mockito.<RemoteIssueLink>any())).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        final Map<String,List<IssueLinkContext>> contexts = RemoteIssueLinkUtils.convertToIssueLinkContexts(ImmutableList.of(link), 1L, "", new MockI18nHelper(), pluginAccessor);
        Assert.assertEquals(Collections.<String,List<IssueLinkContext>>emptyMap(), contexts);
    }

    @Test
    public void testConvertToIssueLinkContextsSurvivesPluginThrowingExceptionFromGetModule() throws Exception
    {
        Mockito.when(goodDescriptorRenderer.getModule()).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        final Map<String,List<IssueLinkContext>> contexts = RemoteIssueLinkUtils.convertToIssueLinkContexts(ImmutableList.of(link), 1L, "", new MockI18nHelper(), pluginAccessor);
        Assert.assertEquals(Collections.<String,List<IssueLinkContext>>emptyMap(), contexts);
    }


    @Test
    public void testGetHtmlSurvivesPluginThrowingExceptionFromGetFinalHtml() throws Exception
    {
        Mockito.when(goodDescriptorRenderer.getFinalHtml(link)).thenReturn("final html");
        Assert.assertEquals("final html", RemoteIssueLinkUtils.getFinalHtml(link, pluginAccessor));

        Mockito.when(goodDescriptorRenderer.getFinalHtml(Mockito.<RemoteIssueLink>any())).thenThrow(new LinkageError("Misbehaving plugin exception - non-fatal error"));

        expectedException.expect(Exception.class);
        RemoteIssueLinkUtils.getFinalHtml(link, pluginAccessor);

    }
}
