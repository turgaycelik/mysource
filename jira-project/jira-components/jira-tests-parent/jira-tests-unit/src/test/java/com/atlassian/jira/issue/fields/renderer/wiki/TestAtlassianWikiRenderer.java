package com.atlassian.jira.issue.fields.renderer.wiki;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.renderer.RenderContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestAtlassianWikiRenderer
{
    @Mock
    private ApplicationProperties applicationProperties;

    private AtlassianWikiRendererExposingRequest wikiRenderer;

    @Before
    public void setUp()
    {
        when(applicationProperties.getDefaultBackedString(anyString())).thenReturn("");

        wikiRenderer = new AtlassianWikiRendererExposingRequest(
                mock(EventPublisher.class),
                applicationProperties,
                new DefaultVelocityRequestContextFactory(new MockApplicationProperties())
        );
    }

    @Test
    public void getRenderContextWithNullIssueRenderContextReturnsContextWithNoIssue()
    {
        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertNull("Should not have an issue context set", renderContext.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY));
    }

    @Test
    public void getRenderContextWithIssueReturnsAContextWithTheIssue()
    {
        MockIssue issue = new MockIssue();

        RenderContext renderContext = wikiRenderer.getRenderContext(new IssueRenderContext(issue));
        assertEquals("Should have found the provided issue in the context", issue, renderContext.getParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY));
    }

    @Test
    public void getRenderContextWhenRenderingInlineDisallowsRenderingParagraphs()
    {
        IssueRenderContext context = new IssueRenderContext(null);
        context.addParam(IssueRenderContext.INLINE_PARAM, true);

        RenderContext renderContext = wikiRenderer.getRenderContext(context);

        assertFalse("Should not render paragraphs inside an inline", renderContext.getRenderMode().renderParagraphs());
    }

    @Test
    public void getRenderContextWhenRenderingInlineInvalidParamAllowsRenderingParagraphs()
    {
        IssueRenderContext context = new IssueRenderContext(null);
        context.addParam(IssueRenderContext.INLINE_PARAM, "true");  // not valid; must be a boolean literal, not a string

        RenderContext renderContext = wikiRenderer.getRenderContext(context);

        assertTrue("Should render paragraphs unless the inline setting is valid", renderContext.getRenderMode().renderParagraphs());
    }

    @Test
    public void getRenderContextWhenRenderingNonInlineAllowsRenderingParagraphs()
    {
        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        boolean canRenderParagraphs = renderContext.getRenderMode().renderParagraphs();
        assertTrue("Should render paragraphs by default", canRenderParagraphs);
    }

    @Test
    public void getRenderContextWhenMimeSniffingIsInsecureDisallowsMacrosErrorMessages()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn(APKeys.MIME_SNIFFING_OWNED);

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanNotRenderMacroErrorMessages(renderContext);
        assertCanRenderImages(renderContext);
        assertCanRenderEmbeddedObjects(renderContext);
    }
    
    @Test
    public void getRenderContextWhenMimeSniffingIsSecureDisallowsMacrosErrorMessagesImagesAndObjectEmbedding()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn(APKeys.MIME_SNIFFING_PARANOID);

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanNotRenderMacroErrorMessages(renderContext);
        assertCanNotRenderImages(renderContext);
        assertCanNotRenderEmbeddedObjects(renderContext);
    }

    @Test
    public void getRenderContextWhenMimeSniffingIsNotSetAllowsMacrosErrorMessagesImagesAndObjectEmbedding()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn("");

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanRenderMacroErrorMessages(renderContext);
        assertCanRenderImages(renderContext);
        assertCanRenderEmbeddedObjects(renderContext);
    }

    @Test
    public void getRenderContextWhenMimeSniffingIsWorkaroundDisallowsMacrosErrorMessagesAndObjectEmbeddingIfBrowserIsInternetExplorerSmallerThanVersion8()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn(APKeys.MIME_SNIFFING_WORKAROUND);
        HttpServletRequest request = mockRequestComingFromIe7();
        wikiRenderer.setRequest(request);

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanNotRenderMacroErrorMessages(renderContext);
        assertCanRenderImages(renderContext);
        assertCanNotRenderEmbeddedObjects(renderContext);
    }

    @Test
    public void getRenderContextWhenMimeSniffingIsWorkaroundDisallowsMacrosErrorMessagesIfBrowserIsAnythingDifferentThanInternetExplorerSmallerThanVersion8()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn(APKeys.MIME_SNIFFING_WORKAROUND);
        HttpServletRequest request = mockRequestComingFromChrome();
        wikiRenderer.setRequest(request);

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanNotRenderMacroErrorMessages(renderContext);
        assertCanRenderImages(renderContext);
        assertCanRenderEmbeddedObjects(renderContext);
    }

    @Test
    public void getRenderContextWhenMimeSniffingIsWorkaroundDisallowsMacrosErrorMessagesAndObjectEmbeddingIfThereIsNoInformationAboutTheRequest()
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING)).thenReturn(APKeys.MIME_SNIFFING_WORKAROUND);
        wikiRenderer.setRequest(null);

        RenderContext renderContext = wikiRenderer.getRenderContext(null);

        assertCanNotRenderMacroErrorMessages(renderContext);
        assertCanRenderImages(renderContext);
        assertCanNotRenderEmbeddedObjects(renderContext);
    }

    private HttpServletRequest mockRequestComingFromChrome()
    {
        return mockRequestWithAgentHeader("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36");
    }

    private HttpServletRequest mockRequestComingFromIe7()
    {
        return mockRequestWithAgentHeader("Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)");
    }

    private HttpServletRequest mockRequestWithAgentHeader(final String agentHeader)
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(BrowserUtils.USER_AGENT_HEADER)).thenReturn(agentHeader);
        return request;
    }

    private void assertCanRenderMacroErrorMessages(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderMacroErrorMessages = renderContext.getRenderMode().renderMacroErrorMessages();
        assertTrue(canRenderMacroErrorMessages);
    }

    private void assertCanNotRenderMacroErrorMessages(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderMacroErrorMessages = renderContext.getRenderMode().renderMacroErrorMessages();
        assertFalse(canRenderMacroErrorMessages);
    }

    private void assertCanRenderImages(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderImages = renderContext.getRenderMode().renderImages();
        assertTrue(canRenderImages);
    }

    private void assertCanNotRenderImages(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderImages = renderContext.getRenderMode().renderImages();
        assertFalse(canRenderImages);
    }

    private void assertCanRenderEmbeddedObjects(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderEmbeddedObjects = renderContext.getRenderMode().renderEmbeddedObjects();
        assertTrue(canRenderEmbeddedObjects);
    }

    private void assertCanNotRenderEmbeddedObjects(@Nonnull final RenderContext renderContext)
    {
        boolean canRenderEmbeddedObjects = renderContext.getRenderMode().renderEmbeddedObjects();
        assertFalse(canRenderEmbeddedObjects);
    }

    private static class AtlassianWikiRendererExposingRequest extends AtlassianWikiRenderer
    {
        private HttpServletRequest request;

        public AtlassianWikiRendererExposingRequest(final EventPublisher eventPublisher, final ApplicationProperties applicationProperties, final VelocityRequestContextFactory velocityRequestContextFactory)
        {
            super(eventPublisher, applicationProperties, velocityRequestContextFactory);
        }

        public void setRequest(final HttpServletRequest request)
        {
            this.request = request;
        }

        @Override
        protected HttpServletRequest getCurrentRequest()
        {
            return request;
        }
    }
}
