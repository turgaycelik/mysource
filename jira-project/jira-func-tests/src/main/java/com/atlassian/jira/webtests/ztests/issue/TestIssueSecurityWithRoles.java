package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests that issue security schemes with roles restrict unauthorized users from seeing issues
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.ROLES, Category.SECURITY })
public class TestIssueSecurityWithRoles extends JIRAWebTest
{
    private static final String HSP_ISSUE_NO_SECURITY = "HSP-1";
    private static final String HSP_ISSUE_DEV_ROLE_SECURITY = "HSP-2";
    private static final String HSP_ISSUE_ADMIN_ROLE_SECURITY = "HSP-3";
    private static final String MKY_ISSUE_NO_SECURITY = "MKY-1";
    private static final String MKY_ISSUE_DEV_ROLE_SECURITY = "MKY-2";
    private static final String ELE_ISSUE_NO_SECURITY = "ELE-1";
    private static final String ELE_ISSUE_DEV_ROLE_SECURITY = "ELE-2";

    public TestIssueSecurityWithRoles(String name) {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueSecurityWithRoles.xml");
    }

    /**
     * Tests that issue security schemes with roles restrict unauthorized users from seeing issues
     * in the issue navigator
     */
    public void testIssueSecurityWithRolesIssueNavigator() {
        // User with authorized role
        displayAllIssues();
        assertLinkPresentWithText(HSP_ISSUE_NO_SECURITY);
        assertLinkPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        gotoIssue(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertTextNotPresent("Permission Violation");

        //User without authorized role
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        displayAllIssues();
        assertLinkPresentWithText(HSP_ISSUE_NO_SECURITY);
        assertLinkNotPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        gotoIssue(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertTextPresent("Permission Violation");
    }

    /**
     * Tests that issue security schemes with roles restrict unauthorized users from seeing issues
     * across multiple projects with different but equivalent issue security schemes
     */
    public void testIssueSecurityWithRolesMultiProject() {
        // User with authorized role
        displayAllIssues();
        assertLinkPresentWithText(HSP_ISSUE_NO_SECURITY);
        assertLinkPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(MKY_ISSUE_NO_SECURITY);
        assertLinkPresentWithText(MKY_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(ELE_ISSUE_NO_SECURITY);
        assertLinkPresentWithText(ELE_ISSUE_DEV_ROLE_SECURITY);
        gotoIssue(ELE_ISSUE_DEV_ROLE_SECURITY);
        assertTextNotPresent("Permission Violation");

        //User without authorized role
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        displayAllIssues();
        assertLinkPresentWithText(HSP_ISSUE_NO_SECURITY);
        assertLinkNotPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(MKY_ISSUE_NO_SECURITY);
        assertLinkNotPresentWithText(MKY_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(ELE_ISSUE_NO_SECURITY);
        assertLinkNotPresentWithText(ELE_ISSUE_DEV_ROLE_SECURITY);
        gotoIssue(ELE_ISSUE_DEV_ROLE_SECURITY);
        assertTextPresent("Permission Violation");
    }

    /**
     * Tests that issue level security field on edit issue shows correct issue levels based on user's role
     * after cache has been reloaded
     */
    public void testIssueSecurityWithRolesEditIssueFields() {
        displayAllIssues();
        assertLinkPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(HSP_ISSUE_ADMIN_ROLE_SECURITY);
        gotoIssue(HSP_ISSUE_ADMIN_ROLE_SECURITY);
        clickLink("edit-issue");
        setWorkingForm("issue-edit");
        assertOptionsEqualIgnoreOrder("security", new String[]{"None", "My Security Level", "Administrators Security Level"});

        //Remove user from developer role
        removeUserFromProjectRole(ADMIN_USERNAME, "homosapien", "Developers");
        displayAllIssues();
        assertLinkNotPresentWithText(HSP_ISSUE_DEV_ROLE_SECURITY);
        assertLinkPresentWithText(HSP_ISSUE_ADMIN_ROLE_SECURITY);
        gotoIssue(HSP_ISSUE_ADMIN_ROLE_SECURITY);
        clickLink("edit-issue");
        setWorkingForm("issue-edit");
        assertOptionsEqualIgnoreOrder("security", new String[]{"None", "Administrators Security Level"});
    }

    /**
     * Tests that issue level security field on edit issue shows correct issue levels based on user's role
     * after cache has been reloaded when going directly to EditIssue
     */
    public void testIssueSecurityWithRolesEditIssueFieldsDirectly() {
        gotoPage("/secure/EditIssue!default.jspa?id=10020");
        setWorkingForm("issue-edit");
        assertOptionsEqualIgnoreOrder("security", new String[]{"None", "My Security Level", "Administrators Security Level"});
        //Remove user from developer role
        removeUserFromProjectRole(ADMIN_USERNAME, "homosapien", "Developers");
        gotoPage("/secure/EditIssue!default.jspa?id=10020");
        setWorkingForm("issue-edit");
        assertOptionsEqualIgnoreOrder("security", new String[]{"None", "Administrators Security Level"});
    }
}
