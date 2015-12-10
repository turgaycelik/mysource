package com.atlassian.jira.webtests.ztests.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * Responsible for verifying XSRF protection of "log-out" functionality. Takes into account remember-me cookies and the
 * different entry points for logging out of jira (Seraph, direct invocation of the Logout Action command methods).
 *
 * @since v4.1.1
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestXsrfLogOut extends FuncTestCase
{
    private static final String LOG_OUT_LINK_ID = "log_out";

    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testXsrfLogoutFromSeraph() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Seraph", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        navigation.gotoDashboard();
                    }
                }, new XsrfCheck.LinkWithIdSubmission(LOG_OUT_LINK_ID)
                {
                    @Override
                    public String getLink() throws Exception
                    {
                        // HACK: Remove the context path from the link obtained from the super-class.
                        // This is necessary because the goToPage() method on WebTester already appends this
                        // and if we don't remove it the link will contain the context path twice.
                        String baseLink = super.getLink();
                        String finalLink = baseLink.replaceFirst(getEnvironmentData().getContext(), "");
                        return finalLink;
                    }
                })
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    public void testXsrfLogoutFromSeraphWithAnExpiredSession() throws Exception
    {
        navigation.gotoDashboard();

        // We clear the browser's cookies to simulate an expired session / token
        tester.getDialog().getWebClient().clearCookies();

        tester.clickLink(LOG_OUT_LINK_ID);
        tester.getDialog().getResponse();
        tester.assertTextPresent("You have already been logged out of JIRA");
    }

    public void testXsrfLogOutFromSeraphConfirmsLogOutWhenRememberMeIsOn() throws Exception
    {
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD, true);

        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Seraph", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        // We are relying on the fact that the test infrastructure tries to submit a dodgy token first.
                        // Therefore, there is no need to log you back in here.
                        navigation.gotoDashboard();
                    }
                }, new XsrfCheck.LinkWithIdSubmission(LOG_OUT_LINK_ID)
                {
                    @Override
                    public String getLink() throws Exception
                    {
                        // HACK: Remove the context path from the link obtained from the super-class.
                        // This is necessary because the goToPage() method on WebTester already appends this
                        // and if we don't remove it the link will contain the context path twice.
                        String baseLink = super.getLink();
                        String finalLink = baseLink.replaceFirst(getEnvironmentData().getContext(), "");
                        return finalLink;
                    }
                })
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    public void testXsrfLogoutFromActionViaDefaultCommand() throws Exception
    {
        final String tokenValue = page.getXsrfToken();

        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Default Action", new XsrfCheck.Setup.None()
                        , new UrlSubmission(new URL(getEnvironmentData().getBaseUrl() + "/Logout!default.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue)))
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    public void testXsrfLogoutFromActionViaDefaultCommandWithAnExpiredSession() throws Exception
    {
        navigation.gotoDashboard();

        // We clear the browser's cookies to simulate an expired session / token
        tester.getDialog().getWebClient().clearCookies();

        final String tokenValue = page.getXsrfToken();

        tester.gotoPage("/Logout!default.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue);
        tester.getDialog().getResponse();
        tester.assertTextPresent("You have already been logged out of JIRA");
    }

    public void testXsrfLogoutFromActionViaDefaultCommandWhenRememberMeIsOn() throws Exception
    {
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD, true);

        final String tokenValue = page.getXsrfToken();

        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Default Action",
                        // We are relying on the fact that the test infrastructure tries to submit a dodgy token first.
                        // Therefore, there is no need to log you back in here.
                        new XsrfCheck.Setup.None()
                        , new UrlSubmission(new URL(getEnvironmentData().getBaseUrl() + "/Logout!default.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue)))
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    public void testXsrfLogoutFromActionViaExecuteCommand() throws Exception
    {
        final String tokenValue = page.getXsrfToken();

        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Action", new XsrfCheck.Setup.None()
                        , new UrlSubmission(new URL(getEnvironmentData().getBaseUrl() + "/Logout.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue)))
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    public void testXsrfLogoutFromActionViaExecuteCommandWithAnExpiredSession() throws Exception
    {
        navigation.gotoDashboard();

        // We clear the browser's cookies to simulate an expired session / token
        tester.getDialog().getWebClient().clearCookies();

        final String tokenValue = page.getXsrfToken();

        tester.gotoPage("/Logout.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue);
        tester.getDialog().getResponse();
        tester.assertTextPresent("You have already been logged out of JIRA");
    }

    public void testXsrfLogoutFromActionViaExecuteCommandWhenRememberMeIsOn() throws Exception
    {
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD, true);

        final String tokenValue = page.getXsrfToken();

        new XsrfTestSuite(
                new XsrfCheck("Log Out XSRF Protection Test - Action",
                        // We are relying on the fact that the test infrastructure tries to submit a dodgy token first.
                        // Therefore, there is no need to log you back in here.
                        new XsrfCheck.Setup.None()
                        , new UrlSubmission(new URL(getEnvironmentData().getBaseUrl() + "/Logout.jspa?" + XsrfCheck.ATL_TOKEN + "=" + tokenValue)))
        ).run(funcTestHelperFactory, "Confirm logout");
    }

    /**
     * <p>Defines a submission based on a caller supplied URL.</p>
     * <p>The URL must contain a XSRF token parameter.</p>
     */
    private final class UrlSubmission implements XsrfCheck.Submission
    {

        private FuncTestHelperFactory funcTestHelperFactory;
        private final URL originalUrl;

        private URL urlToSubmit;
        /**
         * Creates a new URLSubmission instance.
         * @param originalUrl The original originalUrl containing a valid XSRF token.
         * @throws IllegalArgumentException if the originalUrl does not contain an XSRF token.
         */
        private UrlSubmission(final URL originalUrl)
        {
            final String queryString = originalUrl.getQuery();
            final String xsrfTokenParameterRegex = ".*?" + XsrfCheck.ATL_TOKEN + "=" + ".*?";

            if (!Pattern.matches(xsrfTokenParameterRegex, queryString))
            {
                throw new IllegalArgumentException("The URL must contain a XSRF Token parameter");
            }
            this.originalUrl = originalUrl;
            this.urlToSubmit = originalUrl;
        }

        FuncTestHelperFactory getFuncTestHelperFactory()
        {
            return funcTestHelperFactory;
        }

        public void init(final FuncTestHelperFactory funcTestHelperFactory)
        {
            this.funcTestHelperFactory = funcTestHelperFactory;
        }

        /**
         * Changes the urlToSubmit so that it contains an invalid XSRF token.
         * @throws Exception
         */
        public void removeToken() throws Exception
        {
            final String invalidTokenInUrlString = XsrfCheck.invalidTokenInUrl(originalUrl.toString());
            urlToSubmit = new URL(invalidTokenInUrlString);
        }

        /**
         * Goes to the page according to the urlToSubmit and resets the urlToSubmit to the originalUrl.
         * @throws Exception
         */
        public void submitRequest() throws Exception
        {
            getFuncTestHelperFactory().getTester().gotoPage(urlToSubmit.toString());
            resetUrlToSubmit();
        }

        private void resetUrlToSubmit() {urlToSubmit = originalUrl;}

    }
    private void clearCookies()
    {
        tester.getDialog().getWebClient().clearContents();
    }
}