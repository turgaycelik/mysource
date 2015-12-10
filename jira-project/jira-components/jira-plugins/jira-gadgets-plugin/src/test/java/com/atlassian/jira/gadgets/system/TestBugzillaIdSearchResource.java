package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.easymock.classextension.EasyMock;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.eq;

/**
 * Unit test for {@link com.atlassian.jira.gadgets.system.BugzillaIdSearchResource}.
 *
 * @since v4.0
 */
public class TestBugzillaIdSearchResource extends ResourceTest
{
    private SearchService searchService;
    private User mockUser;

    @Override
    protected void setUp() throws Exception
    {
        searchService = createMock(SearchService.class);
        mockUser = new MockUser("test");
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Override
    protected void tearDown() throws Exception
    {
        searchService = null;
        mockUser = null;
        super.tearDown();
    }

    public void testNullIssueKey() throws SearchException
    {
        final String bugzillaKey = "123";

        JiraAuthenticationContext authContext = createMock(JiraAuthenticationContext.class);
        expect(authContext.getLoggedInUser()).andReturn(mockUser);

        EasyMock.replay(authContext);
        BugzillaIdSearchResource bugzillaSearch = new BugzillaIdSearchResource(null, authContext, null)
        {
            @Override
            String findMovedIssueJql(final String bugzillaKey, final User user) throws SearchException
            {
                return null;
            }
        };
        assertEquals(bugzillaSearch.searchBugzillaIssue(bugzillaKey).getEntity().toString(), "");
        EasyMock.verify(authContext);
    }

    public void testIssueInJira() throws SearchException
    {
        final String bugzillaKey = "123";

        SearchResults mockResults = createMock(SearchResults.class);
        expect(searchService.search(eq(mockUser), isA(Query.class), isA(PagerFilter.class))).andReturn(mockResults);
        expect(mockResults.getTotal()).andReturn(1);
        Issue mockIssue = createMock(Issue.class);
        List<Issue> issues = new ArrayList<Issue>();
        issues.add(mockIssue);
        expect(mockIssue.getKey()).andReturn("TST-1");
        expect(mockResults.getIssues()).andReturn(issues);

        EasyMock.replay(searchService, mockResults, mockIssue);

        BugzillaIdSearchResource bugzillaSearch = new BugzillaIdSearchResource(searchService, null, null);

        bugzillaSearch.findMovedIssueJql(bugzillaKey, mockUser);
        EasyMock.verify(searchService, mockResults, mockIssue);
    }

    public void testIssueNotInJira() throws SearchException
    {
        final String bugzillaKey = "123";

        SearchResults mockResults = createMock(SearchResults.class);
        expect(searchService.search(eq(mockUser), isA(Query.class), isA(PagerFilter.class))).andReturn(mockResults);
        expect(mockResults.getTotal()).andReturn(0);

        EasyMock.replay(searchService, mockResults);

        BugzillaIdSearchResource bugzillaSearch = new BugzillaIdSearchResource(searchService, null, null);

        bugzillaSearch.findMovedIssueJql(bugzillaKey, mockUser);
        EasyMock.verify(searchService, mockResults);
    }

    public void testValidateJavascript()
    {
        BugzillaIdSearchResource bugzillaSearch = new BugzillaIdSearchResource(null, null, null);
        Response response = bugzillaSearch.validate("http://bugzilla.mycompany.com/");
        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), response);
        assertBugzillaUrlValidationError(bugzillaSearch.validate("javascript:"));
        assertBugzillaUrlValidationError(bugzillaSearch.validate("javascript:foo()"));
        assertBugzillaUrlValidationError(bugzillaSearch.validate("www.bugzilla.no.protocol.com"));
        assertBugzillaUrlValidationError(bugzillaSearch.validate("invalidurl"));
    }

    private void assertBugzillaUrlValidationError(Response response)
    {
        assertEquals(400, response.getStatus());
        Collection<ValidationError> expectedErrors = asList(new ValidationError("bugzillaUrl", "gadget.bugzilla.invalid.url"));
        assertEquals(ErrorCollection.Builder.newBuilder(expectedErrors).build(), response.getEntity());
    }
}

