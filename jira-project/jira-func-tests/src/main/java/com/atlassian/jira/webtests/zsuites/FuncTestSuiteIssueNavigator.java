package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationIssueNavigator;
import com.atlassian.jira.webtests.ztests.fields.TestResolutionDateField;
import com.atlassian.jira.webtests.ztests.issue.TestNextPreviousPager;
import com.atlassian.jira.webtests.ztests.issue.TestSearchXmlView;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import com.atlassian.jira.webtests.ztests.navigator.TestCustomFieldsVisibilityOnIssueTable;
import com.atlassian.jira.webtests.ztests.navigator.TestCustomFieldsVisibilityWhenExportingAllColumnsToExcel;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigator;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorColumnLinks;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorExcelView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorFullContentView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorPrintableView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorRedirects;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorRssView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorWordView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorXmlView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorXmlViewTimeTracking;
import com.atlassian.jira.webtests.ztests.navigator.TestNavigationBarWebFragment;
import com.atlassian.jira.webtests.ztests.navigator.TestSearchXmlViewErrors;
import com.atlassian.jira.webtests.ztests.user.TestAutoWatches;
import com.atlassian.jira.webtests.ztests.user.TestUserNavigationBarWebFragment;
import com.atlassian.jira.webtests.ztests.user.TestUserVotes;
import com.atlassian.jira.webtests.ztests.user.TestUserWatches;

import junit.framework.Test;

/**
 * A suite of tests related to the issue navigator
 *
 * @since v4.0
 */
public class FuncTestSuiteIssueNavigator extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteIssueNavigator();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteIssueNavigator()
    {
        addTest(TestNavigationBarWebFragment.class);
        addTest(TestUserNavigationBarWebFragment.class);
        addTest(TestIssueNavigator.class);
        addTest(TestIssueNavigatorRedirects.class);
        addTest(TestIssueNavigatorColumnLinks.class);
        addTest(TestIssueNavigatorXmlView.class);
        addTest(TestIssueNavigatorPrintableView.class);
        addTest(TestIssueNavigatorXmlViewTimeTracking.class);
        addTest(TestIssueNavigatorExcelView.class);
        addTest(TestIssueNavigatorRssView.class);
        addTest(TestIssueNavigatorFullContentView.class);
        addTest(TestIssueNavigatorWordView.class);
        addTest(TestBulkOperationIssueNavigator.class);
        addTest(TestResolutionDateField.class);
        addTest(TestNextPreviousPager.class);
        addTest(TestSearchXmlView.class);
        addTest(TestSearchXmlViewErrors.class);
        addTest(TestAutoWatches.class);
        addTest(TestUserWatches.class);
        addTest(TestUserVotes.class);
        addTest(TestReplacedLocalVelocityMacros.class);
        addTest(TestCustomFieldsVisibilityWhenExportingAllColumnsToExcel.class);
        addTest(TestCustomFieldsVisibilityOnIssueTable.class);
    }
}
