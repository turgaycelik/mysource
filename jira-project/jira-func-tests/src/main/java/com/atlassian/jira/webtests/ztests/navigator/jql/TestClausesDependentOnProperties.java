package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests specific clauses ability to be used when their system properties have been disabled.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestClausesDependentOnProperties extends FuncTestCase
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreBlankInstance();
        }
    }

    public void testUserClausesSearchOnFullName() throws Exception
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", "UserCF");

        issueTableAssertions.assertSearchWithResults("assignee = '" + FRED_FULLNAME + "'");
        issueTableAssertions.assertSearchWithResults("assignee = fred");
        issueTableAssertions.assertSearchWithResults("reporter = '" + FRED_FULLNAME + "'");
        issueTableAssertions.assertSearchWithResults("reporter = fred");
        issueTableAssertions.assertSearchWithResults("UserCF = '" + FRED_FULLNAME + "'");
        issueTableAssertions.assertSearchWithResults("UserCF = fred");
    }

    public void testTimeTrackingClausesInvalid() throws Exception
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        issueTableAssertions.assertSearchWithResults("originalEstimate = 999");
        issueTableAssertions.assertSearchWithResults("timeOriginalEstimate = 999");
        issueTableAssertions.assertSearchWithResults("remainingEstimate = 999");
        issueTableAssertions.assertSearchWithResults("timeEstimate = 999");
        issueTableAssertions.assertSearchWithResults("timeSpent = 999");
        issueTableAssertions.assertSearchWithResults("workRatio = 999");

        administration.timeTracking().disable();

        issueTableAssertions.assertSearchWithError("originalEstimate = 999", "Field 'originalEstimate' does not exist or you do not have permission to view it.");
        issueTableAssertions.assertSearchWithError("timeOriginalEstimate = 999", "Field 'timeOriginalEstimate' does not exist or you do not have permission to view it.");
        issueTableAssertions.assertSearchWithError("remainingEstimate = 999", "Field 'remainingEstimate' does not exist or you do not have permission to view it.");
        issueTableAssertions.assertSearchWithError("timeEstimate = 999", "Field 'timeEstimate' does not exist or you do not have permission to view it.");
        issueTableAssertions.assertSearchWithError("timeSpent = 999", "Field 'timeSpent' does not exist or you do not have permission to view it.");
        issueTableAssertions.assertSearchWithError("workRatio = 999", "Field 'workRatio' does not exist or you do not have permission to view it.");
    }

    public void testVotingClausesInvalid() throws Exception
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        administration.generalConfiguration().enableVoting();

        issueTableAssertions.assertSearchWithResults("votes = 123");

        administration.generalConfiguration().disableVoting();

        issueTableAssertions.assertSearchWithError("votes = 123", "Field 'votes' does not exist or you do not have permission to view it.");
    }

    public void testSubTaskClausesInvalid() throws Exception
    {
        administration.subtasks().enable();

        issueTableAssertions.assertSearchWithError("parent = 'HSP-1'", "An issue with key 'HSP-1' does not exist for field 'parent'.");

        administration.subtasks().disable();

        issueTableAssertions.assertSearchWithError("parent = 'HSP-1'", "Field 'parent' does not exist or you do not have permission to view it.");
    }


}
