package com.atlassian.jira.rest.v1.issues;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.rest.mock.MockAuthenticationContextFactory;
import com.atlassian.jira.rest.v1.labels.IssueLabelsResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test case for {@link com.atlassian.jira.rest.v1.issues.IssueResource} endpoint, issue labels
 * manipulation API.
 *
 * @since v4.2
 */
public class TestIssueLabelsResource extends TestCase
{


    private JiraAuthenticationContext mockAuthenticationContext;
    private User mockUser;
    private LabelService mockLabelService;
    private ApplicationProperties mockApplicationProperties;
    private XsrfInvocationChecker mockXsrfChecker;

    private IssueLabelsResource tested;

    @Override
    protected void setUp() throws Exception
    {
        mockUser = new MockUser("test");
        mockAuthenticationContext = MockAuthenticationContextFactory.withAuthenticatedUser(mockUser);
        mockLabelService = createMock(LabelService.class);
        mockApplicationProperties = new MockApplicationProperties();
        mockXsrfChecker = createMock(XsrfInvocationChecker.class);
        tested = createTested();
    }

    private IssueLabelsResource createTested()
    {
        return new IssueLabelsResource(mockAuthenticationContext, mockLabelService,
                EasyMock.createMock(I18nHelper.class), mockApplicationProperties, mockXsrfChecker);
    }

    public void testXssInSuggestions()
    {
        stubLabelServiceFor(1000L, "'>", "'><script>alert('xss')</script>");
        stubApplicationProperties(APKeys.JIRA_AJAX_LABEL_SUGGESTION_LIMIT, "10");
        Response response = tested.getSuggestions(1000L, null, "'>");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        IssueLabelsResource.SuggestionListStruct result = (IssueLabelsResource.SuggestionListStruct) response.getEntity();
        assertEquals(1, result.suggestions().size());
        IssueLabelsResource.SuggestionStruct firstAndOnly = result.suggestions().iterator().next();
        assertEquals("'><script>alert('xss')</script>", firstAndOnly.label() );
        assertEquals("<b>&#39;&gt;</b>&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;", firstAndOnly.html());
    }

    private void stubLabelServiceFor(Long issueId, String token, String... labels)
    {
        expect(mockLabelService.getSuggestedLabels(mockUser, issueId, token)).andReturn(newSuggestionsResponse(labels));
        replay(mockLabelService);
    }

    private LabelService.LabelSuggestionResult newSuggestionsResponse(final String... labels)
    {
        Set<String> suggestions = new LinkedHashSet<String>(Arrays.asList(labels));
        return new LabelService.LabelSuggestionResult(suggestions, new SimpleErrorCollection());
    }

    private void stubApplicationProperties(String key, String value)
    {
        mockApplicationProperties.setString(key, value);
    }
}