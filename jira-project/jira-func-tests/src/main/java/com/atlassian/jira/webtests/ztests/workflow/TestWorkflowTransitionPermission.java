package com.atlassian.jira.webtests.ztests.workflow;

import java.io.IOException;
import java.net.URI;

import com.atlassian.fugue.Iterables;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import org.hamcrest.Matchers;

import static com.atlassian.jira.security.Permissions.BROWSE;
import static com.atlassian.jira.security.Permissions.TRANSITION_ISSUE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2.3
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.REFERENCE_PLUGIN, Category.PERMISSIONS, Category.WORKFLOW })
public class TestWorkflowTransitionPermission extends FuncTestCase
{
    private static final String TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE = "com.atlassian.jira.dev.reference-plugin:brad.odlaw.cant.transition.issue";
    private static final String JIRA_USERS_GROUP = "jira-users";
    private static final long DEFAULT_PERMISSION_SCHEME_ID = 0;
    private static final String PAUL_WITH_TRANSITION_POWER = "paul-with-transition-power";
    private static final String MARK_CANT_PERFORM_TRANSITION = "mark-cant-perform-transition";
    private static final String JIRA_DEVELOPERS_GROUP = "jira-developers";
    //private static final Transition START_PROGRESS_TRANSITION = new Transition("Start Progress", 4, Collections.<Transition.Field>emptyList());
    private static final int START_PROGRESS_TRANSITION_ID = 4;
    private static final String START_PROGRESS_TRANSITION_NAME = "Start Progress";
    // This user will have transition permission vetoed when TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE is enabled
    private static final String BRAD = "brad_the_odlaw";
    private final AsynchronousJiraRestClientFactory restClientFactory = new AsynchronousJiraRestClientFactory();
    private IssueCreateResponse testIssue;
    private JiraRestClient jiraRestClient;

    @Override
    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();

        // clear default settings for TRANSITION_ISSUE permission
        backdoor.permissionSchemes().removeGroupPermission(DEFAULT_PERMISSION_SCHEME_ID, TRANSITION_ISSUE, JIRA_USERS_GROUP);

        // grant TRANSITION_ISSUE for brad
        backdoor.usersAndGroups().addUser(BRAD, BRAD, BRAD, "odlaw@atlassiqan.com").addUserToGroup(BRAD, JIRA_DEVELOPERS_GROUP);
        backdoor.permissionSchemes().addUserPermission(DEFAULT_PERMISSION_SCHEME_ID, TRANSITION_ISSUE, BRAD);

        // mark can't perform transition
        backdoor.usersAndGroups().addUser(MARK_CANT_PERFORM_TRANSITION).addUserToGroup(MARK_CANT_PERFORM_TRANSITION, JIRA_DEVELOPERS_GROUP);

        // paul is smart, he can do transitions \o/
        backdoor.usersAndGroups().addUser(PAUL_WITH_TRANSITION_POWER).addUserToGroup(PAUL_WITH_TRANSITION_POWER, JIRA_DEVELOPERS_GROUP);
        backdoor.permissionSchemes().addUserPermission(DEFAULT_PERMISSION_SCHEME_ID, TRANSITION_ISSUE, PAUL_WITH_TRANSITION_POWER);

