package com.atlassian.jira.issue.fields.renderer.wiki.links;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.renderer.wiki.JiraIconManager;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererConfiguration;
import com.atlassian.renderer.v2.Renderer;
import com.atlassian.renderer.v2.V2LinkRenderer;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.components.LinkRendererComponent;
import com.atlassian.renderer.v2.components.TokenRendererComponent;

/**
 * Tests the creation of a link to a jira issue. This also test the portion of the JiraLinkResolver that delegates to JiraIssueLinks. This
 * also tests the JiraIssueLinkResolver which identifies the issue keys which are turned into links.
 */
public class TestWikiIssueLink
{
    
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;
    
    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private V2RendererFacade renderer;

    @Before
    public void onTestSetUp()
    {
        renderer = newRenderer();
    }

    private V2RendererFacade newRenderer()
    {
        RendererConfiguration rendererConfiguration = mock(RendererConfiguration.class);
        V2SubRenderer v2SubRenderer = new V2SubRenderer();
        Renderer renderer = new V2Renderer(Arrays.asList(
                new LinkRendererComponent(new JiraLinkResolver(mock(PluginAccessor.class), mock(EventPublisher.class))), 
                new JiraIssueLinkRendererComponent(), 
                new TokenRendererComponent(v2SubRenderer))
                );
        v2SubRenderer.setRenderer(renderer);
        V2LinkRenderer linkRenderer = new V2LinkRenderer(v2SubRenderer, new JiraIconManager(), rendererConfiguration);
        return new V2RendererFacade(rendererConfiguration, linkRenderer, null, renderer);
    }

    @Test
    public void testOpenIssueLink()
    {
        MutableIssue issue = newIssue("TST-1", "Open issue", false);
        when(issueManager.getIssueObject(issue.getKey())).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).thenReturn(Boolean.TRUE);
        assertEquals(getExpectedIssueLink(issue), renderer.convertWikiToXHtml(new RenderContext(), issue.getKey()));
    }

    @Test
    public void testResolvedIssueLink()
    {
        MutableIssue issue = newIssue("TST-2", "Resolved issue", true);
        when(issueManager.getIssueObject(issue.getKey())).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).thenReturn(Boolean.TRUE);
        assertEquals(getExpectedIssueLink(issue), renderer.convertWikiToXHtml(new RenderContext(), issue.getKey()));
    }

    @Test
    public void testOpenIssueLinkNoPerm()
    {
        MutableIssue issue = newIssue("TST-1", "Open issue", false);
        when(issueManager.getIssueObject(issue.getKey())).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).thenReturn(Boolean.FALSE);
        // JRA-14893: do not hyperlink an inaccessible issue
        assertEquals(issue.getKey(), renderer.convertWikiToXHtml(new RenderContext(), issue.getKey()));

    }

    @Test
    public void testResolvedIssueLinkNoPerm()
    {
        MutableIssue issue = newIssue("TST-1", "Resolved issue", false);
        when(issueManager.getIssueObject(issue.getKey())).thenReturn(issue);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, (ApplicationUser) null)).thenReturn(Boolean.FALSE);
        // JRA-14893: do not hyperlink an inaccessible issue
        assertEquals(issue.getKey(), renderer.convertWikiToXHtml(new RenderContext(), issue.getKey()));
    }

    @Test
    public void testMultipleIssueLinks()
    {
        MutableIssue issue1 = newIssue("TST-1", "Open issue", false);
        MutableIssue issue2 = newIssue("TST-2", "Closed issue", true);

        when(issueManager.getIssueObject(issue1.getKey())).thenReturn(issue1);
        when(issueManager.getIssueObject(issue2.getKey())).thenReturn(issue2);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue1, (ApplicationUser) null)).thenReturn(Boolean.TRUE);
        when(permissionManager.hasPermission(Permissions.BROWSE, issue2, (ApplicationUser) null)).thenReturn(Boolean.TRUE);

        String expectedRendering = getExpectedIssueLink(issue1) + " " + getExpectedIssueLink(issue2);
        String actualRendering = renderer.convertWikiToXHtml(new RenderContext(), issue1.getKey() + " " + issue2.getKey());
        assertEquals(expectedRendering, actualRendering);
    }

    @Test
    public void testNonExistentIssueLink()
    {
        String unexistingIssueKey = "TST-3";
        assertEquals(unexistingIssueKey, renderer.convertWikiToXHtml(new RenderContext(), unexistingIssueKey));
    }

    @Test
    public void testNonExistentIssueLinkInBrackets()
    {
        assertEquals("<span class=\"error\">&#91;TST-3&#93;</span>", renderer.convertWikiToXHtml(new RenderContext(), "[TST-3]"));
    }

    private MutableIssue newIssue(String key, String summary, boolean resolved)
    {
        MutableIssue result = mock(MutableIssue.class);
        when(result.getKey()).thenReturn(key);
        when(result.getSummary()).thenReturn(summary);
        if (resolved)
        {
            when(result.getResolutionObject()).thenReturn(mock(Resolution.class));
        }
        return result;
    }

    private String getExpectedIssueLink(Issue issue)
    {
        StringBuilder result = new StringBuilder().append("<a href=\"")
                .append(ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL))
                .append("/browse/")
                .append(issue.getKey())
                .append("\" title=\"")
                .append(issue.getSummary())
                .append("\" class=\"issue-link\" data-issue-key=\"")
                .append(issue.getKey())
                .append("\">");
        if (issue.getResolutionObject() != null)
        {
            result.append('-');
        }
        result.append(issue.getKey());
        if (issue.getResolutionObject() != null)
        {
            result.append('-');
        }
        result.append("</a>");
        return result.toString();
    }
}
