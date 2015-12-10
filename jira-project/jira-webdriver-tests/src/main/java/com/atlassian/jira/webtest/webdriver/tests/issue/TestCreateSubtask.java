package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.viewissue.Subtask;
import com.atlassian.jira.pageobjects.pages.viewissue.SubtaskModule;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.Poller;

import com.sun.jersey.api.client.ClientResponse;

import org.junit.Test;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore("xml/TestCreateSubtasks.xml")
public class TestCreateSubtask extends BaseJiraWebTest
{
    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected TraceContext traceContext;

    @Test
    public void testCreateSingleSubtaskOnViewIssue()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.CREATE_SUBTASK);
        CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        createIssueDialog.fill("summary", "test subtask creation").submit(ViewIssuePage.class, "HSP-1");

        viewIssuePage = jira.goToViewIssue("HSP-1");

        SubtaskModule subTasksModule = viewIssuePage.getSubTasksModule();
        assertEquals("test subtask creation", subTasksModule.getSubtasks().get(0).getSummary());

        createIssueDialog = subTasksModule.openCreateSubtaskDialog()
                .checkCreateMultiple()
                .fill("summary", "Two")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        AuiMessage auiMessage = createIssueDialog.getAuiMessage();

        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        createIssueDialog = createIssueDialog.fill("summary", "Three")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        auiMessage = createIssueDialog.getAuiMessage();

        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();


        Tracer tracer = traceContext.checkpoint();
        createIssueDialog.close();

        subTasksModule = viewIssuePage.waitForAjaxRefresh(tracer).getSubTasksModule();

        final List<Subtask> subtasks = subTasksModule.getSubtasks();
        assertEquals("Two", subtasks.get(1).getSummary());
        assertEquals("Three", subtasks.get(2).getSummary());
    }

    @Test
    public void testFilterSubtaskOnViewIssue()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.CREATE_SUBTASK);
        CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        createIssueDialog.fill("summary", "test subtask creation").submit(ViewIssuePage.class, "HSP-1");

        viewIssuePage = jira.goToViewIssue("HSP-1");

        SubtaskModule subTasksModule = viewIssuePage.getSubTasksModule();

        createIssueDialog = subTasksModule.openCreateSubtaskDialog()
                .checkCreateMultiple()
                .fill("summary", "Two")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        createIssueDialog.getAuiMessage().dismiss();

        createIssueDialog = createIssueDialog.fill("summary", "Three")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        createIssueDialog.getAuiMessage().dismiss();

        Tracer tracer = traceContext.checkpoint();
        createIssueDialog.close();
        subTasksModule = viewIssuePage.waitForAjaxRefresh(tracer).getSubTasksModule();

        IssueTransactionsClient client = new IssueTransactionsClient(new LocalTestEnvironmentData());
        client.closeIssue("HSP-7");

        tracer = traceContext.checkpoint();
        subTasksModule.showOpen();
        subTasksModule = viewIssuePage.waitForAjaxRefresh(tracer).getSubTasksModule();
        final List<Subtask> openSubtasks = subTasksModule.getSubtasks();
        assertEquals("test subtask creation", openSubtasks.get(0).getSummary());
        assertEquals("Three", openSubtasks.get(1).getSummary());

        tracer = traceContext.checkpoint();
        subTasksModule.showAll();
        subTasksModule = viewIssuePage.waitForAjaxRefresh(tracer).getSubTasksModule();
        final List<Subtask> allSubtasks = subTasksModule.getSubtasks();
        assertEquals("test subtask creation", allSubtasks.get(0).getSummary());
        assertEquals("Two", allSubtasks.get(1).getSummary());
        assertEquals("Three", allSubtasks.get(2).getSummary());
    }

    @Test
    public void testCreateSingleSubtaskOnIssueNavigator()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "admin");

        AdvancedSearch advancedSearch = jira.visit(AdvancedSearch.class);

        Tracer checkpoint = traceContext.checkpoint();
        advancedSearch.enterQuery("").submit();
        IssueNavigatorResults issueNavigatorResults = pageBinder.bind(IssueNavigatorResults.class);
        Poller.waitUntilTrue(traceContext.condition(checkpoint, "jira.search.stable.update"));

        issueNavigatorResults.getSelectedIssue().getActionsMenu().open().clickItem(DefaultIssueActions.CREATE_SUBTASK);
        CreateIssueDialog createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        assertTrue("Expected Edit Issue Dialog for [MKY-1]", createSubtaskDialog.getTitle().contains("MKY-1"));
        createSubtaskDialog.close();
        issueNavigatorResults.nextIssue();
        issueNavigatorResults.getSelectedIssue().getActionsMenu().open().clickItem(DefaultIssueActions.CREATE_SUBTASK);
        createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
        assertTrue("Expected Edit Issue Dialog for [HSP-5]", createSubtaskDialog.getTitle().contains("HSP-5"));
        final GlobalMessage message = createSubtaskDialog.fill("summary", "My new subtask").submit(GlobalMessage.class);
        assertEquals(GlobalMessage.Type.SUCCESS, message.getType());
    }

    private class IssueTransactionsClient extends IssueClient
    {
        /**
         * Constructs a new IssueClient for a JIRA instance.
         *
         * @param environmentData The JIRA environment data
         */
        public IssueTransactionsClient(JIRAEnvironmentData environmentData)
        {
            super(environmentData);
        }

        public void closeIssue(final String issueKey)
        {

            toResponse(new Method()
            {
                @Override
                public ClientResponse call()
                {
                    IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
                    issueUpdateRequest.transition(ResourceRef.withId("5"));

                    return createResource().path(issueKey + "/transitions").type(APPLICATION_JSON_TYPE).post(ClientResponse.class, issueUpdateRequest);
                }
            });
        }
    }

}
