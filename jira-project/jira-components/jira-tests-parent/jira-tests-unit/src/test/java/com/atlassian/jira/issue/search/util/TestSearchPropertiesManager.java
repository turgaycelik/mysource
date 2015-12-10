package com.atlassian.jira.issue.search.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.mock.propertyset.MockPropertySet;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SearchPropertiesManager}.
 *
 * @since v5.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestSearchPropertiesManager
{
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Rule public MockComponentContainer mockComponentContainer =
            new MockComponentContainer(this);
    private PropertySet propertySet = new MockPropertySet();
    @Mock private SearchRequestService searchRequestService;
    @Mock private SearchService searchService;
    private SearchPropertiesManager searchPropertiesManager;
    @Mock private UserPropertyManager userPropertyManager;

    @Before
    public void setUp()
    {
        // Application properties and defaults for filter ID / JQL preferences.
        ApplicationProperties applicationProperties = new MockApplicationProperties();
        mockComponentContainer.addMock(ApplicationProperties.class,
                applicationProperties);

        User user = new MockUser("fred");
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);

        Query query = new QueryImpl();
        MessageSet messageSet = new MessageSetImpl();
        when(searchService.getJqlString(any(Query.class))).thenReturn("");
        when(searchService.parseQuery(any(User.class), anyString())).thenReturn(
                new SearchService.ParseResult(query, messageSet));

        when(userPropertyManager.getPropertySet(any(User.class))).thenReturn(propertySet);
        mockComponentContainer.addMock(UserPropertyManager.class,
                userPropertyManager);

        searchPropertiesManager = new SearchPropertiesManager(
                jiraAuthenticationContext, searchRequestService, searchService,
                userPropertyManager);
    }

    @Test
    public void testGetSearchRequestAnonymous()
    {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(null);
        assertNull(searchPropertiesManager.getSearchRequest());
    }

    @Test
    public void testGetSearchRequestNotSet()
    {
        assertNull(searchPropertiesManager.getSearchRequest());
    }

    @Test
    public void testGetSetSearchRequestFilter()
    {
        Clause clause = new TerminalClauseImpl("type", Operator.EQUALS, "Bug");
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(clause),
                (ApplicationUser) null, null, null, 1337L, 0L);

        when(searchRequestService.getFilter(any(JiraServiceContext.class),
                anyLong())).thenReturn(searchRequest);

        assertNull(searchPropertiesManager.getSearchRequest());
        searchPropertiesManager.setSearchRequest(searchRequest);
        assertEquals(searchRequest, searchPropertiesManager.getSearchRequest());
        assertFalse(searchPropertiesManager.getSearchRequest().isModified());
    }

    @Test
    public void testGetSetSearchRequestJQL()
    {
        Clause clause = new TerminalClauseImpl("type", Operator.EQUALS, "Bug");
        SearchRequest searchRequest = new SearchRequest(new QueryImpl(clause));

        MessageSet messageSet = new MessageSetImpl();
        when(searchService.getGeneratedJqlString(any(Query.class))).thenReturn("type = Bug");
        when(searchService.parseQuery(any(User.class), anyString())).thenReturn(
                new SearchService.ParseResult(searchRequest.getQuery(), messageSet));

        assertNull(searchPropertiesManager.getSearchRequest());
        searchPropertiesManager.setSearchRequest(searchRequest);
        assertEquals(searchRequest, searchPropertiesManager.getSearchRequest());
        searchPropertiesManager.setSearchRequest(null);
        assertNull(searchPropertiesManager.getSearchRequest());
    }

    @Test
    public void testGetSetSearchRequestModifiedFilter()
    {
        Clause clause = new TerminalClauseImpl("type", Operator.EQUALS, "Bug");
        Query query = new QueryImpl(clause);

        SearchRequest filter = new SearchRequest(query, (ApplicationUser) null, null, null, 1337L, 0L);
        SearchRequest searchRequest = new SearchRequest(null, (ApplicationUser) null, null, null, 1337L, 0L);
        searchRequest.setQuery(new QueryImpl(clause)); // Marks as modified.

        MessageSet messageSet = new MessageSetImpl();
        when(searchService.getGeneratedJqlString(any(Query.class))).thenReturn("type = Bug");
        when(searchService.parseQuery(any(User.class), anyString())).thenReturn(
                new SearchService.ParseResult(searchRequest.getQuery(), messageSet));

        when(searchRequestService.getFilter(any(JiraServiceContext.class),
                anyLong())).thenReturn(filter);

        assertNull(searchPropertiesManager.getSearchRequest());
        searchPropertiesManager.setSearchRequest(searchRequest);
        assertEquals(searchRequest, searchPropertiesManager.getSearchRequest());
        assertTrue(searchPropertiesManager.getSearchRequest().isModified());
    }

    @Test
    public void testGetSearchRequestInvalidJQL()
    {
        MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("The JQL is invalid!");
        when(searchService.parseQuery(any(User.class), anyString())).thenReturn(
                new SearchService.ParseResult(null, messageSet));

        searchPropertiesManager.setSearchRequest(new SearchRequest());
        assertNull(searchPropertiesManager.getSearchRequest());
    }
}