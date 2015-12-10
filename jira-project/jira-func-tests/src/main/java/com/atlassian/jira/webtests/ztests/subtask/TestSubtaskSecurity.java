package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

/**
 * Test the subtask security to ensure a parent cant be seen if security dictates.
 * <p/>
 * The user TED does not have permission to see HSP-1 which is a parent issue to HSP-3, which he can see. The user BIll
 * does have permission to see HSP-1 which is a parent issue to HSP-2, which he can see.
 *
 * @since v3.12
 */
@Ignore ("Disabled pending more investigation --lmiranda")
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestSubtaskSecurity extends JIRAWebTest
{
    private String jiraContextPath;

    public TestSubtaskSecurity(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSubtaskSecurity.xml");
        jiraContextPath = getEnvironmentData().getContext();
        administration.attachments().enable();
    }

    public void tearDown()
    {
        super.tearDown();
    }

    // goto navigator and check that HSP-3 is not linked to HSP-1
    public void testParentIssueShouldNotBeLinkedOnIssueNavigator()
    {
        login("ted", "ted");
        displayAllIssues();
        assertTextPresent("<a href=\"/jira/browse/HSP-3\">HSP-3</a>");
        assertNoLinksToHSP1();
        assertTextNotPresent("a href=\"" + jiraContextPath + "/browse/HSP-1\" class=\"parentIssue\" title=\"A parent bug assigned to bill\">HSP-1</a>");
    }

    // check Manage Attachments page
    public void testParentIssueShouldNotBeLinkedOnManageAttachmentsPage()
    {
        login("ted", "ted");
        navigation.issue().attachments("HSP-3").manage();
        assertTextPresent(unlinkedTextForHSP1());
        assertNoLinksToHSP1();
    }

    public void testParentIssueShouldNotBeLinkedOnViewIssuePage()
    {
        // check that the HSP-3 is not linked to HSP-1
        login("ted", "ted");
        gotoIssue("HSP-3");
        assertTextPresent(unlinkedTextForHSP1());
        assertNoLinksToHSP1();
    }


    // goto navigator and check that HSP-3 is not linked to HSP-1
    public void testParentIssueShouldBeLinkedOnIssueNavigator()
    {
        login("bill", "bill");
        displayAllIssues();
        assertTextPresent("a href=\"" + jiraContextPath + "/browse/HSP-1\" class=\"parentIssue\" title=\"A parent bug assigned to bill\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());
    }

    // check Manage Attachments page
    public void testParentIssueShouldBeLinkedOnManageAttachmentsPage()
    {
        login("bill", "bill");
        navigation.issue().attachments("HSP-2").manage();
        assertTextPresent(linkForHSP1());
        assertTextNotPresent(unlinkedTextForHSP1());
    }

    // check that the HSP-2 is linked to HSP-1
    public void testParentIssueShouldBeLinkedOnViewIssuePage()
    {
        login("bill", "bill");
        gotoIssue("HSP-2");
        assertTextPresent(linkForHSP1());
        assertTextNotPresent(unlinkedTextForHSP1());
    }

    private void assertNoLinksToHSP1()
    {
        assertTextNotPresent("<a id=\"parent_issue_summary\" href=\"" + jiraContextPath + "/browse/HSP-1\">");
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\">");
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" class=\"parentIssue\" title=\"A parent bug assigned to bill\">HSP-1</a>");
    }

    public void disabledTestParentIssueShouldBeLinked()
    {
        // goto Project RoadMap
        gotoProjectBrowse("HSP");
        clickLinkWithText("Road Map");
        assertTextPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());

        // goto Version 1
        gotoProjectBrowse("HSP");
        clickLinkWithText("Versions");
        clickLink("version_10000");
        assertTextPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());

        // goto Component 1
        gotoProjectBrowse("HSP");
        clickLinkWithText("Components");
        clickLink("component_10000");
        assertTextPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());

        // got Version Workload Report
        gotoProjectBrowse("HSP");
        clickLinkWithText("Version Workload Report");
        selectOption("versionId", "- New Version 1");
        submit("Next");
        assertTextPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());

        // goto to time tracking report
        gotoProjectBrowse("HSP");
        clickLinkWithText("Time Tracking Report");
        selectOption("versionId", "- New Version 1");
        submit("Next");
        assertTextPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\">HSP-1</a>");
        assertTextNotPresent(unlinkedTextForHSP1());
    }

    private void disabledTestParentIssueShouldNotBeLinked()
    {
        // goto Project RoadMap
        gotoPage("browse/HSP#selectedTab=com.atlassian.jira.jira-projects-plugin%3Aroadmap-panel");
        assertTextPresent("<span class=\"smallgrey\">HSP-1</span>");
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");

        // goto Version 1
        gotoPage("browse/HSP#selectedTab=com.atlassian.jira.jira-projects-plugin%3Aversions-panel");
        clickLink("version_10000");
        assertTextPresent(unlinkedTextForHSP1());
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");

        // goto Component 1
        gotoPage("browse/HSP#selectedTab=com.atlassian.jira.jira-projects-plugin%3Acomponents-panel");
        clickLink("component_10000");
        assertTextPresent(unlinkedTextForHSP1());
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");

        // got Version Workload Report
        gotoProjectBrowse("HSP");
        clickLinkWithText("Version Workload Report");
        selectOption("versionId", "- New Version 1");
        submit("Next");
        assertTextPresent(unlinkedTextForHSP1());
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");

        // goto to time tracking report
        gotoProjectBrowse("HSP");
        clickLinkWithText("Time Tracking Report");
        selectOption("versionId", "- New Version 1");
        submit("Next");
        assertTextPresent(unlinkedTextForHSP1());
        assertTextNotPresent("<a href=\"" + jiraContextPath + "/browse/HSP-1\" style=\"text-decoration: none;\" title=\"A parent bug assigned to bill\">HSP-1</a>");
    }

    private String linkForHSP1()
    {
        return "<a title=\"A parent bug assigned to bill\" id=\"parent_issue_summary\" href=\"" + jiraContextPath + "/browse/HSP-1\">";
    }

    private String unlinkedTextForHSP1()
    {
        return "<li>HSP-1</li>";
    }
}
