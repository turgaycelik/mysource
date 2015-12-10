package com.atlassian.sal.jira.search;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.search.SearchMatch;
import com.atlassian.sal.core.search.query.DefaultSearchQueryParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.easymock.classextension.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
@SuppressWarnings({"UnusedDeclaration", "JUnitTestMethodWithNoAssertions"})
@RunWith(MockitoJUnitRunner.class)
public class TestJiraSearchProvider
{
    private JiraSearchProvider searchProvider;
    @Mock
    private SearchProvider mockSearchProvider;
    @Mock
    private IssueManager mockIssueManager;
    @Mock
    private JiraAuthenticationContext mockAuthenticationContext;
    @Mock
    private ApplicationProperties mockApplicationProperties;
    @Mock
    private UserManager userManager;

    private Collection<String> keys;

    @Before
    public void setUp() throws Exception
    {
        when(mockApplicationProperties.getBaseUrl(UrlMode.CANONICAL)).thenReturn("http://jira.atlassian.com");
        when(mockApplicationProperties.getBaseUrl(UrlMode.AUTO)).thenReturn("http://jira.atlassian.com");
        when(mockApplicationProperties.getDisplayName()).thenReturn("JIRA");

        keys = new ArrayList<String>();

        searchProvider = new JiraSearchProvider(mockSearchProvider, mockIssueManager,
            mockAuthenticationContext, new DefaultSearchQueryParser(), mockApplicationProperties, null, userManager)
        {
            @Override
            Collection<String> getIssueKeysFromQuery(String query)
            {
                return keys;
            }

            @Override
            SearchContext getSearchContext()
            {
                return null;
            }
        };

        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Test
    public void searchWithNoResults() throws SearchException
    {
        when(mockSearchProvider.search(any(Query.class), (ApplicationUser) isNull(), any(PagerFilter.class))).thenAnswer(
            new Answer()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    Query query = (Query) invocationOnMock.getArguments()[0];
                    assertEquals("{summary ~ \"query\"} OR {description ~ \"query\"} OR {comment ~ \"query\"}", query.toString());
                    return new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter());
                }
            });

        final com.atlassian.sal.api.search.SearchResults results = searchProvider.search((String) null, "query");
        assertNotNull(results);
        assertEquals(0, results.getErrors().size());
        assertEquals(0, results.getMatches().size());
    }

    @Test
    public void allWordsInSearch() throws SearchException
    {
        when(mockSearchProvider.search(any(Query.class), (ApplicationUser) isNull(), any(PagerFilter.class))).thenAnswer(
            new Answer()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    Query query = (Query) invocationOnMock.getArguments()[0];
                    assertEquals(
                        "( {summary ~ \"search\"} OR {description ~ \"search\"} OR {comment ~ \"search\"} ) AND ( {summary ~ \"terms\"} OR {description ~ \"terms\"} OR {comment ~ \"terms\"} )",
                        query.toString());
                    return new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter());
                }
            });

        searchProvider.search((String) null, "search terms");
    }

    @Test
    public void searchWithProject() throws SearchException
    {
        when(mockSearchProvider.search(any(Query.class), (ApplicationUser) isNull(), any(PagerFilter.class))).thenAnswer(
            new Answer()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    Query query = (Query) invocationOnMock.getArguments()[0];
                    assertEquals("{project = \"KEY\"} AND ( {summary ~ \"query\"} OR {description ~ \"query\"} OR {comment ~ \"query\"} )",
                        query.toString());
                    return new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter());
                }
            });

        searchProvider.search((String) null, "query&project=KEY");
    }

    @Test
    public void searchWithResults() throws SearchException
    {
        IssueType mockIssueType = mock(IssueType.class);
        when(mockIssueType.getId()).thenReturn("1");

        Issue mockIssue = mock(Issue.class);
        when(mockIssue.getKey()).thenReturn("JST-234");
        when(mockIssue.getSummary()).thenReturn("Sample Summary");
        when(mockIssue.getDescription()).thenReturn("Sample description for the query issue.");
        when(mockIssue.getIssueTypeObject()).thenReturn(mockIssueType);

        List<Issue> issues = new ArrayList<Issue>();
        issues.add(mockIssue);

        when(mockSearchProvider.search(any(Query.class), (ApplicationUser) isNull(), any(PagerFilter.class))).thenReturn(
            new SearchResults(issues, 1, PagerFilter.getUnlimitedFilter()));

        final com.atlassian.sal.api.search.SearchResults results = searchProvider.search((String) null, "query");
        assertNotNull(results);
        assertEquals(0, results.getErrors().size());
        assertEquals(1, results.getMatches().size());
        //assert stuff about the one match
        final SearchMatch searchMatch = results.getMatches().get(0);
        assertEquals("http://jira.atlassian.com/browse/JST-234", searchMatch.getUrl());
        assertEquals("[JST-234] Sample Summary", searchMatch.getTitle());
        assertEquals("Sample description for the query issue.", searchMatch.getExcerpt());
        assertEquals("1", searchMatch.getResourceType().getType());
        assertEquals("JIRA", searchMatch.getResourceType().getName());
        assertEquals("http://jira.atlassian.com", searchMatch.getResourceType().getUrl());
    }

    @Test
    public void issueKeyInSearch() throws SearchException
    {
        when(mockSearchProvider.search(any(Query.class), (ApplicationUser) isNull(), any(PagerFilter.class))).thenReturn(
            new SearchResults(Collections.<Issue>emptyList(), 0, PagerFilter.getUnlimitedFilter()));

        keys.add("JST-234");

        IssueType mockIssueType = mock(IssueType.class);
        when(mockIssueType.getId()).thenReturn("1");

        MutableIssue mockIssue = mock(MutableIssue.class);
        when(mockIssue.getKey()).thenReturn("JST-234");
        when(mockIssue.getSummary()).thenReturn("Sample Summary");
        when(mockIssue.getDescription()).thenReturn("Sample description for the query issue.");
        when(mockIssue.getIssueTypeObject()).thenReturn(mockIssueType);

        when(mockIssueManager.getIssueObject("JST-234")).thenReturn(mockIssue);

        com.atlassian.sal.api.search.SearchResults results = searchProvider.search((String) null, "JST-234");

        assertNotNull(results);
        assertEquals(0, results.getErrors().size());
        assertEquals(1, results.getMatches().size());
        //assert stuff abou the one match
        final SearchMatch searchMatch = results.getMatches().get(0);
        assertEquals("http://jira.atlassian.com/browse/JST-234", searchMatch.getUrl());
    }

    @SuppressWarnings("unchecked")
    private FieldValuesHolder createFieldValuesHolder(String query)
    {
        FieldValuesHolder holder = new FieldValuesHolderImpl();
        holder.put("text", query);
        return holder;
    }
}
