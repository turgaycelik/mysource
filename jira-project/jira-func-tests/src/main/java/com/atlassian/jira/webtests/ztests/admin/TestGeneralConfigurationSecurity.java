package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.w3c.dom.Node;

/**
 * Func test of viewing general configuration pages as admin/sysadmin.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestGeneralConfigurationSecurity extends FuncTestCase
{

    public void testNonSysadminCannotAccessMimeSnifferOption()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.login(ADMIN_USERNAME);
            navigation.gotoAdminSection("general_configuration");
            assertions.assertNodeDoesNotExist(locator.css("td[data-property-id=ie-mime-sniffing]"));
        }
        finally
        {
            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testNonSysadminCannotEditMimeSnifferOption()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.login(ADMIN_USERNAME);
            navigation.gotoAdminSection("general_configuration");
            tester.clickLink("edit-app-properties");
            assertions.assertNodeDoesNotExist(locator.css("input[name=ieMimeSniffer]"));
        }
        finally
        {
            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

}