package com.atlassian.jira.webtests.ztests.admin.trustedapps;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestTrustedApplications extends JIRAWebTest
{
    private static final String REQUEST_NEW_TRUSTED_APP_DETAILS = "Request New Trusted Application Details";

    public TestTrustedApplications(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestTrustedAppsBlank.xml");
    }

    public void testAdminLinkWorks()
    {
        gotoAdmin();
        gotoViewTrustedAppsConfigs();
        assertTextPresent("View Trusted Applications");
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("No trusted applications have been configured.");
    }

    private void gotoViewTrustedAppsConfigs()
    {
        gotoPage("/secure/admin/trustedapps/ViewTrustedApplications.jspa");
    }

    public void testRequestSelf()
    {
        final String trustedAppName = "theTrustedApplicationName";

        gotoAdmin();
        gotoViewTrustedAppsConfigs();
        setFormElement("trustedAppBaseUrl", getEnvironmentData().getBaseUrl().toExternalForm());
        submit("Send Request");

        setWorkingForm("jiraform");
        assertTextPresent("Add New Trusted Application");
        setFormElement("name", trustedAppName);
        assertTextPresent("Application Name");
        assertTextPresent("Application ID");
        assertTextPresent("Timeout");
        assertTextPresent("IP Address Matches");
        assertTextPresent("URL Paths to Allow");
        assertButtonNotPresent("Update");
        submit("Add");

        assertTextPresent("View Trusted Applications");
        assertTextPresent(trustedAppName);
        assertLinkPresent("edit-10010");
        assertLinkPresent("delete-10010");

        boolean wasEnabled = HttpUnitOptions.isScriptingEnabled();
        HttpUnitOptions.setScriptingEnabled(true);
        try
        {
            clickLink("edit-10010");

            setWorkingForm("jiraform");
            assertTextPresent("Edit Trusted Application Details");
            assertButtonPresent("update_submit");
            assertButtonNotPresent("add_submit");
            clickCancelButton();

            assertTextPresent("View Trusted Applications");
            clickLink("delete-10010");

            setWorkingForm("jiraform");
            assertTextPresent("Delete Trusted Application:");
            assertTextPresent(trustedAppName);
            assertButtonPresent("delete_submit");
            clickCancelButton();
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(wasEnabled);
        }

        assertTextPresent("View Trusted Applications");
        clickLink("delete-10010");

        setWorkingForm("jiraform");
        submit("Delete");

        assertTextPresent("View Trusted Applications");
        assertTextNotPresent(trustedAppName);
        assertLinkNotPresent("edit-10010");
        assertLinkNotPresent("delete-10010");
        assertTextPresent("No trusted applications have been configured.");
    }

    public void testRequestSelfTwice()
    {
        gotoAdmin();
        gotoViewTrustedAppsConfigs();
        setFormElement("trustedAppBaseUrl", getEnvironmentData().getBaseUrl().toExternalForm());
        submit("Send Request");

        setWorkingForm("jiraform");
        assertTextPresent("Add New Trusted Application");
        setFormElement("name", "theTrustedApplicationName");
        submit("Add");
        setFormElement("trustedAppBaseUrl", getEnvironmentData().getBaseUrl().toExternalForm());
        submit("Send Request");

        setWorkingForm("jiraform");
        assertTextPresent("Add New Trusted Application");
        setFormElement("name", "anuvverTrustedApplicationName");
        submit("Add");
        assertTextPresent("The Trusted Application with the specified applicationId");
        assertTextPresent("already exists");
    }

    public void testDirectBrowseWithNoPermission()
    {
        login("regularadmin", "regularadmin");
        gotoAdmin();
        assertLinkNotPresent("trusted_apps");

        gotoViewTrustedAppsConfigs();
        assertTextNotPresent("View Trusted Applications");

        gotoPage("/secure/admin/trustedapps/EditTrustedApplication!default.jspa");
        assertTextNotPresent("Edit Trusted Application");

        gotoPage("/secure/admin/trustedapps/DeleteTrustedApplication!default.jspa");
        assertTextNotPresent("Delete Trusted Application");
    }

    public void testRequestAppBadUrl()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoAdmin();
        gotoViewTrustedAppsConfigs();

        // malformed URL
        setFormElement("trustedAppBaseUrl", "junkUrl-9287349287349");
        submit("Send Request");
        // since url was bad, shouldn't have left the page
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("java.net.MalformedURLException:");

        // unknown host
        final String unknownUrl = "http://www.something.invalid/";
        setFormElement("trustedAppBaseUrl", unknownUrl);
        submit("Send Request");
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("The host " + unknownUrl + " could not be found");

        // known host with no certificate
        final String noTrustUrl = "http://www.google.com";
        setFormElement("trustedAppBaseUrl", noTrustUrl);
        submit("Send Request");
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("The application at URL " + noTrustUrl + " does not support the Trusted Application protocol.");

        // JRA-16003: known host with no certificate and whitespace - should be the same error
        final String noTrustUrlWhitespace = "   http://www.google.com   ";
        setFormElement("trustedAppBaseUrl", noTrustUrlWhitespace);
        submit("Send Request");
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("The application at URL " + noTrustUrlWhitespace.trim() + " does not support the Trusted Application protocol.");

        // known host which refuses connection - add a 1 to the port - may not work if port isn't explicit
        final String wrongTrustUrl = getUnlistenedURL();
        setFormElement("trustedAppBaseUrl", wrongTrustUrl);
        submit("Send Request");
        assertTextPresent(REQUEST_NEW_TRUSTED_APP_DETAILS);
        assertTextPresent("The host "+ wrongTrustUrl + " could not be found.");
    }

    public void testAddEditValidation()
    {
        final String trustedAppName = "theTrustedApplicationName";

        gotoAdmin();
        gotoViewTrustedAppsConfigs();
        setFormElement("trustedAppBaseUrl", getEnvironmentData().getBaseUrl().toExternalForm());
        submit("Send Request");

        // don't set name
        setWorkingForm("jiraform");
        setFormElement("name", "");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("The Trusted Application Name cannot be blank.");

        // don't set timeout
        setWorkingForm("jiraform");
        setFormElement("name", trustedAppName);
        setFormElement("timeout", "");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify a positive Timeout value.");

        // set timeout to NaN
        setWorkingForm("jiraform");
        setFormElement("timeout", "asdasd");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify a positive Timeout value.");

        // set timeout to negative number
        setWorkingForm("jiraform");
        setFormElement("timeout", "-8888");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify a positive Timeout value.");

        // set timeout to zero
        setWorkingForm("jiraform");
        setFormElement("timeout", "0");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify a positive Timeout value.");

        // set timeout to larger than Long.MAX_VALUE
        setWorkingForm("jiraform");
        setFormElement("timeout", "9223372036854775808");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify a positive Timeout value.");

        // IP match string cannot be blank
        setWorkingForm("jiraform");
        setFormElement("timeout", "1000");
        setFormElement("ipMatch", "");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify IP addresses to match against.");

        // URL match string cannot be blank
        setWorkingForm("jiraform");
        setFormElement("timeout", "1000");
        setFormElement("urlMatch", "");
        submit("Add");
        assertTextPresent("Add New Trusted Application");
        assertTextPresent("You must specify URLs to match against.");

        {
            // malformed IP address
            final String malformedIp = "blah";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            submit("Add");
            assertTextPresent("Add New Trusted Application");
            assertBadIPMessage(malformedIp);
        }
        {
            // non IPv4 address
            final String malformedIp = "123.123.123.123.1.1";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            submit("Add");
            assertTextPresent("Add New Trusted Application");
            assertBadIPMessage(malformedIp);
        }
        {
            // greater than 255
            final String malformedIp = "299.299.299.299";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            submit("Add");
            assertTextPresent("Add New Trusted Application");
            assertBadIPMessage(malformedIp);
        }
        {
            // illegal separator
            final String malformedIp = "192,168,0,1";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            submit("Add");
            assertTextPresent("Add New Trusted Application");
            assertBadIPMessage(malformedIp);
        }
        {
            // illegal wildcard character
            final String malformedIp = "192.168.?.1";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            submit("Add");
            assertTextPresent("Add New Trusted Application");
            assertBadIPMessage(malformedIp);
        }
        {
            // multiple IP addresses (works)
            final String malformedIp = "123.123.123.123\n192.168.0.1";
            setWorkingForm("jiraform");
            setFormElement("timeout", "1000");
            setFormElement("ipMatch", malformedIp);
            setFormElement("urlMatch", "/some/url");
            submit("Add");
            assertTextPresent("View Trusted Applications");
            assertTextPresent(trustedAppName);
            assertLinkPresent("delete-10010");
        }
    }

    private void assertBadIPMessage(String malformedIp)
    {
        assertTextPresent("The IP address pattern: " + malformedIp + " is invalid.");
    }

    private String getUnlistenedURL()
    {
        String host = "http://localhost:";
        int port = 8000;
        while (true)
        {
            URL url;
            try
            {
                url = new URL(host + ++port);
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }
            HttpURLConnection connection = null;
            try
            {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
            }
            catch (ConnectException e)
            {
                return url.toExternalForm();
            }
            catch (IOException ignoreAndContinue)
            {
                // ignore and try next port
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
            }
        }
    }
}
