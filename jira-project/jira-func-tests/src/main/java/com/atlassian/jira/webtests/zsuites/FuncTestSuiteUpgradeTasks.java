package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.upgrade.TestUpgradeTasksReindexSuppression;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestPluginStateMigration;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask552;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6038;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6039;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6047;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask606;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6085and6153;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6096;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6123;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6134;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6137;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6140;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6152;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6206;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6208;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6317;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask6327;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask641;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask701;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask707;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask807;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask813;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTask849and6153;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeTasks752To754;
import com.atlassian.jira.webtests.ztests.upgrade.tasks.TestUpgradeWorkflowsWithWhitespacesInNames;
import com.atlassian.jira.webtests.ztests.user.TestUpgradeTask602;
import com.atlassian.jira.webtests.ztests.user.TestUpgradeTask_Build6331;

import junit.framework.Test;

/**
 * A suite of test related to Upgrade Tasks
 *
 * @since v4.0
 */
public class FuncTestSuiteUpgradeTasks extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteUpgradeTasks();

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

    public FuncTestSuiteUpgradeTasks()
    {
        addTest(TestUpgradeTask6038.class);
        addTest(TestUpgradeTask6039.class);
        addTest(TestUpgradeTask6047.class);
        addTest(TestUpgradeTask602.class);
        addTest(TestUpgradeTask606.class);
        addTest(TestUpgradeTask552.class);
        addTest(TestUpgradeTask641.class);
        addTest(TestUpgradeTask701.class);
        addTest(TestUpgradeTask707.class);
        addTest(TestUpgradeTasks752To754.class);
        addTest(TestUpgradeTask813.class);
        addTest(TestUpgradeTask807.class);
        addTest(TestUpgradeTask849and6153.class);
        addTest(TestUpgradeTask6085and6153.class);
        addTest(TestUpgradeTask6096.class);
        addTest(TestUpgradeTask6123.class);
        addTest(TestUpgradeTask6134.class);
        addTest(TestUpgradeTask6137.class);
        addTest(TestUpgradeTask6140.class);
        addTest(TestUpgradeTask6152.class);
        addTest(TestUpgradeTask6206.class);
        addTest(TestUpgradeTask6208.class);
        addTest(TestUpgradeTask6317.class);
        addTest(TestUpgradeTask6327.class);
        addTest(TestUpgradeTask_Build6331.class);
        addTest(TestUpgradeWorkflowsWithWhitespacesInNames.class);
        addTest(TestPluginStateMigration.class);
        addTest(TestUpgradeTasksReindexSuppression.class);
    }
}
