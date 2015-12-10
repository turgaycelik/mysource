package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestUserVotes extends FuncTestCase {
    @Override
    protected void setUpTest() {
        super.setUpTest();
        administration.restoreData("TestUserWatches.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testUnresolvedFilter() throws Exception {

        // should default to All issues being shown, meaning that Unresolved will be the link
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("voted");

        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-3");

        // switch to Unresolved
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("voted_open");
        tester.assertElementNotPresent("issuetable");


        // reopen & unvote that one issue
        navigation.issue().reopenIssue("HSP-3");
        navigation.issue().unvoteIssue("HSP-3");
    }

}
