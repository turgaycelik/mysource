package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.ArrayList;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestCascadingSelectCustomField extends JIRAWebTest
{
    private static final String CUSTOMFIELD_10000 = "customfield_10000";
    private static final String CUSTOMFIELD_10000_OPTION = "customfield_10000:1";
    private static final String CUSTOMFIELD_10000_1 = "customfield_10000:1";
    private static final String CASCADING_SELECT_NAME = "asdf";
    private static final String UPDATE_FIELD_TEXT = "Update the fields of the issue to relate to the new project.";
    private static final String ERROR_PRESENT_FOR_BAD_OPTION = "The option 'a1' is invalid for parent option 'test'";

    private static final String HOMO_PID_OPTION = "10001_1_pid";
    private static final String MONKEY_PID_OPTION = "10000_1_pid";
    private static final String MOVE_ISSUES_CONFIRMATION_TEXT = "Confirmation";

//    private static final String FAILURE_ERROR_FOR_OPTION_SELECT = "The option 'p1' is invalid for parent option 'stuff'";

    public TestCascadingSelectCustomField(String name)
    {
        super(name);
    }

    public void testMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");


        gotoIssue("HSP-1");

        clickLink("move-issue");
        setFormElement("pid", "10001");
        submit("Next >>");

        assertTextPresent(UPDATE_FIELD_TEXT);

        assertTextPresent(CASCADING_SELECT_NAME);
        selectOption(CUSTOMFIELD_10000, "test");
        selectOption(CUSTOMFIELD_10000_OPTION, "t1");

        submit("Next >>");

        assertTextPresent("stuff");
        assertTextPresent("s1");
        assertTextPresent("test");
        assertTextPresent("t1");

        submit("Move");
        assertTextPresent("test");
        assertTextPresent("t1");
    }

    public void testMoveIssueWithCascadingSelectCustomFieldWithBadValues()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        gotoIssue("HSP-1");

        clickLink("move-issue");
        setFormElement("pid", "10001");
        submit("Next >>");

        assertTextPresent(UPDATE_FIELD_TEXT);

        assertTextPresent(CASCADING_SELECT_NAME);
        selectOption(CUSTOMFIELD_10000, "test");
        selectOption(CUSTOMFIELD_10000_OPTION, "a1");

        submit("Next >>");
        assertions.getTextAssertions().assertTextPresentHtmlEncoded(ERROR_PRESENT_FOR_BAD_OPTION);
    }

    public void testBulkMoveWithCascadingSelectCustomFieldWithDifferentContexts()
    {
        restoreData("TestBulkMoveWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();

        selectOption(MONKEY_PID_OPTION, PROJECT_MONKEY);
        selectOption(HOMO_PID_OPTION, PROJECT_HOMOSAP);
        submit("Next");
        // We need to be able to select the values for the project we are going to
        selectOption(CUSTOMFIELD_10000, "test");
        selectOption(CUSTOMFIELD_10000_1, "t1");
        submit("Next");
        // We need to be able to select the values for the project we are going to
        selectOption(CUSTOMFIELD_10000, "stuff");
        selectOption(CUSTOMFIELD_10000_1, "s1");
        submit("Next");

        // If we get the confirmation screen then we were presented with the right values
        // and were able to select them
        assertTextPresent(MOVE_ISSUES_CONFIRMATION_TEXT);
    }

    public void testEditIssueWithCascadingSelectField()
    {
        restoreData("TestBulkMoveWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        gotoIssue("HSP-1");

        clickLink("edit-issue");
        selectOption(CUSTOMFIELD_10000, "cranky");
        selectOption(CUSTOMFIELD_10000_1, "p1");
        submit("Update");

        assertTextPresent("asdf");
        assertTextPresent("cranky");
        assertTextPresent("p1");

        // Assert validation is thrown for cascading select
        clickLink("edit-issue");
        selectOption(CUSTOMFIELD_10000, "stuff");
        selectOption(CUSTOMFIELD_10000_1, "p1");
        submit("Update");

        assertions.getTextAssertions().assertTextPresentHtmlEncoded("The option 'p1' is invalid for parent option 'stuff'");
    }

    public void testCreateIssueWithCascadingSelect()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");

        createIssueStep1();

        setFormElement("summary", "Test summary");

        // Do the negative test
        selectOption(CUSTOMFIELD_10000, "cranky");
        selectOption(CUSTOMFIELD_10000_OPTION, "s1");

        submit("Create");

        assertions.getTextAssertions().assertTextPresentHtmlEncoded("The option 's1' is invalid for parent option 'cranky'");

        selectOption(CUSTOMFIELD_10000, "stuff");
        selectOption(CUSTOMFIELD_10000_OPTION, "s1");

        submit("Create");

        assertTextPresent("asdf");
        assertTextPresent("stuff");
        assertTextPresent("s1");
    }

    public void testCreateIssueWithCascadingSelectChoosingOptionsNoneForParent()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        createIssueStep1();

        setFormElement("summary", "Test summary");

        // Test that we can add with a value of none for the parent
        selectOption(CUSTOMFIELD_10000, "None");

        submit("Create");

        // Since we chose 'None' make sure the field is not displayed on the view issue screen.
        assertTextNotPresent("asdf:");
    }

    public void testCreateIssueWithCascadingSelectChoosingOptionsNoneForChild()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        createIssueStep1();

        setFormElement("summary", "Test summary");

        // Test that we can add with a value of none for the parent
        selectOption(CUSTOMFIELD_10000, "stuff");
        selectOption(CUSTOMFIELD_10000_OPTION, "None");

        submit("Create");

        // Only the parent should be displayed
        assertTextPresent("stuff");
    }

    public void testSetDefaultNoneOptionsForCascadingSelect()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("config_" + CUSTOMFIELD_10000);
        clickLinkWithText("Edit Default Value");
        selectOption(CUSTOMFIELD_10000, "None");

        submit();
    }

    public void testIssueNavigatorSearchCascadingSelectWithAllOption()
    {
        restoreData("TestMoveIssueWithCascadingSelectCustomFieldWithDifferentContexts.xml");
        showIssues("project=homosapien");
        assertTextPresent("This is a test bug for the subversion plugin");
    }

    //JRA-20554: Removing cascading option from Postgres 8.3+ does not work.
    public void testDeleteOptionFromFieldAtFirstLevelOnTheIssue()
    {
        restoreData("TestDeleteOptionFromCascadingSelectCustomField.xml");
        removeOptions("10000","10000","HSP-1");
        assertTextPresent("Edit Options for Custom Field");
        assertTextNotPresent("Option_A");

        navigation.issue().viewIssue("HSP-1");

        assertElementNotPresent("rowForcustomfield_10000");
    }

    //JRA-20554: Removing cascading option from Postgres 8.3+ does not work.
    public void testDeleteOptionFromFieldAtSecondLevelOnTheIssue()
    {
        restoreData("TestDeleteOptionFromCascadingSelectCustomField.xml");
        removeSubOptions(10000L, "Option_B" ,"10003","HSP-3");

        text.assertTextPresent(new XPathLocator(tester, "//*[@class='instructions']"), "with parent option Option_B");
        assertTextPresent("There are currently no options available for this select list");

        navigation.issue().viewIssue("HSP-3");

        text.assertTextPresent(new IdLocator(tester, "customfield_10000-val"), "Option_B");
    }

    //JRA-20554: Removing cascading option from Postgres 8.3+ does not work.
    public void testDeleteOptionFromFieldAtFirstLevelWithSubOptions()
    {
        restoreData("TestDeleteOptionFromCascadingSelectCustomField.xml");
        removeOptions("10000", "10001","HSP-3");

        assertTextPresent("Edit Options for Custom Field");
        assertTextNotPresent("Option_B");

        navigation.issue().viewIssue("HSP-3");

        assertElementNotPresent("rowForcustomfield_10000");
    }

    //JRA-20554: Removing cascading option from Postgres 8.3+ does not work.
    public void testDeleteOptionFromFieldWithNoAssociatedIssues()
    {
        restoreData("TestDeleteOptionFromCascadingSelectCustomField.xml");
        removeOptions("10000", "10002", null);

        assertTextPresent("Edit Options for Custom Field");
        assertTextNotPresent("Option_C");
    }

    private void removeOptions(final String numericCustomFieldId, final String options, String issueAffected)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + numericCustomFieldId);
        tester.clickLinkWithText("Edit Options");
        tester.clickLink("del_" + options);

        assertRemoveOptionConfirmationScreen(issueAffected);

        tester.submit("Delete");
    }

    private void removeSubOptions(final Long customFieldId, final String parentOption, final String optionToRemove, final String issueAffected)
    {
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + customFieldId);
        tester.clickLinkWithText("Edit Options");
        tester.clickLinkWithText(parentOption);
        tester.clickLink("del_" + optionToRemove);

        assertRemoveOptionConfirmationScreen(issueAffected);

        tester.submit("Delete");
    }

    private void assertRemoveOptionConfirmationScreen(final String issueAffected)
    {
        final String issueCount = issueAffected == null ? "0" : "1";
        final List<String> textSequence = new ArrayList<String>();
        textSequence.add("Issues with this value");
        textSequence.add(issueCount);
        if (issueAffected != null)
        {
            textSequence.add(issueAffected);
        }
        text.assertTextSequence(new WebPageLocator(tester), textSequence.toArray(new String[textSequence.size()]));
    }
}