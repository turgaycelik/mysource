package com.atlassian.jira.webtests.ztests.comment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * NOTE: Adding a comment with visibility is tested in {@link TestAddComment}
 */
@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestCommentVisibility extends FuncTestCase
{
    private static final String ANYONE_GROUP_VALUE = "";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestCommentVisibility.xml");
    }

    public void testCommentVisibilitySwitchOnViewIssue()
    {
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.GROUPS_PROJECT_ROLES);

        // goto issue make sure both projects and roles appear
        navigation.issue().viewIssue("HSP-1");

        // Run through the selects so we know they are all there
        tester.assertFormElementPresent("commentLevel");
        tester.assertOptionsEqual("commentLevel", new String[]
                       {
                               "All Users", "Administrators", "Developers", "Users",
                               "jira-administrators", "jira-developers", "jira-users"
                       });

        // Turn on Project Roles only
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);

        // goto issue make sure only roles appear
        navigation.issue().viewIssue("HSP-1");

        // Make sure that the group select options are not present, only project roles should be present
        tester.assertOptionsEqual("commentLevel", new String[] { "All Users", "Administrators", "Developers", "Users" });
    }

    public void testAnonymousCommenterAllowed()
    {
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.GROUPS_PROJECT_ROLES);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(BROWSE, ANYONE_GROUP_VALUE);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(COMMENT_ISSUE, ANYONE_GROUP_VALUE);

        // now try with an anonymous user
        navigation.logout();

        navigation.issue().viewIssue("HSP-1");
        tester.assertFormElementPresent("comment");
        tester.assertFormElementNotPresent("commentLevel");
    }

    public void testLogWorkReflectsCommentVisibilitySwitch()
    {
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);

        // Turn on Project Roles + Groups
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.GROUPS_PROJECT_ROLES);

        // goto issue make sure both projects and roles appear
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("log-work");

        tester.assertFormElementPresent("commentLevel");
        tester.assertOptionsEqual("commentLevel", new String[]
                {
                        "All Users", "Administrators", "Developers", "Users",
                        "jira-administrators", "jira-developers", "jira-users"
                });

        // Turn on Project Roles only
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);

        // goto issue make sure only roles appear
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("log-work");

        // Make sure that the group select options are not present, only project roles should be present
        tester.assertOptionsEqual("commentLevel", new String[] { "All Users", "Administrators", "Developers", "Users" });
    }

    /**
     * Checks that comments searches produce the correct issues in search
     * results when the comment visibility is not restricted to roles or groups.
     */
    public void testCommentSearchNoLevels()
    {
        navigation.issue().addComment("HSP-1", "comment with animal kangaroo");
        navigation.issue().addComment("MKY-1", "comment with animal tasmanian devil");

        // do a few search combinations for each of two users whose username
        // is their password
        final String[] accounts = new String[] { FRED_USERNAME, ADMIN_USERNAME };
        for (String usernameAndPassword : accounts)
        {
            navigation.login(usernameAndPassword, usernameAndPassword);

            assertCommentSearchResults("visible", null,
                    new String[] { },
                    new String[] { "HSP-1", "MKY-1" });
            assertCommentSearchResults("animal", null,
                    new String[] { "HSP-1", "MKY-1" },
                    new String[] { });
            assertCommentSearchResults("kangaroo", null,
                    new String[] { "HSP-1" },
                    new String[] { "MKY-1" });
        }
    }

    /**
     * Checks the search results for comment searches where the comment visibility is restricted to role levels.
     */
    public void testCommentSearchResultsWithLevels()
    {
        // as Admin, make role-restricted comments:
        navigation.issue().addComment("HSP-1", "comment visible to haxors", "Developers");
        navigation.issue().addComment("MKY-1", "comment visible to haxors", "Developers");

        final String[] ALL_PROJECTS = null;
        final String[] PROJECT_MONKEY = new String[] { "monkey" };
        final String[] PROJECT_HOMOSAPIEN = new String[] { "homosapien" };
        final String[] PROJECTS_BOTH = new String[] { "monkey", "homosapien" };

        final String[] ISSUE_MONKEY = new String[] { "MKY-1" };
        final String[] ISSUE_HOMOSAPIEN = new String[] { "HSP-1" };
        final String[] ISSUES_BOTH = new String[] { "MKY-1", "HSP-1" };
        final String[] ISSUES_NONE = new String[] { };

        // fred can only see developer role stuff in monkey
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        assertCommentSearchResults("visible", ALL_PROJECTS,
                ISSUE_MONKEY,
                ISSUE_HOMOSAPIEN);
        assertCommentSearchResults("visible", PROJECT_MONKEY,
                ISSUE_MONKEY,
                ISSUE_HOMOSAPIEN);
        assertCommentSearchResults("visible", PROJECT_HOMOSAPIEN,
                ISSUES_NONE,
                ISSUES_BOTH);
        assertCommentSearchResults("visible", PROJECTS_BOTH,
                ISSUE_MONKEY,
                ISSUE_HOMOSAPIEN);
        assertCommentSearchResults("haxors", PROJECTS_BOTH,
                ISSUE_MONKEY,
                ISSUE_HOMOSAPIEN);

        // admin expects to see all
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertCommentSearchResults("visible", ALL_PROJECTS,
                ISSUES_BOTH,
                ISSUES_NONE);
        assertCommentSearchResults("visible", PROJECT_MONKEY,
                ISSUE_MONKEY,
                ISSUE_HOMOSAPIEN);
        assertCommentSearchResults("visible", PROJECT_HOMOSAPIEN,
                ISSUE_HOMOSAPIEN,
                ISSUE_MONKEY);
        assertCommentSearchResults("visible", PROJECTS_BOTH,
                ISSUES_BOTH,
                ISSUES_NONE);
        assertCommentSearchResults("haxors", PROJECTS_BOTH,
                ISSUES_BOTH,
                ISSUES_NONE);
    }

    /**
     * Asserts that the given comment body search, when searching across the given
     * named projects, gives a search results issue navigator view that contains
     * each of the expectedPresent and none of the expectedAbsent strings. Use these
     * to check which issue keys are expected in the results.
     *
     * @param commentBody some part of the comment to search for
     * @param projects an array of project names to search or null for all
     * @param expectedPresent each string will be asserted to be present in results
     * @param expectedAbsent each string will be asserted to be absent in results
     */
    private void assertCommentSearchResults(final String commentBody, final String[] projects,
            final String[] expectedPresent, final String[] expectedAbsent)
    {
        List<String> jql = new ArrayList<String>();

        if (projects != null && projects.length != 0) {
            jql.add("project in (" + StringUtils.join(projects, ", ") + ")");
        }

        jql.add("comment ~ " + commentBody);

        String jqlStr = StringUtils.join(jql.toArray(), " AND ");

        System.out.println("Searching for " + jqlStr);

        navigation.issueNavigator().createSearch(jqlStr);

        for (String anExpectedPresent : expectedPresent)
        {
            text.assertTextPresent(locator.page(), anExpectedPresent);
        }
        for (String anExpectedAbsent : expectedAbsent)
        {
            text.assertTextNotPresent(locator.page(), anExpectedAbsent);
        }
    }
}
