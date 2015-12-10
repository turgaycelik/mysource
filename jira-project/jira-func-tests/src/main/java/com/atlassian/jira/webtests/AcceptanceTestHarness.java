package com.atlassian.jira.webtests;

import java.util.concurrent.TimeUnit;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.functest.framework.TestSuiteBuilder;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteAdministration;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteAppLinks;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteAttachments;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteAvatars;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteBrowsing;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteBulkOperations;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteCharting;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteCloneIssue;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteComments;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteComponentsAndVersions;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteCustomFields;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteDashboards;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteDatabase;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteEmail;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteFields;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteFilters;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteI18n;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteImportExport;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteIssueNavigator;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteIssues;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteJelly;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteJql;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteLdap;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteLicencing;
import com.atlassian.jira.webtests.zsuites.FuncTestSuitePermissions;
import com.atlassian.jira.webtests.zsuites.FuncTestSuitePlatform;
import com.atlassian.jira.webtests.zsuites.FuncTestSuitePlugins;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteProjectImport;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteProjects;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteREST;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteRandomTests;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteRemote;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteReports;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteRoles;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteSchemes;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteSecurity;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteSetup;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteSharedEntities;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteSubTasks;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteTimeTracking;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteTimeZone;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteUpgradeTasks;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteUsersAndGroups;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteWorkflow;
import com.atlassian.jira.webtests.zsuites.FuncTestSuiteWorklogs;
import com.atlassian.jira.webtests.ztests.about.TestAboutPage;
import com.atlassian.jira.webtests.ztests.bundledplugins2.TestIssueTabPanels;
import com.atlassian.jira.webtests.ztests.bundledplugins2.gadget.TestAssignedToMeGadget;
import com.atlassian.jira.webtests.ztests.dashboard.TestHead;
import com.atlassian.jira.webtests.ztests.plugin.FuncTestSuiteReferencePlugins;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.FuncTestSuiteReloadablePluginModules;

import junit.framework.Test;

/**
 * This is the top level Test Suite for JIRA web functional tests.  In order for a functional test to be run by the
 * nightly / bamboo builds, it must be declared in this class or in a FuncTestSuite that is included via the class.
 */
public class AcceptanceTestHarness extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new AcceptanceTestHarness();

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

    public AcceptanceTestHarness()
    {
        //NOTE: These "setup" tests must run first as they will fail otherwise.
        addTestSuite(FuncTestSuiteSetup.SUITE);

        addTestSuite(FuncTestSuiteAvatars.SUITE);
        addTestSuite(FuncTestSuiteBulkOperations.SUITE);
        addTestSuite(FuncTestSuiteComments.SUITE);
        addTestSuite(FuncTestSuiteComponentsAndVersions.SUITE);
        addTestSuite(FuncTestSuiteDashboards.SUITE);
        addTestSuite(FuncTestSuiteFields.SUITE);
        addTestSuite(FuncTestSuiteFilters.SUITE);
        addTestSuite(FuncTestSuiteIssueNavigator.SUITE);
        addTestSuite(FuncTestSuiteIssues.SUITE);
        addTestSuite(FuncTestSuiteLicencing.SUITE);
        addTestSuite(FuncTestSuitePermissions.SUITE);
        addTestSuite(FuncTestSuiteProjects.SUITE);
        addTestSuite(FuncTestSuiteProjectImport.SUITE);
        addTestSuite(FuncTestSuiteReports.SUITE);
        addTestSuite(FuncTestSuiteRoles.SUITE);
        addTestSuite(FuncTestSuiteSchemes.SUITE);
        addTestSuite(FuncTestSuiteSecurity.SUITE);
        addTestSuite(FuncTestSuiteI18n.SUITE);
        addTestSuite(FuncTestSuiteSharedEntities.SUITE);
        addTestSuite(FuncTestSuiteSubTasks.SUITE);
        addTestSuite(FuncTestSuiteCloneIssue.SUITE);
        addTestSuite(FuncTestSuiteUsersAndGroups.SUITE);
        addTestSuite(FuncTestSuiteWorkflow.SUITE);
        addTestSuite(FuncTestSuiteWorklogs.SUITE);
        addTestSuite(FuncTestSuiteBrowsing.SUITE);
        addTestSuite(FuncTestSuiteImportExport.SUITE);
        addTestSuite(FuncTestSuiteJql.SUITE);
        addTestSuite(FuncTestSuiteJelly.SUITE);
        addTestSuite(FuncTestSuiteCharting.SUITE);
        addTestSuite(FuncTestSuiteTimeTracking.SUITE);
        addTestSuite(FuncTestSuiteAttachments.SUITE);
        addTestSuite(FuncTestSuiteCustomFields.SUITE);
        addTestSuite(FuncTestSuiteEmail.SUITE);
        addTestSuite(FuncTestSuiteUpgradeTasks.SUITE);
        addTestSuite(FuncTestSuiteAdministration.SUITE);
        addTestSuite(FuncTestSuiteRandomTests.SUITE);
        addTestSuite(FuncTestSuiteReloadablePluginModules.SUITE);
        addTestSuite(FuncTestSuiteReferencePlugins.SUITE);
        addTestSuite(FuncTestSuiteAppLinks.SUITE);
        addTestSuite(FuncTestSuitePlatform.SUITE);
        addTestSuite(FuncTestSuiteDatabase.SUITE);

        // this suite only runs with bundled plugins enabled
        addTestSuite(FuncTestSuiteREST.SUITE);
        addTestSuite(FuncTestSuitePlugins.SUITE);
        addTestSuite(FuncTestSuiteRemote.SUITE);

        //this suite should only run when

        addBundledPlugins2Only(TestAssignedToMeGadget.class);
        addBundledPlugins2Only(TestIssueTabPanels.class);

        // this suite only runs on the LDAP TPM builds
        addTestSuite(FuncTestSuiteLdap.SUITE);
        addTestSuite(FuncTestSuiteTimeZone.SUITE);
        addTest(TestAboutPage.class);
        addTest(TestHead.class);
    }

    @Override
    protected TestSuiteBuilder createFuncTestBuilder()
    {
        return super.createFuncTestBuilder().watch(3, 1, TimeUnit.MINUTES, FuncTestCase.class, JIRAWebTest.class);
    }
}