        testIssue = backdoor.issues().createIssue("HSP", "issue summary");
    }

    @Override
    protected void tearDownTest()
    {
        closeRestClient();
    }

    public void testShouldNotBeAbleToPerformTransitionWhenTransitionPermissionIsVetoed() throws IOException
    {
        initializeRestClient(BRAD);
        // without the permission override module Brad the Odlaw can transition the issue
        backdoor.plugins().disablePluginModule(TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE);
        final Iterable<Transition> issueTransitionsBeforeVeto = getIssueTransitions(jiraRestClient, testIssue.key());
        assertThat(issueTransitionsBeforeVeto, not(Matchers.<Transition>emptyIterable()));

        // after enabling permission override module Brad the Odlaw should not have permission to transition issue
        backdoor.plugins().enablePluginModule(TRANSITION_ISSUE_PERMISSION_OVERRIDE_MODULE);
        assertThat(getIssueTransitions(jiraRestClient, testIssue.key()), Matchers.<Transition>emptyIterable());

        final Transition transition = Iterables.first(issueTransitionsBeforeVeto).get();
        assertCannotExecuteTransitionViaRestClient(transition.getId(), transition.getName());
    }

    public void testShouldBeAbleToPerformTransitionWhenTransitionPermissionIsGranted()
    {
        navigation.login(PAUL_WITH_TRANSITION_POWER);
        navigation.issue().closeIssue(testIssue.key(), "Fixed", null);
        initializeRestClient(PAUL_WITH_TRANSITION_POWER);
        assertIssueStatusEquals(testIssue.key(), "Closed");
    }

    public void testShouldNotBeAbleToPerformTransitionWhenTransitionPermissionIsRevokedUsingTransitionDialogWithComment()
    {
        navigation.login(MARK_CANT_PERFORM_TRANSITION);
        try
        {
            navigation.issue().closeIssue(testIssue.key(), "Fixed", "I would like to close this issue, but I can't!");
            fail("Close issue should throw exception, did permission check failed?");
        }
        catch (final AssertionError assertionError)
        {
            assertEquals("Unable to find link with id [action_id_2]", assertionError.getMessage());
        }
    }


    public void testShouldNotBeAbleToPerformTransitionWhenTransitionPermissionIsRevokedUsingTransitionWithoutDialog() throws IOException
    {
        navigation.login(MARK_CANT_PERFORM_TRANSITION);
        try
        {
            navigation.issue().performIssueActionWithoutDetailsDialog(testIssue.key(), "Start Progress");
            fail("Start progress should throw exception, did permission check failed?");
        }
        catch (final AssertionError assertionError)
        {
            assertEquals("Link with text [Start Progress] not found in response.", assertionError.getMessage());
        }
    }

    public void testShouldNotSeeAnyTransitionsWhenTransitionPermissionIsRevoked() throws IOException
    {
        navigation.login(MARK_CANT_PERFORM_TRANSITION);
        navigation.issue().assignIssue(testIssue.key(), null, MARK_CANT_PERFORM_TRANSITION);

        initializeRestClient(MARK_CANT_PERFORM_TRANSITION);
        final Iterable<Transition> issueTransitions = getIssueTransitions(jiraRestClient, testIssue.key());
        assertThat(issueTransitions, Matchers.<Transition>emptyIterable());

        assertCannotExecuteTransitionViaRestClient(START_PROGRESS_TRANSITION_ID, START_PROGRESS_TRANSITION_NAME);
    }

    public void testShouldBeAbleToStartProgressViaRestApiWhenTransitionPermissionIsGranted() throws IOException
    {
        navigation.login(PAUL_WITH_TRANSITION_POWER);
        navigation.issue().assignIssue(testIssue.key(), null, PAUL_WITH_TRANSITION_POWER);

        initializeRestClient(PAUL_WITH_TRANSITION_POWER);
        final Iterable<Transition> issueTransitions = getIssueTransitions(jiraRestClient, testIssue.key());
        assertThat(issueTransitions, not(Matchers.<Transition>emptyIterable()));

        executeTransition(jiraRestClient, testIssue.key(), new TransitionInput(START_PROGRESS_TRANSITION_ID));
        assertIssueStatusEquals(testIssue.key(), "In Progress");
    }

    private void assertIssueStatusEquals(final String issueKey, final String expectedStatus) {
        final String issueStatus = jiraRestClient.getIssueClient().getIssue(issueKey).claim().getStatus().getName();
        assertEquals(expectedStatus, issueStatus);
    }

    private void assertCannotExecuteTransitionViaRestClient(final int transitionId, final String transitionName) {
        try
        {
            executeTransition(jiraRestClient, testIssue.key(), new TransitionInput(transitionId));
            fail("We expected exception here - it (probably) mean that user was able to perform permission!");
        }
        catch (final RestClientException e)
        {
            assertEquals("Status code should be 400", Integer.valueOf(400), e.getStatusCode().orNull());
            final String expectedMessage = "It seems that you have tried to perform a workflow operation "
                    + "(" + transitionName + ") that is not valid for the current state of this "
                    + "issue (" + testIssue.key() + "). The likely cause is that somebody has changed the "
                    + "issue recently, please look at the issue history for details.";
            assertThat(e.getErrorCollections().iterator().next().getErrorMessages(), contains(expectedMessage));
        }
    }

    private Iterable<Transition> getIssueTransitions(final JiraRestClient jiraRestClient, final String issueKey)
    {
        final IssueRestClient issueClient = jiraRestClient.getIssueClient();
        final Issue restClientIssue = issueClient.getIssue(issueKey).claim();
        return issueClient.getTransitions(restClientIssue).claim();
    }

    private void executeTransition(final JiraRestClient jiraRestClient, final String issueKey, final TransitionInput transition)
    {
        final IssueRestClient issueClient = jiraRestClient.getIssueClient();
        final Issue restClientIssue = issueClient.getIssue(issueKey).claim();
        issueClient.transition(restClientIssue, transition).claim();
    }

    private void initializeRestClient(final String user)
    {
        closeRestClient();
        jiraRestClient = restClientFactory.createWithBasicHttpAuthentication(URI.create(environmentData.getBaseUrl().toString()), user, user);
    }

    private void closeRestClient()
    {
        if (jiraRestClient != null) {
            try
            {
                jiraRestClient.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            finally {
                jiraRestClient = null;
            }
        }
    }
}
