package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestSiteDarkFeatures extends FuncTestCase
{
    public void testAdminUI()
    {
        administration.restoreBlankInstance();

        navigation.gotoPage("/secure/admin/SiteDarkFeatures!default.jspa");
        text.assertTextPresent(new IdLocator(tester, "site-disabled-features"), "jira.site.darkfeature.admin");

        backdoor.darkFeatures().enableForSite("jira.site.darkfeature.admin");
        navigation.gotoPage("/secure/admin/SiteDarkFeatures!default.jspa");
        text.assertTextNotPresent(new IdLocator(tester, "site-disabled-features"), "jira.site.darkfeature.admin");
        text.assertTextPresent(new IdLocator(tester, "site-enabled-features"), "jira.site.darkfeature.admin");

        //check admin permissions are required.
        navigation.logout();
        navigation.login("fred");
        navigation.gotoPage("/secure/admin/SiteDarkFeatures!default.jspa");
        text.assertTextPresent("Welcome to jWebTest JIRA installation");
        text.assertTextNotPresent("System Property Dark Features");
    }
}
