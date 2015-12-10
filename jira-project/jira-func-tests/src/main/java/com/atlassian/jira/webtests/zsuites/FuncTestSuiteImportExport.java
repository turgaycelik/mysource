package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.TestXmlRestore;
import com.atlassian.jira.webtests.ztests.admin.TestDataExport;
import com.atlassian.jira.webtests.ztests.admin.TestImportExport;
import com.atlassian.jira.webtests.ztests.imports.properties.TestImportExportExcludedEntities;
import com.atlassian.jira.webtests.ztests.imports.properties.TestImportWithEntityProperties;
import com.atlassian.jira.webtests.ztests.misc.TestDefaultJiraDataFromInstall;
import com.atlassian.jira.webtests.ztests.misc.TestEmptyStringDataRestore;
import junit.framework.Test;

/**
 * Test for project import/export functionality.
 *
 * @since v3.13
 */
public class FuncTestSuiteImportExport extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new FuncTestSuiteImportExport();

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

    public FuncTestSuiteImportExport()
    {
        addTest(TestDataExport.class);
        addTest(TestDefaultJiraDataFromInstall.class);
        addTest(TestEmptyStringDataRestore.class);
        addTest(TestImportExport.class);
        addTest(TestXmlRestore.class);
        addTest(TestImportWithEntityProperties.class);
        addTest(TestImportExportExcludedEntities.class);

    }
}