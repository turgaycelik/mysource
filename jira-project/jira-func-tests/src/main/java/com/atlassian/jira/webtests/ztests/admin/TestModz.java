package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * A func test for the ModzDetector rows in the ViewSystemInfo page
 *
 * @since v3.13
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestModz extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testChangedFiles()
    {
//        turnOnDebug();
        navigation.gotoAdminSection("system_info");

        TableLocator locator = new TableLocator(tester, "system_info_table");
        text.assertTextPresent(locator, "There have been no removed files");
        // the files in WEB-INF/classes are to be loaded from the *classloader*. If they are not excluded from the
        // hash-registry generation process for files to be loaded from the *filesystem*, they will be erroneously
        // identified as missing.
        text.assertTextNotPresent(locator, "WEB-INF/classes");
//        turnOffDebug();
    }

    private void turnOffDebug()
    {
        tester.gotoPage("/secure/admin/jira/ConfigureLogging!default.jspa?loggerName=com.atlassian");
        tester.selectOption("levelName", "WARN");
    }

    private void turnOnDebug()
    {
        tester.gotoPage("/secure/admin/jira/ConfigureLogging!default.jspa?loggerName=com.atlassian");
        tester.selectOption("levelName", "DEBUG");
        tester.submit("Update");
    }

}
