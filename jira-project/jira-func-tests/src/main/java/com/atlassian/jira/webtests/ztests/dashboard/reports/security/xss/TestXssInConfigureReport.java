package com.atlassian.jira.webtests.ztests.dashboard.reports.security.xss;

import com.atlassian.jira.webtests.ztests.security.xss.AbstractXssFuncTest;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test case for XSS exploits in the ConfigureReport page
 *
 * @since v5.2
 */

@WebTest ({ Category.FUNC_TEST, Category.REPORTS, Category.SECURITY })
public class TestXssInConfigureReport extends AbstractXssFuncTest
{

    public void testConfigureReportXSS()
    {
        administration.restoreData("TestConfigureReport.xml");
        assertXssNotInPage("/secure/ConfigureReport.jspa?atl_token=Pi7Pim9fcC&versionId=10000&sortingOrder=least&completedFilter=all&subtaskInclusion='all\"" + XSS + "'&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking&Next=Next");
    }

    public void testConfigureReportXssFromCustomFieldName()
    {
        administration.restoreBlankInstance();
        administration.customFields().addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", XSS);
        assertXssNotInPage("/secure/ConfigureReport!default.jspa?reportKey=com.atlassian.jira.plugin.system.reports:pie-report");
    }
}
