package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.AbstractJqlFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

/**
 * Responsible for verifying that references to deleted users that have been left in
 * assignee and reporter fields are assigned a new username in the user key store so
 * that they can be searched for.
 *
 * @since v6.0.6 (as TestUpgradeTask6104)
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6137 extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreDataWithBuildNumber("TestUpgradeTask6137.xml", 6103);
    }

    public void testSearchForDeletedUsers()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        assertSearchWithResults("assignee = assignee", "HSP-1");
        assertSearchWithResults("reporter = reporter", "HSP-1");
        assertSearchWithResults("assignee = newassignee", "HSP-2");
        assertSearchWithResults("reporter = \"newreporter#1\"", "HSP-2");

        // History searches that work
        assertSearchWithResults("assignee was assignee", "HSP-1");
        assertSearchWithResults("reporter was reporter", "HSP-1");

        // Current value searches that are accepted even though the value is never used
        assertSearchWithResults("reporter = newreporter");
        assertSearchWithResults("assignee = changer");
        assertSearchWithResults("assignee = \"changer#1\"");
        assertSearchWithWarning("assignee = \"changer#2\"", "The value 'changer#2' does not exist for the field 'assignee'.");
        assertSearchWithWarning("assignee = \"changer#3\"", "The value 'changer#3' does not exist for the field 'assignee'.");

        // History searches that probably should work, but currently do not
        assertSearchWithError("assignee changed by changer", "The user 'changer' does not exist and cannot be used in the 'by' predicate.");
        assertSearchWithError("assignee was newassignee", "The value 'newassignee' does not exist for the field 'assignee'.");
        assertSearchWithError("reporter was newreporter", "The value 'newreporter' does not exist for the field 'reporter'.");
        assertSearchWithError("reporter was \"newreporter#1\"", "The value 'newreporter#1' does not exist for the field 'reporter'.");
    }
}

