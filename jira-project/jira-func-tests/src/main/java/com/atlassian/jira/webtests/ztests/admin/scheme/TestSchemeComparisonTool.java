package com.atlassian.jira.webtests.ztests.admin.scheme;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 *
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SCHEMES })
public class TestSchemeComparisonTool extends JIRAWebTest
{
    private static final String MANAGE_WATCHERS = "Manage Watchers";

    public TestSchemeComparisonTool(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSchemeComparisonTool.xml");
    }

    public void testPermissionSchemeComparison()
    {
        gotoPage("secure/admin/SchemeComparisonPicker!default.jspa");

        assertTextPresent("Scheme Comparison: Select Schemes");
        assertTextPresent("Select between 2 and 5 schemes for comparison.");

        selectMultiOptionByValue("selectedSchemeIds", "0");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        assertTextPresent("If any entries for a permission do not match, the row will be displayed in red and bold.");
        assertTextPresent("Scheme Difference: <span class=\"redText\">&nbsp;39%&nbsp;</span>");
        assertTextPresent("Set Issue Security");
        assertTextPresent("Homosapien Permission Scheme");
        assertTextPresent("Default Permission Scheme");

        assertTextPresent("Add Comments");
        assertTextPresent("Create Attachments");
        assertTextPresent("Create Issues");
        assertTextPresent("Browse Projects");
        assertTextPresent("Transition Issues");

        assertTextNotPresent("Administer Projects");
        assertTextNotPresent("Edit Issues");
        assertTextNotPresent("Schedule Issues");
        assertTextNotPresent("Move Issues");
        assertTextNotPresent("Assign Issues");
        assertTextNotPresent("Assignable User");
        assertTextNotPresent("Resolve Issues");
        assertTextNotPresent("Close Issues");
        assertTextNotPresent("Modify Reporter");
        assertTextNotPresent("Delete Issues");
        assertTextNotPresent("Work On Issues");
        assertTextNotPresent("Link Issues");
        assertTextNotPresent("View Issue Source Tab");
        assertTextNotPresent("View Voters and Watchers");
        assertTextNotPresent(MANAGE_WATCHERS);
    }

    public void testNotificationSchemesHaveNoDifference()
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10021");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        // Check that we have the right description since we are looking at notification schemes
        assertTextPresent("If any entries for a notification event do not match");

        assertTextPresent("Scheme Difference: <span class=\"greenText\">&nbsp;0% (identical)&nbsp;</span>");

        // Since all are the same we should not be showing any rows of data
        assertTextNotPresent("Issue Resolved");
        assertTextNotPresent("Issue Assigned");
        assertTextNotPresent("Work Started On Issue");
        assertTextNotPresent("Issue Moved");
        assertTextNotPresent("Work Stopped On Issue");
        assertTextNotPresent("Issue Reopened");
        assertTextNotPresent("Issue Closed");
        assertTextNotPresent("Issue Created");
        assertTextNotPresent("Issue Deleted");
        assertTextNotPresent("Issue Commented");
        assertTextNotPresent("Work Logged On Issue");
        assertTextNotPresent("Issue Updated");

