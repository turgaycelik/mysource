package com.atlassian.jira.issue.fields.renderer.wiki.links;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.fields.renderer.wiki.JiraIconManager;
import com.atlassian.jira.issue.fields.renderer.wiki.resolvers.AttachmentLinkResolver;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2LinkRenderer;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.LinkRendererComponent;
import com.atlassian.renderer.v2.components.TokenRendererComponent;
import com.google.common.collect.ImmutableMap;

/**
 * Tests a link generated for an attachment in jira. This also test the portion of the JiraLinkResolver that delegates to attachmentLinks.
 */
public class TestWikiAttachmentLink
{
    
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    
    @Mock
    private PluginAccessor pluginAccessor;
    
    @Mock
    @AvailableInContainer
    private AttachmentManager attachmentManager;
    
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    private RenderContext renderContextWithoutIssue;
    private RenderContext renderContextWithIssue;
    private V2RendererFacade renderer;

    private Issue issue;
    private Attachment attachment;

    @Before
    public void onTestSetUp()
    {
        ContentLinkResolverDescriptor attachmentContentLinkResolverDescriptor = newContentLinkResolverDescriptor(new AttachmentLinkResolver());
        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ContentLinkResolverDescriptor.class)).thenReturn(
                Arrays.asList(attachmentContentLinkResolverDescriptor));

        attachment = newAttachment(1L, "testfile.jpg");
        issue = newIssue(attachment);

        when(attachmentManager.getAttachment(attachment.getId())).thenReturn(attachment);

        renderContextWithoutIssue = newRenderContext();
        renderContextWithIssue = newRenderContextWithIssue(issue);
        renderer = newRenderer(pluginAccessor);
    }

    private ContentLinkResolverDescriptor newContentLinkResolverDescriptor(ContentLinkResolver contentLinkResolver)
    {
        ContentLinkResolverDescriptor result = Mockito.mock(ContentLinkResolverDescriptor.class);
        when(result.getModule()).thenReturn(contentLinkResolver);
        return result;
    }

    private V2RendererFacade newRenderer(PluginAccessor pluginAccessor)
    {
        RendererConfiguration rendererConfiguration = Mockito.mock(RendererConfiguration.class);
        V2SubRenderer v2SubRenderer = new V2SubRenderer();
        Renderer renderer = new V2Renderer(Arrays.asList(
                new LinkRendererComponent(new JiraLinkResolver(pluginAccessor, Mockito.mock(EventPublisher.class))),
                new TokenRendererComponent(v2SubRenderer)));
        v2SubRenderer.setRenderer(renderer);
        V2LinkRenderer linkRenderer = new V2LinkRenderer(v2SubRenderer, new JiraIconManager(), rendererConfiguration);
        return new V2RendererFacade(rendererConfiguration, linkRenderer, null, renderer);
    }

    private RenderContext newRenderContext()
    {
        RenderContext result = new RenderContext();
        result.setSiteRoot("http://localhost:8080");
        result.setCharacterEncoding("UTF-8");
        return result;
    }

    private RenderContext newRenderContextWithIssue(Issue issue)
    {
        RenderContext result = newRenderContext();
        result.addParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY, issue);
        return result;
    }

    private Attachment newAttachment(long id, String filename)
    {
        Attachment result = Mockito.mock(Attachment.class);
        when(result.getId()).thenReturn(id);
        when(result.getFilename()).thenReturn(filename);
        return result;
    }

    private Issue newIssue(Attachment attachment)
    {
        GenericValue issueOpen = UtilsForTests.getTestEntity("Issue",
                ImmutableMap.of("id", new Long(1), "key", "TST-1", "summary", "summary", "security", new Long(1)));

        Issue result = Mockito.mock(Issue.class);
        when(result.getKey()).thenReturn("TST-1");
        when(result.getAttachments()).thenReturn(Arrays.asList(attachment));
        when(result.getGenericValue()).thenReturn(issueOpen);
        return result;
    }

    @Test
    public void testIssueAttachmentLink()
    {
        assertEquals(getExpectedAttachmentLink(renderContextWithIssue, issue, attachment),
                renderer.convertWikiToXHtml(renderContextWithIssue, "[^" + attachment.getFilename() + "]"));
    }

    @Test
    public void testIssueAttachmentLinkWithNoAttachment()
    {
        String filename = "unexistingfile.jpg";
        assertEquals(getExpectedErrorAttachmentLink(filename),
                renderer.convertWikiToXHtml(renderContextWithIssue, "[^" + filename + "]"));
    }

    @Test
    public void testNoGenericValueError()
    {
        // Do not use the context with an issue in it, we want to generate an exception within the JiraAttachmentLink
        assertEquals(getExpectedErrorAttachmentLink(attachment.getFilename()),
                renderer.convertWikiToXHtml(renderContextWithoutIssue, "[^" + attachment.getFilename() + "]"));
    }

    private String getExpectedAttachmentLink(RenderContext renderContext, Issue issue, Attachment attachment)
    {
        StringBuilder result = new StringBuilder().append("<span class=\"nobr\"><a href=\"")
                .append(renderContext.getSiteRoot())
                .append("/secure/attachment/")
                .append(attachment.getId())
                .append('/')
                .append(attachment.getId())
                .append("_")
                .append(attachment.getFilename())
                .append("\" title=\"")
                .append(attachment.getFilename())
                .append(" attached to ")
                .append(issue.getKey())
                .append("\">")
                .append(attachment.getFilename())
                .append("<sup><img class=\"rendericon\" src=\"")
                .append(renderContext.getSiteRoot())
                .append("/images/icons/link_attachment_7.gif\" height=\"7\" width=\"7\" align=\"absmiddle\" alt=\"\" border=\"0\"/></sup></a></span>");
        return result.toString();
    }

    private String getExpectedErrorAttachmentLink(String filename)
    {
        return "<span class=\"error\">&#91;^" + filename + "&#93;</span>";
    }
}
