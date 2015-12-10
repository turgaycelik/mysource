package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestSubTaskQuickCreation extends FuncTestCase
{
    private static final String ISSUE_PARENT = "HSP-6";
    private static final String SUB_TASKS_TABLE_ID = "issuetable";
    public static final String JIRA_LOZENGE_DARK_FEATURE = "jira.issue.status.lozenge";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestTimeTrackingAggregates.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testSubTaskDisplayOptions()
    {
        subTaskDisplayOptions(false);
    }

    public void testSubTaskDisplayOptionsWithStatusLozengesEnabled() throws Exception
    {
        final boolean isLozengeEnabled = backdoor.darkFeatures().isGlobalEnabled(JIRA_LOZENGE_DARK_FEATURE);
        try
        {
            backdoor.darkFeatures().enableForSite(JIRA_LOZENGE_DARK_FEATURE);
            subTaskDisplayOptions(true);
        }
        finally
        {
            if (!isLozengeEnabled)
            {
                backdoor.darkFeatures().disableForSite(JIRA_LOZENGE_DARK_FEATURE);
            }
        }
    }

    private void subTaskDisplayOptions(final boolean statusLozengesEnabled)
    {
        // HSP-7 and HSP-8 are children of HSP-6
        navigation.issue().resolveIssue("HSP-7", "Fixed", "");

        navigation.issue().gotoIssue(ISSUE_PARENT);

        // should be in "Show All" view
        if (statusLozengesEnabled)
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 1", "Resolved", "sub 2", "Open");
        }
        else
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID).getHTML(), "sub 1", "<img", "Resolved", "sub 2", "<img", "Open");
        }

        // click "Show Open"
        tester.clickLink("subtasks-show-open");

        // now only open sub tasks are shown
        if (statusLozengesEnabled)
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 2", "Open");
            text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID), "sub 1");
            text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID), "Resolved");
        }
        else
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID).getHTML(), "sub 2", "<img", "Open");
            text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID).getHTML(), "sub 1");
            text.assertTextNotPresent(locator.table(SUB_TASKS_TABLE_ID).getHTML(), "Resolved");
        }


        // click "Show All"
        tester.clickLink("subtasks-show-all");

        // all sub tasks are visible again
        if (statusLozengesEnabled)
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID), "sub 1", "Resolved", "sub 2", "Open");
        }
        else
        {
            text.assertTextSequence(locator.table(SUB_TASKS_TABLE_ID).getHTML(), "sub 1", "<img", "Resolved", "sub 2", "<img", "Open");
        }
    }

    public void testCreateSubTaskNotVisibleWithoutPermission()
    {
        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkPresent("stqc_show");

        // now change permissions so that the current user doesn't have permission to create sub-tasks.
        administration.permissionSchemes().defaultScheme().removePermission(11, "jira-users");

        navigation.issue().viewIssue(ISSUE_PARENT);

        tester.assertLinkNotPresent("stqc_show");
    }
}
