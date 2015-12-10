package com.atlassian.jira.issue.history;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.jira.plugin.webfragment.EqWebItem.eqWebItem;
import static com.google.common.collect.Iterables.isEmpty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(ListeningMockitoRunner.class)
public class TestIssueHistoryLinkFactory
{
    @Mock
    private UserIssueHistoryManager historyManager;
    @Mock
    private VelocityRequestContext requestContext;
    @Mock
    private VelocityRequestContextFactory requestContextFactory;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    private User user;

    @Mock
    private IssueHistoryLinkFactory linkFactory;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        linkFactory = new IssueHistoryLinkFactory(requestContextFactory, historyManager, applicationProperties, i18nFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        historyManager = null;
        requestContext = null;
        requestContextFactory = null;
        applicationProperties = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
    }

    @Test
    public void testNullUserEmptyHistory()
    {
        when(historyManager.getShortIssueHistory((ApplicationUser)null)).thenReturn(Lists.<Issue>newArrayList());
        assertTrue(isEmpty(linkFactory.getItems(Maps.<String, Object>newHashMap())));
    }

    @Test
    public void testEmptyHistory()
    {
        when(historyManager.getShortIssueHistory((ApplicationUser)null)).thenReturn(Lists.<Issue>newArrayList());
        assertTrue(isEmpty(linkFactory.getItems(MapBuilder.<String, Object>build("user", user))));
    }

    @Test
    public void testOneIssueHistorySummaryVariations()
    {
        testOneIssueSummary("Summary 1", "/images/bug.gif", "/jira",
                "TEST-1 Summary 1", "TEST-1 Summary 1", "/jira/images/bug.gif", "/jira/browse/TEST-1");
        testOneIssueSummary("12345678901234567890123", "/images/bug.gif", "/jira",
                "TEST-1 12345678901234567890123", "TEST-1 12345678901234567890123", "/jira/images/bug.gif", "/jira/browse/TEST-1");
        testOneIssueSummary("123456789012345678901234567890", "/images/bug.gif", "/jira",
                "TEST-1 12345678901234567890123...", "TEST-1 123456789012345678901234567890", "/jira/images/bug.gif", "/jira/browse/TEST-1");
    }

    @Test
    public void testOneIssueHistoryIconUrlVariations()
    {
        testOneIssueSummary("Summary 1", "https://images/bug.gif", "/jira",
                "TEST-1 Summary 1", "TEST-1 Summary 1", "https://images/bug.gif", "/jira/browse/TEST-1");
        testOneIssueSummary("Summary 1", "/images/bug.gif", "",
                "TEST-1 Summary 1", "TEST-1 Summary 1", "/images/bug.gif", "/browse/TEST-1");
    }

