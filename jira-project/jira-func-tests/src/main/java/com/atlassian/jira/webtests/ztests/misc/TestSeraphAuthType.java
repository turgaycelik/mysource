package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestSeraphAuthType extends FuncTestCase
{
    // There is no need to test NONE since that's what every other test in JIRA will be doing.

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        navigation.logout();

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
    }

    public void testBasic() throws Exception
    {
        tester.beginAt("/?os_authType=basic");
        final WebResponse response = tester.getDialog().getResponse();
        assertEquals(401, response.getResponseCode());
        assertEquals("text/html", response.getContentType());
    }

    public void testCookie() throws Exception
    {
        final WebTester webTester = WebTesterFactory.createNewWebTester(environmentData);
        webTester.getTestContext().addCookie("JSESSIONID", "bad-cookie");

        webTester.beginAt("/?os_authType=cookie");
        final WebResponse response = webTester.getDialog().getResponse();
        assertEquals(401, response.getResponseCode());
        assertEquals("text/html", response.getContentType());
    }

    public void testAny_fail() throws Exception
    {
        final WebTester webTester = WebTesterFactory.createNewWebTester(environmentData);
        webTester.getTestContext().addCookie("JSESSIONID", "bad-cookie");
        webTester.beginAt("/?os_authType=any");

        final WebResponse response = webTester.getDialog().getResponse();
        assertEquals(401, response.getResponseCode());
        assertEquals("text/html", response.getContentType());
    }

    public void testAny_anon() throws Exception
    {
        // create a new webtester with no JSESSION cookie
        final WebTester webTester = WebTesterFactory.createNewWebTester(environmentData);

        webTester.beginAt("/?os_authType=any");
        final WebResponse response = webTester.getDialog().getResponse();
        assertEquals(200, response.getResponseCode());
        assertEquals("text/html", response.getContentType());
    }
}
