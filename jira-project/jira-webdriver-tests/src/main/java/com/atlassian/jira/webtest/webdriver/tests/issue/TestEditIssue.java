package com.atlassian.jira.webtest.webdriver.tests.issue;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.components.fields.PageElementMatchers;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;

import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.ASSIGNEE;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.COMMENT;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.COMPONENTS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.ENVIRONMENT;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.FIX_VERSIONS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.LABELS;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.PRIORITY;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.SUMMARY;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.TIMETRACKING;
import static com.atlassian.jira.pageobjects.dialogs.quickedit.FieldPicker.WORKLOG;
import static org.junit.Assert.assertEquals;


/**
 * Tests for editing an issue using quick edit
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestEditIssue extends BaseJiraWebTest
{
    public static final String CUSTOMFIELD = "customfield_10000";

    @Inject
    private PageElementFinder finder;

    public static final String UNASSIGNED_ASSIGNEE = "Unassigned";

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testTogglingModesRetainsValues()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode().fill(SUMMARY, "test");
        Poller.waitUntil("summary was not updated.",
                editIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));

        editIssueDialog = editIssueDialog.switchToCustomMode();
        Poller.waitUntil("summary value was not maintained.",
                editIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));
        editIssueDialog.fill(COMMENT, "test another switch");
        editIssueDialog = editIssueDialog.switchToFullMode();
        Poller.waitUntil("comment was not updated.",
                editIssueDialog.getTimedFieldValue(COMMENT),
                Matchers.containsString("test another switch"));
        Poller.waitUntil("summary value was not maintained.",
                editIssueDialog.getTimedFieldValue(SUMMARY),
                Matchers.containsString("test"));

    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testCustomFieldJavaScriptExecutes()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode();
        Poller.waitUntilTrue("does not have class \"custom-field-js-applied\"",
                finder.find(By.id(CUSTOMFIELD)).timed().hasClass("custom-field-js-applied"));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testCustomFieldIsDisplayedAndJavascriptExecutesWhenChecked()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToCustomMode();
        editIssueDialog.addFields(CUSTOMFIELD);
        Poller.waitUntilTrue("custom field was not added.", finder.find(By.id(CUSTOMFIELD)).timed().isPresent());
        Poller.waitUntilTrue("does not have class \"custom-field-js-applied\"",
                finder.find(By.id(CUSTOMFIELD)).timed().hasClass("custom-field-js-applied"));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testEditingSimpleIssueInCustomMode()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToCustomMode();
        editIssueDialog.addFields(SUMMARY);
        Poller.waitUntilTrue("Summary field was not added.", finder.find(By.id(SUMMARY)).timed().isPresent());
        editIssueDialog.fill(SUMMARY, "Scott's Issue");

        final ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        Poller.waitUntil("Expecting view issue summary to be \"Scott's Issue\"",
                issuePage.getTimedSummary(),
                Matchers.containsString("Scott's Issue"));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testEditingSimpleIssueInFullMode()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "Scott's Issue");

        final ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        Poller.waitUntil("Expecting view issue summary to be \"Scott's Issue\"",
                issuePage.getTimedSummary(),
                Matchers.containsString("Scott's Issue"));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testEditingSimpleIssueAddVersionInline()
    {
        final String VERSION_ONE_POINT_OH = "1.0";

        final String NEW_AFFECTS_VERSION_ID = "10011";
        final String NEW_AFFECTS_VERSION_NAME = "NEWAFFECTSVERSION";
        final String NEW_VERSION_ID = "10012";
        final String NEW_VERSION_NAME = "NEWFIXVERSION";


        backdoor.versions().create(new Version().name(VERSION_ONE_POINT_OH).project("HSP"));

        Response response = backdoor.versions().getResponse(NEW_VERSION_ID);
        assertEquals("Found version that should not exist yet!", 404, response.statusCode);

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "Scott's Issue");
        editIssueDialog.setFixVersions(VERSION_ONE_POINT_OH, NEW_VERSION_NAME);
        editIssueDialog.setAffectsVersion(VERSION_ONE_POINT_OH, NEW_AFFECTS_VERSION_NAME);

        final ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        Poller.waitUntil("Expecting view issue summary to be \"Scott's Issue\"",
                issuePage.getTimedSummary(),
                Matchers.containsString("Scott's Issue"));
        Issue editedIssue = backdoor.issues().getIssue("HSP-1");
        List<Version> fixVersions = editedIssue.fields.fixVersions;
        List<Version> affectsVersions = editedIssue.fields.versions;
        assertEquals(Lists.newArrayList(VERSION_ONE_POINT_OH, NEW_VERSION_NAME), Lists.newArrayList(fixVersions.get(0).name, fixVersions.get(1).name));
        assertEquals(Lists.newArrayList(VERSION_ONE_POINT_OH, NEW_AFFECTS_VERSION_NAME), Lists.newArrayList(affectsVersions.get(0).name, affectsVersions.get(1).name));

        final Version newFixVersion = backdoor.versions().get(NEW_VERSION_ID);
        assertEquals("New fix version wasn't created as expected.", NEW_VERSION_NAME, newFixVersion.name);

        final Version newAffectsVersion = backdoor.versions().get(NEW_AFFECTS_VERSION_ID);
        assertEquals("New affects version wasn't created as expected.", NEW_AFFECTS_VERSION_NAME, newAffectsVersion.name);
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testEditingSimpleIssueAddComponentInline()
    {
        final String EXISTING_COMPONENT = "abc";

        final String NEW_COMPONENT_ID = "10011";
        final String NEW_COMPONENT_NAME = "def";


        backdoor.components().create(new Component().name(EXISTING_COMPONENT).project("HSP"));

        Response response = backdoor.components().getResponse(NEW_COMPONENT_ID);
        assertEquals("Found version that should not exist yet!", 404, response.statusCode);

        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "Scott's Issue");
        editIssueDialog.setComponents(EXISTING_COMPONENT, NEW_COMPONENT_NAME);

        final ViewIssuePage issuePage = editIssueDialog.submitExpectingViewIssue("HSP-1");
        Poller.waitUntil("Expecting view issue summary to be \"Scott's Issue\"",
                issuePage.getTimedSummary(),
                Matchers.containsString("Scott's Issue"));
        Issue editedIssue = backdoor.issues().getIssue("HSP-1");
        List<Component> components = editedIssue.fields.components;
        assertEquals(Lists.newArrayList(EXISTING_COMPONENT, NEW_COMPONENT_NAME), Lists.newArrayList(components.get(0).name, components.get(1).name));

        final Component newComponent = backdoor.components().get(NEW_COMPONENT_ID);
        assertEquals("New component wasn't created as expected.", NEW_COMPONENT_NAME, newComponent.name);
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testValidationErrorsInCustomMode()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToCustomMode();
        editIssueDialog.addFields(SUMMARY);
        Poller.waitUntilTrue("Summary field was not added.", finder.find(By.id(SUMMARY)).timed().isPresent());
        editIssueDialog.fill(SUMMARY, "");
        editIssueDialog.submit(EditIssueDialog.class).waitForFormErrors();
        Poller.waitUntil("Expected inline error for summary as it is a required field and has no value",
                editIssueDialog.getFormErrorElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttribute("data-field", SUMMARY)));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testValidationErrorsInFullMode()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToFullMode();
        editIssueDialog.fill(SUMMARY, "");
        editIssueDialog.submit(EditIssueDialog.class).waitForFormErrors();
        Poller.waitUntil("Expected inline error for summary as it is a required field and has no value",
                editIssueDialog.getFormErrorElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttribute("data-field", SUMMARY)));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testAddingAndRemovingFields()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());

        editIssueDialog.switchToCustomMode();

        editIssueDialog.removeFields(PRIORITY, COMPONENTS, FIX_VERSIONS, LABELS, COMMENT).addFields(SUMMARY, ENVIRONMENT);
        editIssueDialog.close();

        viewIssuePage = jira.goToViewIssue("HSP-2");
        editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntil(editIssueDialog.getVisibleFieldElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttributes("for", Arrays.asList(SUMMARY, ASSIGNEE, ENVIRONMENT))));
    }

    @Test
    @Restore ("xml/TestLogWorkInlineErrors.xml")
    public void testLogworkInlineErrors()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final EditIssueDialog editIssueDialog = viewIssuePage.editIssue();
        Poller.waitUntilTrue("edit issue dialog is not open", editIssueDialog.isOpen());
        editIssueDialog.submit(EditIssueDialog.class).waitForFormErrors();

        // Test that the time tracking fields are added to the dialog and errors displayed beneath them
        Poller.waitUntil("Expected inline error for summary as it is a required field and has no value",
                editIssueDialog.getFormErrorElements(),
                IterableMatchers.hasItemThat(PageElementMatchers.containsAttributes(
                        "data-field",
                        Arrays.asList("worklog_timeLogged", "timetracking_originalestimate")
                )));
    }

    @Test
    @Restore ("xml/TestQuickEditIssue.xml")
    public void testEditAssignee()
    {
        jira.gotoHomePage();
        backdoor.generalConfiguration().allowUnassignedIssues();

        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "Issue created from testEditAssignee");
        final String assignee = pageBinder.bind(AssigneeField.class).getAssignee();
        assertEquals("Automatic", assignee);

        createIssueDialog.submit(GlobalMessage.class);

        final String issueKey = getNewIssueKey();

        ViewIssuePage viewIssuePage = jira.goToViewIssue(issueKey);
        assertEquals("Administrator", viewIssuePage.getPeopleSection().getAssignee());

        viewIssuePage.execKeyboardShortcut("e");
        final EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).setAssignee(UNASSIGNED_ASSIGNEE);

        viewIssuePage = editIssueDialog.submitExpectingViewIssue(issueKey);
        assertEquals(UNASSIGNED_ASSIGNEE, viewIssuePage.getPeopleSection().getAssignee());

        viewIssuePage.execKeyboardShortcut("e");
        viewIssuePage = pageBinder.bind(EditIssueDialog.class).submitExpectingViewIssue(issueKey);
        assertEquals(UNASSIGNED_ASSIGNEE, viewIssuePage.getPeopleSection().getAssignee());
    }

    @Test
    @Restore ("xml/TestLogWorkInlineErrors.xml")
    public void testEditOriginalEstimate()
    {
        jira.goToViewIssue("HSP-1").execKeyboardShortcut("e");
        final EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).switchToCustomMode()
                .addFields(TIMETRACKING, WORKLOG)
                .setOriginalEstimate("5d").setTimeSpent("1h");
        editIssueDialog.submit(ViewIssuePage.class, "HSP-1"); // this will fail if there are some problems with validation
    }

    private String getNewIssueKey()
    {
        final SearchClient searchClient = new SearchClient(jira.environmentData());
        final SearchResult result = searchClient.postSearch(new SearchRequest().jql("summary ~ \"\\\"Issue created from testEditAssignee\\\"\" "));
        return result.issues.get(0).key;
    }

}

