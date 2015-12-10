package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Tests that when JIRA is not setup, the required error responses are sent
 * for various request URL patterns. For reasons that should be obvious, this
 * test is only valid if JIRA is not yet set up.
 */
@SystemTenantOnly
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.SETUP })
public class TestJohnsonFiltersWhileNotSetup extends FuncTestCase
{
    private static final Logger log = Logger.getLogger(TestJohnsonFiltersWhileNotSetup.class);

    /**
     * Checks that the contentless 503 response is returned for a few URLs
     *
     * @throws IOException if web client does
     */
    public void test503Only() throws IOException
    {
        assert503Only("/si/whatever");
        assert503Only("/sr/whatever");
        assert503Only("/download/*");
        assert503Only("/plugins/servlet/*");
        assert503Only("/secure/attachment/blah-tricky.gif");
        assert503Only("/rest/some/rest");
        assert503Only("/rest/a");
    }

    /**
     * Checks that the setup page is shown when JIRA is not setup.
     */
    public void testNotSetupMessage()
    {
        tester.beginAt("/browse/ABC-123");
        WebResponse webResponse = tester.getDialog().getResponse();
        assertEquals("should be a 200 response", 200, webResponse.getResponseCode());
        text.assertTextPresent(locator.page(), "Let's start setting up your JIRA");
    }

    private void assert503Only(String atUrl) throws IOException
    {
        try
        {
            tester.beginAt(atUrl);
        }
        catch (RuntimeException re)
        {
            // if we get here, it's possible that we are running on java 1.5 +
            // and the server is websphere. in this combination (and only this
            // one AFAIK) the HttpUnit client receives a null from
            // HttpUrlConnection.getErrorStream() which, according to the JDK javadoc
            // is supposed to indicate that there is no error. It certainly means
            // nothing can be read from it!
            // Unfortunately, when HttpUnit then follows the getInputStream() call
            // tree it ends up with an IOException it didn't expect.
            // To add insult to injury, this IOException is not set as the cause
            // of the RuntimeException jwebunit wraps it in (probably because it
            // was written for jdk 1.3.)
            // So we just make some small efforts to ensure the basis of the
            // exception is the 503 we were hoping for.
            log.warn("Not able to properly assert the response code, using crude (websphere) workaround instead");
            assertTrue(re.getMessage().contains("IOException"));
            assertTrue(re.getMessage().contains("503"));

            return;
        }
        WebResponse webResponse = tester.getDialog().getResponse();
        assertEquals("expected service unavailable response for url: '" + atUrl + "'", 503, webResponse.getResponseCode());
        assertEquals("should be no content for this url: '" + atUrl + "'", 0, webResponse.getText().length());
    }

    @Override
    protected boolean shouldSkipSetup()
    {
        return true;
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);
    }

    @Override
    public void setUpTest()
    {
        tester.getTestContext().setBaseUrl(getEnvironmentData().getBaseUrl().toExternalForm());
    }
}
