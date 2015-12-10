package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.TextCell;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestLinkIssue extends JIRAWebTest
{
    private static final String DELETE_LINK_TEXT = "Delete this link";

    public TestLinkIssue(String name)
    {
        super(name);
    }

    public void tearDown()
    {
        administration.issueLinking().disable();
        super.tearDown();
    }

    public void testLinkIssue()
    {
        administration.restoreBlankInstance();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        administration.issueLinking().enable();

        // We want this active so that we can make certain that the subtask links do not appear on the user-defined
        // link screens and dropdowns JRA-10700.
        administration.subtasks().enable();

        assertFalse(administration.issueLinking().exists("jira_subtask_link"));

        if (administration.issueLinking().exists("Duplicate"))
        {
            administration.issueLinking().delete("Duplicate");
        }
        if (administration.issueLinking().exists("Cloners"))
        {
            administration.issueLinking().delete("Cloners");
        }
        if (administration.issueLinking().exists("related"))
        {
            administration.issueLinking().delete("related");
        }

        administration.issueLinking().disable();

        final String issueKey1 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");
        final String issueKey2 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");

        availabilityOfIssueLinkLink(issueKey1);
        linkingIssue(issueKey1, issueKey2);

        // Make certain that the subtask links do not appear on the user-defined list in the select box JRA-10700.
        assertSubTaskLinkNotPresentOn(issueKey1);

        duplicateIssueLinkTypeNameError();
        notFoundNotDoubleescapedIssueLinkError();
        issueOperationForCloningWithIssueLinking(issueKey1);
        cancelLinkDeletion();
        confirmLinkDeletion();
        navigation.issue().deleteIssue(issueKey1);
        navigation.issue().deleteIssue(issueKey2);
        administration.issueLinking().enable();
        administration.issueLinking().delete("Duplicate");
        administration.issueLinking().delete("Cloners");
        administration.issueLinking().delete("related");
        administration.issueLinking().disable();
    }

    /**
     * Tests if the 'Link Issue' link is not available if 'Issue Linking' is deactivated.
     * Note: The 'Link Issue' link is not present even when there are no saved link types, due to remote issue linking.
     */
    public void availabilityOfIssueLinkLink(String issueKey)
    {
        log("Link Issue: test for availability of 'link issue' link");

        // Test link is not present when issue linking disabled
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkNotPresent("link-issue");
        administration.issueLinking().enable();

        // Test link is present when issue linking enabled and no link types defined
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkPresent("link-issue");
        administration.issueLinking().disable();

        // Test link is present when issue linking enabled and there is a link type defined
        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("Duplicate", "is a duplicate of", "duplicates");
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkPresent("link-issue");
        administration.issueLinking().disable();
    }

    /**
     * Tests if HSP-2 can be linked to HSP-1
     */
    public void linkingIssue(String issueKey1, String issueKey2)
    {
        log("Link Issue: test linking an issue");
        administration.issueLinking().enable();
        navigation.issue().gotoIssue(issueKey1);
        linkIssueWithComment("HSP-1", "duplicates", "HSP-2", null, null);
        text.assertTextPresent(locator.page(), issueKey2);
        administration.issueLinking().disable();
    }

    public void assertSubTaskLinkNotPresentOn(String issueKey)
    {
        log("Link Issue: test that the subtask links are not present when linking an issue");
        administration.issueLinking().enable();
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("link-issue");

        tester.assertRadioOptionValueNotPresent("linkDesc", "jira_subtask_outward");
        tester.assertRadioOptionValueNotPresent("linkDesc", "jira_subtask_inward");

        tester.assertRadioOptionLabelNotPresent("linkDesc", "jira_subtask_outward");
        tester.assertRadioOptionLabelNotPresent("linkDesc", "jira_subtask_inward");

        administration.issueLinking().disable();
    }

    /**
     * Tests error is handled if 2 links are given the same name
     */
    public void duplicateIssueLinkTypeNameError()
    {
        log("Link Issue: test attempting to add duplicate link type name");
        administration.issueLinking().enable();
        tester.setFormElement("name", "Duplicate");
        tester.setFormElement("outward", "is a duplicate of");
        tester.setFormElement("inward", "duplicates");
        tester.submit();
        text.assertTextPresent(locator.page(), "Another link type with that name already exists");
        administration.issueLinking().disable();
    }

    /**
     * Tests error message is not double escaped
     */
    public void notFoundNotDoubleescapedIssueLinkError()
    {
        log("Link Issue: test attempting to link an invalid issue using an HTML string");

        administration.issueLinking().enable();
        linkIssueWithComment("HSP-1", "duplicates", "<b>BOLD</b>", null, null, "The issue key &quot;&lt;b&gt;BOLD&lt;/b&gt;&quot; does not exist.");
        administration.issueLinking().disable();
    }

    /**
     * Tests if a cloned issue is automatically linked to its parent if a 'Cloners' link is created
     */
    public void issueOperationForCloningWithIssueLinking(String issueKey)
    {
        log("Issue Operation: Test the ability to automatically link the clone to its parent");

        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("Cloners", "clones", "is cloned by");

        navigation.issue().gotoIssue(issueKey);
        tester.clickLinkWithText("Clone");
        tester.setFormElement("summary", "Second Clone of Test 1");
        tester.submit();

        text.assertTextPresent(locator.page(), "Second Clone of Test 1");
        text.assertTextPresent(locator.page(), "Issue Links");

        tester.clickLink("delete-issue");
        tester.submit("Delete");
        administration.issueLinking().disable();
    }

    /**
     * Tests that cancelling a deletion confirmation will redirect to the manage links screen
     */
    public void cancelLinkDeletion()
    {
        log("Testing cancel option of delete link wizard");
        final String issueKey1 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test issue 1");
        final String issueKey2 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test issue 2");
        administration.issueLinking().enable();
        gotoLinkDeleteConfirmationScreen(issueKey1, issueKey2);
        tester.clickLink("issue-link-delete-cancel");
        text.assertTextPresent(locator.page(), issueKey1);
        text.assertTextPresent(locator.page(), "test issue 1");
        navigation.issue().deleteIssue(issueKey1);
        navigation.issue().deleteIssue(issueKey2);
        administration.issueLinking().disable();
    }

    /**
     * Check that the delete issue link is only visible to users with the 'link issue' permission.
     */
    public void testVisibilityOfDeleteIssueLink() throws SAXException
    {
        administration.restoreBlankInstance();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("Duplicate", "is a duplicate of", "duplicates");

        final String issueKey1 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");
        final String issueKey2 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");

        linkingIssue(issueKey1, issueKey2);
        administration.issueLinking().enable();

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issue().gotoIssue(issueKey1);
        tester.assertLinkPresent("delete-link_internal-10001_10000");

        // make sure fred does not have permission to delete links
        administration.permissionSchemes().defaultScheme().removePermission(LINK_ISSUE, Groups.USERS);
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.issue().gotoIssue(issueKey1);
        tester.assertLinkNotPresent("delete-link_internal-10001_10000");
        tester.gotoPage("/secure/DeleteLink.jspa?id=10000&sourceId=10001&linkType=10000&atl_token=" + page.getXsrfToken());
        text.assertTextPresent(locator.page(), "You do not have permission to delete links in this project.");

        //login as admin and grant link issue permission for fred and check he can now see the link
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(LINK_ISSUE, Groups.USERS);
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.issue().gotoIssue(issueKey1);
        tester.assertLinkPresent("delete-link_internal-10001_10000");

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.permissionSchemes().defaultScheme().removePermission(LINK_ISSUE, Groups.USERS);
        navigation.issue().deleteIssue(issueKey1);
        navigation.issue().deleteIssue(issueKey2);
        administration.issueLinking().disable();
    }

    /**
     * Tests the confirmation of link delete will remove the link between two issues
     */
    public void confirmLinkDeletion()
    {
        log("Testing confirm option of delete link wizard");
        final String issueKey1 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");
        final String issueKey2 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "test 1");

        administration.issueLinking().enable();
        gotoLinkDeleteConfirmationScreen(issueKey1, issueKey2);
        tester.submit("Delete");
        text.assertTextPresent(locator.page(), issueKey1);
        text.assertTextNotPresent(locator.page(), "relates to");
        navigation.issue().deleteIssue(issueKey1);
        navigation.issue().deleteIssue(issueKey2);
        administration.issueLinking().disable();
    }

    /**
     * Tests comment visibility when linking an issue
     */
    public void testLinkComment()
    {
        administration.restoreData("TestBlankInstancePlusAFewUsers.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("duplicates", "duplicates", "is duplicated by");
        administration.issueLinking().addIssueLink("resembles", "resembles", "is resembled by");
        administration.issueLinking().addIssueLink("jokingly duplicates", "jokingly duplicates", "is jokingly duplicated by");
        administration.issueLinking().addIssueLink("similar", "similar to", "is similar to");
        administration.issueLinking().addIssueLink("duplicates", "duplicates", "is duplicated by");

        final String issueKey1 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "1");
        final String issueKey2 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "2");
        final String issueKey3 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "3");
        final String issueKey4 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "4");
        final String issueKey5 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "5");
        final String issueKey6 = navigation.issue().createIssue(PROJECT_HOMOSAP, "Test Bug", "Bug" + "6");

        final String commentJiraUsers1 = "comment visible to jira-users";
        final String commentJiraUsers2 = "comment visible to All Users";
        final String commentAdmins1 = "comment visible to jira-administrators";
        final String commentAdmins2 = "comment visible to Administrators";
        final String commentDev1 = "comment visible to jira-developers";
        final String commentDev2 = "comment visible to Developers";

        final List<String> jiraUserComments = ImmutableList.of(commentJiraUsers1, commentJiraUsers2);
        final List<String> jiraAdminComments = ImmutableList.of(commentAdmins1, commentAdmins2);
        final List<String> jiraDevComments = ImmutableList.of(commentDev1, commentDev2);

        administration.issueLinking().enable();

        linkIssueWithComment(issueKey1, "duplicates", issueKey2, commentJiraUsers1, "group:jira-users");
        linkIssueWithComment(issueKey1, "resembles", issueKey3, commentAdmins1, "group:jira-administrators");
        linkIssueWithComment(issueKey1, "duplicates", issueKey4, commentDev1, "group:jira-developers");
        linkIssueWithComment(issueKey1, "duplicates", issueKey5, commentJiraUsers2, "role:10000");
        linkIssueWithComment(issueKey1, "duplicates", issueKey6, commentDev2, "role:10001");
        linkIssueWithComment(issueKey1, "duplicates", issueKey6, commentAdmins2, "role:10002");

        //verify fred sees only user comments
        assertions.comments(jiraUserComments).areVisibleTo(FRED_USERNAME, issueKey1);
        assertions.comments(Iterables.concat(jiraAdminComments, jiraDevComments)).areNotVisibleTo(FRED_USERNAME, issueKey1);

        //verify devman shouldn't see admin comments
        assertions.comments(Iterables.concat(jiraUserComments, jiraDevComments)).areVisibleTo("devman", issueKey1);
        assertions.comments(jiraAdminComments).areNotVisibleTo("devman", issueKey1);

        //verify adminonly should not see jiradev comments
        assertions.comments(Iterables.concat(jiraUserComments, jiraAdminComments)).areVisibleTo("onlyadmin", issueKey1);
        assertions.comments(jiraDevComments).areNotVisibleTo("onlyadmin", issueKey1);

        //verify admin sees all comments
        assertions.comments(Iterables.concat(jiraUserComments, jiraAdminComments, jiraDevComments)).areVisibleTo(ADMIN_USERNAME, issueKey1);
    }

    // JRA-14893: under certain situations, do not render an HTML link for an Issue Link change item
    public void testChangeHistoryShowsHyperlinkCorrectly()
    {
        // data contains:
        // HSP using custom Security Scheme
        // MKY only visible to developers
        // HSP-1 linked to HSP-2 (restricted by Security Level) and MKY-1 (restricted by Project)
        administration.restoreData("TestLinkIssueChangeHistoryShowsHyperlink.xml");
        navigation.logout();
        navigation.login(FRED_USERNAME);

        // check for Project permission of the linked issue
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        text.assertTextPresent(locator.page(), "MKY-1");
        tester.assertLinkNotPresentWithText("MKY-1");

        // check for Issue Security permission
        text.assertTextPresent(locator.page(), "HSP-2");
        tester.assertLinkNotPresentWithText("HSP-2");

        // remove Project permission restriction
        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        Long projectId = backdoor.project().getProjectId("MKY");
        tester.gotoPage("/secure/project/SelectProjectPermissionScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeIds", "Default Permission Scheme");
        tester.submit("Associate");

        // remove Security Level on issue
        navigation.issue().gotoIssue("HSP-2");
        tester.clickLink("edit-issue");
        tester.selectOption("security", "None");
        tester.submit("Update");

        // check that the links are now visible
        navigation.logout();
        navigation.login(FRED_USERNAME);
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        text.assertTextPresent(locator.page(), "MKY-1");
        tester.assertLinkPresentWithText("MKY-1");
        text.assertTextPresent(locator.page(), "HSP-2");
        tester.assertLinkPresentWithText("HSP-2");

        // delete one of the issues and check that a link is no longer visible
        navigation.logout();
        navigation.login(ADMIN_USERNAME);
        navigation.issue().deleteIssue("HSP-2");
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        text.assertTextPresent(locator.page(), "HSP-2");
        tester.assertLinkNotPresentWithText("HSP-2");
    }

    private void gotoLinkDeleteConfirmationScreen(String issueKey1, String issueKey2)
    {
        navigation.issue().gotoIssue(issueKey1);
        final String linkTypeId = createIssueLinkType("related", "is related to", "relates to");
        final String destId = navigation.issue().getId(issueKey2);
        linkIssueWithComment(issueKey1, "relates to", issueKey2, null, null);
        administration.issueLinking().enable();
        navigation.issue().gotoIssue(issueKey1);
        tester.clickLink("delete-link_internal-" + destId + "_" + linkTypeId);
        text.assertTextSequence(locator.page(), "Delete Link: ", issueKey1 + " relates to " + issueKey2);
    }
}