        assertTextPresent("Notification Scheme Equal A");
        assertTextPresent("Notification Scheme Equal B");
        assertTextPresent("have no differences");
    }

    public void testUnableToSelectMoreThan5Schemes()
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10021");
        selectMultiOptionByValue("selectedSchemeIds", "10022");
        selectMultiOptionByValue("selectedSchemeIds", "10023");
        selectMultiOptionByValue("selectedSchemeIds", "10024");
        selectMultiOptionByValue("selectedSchemeIds", "10010");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        assertTextPresent("Can not perform a comparison on more than 5 schemes.");
    }

    public void testMustSelectMoreThan1Scheme()
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        selectMultiOptionByValue("selectedSchemeIds", "10021");

        submit("Compare Schemes");

        assertTextPresent("At least 2 schemes must be selected for comparison.");
    }

    public void testPermissionSchemesWithSomeDifference() throws SAXException
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");
        selectMultiOptionByValue("selectedSchemeIds", "0");
        selectMultiOptionByValue("selectedSchemeIds", "10020");

        submit("Compare Schemes");

        assertTextPresent("Scheme Difference: <span class=\"redText\">&nbsp;8%&nbsp;</span>");

        // Since all are the same we should not be showing any rows of data
        assertTextPresent("Administer Projects");

        assertTextNotPresent("Add Comments");
        assertTextNotPresent("Create Attachments");
        assertTextNotPresent("Set Issue Security");
        assertTextNotPresent("Create Issues");
        assertTextNotPresent("Browse Projects");
        assertTextNotPresent("Edit Issues");
        assertTextNotPresent("Schedule Issues");
        assertTextNotPresent("Move Issues");
        assertTextNotPresent("Assign Issues");
        assertTextNotPresent("Assignable User");
        assertTextNotPresent("Resolve Issues");
        assertTextNotPresent("Close Issues");
        assertTextNotPresent("Modify Reporter");
        assertTextNotPresent("Delete Issues");
        assertTextNotPresent("Work On Issues");
        assertTextNotPresent("Link Issues");
        assertTextNotPresent("View Issue Source Tab");
        assertTextNotPresent("View Voters and Watchers");
        assertTextNotPresent(MANAGE_WATCHERS);

        assertTextPresent("Default Permission Scheme");
        assertTextPresent("Permission Scheme To Compare");

        //Check that the comparison table contains the correct groups for each scheme.
        WebTable projectRolesTable = getDialog().getResponse().getTableWithID("scheme_comparison_table");
        assertTrue(tableCellHasText(projectRolesTable, 1, 0, "Administer Projects"));
        assertTrue(tableCellHasText(projectRolesTable, 1, 1, "Group (jira-administrators)"));
        assertFalse(tableCellHasText(projectRolesTable, 1, 1, "Project Role"));
        assertFalse(tableCellHasText(projectRolesTable, 1, 1, "Single User"));

        assertTrue(tableCellHasText(projectRolesTable, 1, 2, "Group (jira-administrators)"));
        assertTrue(tableCellHasText(projectRolesTable, 1, 2, "Project Role (Administrators)"));
        assertTrue(tableCellHasText(projectRolesTable, 1, 2, "Single User (Fred Normal)"));
    }

    public void testNotificationSchemesAreCompletelyDifferent()
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10022");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        // Check that we have the right description since we are looking at notification schemes
        assertTextPresent("If any entries for a notification event do not match");

        assertTextPresent("Scheme Difference: <span class=\"redText\">&nbsp;100%&nbsp;</span>");

        // Since schemes are different, all entity types should be listed
        assertTextPresent("Issue Resolved");
        assertTextPresent("Issue Assigned");
        assertTextPresent("Work Started On Issue");
        assertTextPresent("Issue Moved");
        assertTextPresent("Work Stopped On Issue");
        assertTextPresent("Issue Reopened");
        assertTextPresent("Issue Closed");
        assertTextPresent("Issue Created");
        assertTextPresent("Issue Deleted");
        assertTextPresent("Issue Commented");
        assertTextPresent("Work Logged On Issue");
        assertTextPresent("Issue Updated");

        assertTextPresent("Notification Scheme Unequal B");
        assertTextPresent("Notification Scheme Equal A");
    }

    public void testDistilledSchemes() throws SAXException
    {
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10021");
        selectMultiOptionByValue("selectedSchemeIds", "10000");
        selectMultiOptionByValue("selectedSchemeIds", "10010");

        submit("Compare Schemes");

        // Check that we have the right description since we are looking at notification schemes
        assertTextPresent("If any entries for a notification event do not match");

        assertTextPresent("Scheme Difference: <span class=\"redText\">&nbsp;9%&nbsp;</span>");

        // Since schemes are different, all entity types should be listed
        assertTextPresent("Issue Resolved");
        assertTextPresent("Issue Assigned");
        assertTextNotPresent("Work Started On Issue");
        assertTextNotPresent("Issue Moved");
        assertTextNotPresent("Work Stopped On Issue");
        assertTextNotPresent("Issue Reopened");
        assertTextNotPresent("Issue Closed");
        assertTextNotPresent("Issue Created");
        assertTextNotPresent("Issue Deleted");
        assertTextNotPresent("Issue Commented");
        assertTextNotPresent("Work Logged On Issue");
        assertTextNotPresent("Issue Updated");
        assertTextNotPresent("Issue Worklog Updated");
        assertTextNotPresent("Issue Worklog Deleted");

        //Check that the comparison table contains a column with the matching schemes
        WebTable projectRolesTable = getDialog().getResponse().getTableWithID("scheme_comparison_table");
        assertTrue(tableCellHasText(projectRolesTable, 0, 1, "Matching Schemes"));
        assertTrue(tableCellHasText(projectRolesTable, 0, 1, "Notification Scheme Equal A"));
        assertTrue(tableCellHasText(projectRolesTable, 0, 1, "Notification Scheme Equal B"));
        assertTrue(tableCellHasText(projectRolesTable, 0, 2, "Notification Scheme Unequal A"));
    }

    public void testLinkToMergeTool()
    {
        // Compare two schemes that are the same so that we get the link to the merge tool
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10021");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        assertTextPresent("merge duplicate schemes tool");
        clickLinkWithText("merge duplicate schemes tool");

        assertTextPresent("Choose Schemes to Merge");
        assertTextPresent("Notification Scheme Equal A");
        assertTextPresent("Notification Scheme Equal B");
    }

    // This test specifically tests the scenario in JRA-11357
    public void testLinkToMergeToolWithDifferentSchemes()
    {
        // Compare two schemes that are the same so that we get the link to the merge tool
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10021");
        selectMultiOptionByValue("selectedSchemeIds", "10000");

        submit("Compare Schemes");

        assertTextPresent("merge duplicate schemes tool");
        clickLinkWithText("merge duplicate schemes tool");

        assertTextPresent("Choose Schemes to Merge");
        assertTextPresent("Notification Scheme Equal A");
        assertTextPresent("Notification Scheme Equal B");

        // Compare two schemes that are the same so that we get the link to the merge tool
        gotoPage("secure/admin/SchemeComparisonPicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");
        selectMultiOptionByValue("selectedSchemeIds", "10024");
        selectMultiOptionByValue("selectedSchemeIds", "10010");

        submit("Compare Schemes");

        assertTextPresent("merge duplicate schemes tool");
        clickLinkWithText("merge duplicate schemes tool");

        assertTextPresent("Choose Schemes to Merge");
        assertTextPresent("Copy of Notification Scheme Unequal A");
        assertTextPresent("Notification Scheme Unequal A");
    }
}
