package com.atlassian.jira.webtest.webdriver.tests.security;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.UserCredentials;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.GenericPageWithWarningMessage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewLicensePage;
import com.atlassian.jira.pageobjects.pages.admin.applicationproperties.AdvancedPropertiesPage;
import com.atlassian.jira.pageobjects.pages.btf.JiraBtfLoginPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@WebTest ({ com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST })
public class TestLoginPage extends BaseJiraWebTest
{

    @Before
    public void setUp() throws Exception
    {
        backdoor.restoreBlankInstance();

        jira.logout();
    }

    @Test
    public void testUserRoleSysadminViewLicense() throws Exception
    {
        UserCredentials credentials = jira.getSysadminCredentials();
        SysAdminLoginPage loginPage = pageBinder.navigateToAndBind(SysAdminLoginPage.class);

        // according to actions.xml, ViewLicensePage is available for sysadmin
        loginPage.login(credentials.getUsername(), credentials.getPassword(), ViewLicensePage.class);
    }

    @Test
    public void testLoginPageMessages()
    {
        jira.visit(GenericPageWithWarningMessage.class, SysAdminLoginPage.URI, "You must log in as a system administrator to access this page.");
        jira.visit(GenericPageWithWarningMessage.class, AdminLoginPage.URI, "You must log in as an administrator to access this page.");
    }

    @Test
    public void testIframeLoginPage()
    {
        IframeLoginPage loginPage = jira.visit(IframeLoginPage.class);

        assertFalse(loginPage.getBody().find(By.cssSelector("div#page>header")).timed().isPresent().now());
        assertFalse(loginPage.getBody().find(By.cssSelector("div#page>footer")).timed().isPresent().now());
    }

    @Test
    public void testEmptyPageCaps()
    {
        CanonicalLoginPage loginPage = jira.visit(CanonicalLoginPage.class);

        assertTrue(loginPage.getBody().find(By.cssSelector("div#page>header")).timed().isPresent().now());
        assertTrue(loginPage.getBody().find(By.cssSelector("div#page>footer")).timed().isPresent().now());
    }

    public static class SysAdminLoginPage extends JiraBtfLoginPage
    {
        public static String URI = JiraBtfLoginPage.URI + "?user_role=SYSADMIN&permissionViolation=true";

        @Override
        public String getUrl()
        {
            return URI;
        }
    }

    public static class AdminLoginPage extends JiraBtfLoginPage
    {
        public static String URI = JiraBtfLoginPage.URI + "?user_role=ADMIN&permissionViolation=true";

        @Override
        public String getUrl()
        {
            return URI;
        }
    }

    public static class CanonicalLoginPage extends JiraBtfLoginPage
    {
        public static String URI = JiraBtfLoginPage.URI + "?page_caps=";

        @ElementBy (cssSelector = "body")
        private PageElement body;

        @Override
        public String getUrl()
        {
            return URI;
        }

        public PageElement getBody()
        {
            return body;
        }
    }

    public static class IframeLoginPage extends CanonicalLoginPage
    {

        public static String URI = JiraBtfLoginPage.URI + "?page_caps=IFRAME";

        @Override
        public String getUrl()
        {
            return URI;
        }
    }
}
