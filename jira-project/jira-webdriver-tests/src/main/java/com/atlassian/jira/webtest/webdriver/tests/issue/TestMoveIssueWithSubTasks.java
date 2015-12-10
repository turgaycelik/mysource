package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.net.URISyntaxException;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.navigator.BulkOperationProgressPage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssueWithSubtasksPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.pageobjects.PageBinder;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.rest.client.api.domain.EntityHelper.findEntityByName;
import static com.atlassian.jira.webtest.webdriver.tests.issue.TestMoveIssue.goToIssue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @since v6.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestMoveIssueWithSubTasks extends BaseJiraWebTest
{
    @Inject
    private PageBinder binder;

    private JiraRestClient restClient;
    private IssueRestClient issueClient;

    @Before
    public void setUp() throws URISyntaxException
    {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jira.environmentData().getBaseUrl().toURI(), JiraLoginPage.USER_ADMIN, JiraLoginPage.PASSWORD_ADMIN);
        issueClient = restClient.getIssueClient();
    }

    /*
     * Test for https://jdog.jira-dev.com/browse/JDEV-26548
     */
    @Test
    public void testMovedIssueHasSubTask()
    {
        backdoor.restoreBlankInstance();
        backdoor.subtask().enable();

        final Iterable<CimProject> metadataProjects = issueClient.getCreateIssueMetadata(
                new GetCreateIssueMetadataOptionsBuilder().withProjectKeys("HSP").withExpandedIssueTypesFields().build()).claim();

        assertEquals(1, Iterables.size(metadataProjects));

        final CimProject project = metadataProjects.iterator().next();
        final CimIssueType subTaskType = findEntityByName(project.getIssueTypes(), "Sub-task");

        BasicIssue parentIssue = restClient.getIssueClient().createIssue(
                new IssueInputBuilder(project, project.getIssueTypes().iterator().next(), "Parent").build()).claim();

        BasicIssue subTaskIssue = restClient.getIssueClient().createIssue(
                new IssueInputBuilder(project, subTaskType, "Sub-task")
                        .setFieldValue("parent", ComplexIssueInputFieldValue.with("key", parentIssue.getKey())).build()).claim();

        ViewIssuePage viewIssuePage = jira.goToViewIssue(parentIssue.getKey());
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssueWithSubtasksPage moveIssuePage = binder.bind(MoveIssueWithSubtasksPage.class, parentIssue.getKey());
        moveIssuePage.setNewProject("monkey");
        moveIssuePage.setIssueType("Bug");

        final BulkOperationProgressPage progressPage = moveIssuePage.next().next().next().next().move();
        final ViewIssuePage issuePage = progressPage.submit("MKY-1");

        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
        assertThat(issuePage.getIssueKey(), equalTo("MKY-1"));

        // should be possible to access it through old issue key
        goToIssue(parentIssue.getKey());
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "MKY-1");
        assertThat(viewIssuePage.getIssueKey(), equalTo("MKY-1"));

        // should be possible to open sub-task through old issue key
        goToIssue(subTaskIssue.getKey());
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "MKY-2");
        assertThat(viewIssuePage.getIssueKey(), equalTo("MKY-2"));
        assertThat(viewIssuePage.getSummary(), equalTo("Sub-task"));
    }


}
