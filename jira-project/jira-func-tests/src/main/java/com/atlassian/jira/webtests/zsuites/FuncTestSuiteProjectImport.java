package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportResults;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportSelectBackup;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportSelectProject;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportSummary;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportUsersDoNotExistPage;
import com.atlassian.jira.webtests.ztests.imports.project.TestProjectImportWithProjectKeyRename;
import junit.framework.Test;

/**
 * A suite of tests around JIRA project import
 *
 * @since v4.0
 */
public class FuncTestSuiteProjectImport extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteProjectImport();

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

    public FuncTestSuiteProjectImport()
    {
        addTest(TestProjectImportSelectBackup.class);
        addTest(TestProjectImportUsersDoNotExistPage.class);
        addTest(TestProjectImportSummary.class);
        addTest(TestProjectImportResults.class);
        addTest(TestProjectImportSelectProject.class);
        addTest(TestProjectImportWithProjectKeyRename.class);
    }
}