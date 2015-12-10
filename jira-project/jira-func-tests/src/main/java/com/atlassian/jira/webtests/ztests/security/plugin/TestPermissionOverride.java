package com.atlassian.jira.webtests.ztests.security.plugin;

import java.io.IOException;
import java.net.URI;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.CommentClient;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.Visibility;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;

import static javax.ws.rs.core.Response.Status;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.REFERENCE_PLUGIN })
public class TestPermissionOverride extends FuncTestCase
{
    private static final String TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE = "com.atlassian.jira.dev.reference-plugin:brad.odlaw.cant.transition.issue";
    private static final String COMMENT_ISSUE_PERMISSION_OVERRIDE_MODULE = "com.atlassian.jira.dev.reference-plugin:brad.odlaw.cant.comment";
    private static final String EDIT_ISSUE_PERMISSION_OVERRIDE_MODULE = "com.atlassian.jira.dev.reference-plugin:brad.odlaw.cant.edit.issue";
    private static final String BRAD = "brad_the_odlaw";

    private IssueClient issueClient;
    private CommentClient commentClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        backdoor.usersAndGroups().addUser(BRAD, BRAD, BRAD, "odlaw@atlassiqan.com").addUserToGroup(BRAD, "jira-developers");
        this.issueClient = new IssueClient(environmentData);
        this.commentClient = new CommentClient(environmentData);
    }

    public void testPermissionToEditIssueOverridden()
    {
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "issue summary");

        // without the permission override module Brad should be able to set the summary
        backdoor.plugins().disablePluginModule(EDIT_ISSUE_PERMISSION_OVERRIDE_MODULE);
        final Response response = issueClient.loginAs(BRAD, BRAD).updateResponse(issue.key(), summary("new summary"));
        assertThat(response.statusCode, statusCode(Status.NO_CONTENT));

        // now once the permission is overriden Brad The Odlaw can't edit the issue.
        backdoor.plugins().enablePluginModule(EDIT_ISSUE_PERMISSION_OVERRIDE_MODULE);
        final Response updateWithoutPermissionResponse = issueClient.loginAs(BRAD, BRAD).updateResponse(issue.key(), summary("summary which will never be set"));
        assertThat(updateWithoutPermissionResponse.statusCode, not(statusCodeFamily(200)));
    }

    public void testPermissionToCommentIssueOverridden()
    {
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "issue summary");

        // without the permission override module Brad the Odlaw can comment on the issue
        backdoor.plugins().disablePluginModule(COMMENT_ISSUE_PERMISSION_OVERRIDE_MODULE);
        final Response<Comment> response = commentClient.loginAs(BRAD, BRAD).post(issue.key(), comment("I comment because I can"));

        assertThat(response.statusCode, Matchers.is(Status.CREATED.getStatusCode()));

        backdoor.plugins().enablePluginModule(COMMENT_ISSUE_PERMISSION_OVERRIDE_MODULE);
        final Response<Comment> commentWithoutPermissionResponse = commentClient.loginAs(BRAD, BRAD).post(issue.key(), comment("I try to comment"));

        assertThat(commentWithoutPermissionResponse.statusCode, not(statusCodeFamily(200)));
    }

    public void testPermissionToTransitionIssueOverridden() throws IOException
    {
        final IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "issue summary");

        final JiraRestClient jiraRestClient = createRestClient(BRAD);
        try
        {
            // without the permission override module Brad the Odlaw can transition the issue
            backdoor.plugins().disablePluginModule(TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE);
            assertThat(getIssueTransitions(jiraRestClient, issue.key()), not(Matchers.<Transition>emptyIterable()));

            // after enabling permission override module Brad the Odlaw should not have permission to transition issue
            backdoor.plugins().enablePluginModule(TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE);
            assertThat(getIssueTransitions(jiraRestClient, issue.key()), Matchers.<Transition>emptyIterable());
        }
        finally
        {
            jiraRestClient.close();
        }
    }

    private IssueUpdateRequest summary(final String summary)
    {
        return new IssueUpdateRequest().fields(new IssueFields().summary(summary));
    }

    private Comment comment(final String commentText)
    {
        final Comment comment = new Comment();
        comment.body = commentText;
        comment.visibility = new Visibility("group", "jira-users");
        return comment;
    }

    private Matcher<? super Integer> statusCode(final Status status)
    {
        return Is.is(status.getStatusCode());
    }

    private Matcher<? super Integer> statusCodeFamily(final int family)
    {
        final int statusCodeFamilyLowerBound = (family / 100) * 100;
        //noinspection unchecked
        return Matchers.allOf(Matchers.lessThan(statusCodeFamilyLowerBound + 100), Matchers.greaterThanOrEqualTo(statusCodeFamilyLowerBound));
    }

    private JiraRestClient createRestClient(final String user)
    {
        return createRestClient(user, user);
    }

    private Iterable<Transition> getIssueTransitions(final JiraRestClient jiraRestClient, final String issueKey)
    {
        final IssueRestClient issueClient = jiraRestClient.getIssueClient();
        final Issue restClientIssue = issueClient.getIssue(issueKey).claim();
        return issueClient.getTransitions(restClientIssue).claim();
    }
}
