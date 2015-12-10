package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.RunOnce;

import static com.atlassian.jira.functest.framework.NavigationImpl.PROJECT_PLUGIN_PREFIX;
import static com.atlassian.jira.functest.framework.util.RegexMatchers.regexMatches;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for issue constants (Issue type, priority, status, resolution).
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueConstants extends FuncTestCase
{

    private static final RunOnce RESTORE_ONCE = new RunOnce();


    private static final String INVALID_ELEMENT_NAME = "wrong";
    private static final String SUFFIX_HTML = " </td><td><input name=&quot;" + INVALID_ELEMENT_NAME + "&quot;>";
    private static final String SUFFIX_TEXT = " </td><td><input name=\"" + INVALID_ELEMENT_NAME + "\">";
    private static final String SUFFIX_ESC = " &lt;/td&gt;&lt;td&gt;&lt;input name=&quot;" + INVALID_ELEMENT_NAME + "&quot;&gt;";
    private static final String NAME_PREFIX = "name ";
    private static final String DESC_PREFIX = "desc ";

    private static final String ISSUE_TYPE_HTML_OPTION = NAME_PREFIX + "type" + SUFFIX_TEXT;
    private static final String ISSUE_TYPE_NAME_HTML = NAME_PREFIX + "type" + SUFFIX_HTML;
    private static final String ISSUE_TYPE_DESC_HTML = DESC_PREFIX + "type" + SUFFIX_HTML;
    private static final String ISSUE_TYPE_NAME_HTML_ESC = NAME_PREFIX + "type" + SUFFIX_ESC;
    private static final String ISSUE_TYPE_DESC_HTML_ESC = DESC_PREFIX + "type" + SUFFIX_ESC;
    private static final String ISSUE_TYPE_NAME_TEXT = "New Feature";
    private static final String ISSUE_TYPE_DESC_TEXT = "A new feature of the product, which has yet to be developed.";

    private static final String SUBTASK_TYPE_NAME_HTML = NAME_PREFIX + "subtype" + SUFFIX_HTML;
    private static final String SUBTASK_TYPE_DESC_HTML = DESC_PREFIX + "subtype" + SUFFIX_HTML;
    private static final String SUBTASK_TYPE_NAME_HTML_ESC = NAME_PREFIX + "subtype" + SUFFIX_ESC;
    private static final String SUBTASK_TYPE_DESC_HTML_ESC = DESC_PREFIX + "subtype" + SUFFIX_ESC;

    private static final String PRIORITY_HTML_OPTION = NAME_PREFIX + "priority" + SUFFIX_TEXT;
    private static final String PRIORITY_NAME_HTML = NAME_PREFIX + "priority" + SUFFIX_HTML;
    private static final String PRIORITY_DESC_HTML = DESC_PREFIX + "priority" + SUFFIX_HTML;
    private static final String PRIORITY_NAME_HTML_ESC = NAME_PREFIX + "priority" + SUFFIX_ESC;
    private static final String PRIORITY_DESC_HTML_ESC = DESC_PREFIX + "priority" + SUFFIX_ESC;
    private static final String PRIORITY_NAME_TEXT = "Major";
    private static final String PRIORITY_DESC_TEXT = "Major loss of function.";

    private static final String RESOLUTION_HTML_OPTION = NAME_PREFIX + "resolution" + SUFFIX_TEXT;
    private static final String RESOLUTION_NAME_HTML = NAME_PREFIX + "resolution" + SUFFIX_HTML;
    private static final String RESOLUTION_DESC_HTML = DESC_PREFIX + "resolution" + SUFFIX_HTML;
    private static final String RESOLUTION_NAME_HTML_ESC = NAME_PREFIX + "resolution" + SUFFIX_ESC;
    private static final String RESOLUTION_DESC_HTML_ESC = DESC_PREFIX + "resolution" + SUFFIX_ESC;
    private static final String RESOLUTION_NAME_TEXT = "Duplicate";
    private static final String RESOLUTION_DESC_TEXT = "The problem is a duplicate of an existing issue.";

    private static final String STATUS_HTML_OPTION = NAME_PREFIX + "status" + SUFFIX_TEXT;
    private static final String STATUS_NAME_HTML = NAME_PREFIX + "status" + SUFFIX_HTML;
    private static final String STATUS_DESC_HTML = DESC_PREFIX + "status" + SUFFIX_HTML;
    private static final String STATUS_NAME_HTML_ESC = NAME_PREFIX + "status" + SUFFIX_ESC;
    private static final String STATUS_DESC_HTML_ESC = DESC_PREFIX + "status" + SUFFIX_ESC;
    private static final String STATUS_NAME_TEXT = "In Progress";
    private static final String STATUS_DESC_TEXT = "This issue is being actively worked on at the moment by the assignee.";

    private static final String RESOLVED_STATUS_HTML_OPTION = NAME_PREFIX + "resolved" + SUFFIX_TEXT;
    private static final String RESOLVED_STATUS_NAME_HTML = NAME_PREFIX + "resolved" + SUFFIX_HTML;
    private static final String RESOLVED_STATUS_DESC_HTML = DESC_PREFIX + "resolved" + SUFFIX_HTML;
    private static final String RESOLVED_STATUS_NAME_HTML_ESC = NAME_PREFIX + "resolved" + SUFFIX_ESC;
    private static final String RESOLVED_STATUS_DESC_HTML_ESC = DESC_PREFIX + "resolved" + SUFFIX_ESC;
    public static final String JIRA_LOZENGE_DARK_FEATURE = "jira.issue.status.lozenge";

    @Override
    protected void setUpTest()
    {
        RESTORE_ONCE.run(new Runnable()
        {
            @Override
            public void run()
            {
                administration.restoreData("TestIssueConstants.xml");
            }
        });
    }

    public void testIssueConstantsAreEncodedOnBrowseProjectPage()
    {
        navigation.browseProjectTabPanel(PROJECT_HOMOSAP_KEY, PROJECT_PLUGIN_PREFIX + "issues-panel-panel");
        //check the right hand side of the browse project page (Note: not description in tooltip)
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        tester.assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + "");
        tester.assertTextPresent("title=\"" + PRIORITY_NAME_TEXT + "");
        tester.assertTextPresent(STATUS_NAME_HTML_ESC);
        tester.assertTextPresent(STATUS_NAME_TEXT);

        //road map project tab
        //gotoProjectTabPanel(PROJECT_HOMOSAP_KEY, PROJECT_TAB_ROAD_MAP);
        tester.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.jira-projects-plugin:roadmap-panel&expandVersion=10002");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();

        //change log project tab
        navigation.gotoPage("/browse/HSP?selectedTab=com.atlassian.jira.jira-projects-plugin:changelog-panel&expandVersion=10001");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();
    }

    public void testIssueConstantsAreEncodedOnReports()
    {
        //single level group by report
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        tester.selectOption("mapper", "Issue Type");
        tester.setFormElement("filterid", "10002");
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();
    }

    public void testIssueConstantsAreEncodedOnBulkOperation()
    {
        //assert on bulk edit all issues confirmation page

        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        selectIssuesForBulkOperation();
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        tester.checkCheckbox("actions", "issuetype");
        tester.selectOption("issuetype", ISSUE_TYPE_HTML_OPTION);
        tester.checkCheckbox("actions", "priority");
        tester.selectOption("priority", PRIORITY_HTML_OPTION);
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantHTMLTitlesPresent();

        //assert on bulk move issue
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        selectIssuesForBulkOperation();
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();

        //assert on bulk transition issue
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        selectIssuesForBulkOperation();
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");
        tester.checkCheckbox("wftransition", "jira_4_3");
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        tester.assertTextNotPresent(STATUS_NAME_HTML);
        tester.assertTextPresent(STATUS_NAME_HTML_ESC);

        //assert on bulk delete issue
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        selectIssuesForBulkOperation();
        tester.checkCheckbox("operation", "bulk.delete.operation.name");
        tester.submit("Next");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();
    }

    /**
     * When Status Lozenge eventually won't be a dark feature, this test and all code which checks presenting icon of
     * desired status can be removed
     */
    public void testIssueConstantsAreEncodedOnViewIssue()
    {
        //check the standard view issue page
        navigation.issue().viewIssue("HSP-3");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        assertIssueConstantStraightHTMLNotPresent();
        tester.assertTextPresent("title=\"" + ISSUE_TYPE_NAME_HTML_ESC + " - " + ISSUE_TYPE_DESC_HTML_ESC + "\"");
        tester.assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + " - " + PRIORITY_DESC_HTML_ESC + "\"");
        assertLozengePresent(RESOLVED_STATUS_NAME_HTML_ESC, RESOLVED_STATUS_DESC_HTML_ESC);

        tester.assertTextPresent(RESOLUTION_NAME_HTML_ESC);
        //check the quick subtask create form for professional/enterprise
        tester.assertTextPresent(SUBTASK_TYPE_NAME_HTML_ESC);

        //check the subtask's view issue page (for standard, this is just a plain issue still with the subtask type)
        navigation.issue().viewIssue("HSP-4");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        tester.assertTextNotPresent(SUBTASK_TYPE_NAME_HTML);
        tester.assertTextNotPresent(PRIORITY_NAME_HTML);
        tester.assertTextNotPresent(RESOLVED_STATUS_NAME_HTML);
        tester.assertTextNotPresent(SUBTASK_TYPE_DESC_HTML);
        tester.assertTextNotPresent(PRIORITY_DESC_HTML);
        tester.assertTextNotPresent(RESOLVED_STATUS_DESC_HTML);
        tester.assertTextPresent("title=\"" + SUBTASK_TYPE_NAME_HTML_ESC + " - " + SUBTASK_TYPE_DESC_HTML_ESC + "\"");
        tester.assertTextPresent("title=\"" + PRIORITY_NAME_HTML_ESC + " - " + PRIORITY_DESC_HTML_ESC + "\"");
        assertLozengePresent(STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);

        //check view issue's printable view
        navigation.issue().viewIssue("HSP-3");
        tester.clickLinkWithText("Printable");
        assertIssueConstantHTMLContentViewPresent();
        text.assertTextSequence(locator.page().getHTML(), "Resolution:", RESOLUTION_NAME_HTML_ESC);
        tester.assertFormNotPresent();
        navigation.gotoPage("/secure/Dashboard.jspa");

        //check view issues's XML view
        navigation.issue().viewIssue("HSP-3");
        tester.clickLinkWithText("XML");
        assertIssueConstantHTMLPresentInXML();
        text.assertTextSequence(tester.getDialog().getResponseText(),"<status id=\"5\"", ">", RESOLVED_STATUS_NAME_HTML_ESC, "</status>");
        navigation.gotoPage("/secure/Dashboard.jspa");

        //check view issues's Word view (just like looking at printable view)
        navigation.issue().viewIssue("HSP-3");
        tester.clickLinkWithText("Word");
        assertIssueConstantHTMLContentViewPresent();
        text.assertTextSequence(tester.getDialog().getResponseText(), "Resolution:", RESOLUTION_NAME_HTML_ESC);
        navigation.gotoPage("/secure/Dashboard.jspa");
    }

    private void assertLozengePresent(final String lozengeContent, final String lozengeTitle)
    {
        assertThat(tester.getDialog().getResponseText(), regexMatches("<span(.*)class=\"(.*)aui-lozenge(.*)\"(.*)>"+lozengeContent+"</span>"));
    }

    public void testIssueConstantsAreEncodedOnAdminPage()
    {
        //manage issue types page
        navigation.gotoAdmin();
        tester.clickLink("issue_types");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        final String issueTypesHTML = locator.page().getHTML();
        text.assertTextSequence(issueTypesHTML, ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);
        text.assertTextSequence(issueTypesHTML, SUBTASK_TYPE_NAME_HTML_ESC, SUBTASK_TYPE_DESC_HTML_ESC);
        //check the translation page for the issue types
        navigation.gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=issuetype");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        final String issueTypesTranslationsHTML = locator.page().getHTML();
        text.assertTextSequence(issueTypesTranslationsHTML, ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);

        //view priorities
        navigation.gotoAdmin();
        tester.clickLink("priorities");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        final String prioritiesHTML = locator.page().getHTML();
        text.assertTextSequence(prioritiesHTML, PRIORITY_NAME_HTML_ESC, PRIORITY_DESC_HTML_ESC);
        navigation.gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=priority");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        text.assertTextSequence(prioritiesHTML, PRIORITY_NAME_HTML_ESC, PRIORITY_DESC_HTML_ESC);

        //view resolutions
        navigation.gotoAdmin();
        tester.clickLink("resolutions");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        final String resolutionsHTML = locator.page().getHTML();
        text.assertTextSequence(resolutionsHTML, RESOLUTION_NAME_HTML_ESC, RESOLUTION_DESC_HTML_ESC);
        navigation.gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=resolution");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        text.assertTextSequence(resolutionsHTML, RESOLUTION_NAME_HTML_ESC, RESOLUTION_DESC_HTML_ESC);

        //view statuses
        navigation.gotoAdmin();
        tester.clickLink("statuses");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        final String statusesHTML = locator.page().getHTML();
        text.assertTextSequence(statusesHTML, STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);
        text.assertTextSequence(statusesHTML, RESOLVED_STATUS_NAME_HTML_ESC, RESOLVED_STATUS_DESC_HTML_ESC);
        navigation.gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=status");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        text.assertTextSequence(statusesHTML, STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);
        text.assertTextSequence(statusesHTML, RESOLVED_STATUS_NAME_HTML_ESC, RESOLVED_STATUS_DESC_HTML_ESC);
    }

    public void testIssueConstantsAreEncodedOnEditIssue()
    {
        navigation.issue().viewIssue("HSP-3");
        tester.clickLink("edit-issue");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        //for now just assert more than one issue type is available
        tester.assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        tester.assertTextPresent(ISSUE_TYPE_NAME_TEXT);

        //delete all but 1 issue type and check the edit page displays only one issue type (issuetype-edit-not-allowed.vm)
        navigation.gotoAdmin();
        tester.clickLink("issue_types");
        navigation.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=4"); // Improvements
        tester.submit("Delete");
        navigation.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=3"); // Task
        tester.submit("Delete");
        navigation.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=2"); // New Feature
        tester.submit("Delete");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        text.assertTextSequence(locator.page().getHTML(), ISSUE_TYPE_NAME_HTML_ESC, ISSUE_TYPE_DESC_HTML_ESC);
        //now go back to the edit issue page, and check that issuetype cannot be changed (issuetype-edit-not-allowed.vm)
        navigation.issue().viewIssue("HSP-3");
        tester.clickLink("edit-issue");
        tester.assertFormElementNotPresent(INVALID_ELEMENT_NAME);
        tester.assertTextPresent(ISSUE_TYPE_NAME_HTML_ESC);
        tester.assertTextNotPresent(ISSUE_TYPE_NAME_TEXT);
        tester.assertTextPresent("There are no issue types with compatible field configuration and/or workflow associations.");
    }

    //--------------------------------------------------------------------------------------------------- helper methods
    private void assertIssueConstantHTMLTitlesPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        tester.assertTextPresent("title=\"" + ISSUE_TYPE_NAME_HTML_ESC + " - " + ISSUE_TYPE_DESC_HTML_ESC);

        assertLozengePresent(STATUS_NAME_HTML_ESC, STATUS_DESC_HTML_ESC);
        assertLozengePresent(STATUS_NAME_TEXT, STATUS_DESC_TEXT);
    }

    private void assertIssueConstantHTMLPresentInXML()
    {
        assertIssueConstantStraightHTMLNotPresent();
        final String pageHtml = tester.getDialog().getResponseText();
        text.assertTextSequence(pageHtml, new String[] { "<type id=\"1\"", ">", ISSUE_TYPE_NAME_HTML_ESC, "</type>" });
        text.assertTextSequence(pageHtml, new String[] { "<priority id=\"1\"", ">", PRIORITY_NAME_HTML_ESC, "</priority>" });
        text.assertTextSequence(pageHtml, new String[] { "<resolution id=\"1\"", ">", RESOLUTION_NAME_HTML_ESC, "</resolution>" });
    }

    private void assertIssueConstantHTMLContentViewPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        final String pageHtml = tester.getDialog().getResponseText();
        text.assertTextSequence(pageHtml, "Type:", ISSUE_TYPE_NAME_HTML_ESC);
        text.assertTextSequence(pageHtml, "Priority:", PRIORITY_NAME_HTML_ESC);
        text.assertTextSequence(pageHtml, "Status:", STATUS_NAME_HTML_ESC);
    }

    private void assertIssueConstantTextContentViewPresent()
    {
        assertIssueConstantStraightHTMLNotPresent();
        text.assertTextSequence(locator.page(), "Type:", ISSUE_TYPE_NAME_TEXT);
        text.assertTextSequence(locator.page(), "Priority:", PRIORITY_NAME_TEXT);
        text.assertTextSequence(locator.page(), "Status:", STATUS_NAME_TEXT);
    }

    private void assertIssueConstantStraightHTMLNotPresent()
    {
        tester.assertTextNotPresent(ISSUE_TYPE_NAME_HTML);
        tester.assertTextNotPresent(PRIORITY_NAME_HTML);
        tester.assertTextNotPresent(STATUS_NAME_HTML);

        tester.assertTextNotPresent(ISSUE_TYPE_DESC_HTML);
        tester.assertTextNotPresent(PRIORITY_DESC_HTML);
        tester.assertTextNotPresent(STATUS_DESC_HTML);
    }

    private void selectIssuesForBulkOperation()
    {
        tester.checkCheckbox("bulkedit_10010", "on"); //HSP-3
        tester.checkCheckbox("bulkedit_10001", "on"); //HSP-2
        tester.checkCheckbox("bulkedit_10000", "on"); //HSP-1
        navigation.clickOnNext();
    }
}
