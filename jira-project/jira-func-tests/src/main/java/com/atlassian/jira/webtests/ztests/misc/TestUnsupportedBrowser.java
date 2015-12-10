package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import net.sourceforge.jwebunit.TestContext;

/**
 * Tests the Unsupported Browser functionality
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestUnsupportedBrowser extends FuncTestCase
{
    public static final String IE6_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1";
    public static final String IE7_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB6.4; .NET CLR 1.1.4322; FDM; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)";
    public static final String IE8_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)";

    public void testIE6()
    {
        loginWithUserAgent(IE6_USER_AGENT);
        assertBrowserUnsupportedMessageIsDisplayed();

        dismissBrowserUnsupportedMessage();
        logout();
        assertBrowserUnsupportedMessageIsNotDisplayed();
    }

    public void testIE7()
    {
        loginWithUserAgent(IE7_USER_AGENT);
        assertBrowserUnsupportedMessageIsDisplayed();

        dismissBrowserUnsupportedMessage();
        logout();
        assertBrowserUnsupportedMessageIsNotDisplayed();
    }

    public void testIE8()
    {
        loginWithUserAgent(IE8_USER_AGENT);
        assertBrowserUnsupportedMessageIsDisplayed();

        dismissBrowserUnsupportedMessage();
        logout();
        assertBrowserUnsupportedMessageIsNotDisplayed();
    }

    private void loginWithUserAgent(String userAgent)
    {
        TestContext ctx = tester.getTestContext();
        ctx.setUserAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)");
        navigation.loginUsingForm(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    private void dismissBrowserUnsupportedMessage()
    {
        TestContext ctx = tester.getTestContext();
        ctx.addCookie("UNSUPPORTED_BROWSER_WARNING", "handled");
    }

    private void logout()
    {
        navigation.logout();
    }

    private void assertBrowserUnsupportedMessageIsDisplayed()
    {
        tester.assertElementPresent("browser-warning");
    }

    private void assertBrowserUnsupportedMessageIsNotDisplayed()
    {
        tester.assertElementNotPresent("browser-warning");
    }
}
