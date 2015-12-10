package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;

@WebTest ({ FUNC_TEST })
public class TestReportProblem extends FuncTestCase
{
    private static final String SUPPORT_TOOLS_PLUGIN_PATH = "/plugins/servlet/stp/view/";
    private static final String CONTACT_ADMIN_PATH = "/secure/ContactAdministrators!default.jspa";
    private static final String CREATE_ISSUE_PATH = "/secure/CreateIssue!default.jspa";
    private static final String JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM = "jira.show.contact.administrators.form";

    public void testReportProblemDoesNotRedirectToSupportToolsPluginForJiraAdmin()
    {
        administration.restoreData("TestWithSystemAdmin.xml");
        try
        {
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            text.assertTextNotPresent(getReportProblemHref(), SUPPORT_TOOLS_PLUGIN_PATH);
        }
        finally
        {
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testReportProblemRedirectsToSupportToolsPluginForSysAdmin()
    {
        administration.restoreData("TestWithSystemAdmin.xml");
        try
        {
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            text.assertTextPresent(getReportProblemHref(), SUPPORT_TOOLS_PLUGIN_PATH);
        }
        finally
        {
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testReportProblemRedirectsToContactAdminWhenEnabledForLoggedInUsers()
    {
        try
        {
            administration.restoreBlankInstance();
            setShowContactAdminForm(true);
            navigation.login(FRED_USERNAME, FRED_PASSWORD);
            text.assertTextPresent(getReportProblemHref(), CONTACT_ADMIN_PATH);
        }
        finally
        {
            setShowContactAdminForm(false);
        }
    }

    public void testReportProblemRedirectsToContactAdminWhenEnabledForLoggedOutUsers()
    {
        setShowContactAdminForm(true);
        try
        {
            navigation.logout();
            text.assertTextPresent(getReportProblemHref(), CONTACT_ADMIN_PATH);
        }
        finally
        {
            setShowContactAdminForm(false);
        }
    }

    public void testReportProblemRedirectsToCreateIssueWhenContactAdminDisabledForLoggedInUsers()
    {
        administration.restoreBlankInstance();
        setShowContactAdminForm(false);
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        text.assertTextPresent(getReportProblemHref(), CREATE_ISSUE_PATH);
    }

    public void testReportProblemRedirectsToCreateIssueWhenContactAdminDisabledForLoggedOutUsers()
    {
        navigation.logout();
        text.assertTextPresent(getReportProblemHref(), CREATE_ISSUE_PATH);
    }

    private String getReportProblemHref()
    {
        return page.getFooter().getReportProblemLink().getURLString();
    }

    private void setShowContactAdminForm(boolean show)
    {
        backdoor.applicationProperties().setOption(JIRA_SHOW_CONTACT_ADMINISTRATORS_FORM, show);
    }
}
