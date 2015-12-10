package com.atlassian.jira.webtests.ztests.issue.security;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.DoesNotContainIssueKeyCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.permission.ProjectPermissions;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests that the issue security level is only checked when the permission being tested is {@link ProjectPermissions.BROWSE_PROJECTS}
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.SECURITY, Category.ISSUES })
public class TestIssueSecurityLevelOnlyCheckedForBrowseProjectPermission extends FuncTestCase
{
    private static final String USERNAME = "test";
    private static final String ISSUE_KEY = "TEST-1";

    @Override
    protected void setUpTest()
    {
        // Scenario:
        // An single issue TEST-1 with a security level that allows visibility of the issue only to admins and current assignee. TEST-1 is unassigned.
        // An user, "test" that is a developer of TEST but not an admin
        this.administration.restoreData("TestIssueSecurityLevelOnlyCheckedForBrowseProjectPermission.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, USERNAME);
    }

    public void testUserCanBeAssignedToIssueEventThoughSheDoesNotBelongToTheGroupsOfTheIssuesSecurityLevel()
    {
        assertUserCanNotSeeIssueSinceSheIsNotAdminNorAssignee(USERNAME, ISSUE_KEY);

        // We must be able to assign the issue to the user, as the issue security level should only be checked when asking for the BROWSE_PROJECTS permission
        assignIssueToUser(ISSUE_KEY, USERNAME);

        assertUserCanSeeIssueSinceSheIsTheAssignee(USERNAME, ISSUE_KEY);
    }

    private void assignIssueToUser(final String issueKey, final String username)
    {
        this.navigation.login("admin");
        this.navigation.issue().assignIssue(issueKey, "", username);
    }

    private void assertUserCanNotSeeIssueSinceSheIsNotAdminNorAssignee(final String username, final String issueKey)
    {
        this.navigation.login(username);
        navigation.issueNavigator().createSearch("");
        List<SearchResultsCondition> condition = new ArrayList<SearchResultsCondition>();
        condition.add(new DoesNotContainIssueKeyCondition(text, issueKey));
        condition.add(new NumberOfIssuesCondition(text, 0));
        assertions.getIssueNavigatorAssertions().assertSearchResults(condition);
    }

    private void assertUserCanSeeIssueSinceSheIsTheAssignee(final String username, final String issueKey)
    {
        this.navigation.login(username);
        navigation.issueNavigator().createSearch("");
        List<SearchResultsCondition> condition = new ArrayList<SearchResultsCondition>();
        condition.add(new ContainsIssueKeysCondition(text, issueKey));
        condition.add(new NumberOfIssuesCondition(text, 1));
        assertions.getIssueNavigatorAssertions().assertSearchResults(condition);
    }
}