    private void testOneIssueSummary(final String summary, final String iconUrl, final String contextPath,
            final String expectedLabel, final String expectedTitle, final String expectedIconUrl, final String expectedUrl)
    {
        MockIssue issue = new MockIssue(1, "TEST-1");
        issue.setSummary(summary);
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl(iconUrl);
        issue.setIssueTypeObject(mockIssueType);

        when(historyManager.getShortIssueHistory(user)).thenReturn(Lists.newArrayList((Issue) issue));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn(contextPath);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS)).thenReturn("10");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));

        final WebItem link = new WebFragmentBuilder(10).
                id("issue_lnk_1").
                label(expectedLabel).
                title(expectedTitle).
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-1").
                addParam("iconUrl", expectedIconUrl).
                webItem("find_link/issues_history_main").
                url(expectedUrl).
                build();

        assertThat(items, hasItems(eqWebItem(link)));
    }

    @Test
    public void testManyIssuesHistory()
    {
        final List<Issue> issues = getTestIssues();
        when(historyManager.getShortIssueHistory(user)).thenReturn(issues);
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("/jira");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS)).thenReturn("10");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));

        final WebItem link1 = new WebFragmentBuilder(10).
                id("issue_lnk_1").
                label("TEST-1 Summary 1").
                title("TEST-1 Summary 1").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-1").
                addParam("iconUrl", "/jira/images/bug.gif").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-1").
                build();
        final WebItem link2 = new WebFragmentBuilder(20).
                id("issue_lnk_2").
                label("TEST-2 Summary 2").
                title("TEST-2 Summary 2").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-2").
                addParam("iconUrl", "/jira/images/issuetypes/task.png").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-2").
                build();
        final WebItem link3 = new WebFragmentBuilder(30).
                id("issue_lnk_3").
                label("TEST-3 Summary 3").
                title("TEST-3 Summary 3").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-3").
                addParam("iconUrl", "/jira/images/feature.gif").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-3").
                build();
        final WebItem link4 = new WebFragmentBuilder(40).
                id("issue_lnk_4").
                label("TEST-4 Summary 4").
                title("TEST-4 Summary 4").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-4").
                addParam("iconUrl", "/jira/images/improv.gif").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-4").
                build();

        assertThat(items, hasItems(eqWebItem(link1), eqWebItem(link2), eqWebItem(link3), eqWebItem(link4)));
    }

    @Test
    public void testTooManyIssuesHistory()
    {
        final List<Issue> issues = getTestIssues();
        when(historyManager.getShortIssueHistory(user)).thenReturn(issues);
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("/jira");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS)).thenReturn("3");
        when(i18nFactory.getInstance(user)).thenReturn(new MockI18nHelper());

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));

        final WebItem link1 = new WebFragmentBuilder(10).
                id("issue_lnk_1").
                label("TEST-1 Summary 1").
                title("TEST-1 Summary 1").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-1").
                addParam("iconUrl", "/jira/images/bug.gif").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-1").
                build();
        final WebItem link2 = new WebFragmentBuilder(20).
                id("issue_lnk_2").
                label("TEST-2 Summary 2").
                title("TEST-2 Summary 2").
                addParam("class", "issue-link").
                addParam("data-issue-key", "TEST-2").
                addParam("iconUrl", "/jira/images/issuetypes/task.png").
                webItem("find_link/issues_history_main").
                url("/jira/browse/TEST-2").
                build();
        final WebItem moreLink = new WebFragmentBuilder(30).
                id("issue_lnk_more").
                label("menu.issues.history.more").
                title("menu.issues.history.more.desc").
                addParam("class", "filter-link").
                addParam("data-filter-id", "-3").
                webItem("find_link/issues_history_main").
                url("/jira/issues/?filter=-3").
                build();

        assertThat(items, hasItems(eqWebItem(link1), eqWebItem(link2), eqWebItem(moreLink)));
    }

    private List<Issue> getTestIssues()
    {
        MockIssue issue1 = new MockIssue(1L);
        issue1.setKey("TEST-1");
        issue1.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue1.setIssueTypeObject(mockIssueType);

        MockIssue issue2 = new MockIssue(2L);
        issue2.setKey("TEST-2");
        issue2.setSummary("Summary 2");
        final MockIssueType mockIssueType2 = new MockIssueType("type2", "type2name");
        mockIssueType2.setIconUrl("/images/issuetypes/task.png");
        issue2.setIssueTypeObject(mockIssueType2);

        MockIssue issue3 = new MockIssue(3L);
        issue3.setKey("TEST-3");
        issue3.setSummary("Summary 3");
        final MockIssueType mockIssueType3 = new MockIssueType("type3", "type3name");
        mockIssueType3.setIconUrl("/images/feature.gif");
        issue3.setIssueTypeObject(mockIssueType3);

        MockIssue issue4 = new MockIssue(4L);
        issue4.setKey("TEST-4");
        issue4.setSummary("Summary 4");
        final MockIssueType mockIssueType4 = new MockIssueType("type4", "type4name");
        mockIssueType4.setIconUrl("/images/improv.gif");
        issue4.setIssueTypeObject(mockIssueType4);

        return Lists.<Issue>newArrayList(issue1, issue2, issue3, issue4);
    }
}
