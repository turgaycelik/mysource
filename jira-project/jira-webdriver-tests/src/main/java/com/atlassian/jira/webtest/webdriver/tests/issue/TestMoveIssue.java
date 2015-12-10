package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import javax.inject.Inject;

import com.atlassian.fugue.Iterables;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.EnableAttachmentsRule;
import com.atlassian.jira.functest.rules.RemoveAttachmentsRule;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;

import com.sun.jersey.api.client.UniformInterfaceException;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@Restore("TestProjectSelectForCreate.xml")
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestMoveIssue extends BaseJiraWebTest
{

    private static final String HSP_1 = "HSP-1";
    @Inject
    private PageBinder binder;
    private JiraRestClient restClient;

    @Rule
    public TestRule enabledAttachments = RuleChain.outerRule(webTestRule)
            .around(new EnableAttachmentsRule(backdoor))
            .around(new RemoveAttachmentsRule(backdoor));

    @Before
    public void setUp() throws URISyntaxException
    {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jira.environmentData().getBaseUrl().toURI(), JiraLoginPage.USER_ADMIN, JiraLoginPage.PASSWORD_ADMIN);
    }

    @Test
    public void testProjectSelect()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(HSP_1);
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, HSP_1);
        assertEquals(asList("Bug", "New Feature", "Task", "Improvement"), newArrayList(moveIssuePage.getIssueTypes()));
        moveIssuePage.setNewProject("gorilla");
        assertEquals(asList("Task"), newArrayList(moveIssuePage.getIssueTypes()));
        moveIssuePage.setNewProject("monkey");
        assertEquals(asList("Bug"), newArrayList(moveIssuePage.getIssueTypes()));
        final ViewIssuePage issuePage = moveIssuePage.next().next().move();
        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
    }

    @Test
    public void testProjectMoveIssueAndProjectRename()
    {
        // there's already HSP project, let's see if we can open the issue
        ViewIssuePage viewIssuePage = jira.goToViewIssue(HSP_1);
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, HSP_1);
        moveIssuePage.setNewProject("monkey");

        final ViewIssuePage issuePage = moveIssuePage.next().next().move();
        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
        assertThat(issuePage.getIssueKey(), equalTo("MKY-2"));

        // should be possible to access it through old issue key
        goToIssue(HSP_1);
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "MKY-2");
        assertThat(viewIssuePage.getIssueKey(), equalTo("MKY-2"));

        // should be possible to access it through old issue key after rename
        backdoor.project().editProjectKey(backdoor.project().getProjectId("MKY"), "ALC");

        goToIssue(HSP_1);
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "ALC-2");
        assertThat(viewIssuePage.getIssueKey(), equalTo("ALC-2"));
    }

    @Test
    public void testProjectRenameAndMoveIssue()
    {
        // should be possible to access it through old issue key after rename
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "ALC");

        // there's already HSP project, let's see if we can open the issue
        ViewIssuePage viewIssuePage = jira.goToViewIssue("ALC-1");
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, "ALC-1");
        moveIssuePage.setNewProject("monkey");

        final ViewIssuePage issuePage = moveIssuePage.next().next().move();
        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
        assertThat(issuePage.getIssueKey(), equalTo("MKY-2"));

        // should be possible to access it through old issue key
        goToIssue("ALC-1");
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "MKY-2");
        assertThat(viewIssuePage.getIssueKey(), equalTo("MKY-2"));

        goToIssue(HSP_1);
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "MKY-2");
        assertThat(viewIssuePage.getIssueKey(), equalTo("MKY-2"));
    }

    @Test
    public void testItIsPossibleToOpenIssueAfterProjectKeyRename()
    {
        // there's already HSP project, let's see if we can open the issue
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "ALC");

        // should be possible to access it through old issue key before rename
        goToIssue("HSP-1");
        ViewIssuePage viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "ALC-1");
        Assert.assertThat(viewIssuePage.getIssueKey(), IsEqual.equalTo("ALC-1"));

        // should be possible to access it through old issue key after rename
        goToIssue("ALC-1");
        viewIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, "ALC-1");
        Assert.assertThat(viewIssuePage.getIssueKey(), IsEqual.equalTo("ALC-1"));
    }

    @Test
    public void testItIsPossibleToGetIssueByCurrentKeyAfterMoveIssueAndProjectKeyRename() throws Exception
    {
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "ALC");

        // there's already HSP project, let's see if we can open the issue
        ViewIssuePage viewIssuePage = jira.goToViewIssue("ALC-1");
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, "ALC-1");
        moveIssuePage.setNewProject("monkey").next().next().move();

        backdoor.project().editProjectKey(backdoor.project().getProjectId("MKY"), "ORG");

        assertNotFound(new Runnable()
        {
            @Override
            public void run()
            {
                backdoor.issueNavControl().getIssueIdByCurrentKey("HSP-1");
            }
        });
        assertNotFound(new Runnable()
        {
            @Override
            public void run()
            {
                backdoor.issueNavControl().getIssueIdByCurrentKey("ALC-1");
            }
        });
        assertNotFound(new Runnable()
        {
            @Override
            public void run()
            {
                backdoor.issueNavControl().getIssueIdByCurrentKey("MKY-2");
            }
        });
        String issueId = backdoor.issueNavControl().getIssueIdByCurrentKey("ORG-2");
        assertThat(issueId, equalTo(backdoor.issues().getIssue("ORG-2").id));

    }

    /**
     * JDEV-24895: moving to a renamed project would place attachments in the wrong directory
     */
    @Test
    public void movingToRenamedProjectShouldNotBreakAttachments() throws Exception
    {
        backdoor.attachments().enable();
        final Long projectId = backdoor.project().addProject("Renamed", "REN", "admin");
        backdoor.project().editProjectKey(projectId, "NAME");

        final com.atlassian.jira.rest.client.api.domain.Issue issue = restClient.getIssueClient().getIssue("HSP-1").claim();
        restClient.getIssueClient().addAttachment(issue.getAttachmentsUri(), new ByteArrayInputStream("testing attachment".getBytes("UTF-8")), "attachment.txt").claim();

        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, "HSP-1");
        viewIssuePage = moveIssuePage.setNewProject("Renamed").next().next().move();
        assertThat(viewIssuePage.getIssueKey(), startsWith("NAME-"));

        final com.atlassian.jira.rest.client.api.domain.Issue movedIssue = restClient.getIssueClient().getIssue(viewIssuePage.getIssueKey()).claim();
        assertNotNull(movedIssue.getAttachments());
        final Attachment attachment = Iterables.first(movedIssue.getAttachments()).getOrNull();
        assertNotNull(attachment.getContentUri());
        assertThat(IOUtils.toString(restClient.getIssueClient().getAttachment(attachment.getContentUri()).claim()), equalTo("testing attachment"));
    }

    /**
     * JDEV-24882: moving from a renamed project would place attachments in the wrong directory
     */
    @Test
    public void movingFromRenamedProjectShouldNotBreakAttachments() throws Exception
    {
        backdoor.attachments().enable();
        final Long projectId = backdoor.project().addProject("Renamed", "REN", "admin");

        com.atlassian.jira.rest.client.api.domain.Issue issue = restClient.getIssueClient().getIssue("HSP-1").claim();
        restClient.getIssueClient().addAttachment(issue.getAttachmentsUri(), new ByteArrayInputStream("testing attachment".getBytes("UTF-8")), "attachment.txt").claim();

        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "ORG");


        // moving the attachment from a renamed project to a project
        ViewIssuePage viewIssuePage = jira.goToViewIssue("ORG-1");
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, "ORG-1");
        viewIssuePage = moveIssuePage.setNewProject("Renamed").next().next().move();
        assertThat(viewIssuePage.getIssueKey(), startsWith("REN-"));

        com.atlassian.jira.rest.client.api.domain.Issue movedIssue = restClient.getIssueClient().getIssue(viewIssuePage.getIssueKey()).claim();
        assertNotNull(movedIssue.getAttachments());
        Attachment attachment = Iterables.first(movedIssue.getAttachments()).getOrNull();
        assertNotNull(attachment.getContentUri());
        assertThat(IOUtils.toString(restClient.getIssueClient().getAttachment(attachment.getContentUri()).claim()), equalTo("testing attachment"));

        // now let's rename key for Renamed and move another issue (moving the attachment from a renamed project to a rename project)
        backdoor.project().editProjectKey(projectId, "NAME");

        issue = restClient.getIssueClient().getIssue("HSP-5").claim();
        restClient.getIssueClient().addAttachment(issue.getAttachmentsUri(), new ByteArrayInputStream("testing another attachment".getBytes("UTF-8")), "attachment.txt").claim();

        viewIssuePage = jira.goToViewIssue("ORG-5");
        issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        moveIssuePage = binder.bind(MoveIssuePage.class, "ORG-5");
        viewIssuePage = moveIssuePage.setNewProject("Renamed").next().next().move();
        assertThat(viewIssuePage.getIssueKey(), startsWith("NAME-"));

        movedIssue = restClient.getIssueClient().getIssue(viewIssuePage.getIssueKey()).claim();
        assertNotNull(movedIssue.getAttachments());
        attachment = Iterables.first(movedIssue.getAttachments()).getOrNull();
        assertNotNull(attachment.getContentUri());
        assertThat(IOUtils.toString(restClient.getIssueClient().getAttachment(attachment.getContentUri()).claim()), equalTo("testing another attachment"));
    }


    @Test
    public void testSearchAfterProjectRenameAndMoveIssue()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "admin");
        // should be possible to access it through old issue key after rename
        backdoor.project().editProjectKey(backdoor.project().getProjectId("HSP"), "ALC");

        // there's already HSP project, let's see if we can open the issue
        ViewIssuePage viewIssuePage = jira.goToViewIssue("ALC-1");
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.invoke(DefaultIssueActions.MOVE);
        final MoveIssuePage moveIssuePage = binder.bind(MoveIssuePage.class, "ALC-1");
        moveIssuePage.setNewProject("monkey");

        final ViewIssuePage issuePage = moveIssuePage.next().next().move();
        assertEquals("Bug", issuePage.getDetailsSection().getIssueType());
        assertEquals("monkey", issuePage.getProject());
        assertThat(issuePage.getIssueKey(), equalTo("MKY-2"));

        jira.visit(AdvancedSearch.class).enterQuery("issue=ALC-1").submit();
        IssueNavigatorResults issueNavigatorResults = pageBinder.bind(IssueNavigatorResults.class);
        assertThat(issueNavigatorResults.selectIssue("MKY-2").getSelectedIssueKey(), equalTo("MKY-2"));

        final String jqlError = jira.visit(AdvancedSearch.class).enterQuery("issue>=ALC-1").submit().returnJQLErrorMessage();
        assertThat(jqlError, equalTo("Operator '>=' cannot be applied to moved issue key 'ALC-1'."));
    }

    private void assertNotFound(final Runnable runnable)
    {
        try
        {
            runnable.run();
            fail("Expected 404 response.");
        }
        catch (UniformInterfaceException e)
        {
            assertThat(e.getResponse().getStatus(), equalTo(404));
        }
    }

    public static void goToIssue(String key)
    {
        jira.getTester().getDriver().navigate().to(jira.getProductInstance().getBaseUrl() + new ViewIssuePage(key).getUrl());
    }

    private BaseMatcher<Issue> issue(final String key)
    {
        return new BaseMatcher<Issue>()
        {

            @Override
            public boolean matches(final Object item)
            {
                return ((Issue) item).key.equals(key);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Issue with: key=").appendValue(key);
            }
        };
    }
}

