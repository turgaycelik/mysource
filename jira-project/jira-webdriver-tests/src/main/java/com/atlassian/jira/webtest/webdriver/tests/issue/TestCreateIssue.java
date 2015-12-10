package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.jira.pageobjects.components.fields.PageElementMatchers;
import com.atlassian.jira.pageobjects.components.menu.IssuesMenu;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.testkit.client.restclient.FieldMetaData;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueCreateMeta;
import com.atlassian.jira.testkit.client.restclient.TimeTracking;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;

import com.google.common.base.Supplier;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import junit.framework.Assert;

import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.AFFECTS_VERSIONS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.FIX_VERSIONS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.SUMMARY;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for creating an issue using quick create
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestCreateIssue extends BaseJiraWebTest
{
    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder finder;

    IssueClient issueClient;

    @Before
    public void setUp() throws Exception
    {
        issueClient = new IssueClient(new LocalTestEnvironmentData());
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testTogglingModesRetainsValues()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.execKeyboardShortcut("c");

        CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());
        createIssueDialog.switchToFullMode();

        createIssueDialog.fill("summary", "test");
        Poller.waitUntil("summary was not updated.",
                createIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));

        createIssueDialog = createIssueDialog.switchToCustomMode();
        Poller.waitUntil("summary value was not maintained.",
                createIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));

        createIssueDialog.fill("description", "test another switch");
        Poller.waitUntil("description was not updated.",
                createIssueDialog.getTimedFieldValue("description"),
                Matchers.containsString("test another switch"));

        createIssueDialog = createIssueDialog.switchToFullMode();
        Poller.waitUntil("description value was not maintained.",
                createIssueDialog.getTimedFieldValue("description"),
                Matchers.containsString("test another switch"));
        Poller.waitUntil("summary value was not maintained.",
                createIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCustomFieldJavaScriptExecutes()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        assertTrue(finder.find(By.id("customfield_10000")).hasClass("custom-field-js-applied"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCustomFieldIsDisplayedAndJavaScriptExecutesWhenChecked()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode().addFields("customfield_10000");
        Poller.waitUntilTrue("custom field was not added.", finder.find(By.id("customfield_10000")).timed().isPresent());
        Poller.waitUntilTrue("does not have class \"custom-field-js-applied\"",
                finder.find(By.id("customfield_10000")).timed().hasClass("custom-field-js-applied"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreatingSimpleIssueInCustomMode()
    {
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToCustomMode();

        createIssueDialog.fill("summary", "Scott's Issue");
        Poller.waitUntil("summary was not updated.",
                createIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("Scott's Issue"));

        createIssueDialog.fill("description", "I own this issue!");
        Poller.waitUntil("description was not updated.",
                createIssueDialog.getTimedFieldValue("description"),
                Matchers.containsString("I own this issue!"));

        final GlobalMessage successMsg = createIssueDialog.submit(GlobalMessage.class);

        Poller.waitUntilTrue("Success message never loaded.", successMsg.isPresent());
        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        Poller.waitUntil("Expected success message to contain issue summary", successMsg.getMessage(), Matchers.containsString("Scott's Issue"));
        assertTrue("Expected success message to contain close button", successMsg.isCloseable());
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreatingSimpleIssueInFullMode()
    {
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToFullMode();
        createIssueDialog.fill("summary", "Scott's Issue")
                .fill("description", "I own this issue!");

        final GlobalMessage successMsg = createIssueDialog.submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        Poller.waitUntil("Expected success message to contain issue summary", successMsg.getMessage(), containsString("Scott's Issue"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreatingSimpleIssueWithFreeInputVersion()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        // select via free input
        MultiSelect selectFixVersions = pageBinder.bind(MultiSelect.class, FIX_VERSIONS);
        selectFixVersions.freeInput("New Version 1");

        // select by clicking on suggestion
        MultiSelect selectAffectsVersions = pageBinder.bind(MultiSelect.class, AFFECTS_VERSIONS);
        selectAffectsVersions.query("New Version 5").selectActiveSuggestion();

        createIssueDialog.fill(SUMMARY, "Perfect issue summary");

        final GlobalMessage successMsg = createIssueDialog.submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        Poller.waitUntil("Expected success message to contain issue summary", successMsg.getMessage(), containsString("Perfect issue summary"));

        final Issue issue = backdoor.issues().getIssue(backdoor.issueNavControl().getIssueKeyForSummary("Perfect issue summary"));
        Version fixVersions = ((Version) ((ArrayList) issue.fields.get(FIX_VERSIONS)).get(0));
        Version affectsVersions = ((Version) ((ArrayList) issue.fields.get(AFFECTS_VERSIONS)).get(0));
        assertEquals("Issue has selected fix version", "New Version 1", fixVersions.name);
        assertEquals("Issue has selected affects version", "New Version 5", affectsVersions.name);
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testValidationErrorsInCustomMode()
    {
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToCustomMode();
        createIssueDialog.addFields(SUMMARY);
        Poller.waitUntilTrue("Summary field was not added.", finder.find(By.id(SUMMARY)).timed().isPresent());
        createIssueDialog.fill(SUMMARY, "");
        createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE).waitForFormErrors();
        Poller.waitUntil("Expected inline error for summary as it is a required field and has no value",
                createIssueDialog.getFormErrorElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttribute("data-field", SUMMARY)));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testValidationErrorsInFullMode()
    {
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToFullMode();
        createIssueDialog.fill(SUMMARY, "");
        createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE).waitForFormErrors();
        Poller.waitUntil("Expected inline error for summary as it is a required field and has no value",
                createIssueDialog.getFormErrorElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttribute("data-field", SUMMARY)));
    }

    @Test
    @Restore ("xml/TestProjectSelectForCreate.xml")
    public void testProjectSelectInCustomMode()
    {
        List<String> issueTypesString;

        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToCustomMode();

        // homosapien project
        issueTypesString = getIssueTypes("HSP");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // gorilla project
        createIssueDialog.selectProject("gorilla");
        issueTypesString = getIssueTypes("GRL");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // monkey project
        createIssueDialog.selectProject("monkey");
        issueTypesString = getIssueTypes("MKY");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));
    }

    @Test
    @Restore ("xml/TestProjectSelectForCreate.xml")
    public void testProjectSelectInFullMode()
    {
        List<String> issueTypesString;

        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.switchToFullMode();

        // homosapien project
        issueTypesString = getIssueTypes("HSP");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // gorilla project
        createIssueDialog.selectProject("gorilla");
        issueTypesString = getIssueTypes("GRL");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // monkey project
        createIssueDialog.selectProject("monkey");
        issueTypesString = getIssueTypes("MKY");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testAddingAndRemovingFields()
    {
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode()
                .removeFields("priority", "description")
                .addFields("fixVersions", "reporter");
        createIssueDialog.close();
        jira.gotoHomePage();
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        assertEquals(createIssueDialog.getVisibleFields(), asList("summary", "components", "versions", "fixVersions", "reporter"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreateMultipleInCustomMode()
    {
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue()
                .fill("summary", "Issue 1")
                .fill("description", "my description")
                .checkCreateMultiple()
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        assertEquals(AuiMessage.Type.SUCCESS, createIssueDialog.getAuiMessage().getType());

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("", createIssueDialog.getFieldValue("description"));

        createIssueDialog = createIssueDialog.fill("summary", "Issue 2")
                .fill("description", "a different description")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        final AuiMessage auiMessage = createIssueDialog.getAuiMessage();
        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("", createIssueDialog.getFieldValue("description"));

        final GlobalMessage successMsg = createIssueDialog.uncheckCreateMultiple()
                .fill("summary", "Issue 3")
                .submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        Poller.waitUntil("Expected success message to contain issue summary", successMsg.getMessage(), containsString("Issue 3"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreateMultipleInFullMode()
    {
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue()
                .switchToFullMode()
                .fill("summary", "Issue 1")
                .fill("description", "my description")
                .checkCreateMultiple()
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("", createIssueDialog.getFieldValue("description"));

        createIssueDialog = createIssueDialog.fill("summary", "Issue 2")
                .fill("description", "a different description")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);


        final AuiMessage auiMessage = createIssueDialog.getAuiMessage();
        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("", createIssueDialog.getFieldValue("description"));

        final GlobalMessage successMsg = createIssueDialog.uncheckCreateMultiple()
                .fill("summary", "Issue 3")
                .submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        Poller.waitUntil("Expected success message to contain issue summary", successMsg.getMessage(), containsString("Issue 3"));
    }


    @Test
    @Restore ("xml/TestIssueTypesForCreate.xml")
    public void testIssueTypeSelectInCustomMode()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class)
                .createIssue()
                .switchToCustomMode();

        createIssueDialog.selectIssueType("Bug");
        assertEquals(asList("versions", "summary"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("New Feature");
        assertEquals(asList("summary", "components"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("Task");
        assertEquals(asList("summary"), createIssueDialog.getVisibleFields());
    }


    @Test
    @Restore ("xml/TestIssueTypesForCreate.xml")
    public void testIssueTypeSelectInFullMode()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        createIssueDialog.selectIssueType("Bug");
        assertEquals(asList("versions", "summary"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("New Feature");
        assertEquals(asList("summary", "components"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("Task");
        assertEquals(asList("assignee", "summary"), createIssueDialog.getVisibleFields());
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testCreateWithEscGivesDirtyFormWarning()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialogEscape = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialogEscape.isOpen());

        createIssueDialogEscape.fill("summary", "A sample Summary");
        createIssueDialogEscape.escape();

        Poller.waitUntilTrue(Conditions.forSupplier(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return createIssueDialogEscape.acceptDirtyFormWarning();
            }
        }));

        //Hitting Cancel should not provide a dirty form warning though.
        final CreateIssueDialog createIssueDialogClose = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialogClose.isOpen());

        createIssueDialogClose.fill("summary", "A sample Summary");
        createIssueDialogClose.close();

        Poller.waitUntilFalse(Conditions.forSupplier(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return createIssueDialogClose.acceptDirtyFormWarning();
            }
        }));
    }

    @Test
    @Restore("xml/TestSubtasksInCreateIssue.xml")
    public void testSubtaskIssueType()
    {
        jira.gotoHomePage();
        // homosapien project - check no subtasks in issue types
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        createIssueDialog.selectProject("homosapien");

        final List<String> issueTypes = getIssueTypes("HSP");
        Assert.assertEquals(issueTypes.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypes), new HashSet<String>(createIssueDialog.getIssueTypes()));

        createIssueDialog.close();

        // homosapien project - check only subtasks in issue types
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.CREATE_SUBTASK);
        final CreateIssueDialog createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        final List<String> issueSubtasks = getSubtaskTypes("HSP");
        Assert.assertEquals(issueSubtasks.size(), createSubtaskDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueSubtasks), new HashSet<String>(createSubtaskDialog.getIssueTypes()));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    public void testNoPermission()
    {
        backdoor.permissionSchemes().removeGroupPermission(0, 11, "jira-users"); // Remove create permissions
        jira.quickLogin("fred", "fred", DashboardPage.class);
        assertFalse(pageBinder.bind(JiraHeader.class).hasCreateLink());
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testProjectPreselect()
    {
        jira.gotoHomePage();
        // Should select first project in list as we have no history
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        Poller.waitUntil("Expecting homosapien to be preselected.", createIssueDialog.getTimedProject(), containsString("homosapien"));

        // We create an issue in monkey so the next time it should be a preselected on monkey
        createIssueDialog.selectProject("monkey");
        createIssueDialog.fill("summary", "test").submit(GlobalMessage.class);
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());

        Poller.waitUntil("Expecting monkey to be preselected.", createIssueDialog.getTimedProject(), containsString("monkey"));

        jira.goToViewIssue("HSP-1");
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        Poller.waitUntil("Expecting homosapien to be preselected.", createIssueDialog.getTimedProject(), containsString("homosapien"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testIssueTypePreselect()
    {
        jira.gotoHomePage();
        // Should select first issue type in list as we have no history
        backdoor.project().setDefaultIssueType(10000, null);
        final JiraHeader header = pageBinder.bind(JiraHeader.class);

        final CreateIssueDialog createIssueDialog = header.createIssue();
        assertEquals("Bug", createIssueDialog.getIssueType());
        createIssueDialog.close();
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testDefaultIssueTypePreselect()
    {
        jira.gotoHomePage();
        backdoor.project().setDefaultIssueType(10000, "2");
        final JiraHeader header = pageBinder.bind(JiraHeader.class);
        final CreateIssueDialog createIssueDialog = header.createIssue();
        assertEquals("New Feature", createIssueDialog.getIssueType());
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testCreatedIssuesGetAddedToHistory()
    {
        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "My Summary").submit(DashboardPage.class);
        JiraHeader header = pageBinder.bind(JiraHeader.class);

        IssuesMenu issuesMenu = header.getIssuesMenu().open();
        List<String> recentIssues = issuesMenu.getRecentIssues();

        assertTrue(recentIssues.get(0).endsWith("My Summary"));
        header.createIssue().fill("summary", "My Summary 2").submit(DashboardPage.class);

        header = pageBinder.bind(JiraHeader.class);
        issuesMenu = header.getIssuesMenu().open();
        recentIssues = issuesMenu.getRecentIssues();

        assertTrue(recentIssues.get(0).endsWith("My Summary 2"));
        assertTrue(recentIssues.get(1).endsWith("My Summary"));

    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testTimetrackingNotRetainedOnCreateMultiple()
    {
        backdoor.getTestkit().timeTracking().enable("1", "7", TimeTracking.Format.DAYS, TimeTracking.Unit.DAY, TimeTracking.Mode.MODERN);

        jira.gotoHomePage();
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue().switchToCustomMode().addFields("timetracking");
        Poller.waitUntilTrue("CreateIssueDialog was not opened.", createIssueDialog.isOpen());
        createIssueDialog.checkCreateMultiple();

        createIssueDialog.fill("summary", "My summary");
        createIssueDialog.fill("timetracking_originalestimate", "1h");

        createIssueDialog = createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);
        assertEquals("Time tracking value was not retained", "", createIssueDialog.getFieldValue("timetracking_originalestimate"));
    }

    @Test
    @Restore ("xml/TestQuickCreateIssue.xml")
    @LoginAs (user = "fred")
    public void testTimetrackingRetainedOnIssueTypeFormNodeAndProjectToggle()
    {
        backdoor.getTestkit().timeTracking().enable("1", "7", TimeTracking.Format.DAYS, TimeTracking.Unit.DAY, TimeTracking.Mode.LEGACY);

        jira.gotoHomePage();
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue().switchToFullMode();

        createIssueDialog.selectProject("homosapien");
        createIssueDialog.selectIssueType("Bug");
        createIssueDialog.fill("summary", "My summary");
        createIssueDialog.fill("timetracking", "1h");

        createIssueDialog.selectIssueType("Task");
        assertEquals("Time tracking value was retained on issue type toggle", "1h", createIssueDialog.getFieldValue("timetracking"));

        createIssueDialog.selectProject("monkey");
        assertEquals("Time tracking value was retained on project toggle", "1h", createIssueDialog.getFieldValue("timetracking"));

        createIssueDialog.switchToCustomMode().addFields("timetracking");
        assertEquals("Time tracking value was retained form mode toggle", "1h", createIssueDialog.getFieldValue("timetracking"));
    }

    private List<String> getIssueTypes(final String projectKey)
    {
        final List<String> issueTypesString = new ArrayList<String>();

        final IssueCreateMeta meta = issueClient.getCreateMeta(null, asList(new StringList(projectKey)), null, null, IssueCreateMeta.Expand.fields);
        final List<IssueCreateMeta.IssueType> issueTypes = meta.projects.get(0).issuetypes;

        for (final IssueCreateMeta.IssueType issueType : issueTypes) {

            final FieldMetaData fieldMeta = issueType.fields.get("parent");

            // skip sub-tasks
            if (fieldMeta == null || !fieldMeta.required) {
                issueTypesString.add(issueType.name);
            }
        }

        return issueTypesString;
    }

    private List<String> getSubtaskTypes(final String projectKey)
    {
        final List<String> subtaskTypesString = new ArrayList<String>();

        final IssueCreateMeta meta = issueClient.getCreateMeta(null, asList(new StringList(projectKey)), null, null, IssueCreateMeta.Expand.fields);
        final List<IssueCreateMeta.IssueType> subtaskTypes = meta.projects.get(0).issuetypes;

        for (final IssueCreateMeta.IssueType subtaskType : subtaskTypes) {

            final FieldMetaData fieldMeta = subtaskType.fields.get("parent");

            // only sub-tasks
            if (fieldMeta != null && fieldMeta.required) {
                subtaskTypesString.add(subtaskType.name);
            }
        }

        return subtaskTypesString;
    }


}

