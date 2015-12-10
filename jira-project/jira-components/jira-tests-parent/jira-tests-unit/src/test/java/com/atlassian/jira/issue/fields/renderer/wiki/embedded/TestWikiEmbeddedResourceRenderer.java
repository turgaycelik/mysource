package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.fields.renderer.wiki.JiraIconManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2LinkRenderer;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.EmbeddedImageRendererComponent;
import com.atlassian.renderer.v2.components.EmbeddedObjectRendererComponent;
import com.atlassian.renderer.v2.components.EmbeddedUnembeddableRendererComponent;
import com.atlassian.renderer.v2.components.TokenRendererComponent;
import com.google.common.collect.ImmutableMap;

/**
 * Tests the renderer that handles embedding attached images within the markup.
 */
public class TestWikiEmbeddedResourceRenderer
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PluginAccessor pluginAccessor;
    
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;
    
    @Mock
    @AvailableInContainer
    private AttachmentManager attachmentManager;
    
    @Mock
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
    
    private RenderContext renderContext;
    private V2RendererFacade renderer;

    private Issue issue;
    private Attachment attachment;

    @Before
    public void onTestSetUp() throws Exception
    {
        new MockComponentWorker().addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(AttachmentManager.class, attachmentManager)
                .init();

        ThumbnailManager thumbnailManager = mock(ThumbnailManager.class);

        attachment = newAttachment(1L, "testfile.jpg");
        String attachmentFileName = attachment.getFilename();
        Long attachemntId = attachment.getId();
        when(attachmentManager.getAttachment(attachment.getId())).thenReturn(attachment);

        issue = newIssue(attachment);
        renderContext = newRenderContext(issue);

        Thumbnail thumbnail = mock(Thumbnail.class);
        when(thumbnail.getAttachmentId()).thenReturn(attachemntId);
        when(thumbnail.getFilename()).thenReturn(attachmentFileName);

        ThumbnailedImage thumbnailedImage = mock(ThumbnailedImage.class);
        when(thumbnailedImage.getAttachmentId()).thenReturn(attachemntId);
        when(thumbnailedImage.getFilename()).thenReturn(attachmentFileName);
        String thumbnailURL = getThumbnailURL(attachment);
        when(thumbnailedImage.getImageURL()).thenReturn(thumbnailURL);

        when(thumbnailManager.isThumbnailable(attachment)).thenReturn(true);
        when(thumbnailManager.toThumbnailedImage(thumbnail)).thenReturn(thumbnailedImage);
        when(thumbnailManager.getThumbnail(attachment)).thenReturn(thumbnail);

        renderer = newRenderer(pluginAccessor, attachmentManager, thumbnailManager, jiraAuthenticationContext);
    }

    private V2RendererFacade newRenderer(PluginAccessor pluginAccessor, AttachmentManager attachmentManager,
            ThumbnailManager thumbnailManager, JiraAuthenticationContext authenticationContext)
    {
        RendererConfiguration rendererConfiguration = mock(RendererConfiguration.class);

        EmbeddedResourceRenderer embeddedRenderer = new JiraEmbeddedResourceRenderer(new RendererAttachmentManager(attachmentManager,
                thumbnailManager, authenticationContext));

        V2SubRenderer v2SubRenderer = new V2SubRenderer();
        Renderer renderer = new V2Renderer(Arrays.asList(new EmbeddedImageRendererComponent(), new EmbeddedObjectRendererComponent(),
                new EmbeddedUnembeddableRendererComponent(), new TokenRendererComponent(v2SubRenderer)));
        v2SubRenderer.setRenderer(renderer);
        V2LinkRenderer linkRenderer = new V2LinkRenderer(v2SubRenderer, new JiraIconManager(), rendererConfiguration);
        return new V2RendererFacade(rendererConfiguration, linkRenderer, embeddedRenderer, renderer);
    }

    private Attachment newAttachment(long id, String filename)
    {
        Attachment result = mock(Attachment.class);
        when(result.getId()).thenReturn(id);
        when(result.getFilename()).thenReturn(filename);
        return result;
    }

    private Issue newIssue(Attachment attachment)
    {
        GenericValue issueOpen = UtilsForTests.getTestEntity("Issue",
                ImmutableMap.of("id", new Long(1), "key", "TST-1", "summary", "summary", "security", new Long(1)));

        Issue result = mock(Issue.class);
        when(result.getKey()).thenReturn("TST-1");
        when(result.getAttachments()).thenReturn(Arrays.asList(attachment));
        when(result.getGenericValue()).thenReturn(issueOpen);
        return result;
    }

    private RenderContext newRenderContext(Issue issue)
    {
        RenderContext result = new RenderContext();
        result.setSiteRoot("http://localhost:8080");
        result.setCharacterEncoding("UTF-8");
        result.addParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY, issue);
        return result;
    }

    @Test
    public void testEmbeddedImageAttachment()
    {
        assertEquals(getExpectedEmbeddedImageLink(renderContext, attachment),
                renderer.convertWikiToXHtml(renderContext, "!" + attachment.getFilename() + "!"));
    }

    @Test
    public void testEmbeddedImageExternal()
    {
        String externalLink = "http://www.google.com.au/intl/en_au/images/logo.gif";
        assertEquals(getExpectedEmbeddedExternalImageLink(externalLink),
                renderer.convertWikiToXHtml(renderContext, "!" + externalLink + "!"));
    }

    @Test
    public void testExternalLinkSuccessWithNoIssue()
    {
        String externalLink = "http://www.google.com.au/intl/en_au/images/logo.gif";
        assertEquals(getExpectedEmbeddedExternalImageLink(externalLink),
                renderer.convertWikiToXHtml(renderContext, "!" + externalLink + "!"));
    }

    @Test
    public void testInternalLinkFailureWithNoIssue()
    {
        assertEquals(getExpectedEmbeddedLinkFailure(attachment),
                renderer.convertWikiToXHtml(new RenderContext(), "!" + attachment.getFilename() + "!"));
    }

    @Test
    public void testInternalLinkFailureWithNoAttachment()
    {
        String filename = "unexisting.jpg";
        assertEquals(getExpectedEmbeddedLinkFailureNoFile(filename),
                renderer.convertWikiToXHtml(renderContext, "!" + filename + "!"));
    }

    @Test
    public void testEmbeddedImageThumbnailAttachment()
    {
        assertEquals(getExpectedImageThumbnailLink(renderContext, attachment),
                renderer.convertWikiToXHtml(renderContext, "!" + attachment.getFilename() + "|thumbnail!"));
    }

    private String getExpectedEmbeddedImageLink(RenderContext renderContext, Attachment attachment)
    {
        return new StringBuilder().append("<img src=\"")
                .append(renderContext.getSiteRoot())
                .append("/secure/attachment/")
                .append(attachment.getId())
                .append('/')
                .append(attachment.getId())
                .append("_")
                .append(attachment.getFilename())
                .append("\" align=\"absmiddle\" border=\"0\" />")
                .toString();
    }

    private String getExpectedEmbeddedExternalImageLink(String externalLink)
    {
        return new StringBuilder().append("<img src=\"")
                .append("http://www.google.com.au/intl/en_au/images/logo.gif")
                .append("\" align=\"absmiddle\" border=\"0\" />")
                .toString();
    }

    private String getExpectedEmbeddedLinkFailure(Attachment attachment)
    {
        return new StringBuilder().append("<span class=\"error\">No usable issue stored in the context, unable to resolve filename &#39;")
                .append(attachment.getFilename())
                .append("&#39;</span>")
                .toString();
    }

    private String getExpectedEmbeddedLinkFailureNoFile(String filename)
    {
        return new StringBuilder().append("<span class=\"error\">Unable to render embedded object: File (")
                .append(filename)
                .append(") not found.</span>")
                .toString();
    }

    private String getExpectedImageThumbnailLink(RenderContext renderContext, Attachment attachment)
    {
        return new StringBuilder().append("<a id=\"1_thumb\" href=\"")
                .append(renderContext.getSiteRoot())
                .append("/secure/attachment/")
                .append(attachment.getId())
                .append('/')
                .append(attachment.getId())
                .append('_')
                .append(attachment.getFilename())
                .append("\" title=\"")
                .append(attachment.getFilename())
                .append("\">")
                .append("<img src=\"")
                .append("http://localhost:8080/secure/thumbnail/")
                .append(attachment.getId())
                .append('/')
                .append(attachment.getId())
                .append('_')
                .append(attachment.getFilename())
                .append("\" align=\"absmiddle\" border=\"0\" />")
                .append("</a>")
                .toString();
    }

    private String getThumbnailURL(Attachment attachment)
    {
        return new StringBuilder().append("http://localhost:8080/secure/thumbnail/")
                .append(attachment.getId())
                .append('/')
                .append(attachment.getId())
                .append('_')
                .append(attachment.getFilename())
                .toString();
    }

}
