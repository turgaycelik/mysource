package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.Priority;
import com.atlassian.jira.testkit.client.restclient.Version;

import com.meterware.httpunit.HttpUnitOptions;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkChangeIssues extends FuncTestCase
{

    private static final String NOTE_NO_VERSIONS = "The project of the selected issue(s) does not have any versions.";
    private static final String NOTE_NO_COMPONENTS = "The project of the selected issue(s) does not have any components.";
    private static final String NOTE_NO_CUSTOM_FIELDS = "There are no available custom fields for the selected issues.";

    /**
     * SETUP_ISSUE_COUNT is the number of 'known' issues to add<br>
     * 'known' issues are issues that are used to control some of the events<br>
     * and to validate through the bulk edit process
     */
    private static final int SETUP_ISSUE_COUNT = 11;
    private static final int PAGE_SIZE = 50;

    /** A variant of the commonly used version name used to show unescaped HTML problems. */
    private static final String MY_OPTION_VERSION_ONE_UNESCAPED = "New Version 1 &trade;";
    /** A variant of the commonly used component name used to show unescaped HTML problems. */
    private static final String MY_OPTION_COMPONENT_ONE_UNESCAPED = "New Component 1 &trade;";

    public void setUpTest()
    {
        backdoor.restoreBlankInstance();
        produceIssues(PROJECT_HOMOSAP_KEY, SETUP_ISSUE_COUNT);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    private void produceIssues(final String projectKey, final int howMany)
    {
        for (int i = 0; i < howMany; i++)
        {
            final String summary = Integer.toBinaryString(i);
            if(backdoor.issues().createIssue(projectKey, summary).id() == null)
            {
                fail(String.format("Failed at adding issue: '%s' while adding %d out of %d issues.", summary, i + 1, SETUP_ISSUE_COUNT));
            }
        }
    }

    /**
     *  test to check each dynamic labels in each step. <br>
     * selected ONE issues from a SINGLE project
     */
    public void testCheckLabelsSelectAllIssuesMultipleProject()
    {
        log("Bulk Change - Check Labels: select ALL known issues");

        backdoor.issues().createIssue(PROJECT_MONKEY_KEY, "OneIssueFromAnotherProject");

        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);

        wizard.selectAllIssues();
        checkLabelForStepChooseOperation(SETUP_ISSUE_COUNT + 1, 2);

        wizard.chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);
        checkLabelForStepOperationDetails(SETUP_ISSUE_COUNT + 1, 2);

        wizard.checkActionForField(FIELD_ASSIGNEE).setFieldValue(FIELD_ASSIGNEE, ADMIN_USERNAME)
                .checkActionForField(FIELD_PRIORITY).setFieldValue(FIELD_PRIORITY, "4")
                .finaliseFields();
        checkLabelForStepConfirmationEdit(SETUP_ISSUE_COUNT + 1, 2);

        wizard.revertTo(BulkChangeWizard.WizardState.CHOOSE_OPERATION);
        wizard.chooseOperation(BulkChangeWizard.BulkOperationsImpl.DELETE);
        checkLabelForStepConfirmationDelete(SETUP_ISSUE_COUNT + 1, 2);
    }

    /**
     *  test to check each dynamic labels in each step. <br>
     * selected ALL issues from a SINGLE project
     */
    public void testXssNonExistentVersionAndComponent()
    {
        log("Bulk Change - test XSS on reported missing Component and Version.");

        final Version version = backdoor.versions().create(new Version()
                .name(MY_OPTION_VERSION_ONE_UNESCAPED)
                .description("xss possible here")
                .project("HSP"));

        final Component component = backdoor.components().create(new Component()
                .name(MY_OPTION_COMPONENT_ONE_UNESCAPED)
                .description("xss possible here")
                .project("HSP"));

        navigation.issueNavigator().displayAllIssues();

        final BulkChangeWizard wizard = navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues();
        checkLabelForStepChooseOperation(SETUP_ISSUE_COUNT, 1);

        wizard.chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);
        checkLabelForStepOperationDetails(SETUP_ISSUE_COUNT, 1);

        wizard.checkActionForField(FIELD_FIX_VERSIONS).setFieldValue(FIELD_FIX_VERSIONS, Long.toString(version.id))
            .checkActionForField(FIELD_COMPONENTS).setFieldValue(FIELD_COMPONENTS, Long.toString(component.id))
            .finaliseFields();
        tester.assertTextNotPresent(MY_OPTION_COMPONENT_ONE_UNESCAPED);
        tester.assertTextNotPresent(MY_OPTION_VERSION_ONE_UNESCAPED);
        checkLabelForStepConfirmationEdit(SETUP_ISSUE_COUNT, 1);
    }

    /**
     * adds a temporary project and a issue (since its new, it has no versions and components)<br>
     * checks that in step Operation details, the version and component fields are not present
     */
    public void testComponentsAndVersionsNotSelectableInProjectWithoutSuch()
    {
        log("Bulk Change - Check label & contents: Step Operation Details");

        final String tmpProjectKey = "TMP";
        backdoor.project().addProject("prj_tmp", tmpProjectKey, ADMIN_USERNAME);
        backdoor.issues().createIssue(tmpProjectKey, "someSummary");

        navigation.issueNavigator().runSearch("project = " + tmpProjectKey);
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);

        tester.assertTextPresent(NOTE_NO_VERSIONS);
        tester.assertTextPresent(NOTE_NO_COMPONENTS);
        tester.assertTextPresent(NOTE_NO_CUSTOM_FIELDS);

        tester.assertFormElementNotPresent(FIELD_FIX_VERSIONS);
        tester.assertFormElementNotPresent(FIELD_VERSIONS);
        tester.assertFormElementNotPresent(FIELD_COMPONENTS);

        tester.assertFormElementPresent(FIELD_ASSIGNEE);
        tester.assertFormElementPresent(FIELD_PRIORITY);
    }

    /**
     * Tests if the correct Error message is displayed when clicking Next
     * without any required fields completed for delete operations
     */
    public void testClickNextWithoutFormCompletion()
    {
        log("Bulk Change - Errors: Click NEXT without form completion");
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        navigation.clickOnNext();
        assertErrorNodeWithText(BulkChangeIssues.ERROR_NEXT_CHOOSE_ISSUE);

        wizard.selectAllIssues();
        navigation.clickOnNext();
        assertErrorNodeWithText(BulkChangeIssues.ERROR_NEXT_CHOOSE_OPERATION);

        wizard.chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);
        navigation.clickOnNext();
        assertErrorNodeWithText(BulkChangeIssues.ERROR_NEXT_OPERATION_DETAILS);
    }

    private void assertErrorNodeWithText(final String text)
    {
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-message.error"), text);
    }

    /**
     * Goes to Step Choose Issues<br>
     * and checks that the cancel button works properly
     */
    public void testCancelLink()
    {
        navigation.issueNavigator().displayAllIssues();

        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);

        final Priority priority = backdoor.issues().getIssue("HSP-1").fields.priority;

        log("Bulk Change - Navigation: Click CANCEL at step CONFIRMATION EDIT");
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .checkActionForField(FIELD_PRIORITY).setFieldValue(FIELD_PRIORITY, "2")
                .finaliseFields()
                .cancel();
        tester.assertTextPresent(LABEL_ISSUE_NAVIGATOR);
        assertEquals("Issue should not have been changed by the cancelled form(s).",
                backdoor.issues().getIssue("HSP-1").fields.priority, priority);

        log("Bulk Change - Navigation: Click CANCEL at step OPERATION DETAILS");
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .cancel();
        tester.assertTextPresent(LABEL_ISSUE_NAVIGATOR);

        log("Bulk Change - Navigation: Click CANCEL at step CHOOSE OPERATION");
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .cancel();
        tester.assertTextPresent(LABEL_ISSUE_NAVIGATOR);

        log("Bulk Change - Navigation: Click CANCEL at step CHOOSE ISSUES");
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .cancel();
        tester.assertTextPresent(LABEL_ISSUE_NAVIGATOR);
        HttpUnitOptions.setScriptingEnabled(false);
    }

    /**
     * Goes to Step Confirmation Delete<br>
     * and checks that the cancel button works properly
     */
    public void testCancelLinkForStepConfirmationDelete()
    {
        navigation.issueNavigator().displayAllIssues();

        // Only turn this on so that the cancel button will work
        HttpUnitOptions.setScriptingEnabled(true);

        log("Bulk Change - Navigation: Click CANCEL at step CONFIRM DELETE");
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.DELETE)
                .cancel();
        tester.assertTextPresent(LABEL_ISSUE_NAVIGATOR);
        HttpUnitOptions.setScriptingEnabled(false);
        assertNotNull("Issue should not have been deleted by the cancelled form.", backdoor.issues().getIssue("HSP-1").id);
    }

    /**
     * Tests if each side menu links are working correctly
     */
    public void testRevertingToPreviousStagesInWizard()
    {
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);

        // step1:
        log("Bulk Change - Navigation: left navigation links: step 1");
        assertWizardStepLinks(wizard.getState());
        wizard.selectAllIssues();

        // step 2:
        log("Bulk Change - Navigation: left navigation links: step 2");
        assertWizardStepLinks(wizard.getState());
        wizard.revertTo(BulkChangeWizard.WizardState.SELECT_ISSUES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);

        // step 3:
        log("Bulk Change - Navigation: left navigation links: step 3");
        assertWizardStepLinks(wizard.getState());
        wizard.revertTo(BulkChangeWizard.WizardState.SELECT_ISSUES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);
        wizard.revertTo(BulkChangeWizard.WizardState.CHOOSE_OPERATION)
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .checkActionForField(FIELD_ASSIGNEE).setFieldValue(FIELD_ASSIGNEE, ADMIN_USERNAME)
                .finaliseFields();

        // step4:
        log("Bulk Change - Navigation: left navigation links: step 4");
        assertWizardStepLinks(wizard.getState());
        wizard.revertTo(BulkChangeWizard.WizardState.SELECT_ISSUES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .checkActionForField(FIELD_ASSIGNEE).setFieldValue(FIELD_ASSIGNEE, ADMIN_USERNAME)
                .finaliseFields();
        wizard.revertTo(BulkChangeWizard.WizardState.CHOOSE_OPERATION)
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT)
                .checkActionForField(FIELD_ASSIGNEE).setFieldValue(FIELD_ASSIGNEE, ADMIN_USERNAME)
                .finaliseFields();
        wizard.revertTo(BulkChangeWizard.WizardState.SET_FIELDS)
                .checkActionForField(FIELD_ASSIGNEE).setFieldValue(FIELD_ASSIGNEE, ADMIN_USERNAME)
                .finaliseFields();

        assertWizardStepLinks(wizard.getState());
    }

    private void assertWizardStepLinks(final BulkChangeWizard.WizardState forState)
    {
        for(final BulkChangeWizard.WizardState state: BulkChangeWizard.WizardState.valuesWithLinks())
        {
            if(state.getStage() < forState.getStage())
            {
                log(state.getLinkText() + " should be a link.");
                tester.assertLinkPresentWithText(state.getLinkText());
            }
            else
            {
                log(state.getLinkText() + " should not be a link.");
                tester.assertLinkNotPresentWithText(state.getLinkText());
            }
        }
    }

    /**
     * Includes the Current page and checks that ISSUE_ON_NEXT_PAGE is not
     * included for bulk change, and that issues with the prefix
     * PREFIX_ISSUE_ON_CURR_PG are in the same page.
     */
    public void testCheckIssueContent()
    {
        final String nextPageSummary = "on next page";
        final String firstPageSummary = Integer.toBinaryString(PAGE_SIZE - 1);

        produceIssues(PROJECT_MONKEY_KEY, PAGE_SIZE);
        backdoor.issues().createIssue(PROJECT_MONKEY_KEY, nextPageSummary);

        log("Bulk Change - Issue Content: Check correct issues are displayed, Include CURRENT page from CURRENT page");
        navigation.issueNavigator().runSearch(String.format("project = %s order by issuekey", PROJECT_MONKEY));
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.CURRENT_PAGE);
        //check that the issue on the next page is NOT included
        tester.assertLinkNotPresentWithText(nextPageSummary);
        //check that all the 'known' issues are included
        tester.assertLinkPresentWithText(firstPageSummary);

        log("Bulk Change - Issue Content: Check correct issues are displayed, Include ALL page from NEXT page");
        navigation.issueNavigator().runSearch(String.format("project = %s order by issuekey", PROJECT_MONKEY));
        navigation.gotoPage(navigation.getCurrentPage() + "&startIndex=50");
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        //check that the issue on the next page is included
        tester.assertLinkPresentWithText(nextPageSummary);
        //check that all the 'known' issues are included
        tester.assertLinkPresentWithText(firstPageSummary);

        log("Bulk Change - Issue Content: Check correct issues are displayed, Include CURRENT page from NEXT page");
        navigation.issueNavigator().runSearch(String.format("project = %s order by issuekey", PROJECT_MONKEY));
        navigation.gotoPage(navigation.getCurrentPage() + "&startIndex=50");
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.CURRENT_PAGE);
        //check that the issue on the next page is included
        tester.assertLinkPresentWithText(nextPageSummary);
        //check that all the 'known' issues are NOT included
        tester.assertLinkNotPresentWithText(firstPageSummary);
    }

    protected void checkLabelForStepChooseOperation(final int numOfSelectedIssues, final int numOfSelectedProjects)
    {
        checkSideMenuLabel(numOfSelectedIssues, numOfSelectedProjects);
        tester.assertTextPresent("Choose the operation you wish to perform on the selected <strong>" + numOfSelectedIssues + "</strong> issue(s).");
    }

    protected void checkLabelForStepOperationDetails(final int numOfSelectedIssues, final int numOfSelectedProjects)
    {
        checkSideMenuLabel(numOfSelectedIssues, numOfSelectedProjects);
        tester.assertTextPresent("Choose the bulk action(s) you wish to perform on the selected <b>" + numOfSelectedIssues + "</b> issue(s).");
    }

    private void checkLabelForStepConfirmationEdit(final int numOfSelectedIssues, final int numOfSelectedProjects)
    {
        checkSideMenuLabel(numOfSelectedIssues, numOfSelectedProjects);
        tester.assertTextPresent("The above table summarises the changes you are about to make to the following <strong>" + numOfSelectedIssues + "</strong> issues. Do you wish to continue?");
    }

    private void checkLabelForStepConfirmationDelete(final int numOfSelectedIssues, final int numOfSelectedProjects)
    {
        checkSideMenuLabel(numOfSelectedIssues, numOfSelectedProjects);
        text.assertTextSequence(locator.css("#content .aui-page-panel-content"), "Please confirm that you wish to delete the following", String.valueOf(numOfSelectedIssues), "issues");
    }

    private void checkSideMenuLabel(final int numOfSelectedIssues, final int numOfSelectedProjects)
    {
        text.assertTextSequence(locator.css(".steps li"), "Selected ", String.valueOf(numOfSelectedIssues), " issues from ", String.valueOf(numOfSelectedProjects), " project(s)");
    }

}
