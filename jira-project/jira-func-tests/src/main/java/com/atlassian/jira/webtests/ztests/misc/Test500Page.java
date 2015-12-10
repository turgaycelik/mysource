package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;

import org.hamcrest.Matchers;

import net.sourceforge.jwebunit.TestContext;

import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class Test500Page extends FuncTestCase
{
    private boolean isBeforeJdk = false;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("Test500Page.xml");
        // isBeforeJdk moves the current page to the system info page
        isBeforeJdk = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isJavaBeforeJdk15();
        navigation.gotoDashboard();
    }

    public void test500PageServiceParamVisibility()
    {
        //check admins can see the service params
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.gotoPage("/internal-error");

        //check listeners
        text.assertTextSequence(locator.page(), "ParamListeners", "com.atlassian.jira.event.listeners.DebugParamListener");
        text.assertTextSequence(locator.page(), "Param1", "paramKey");
        text.assertTextSequence(locator.page(), "Param2", "paramValue");

        //check services
        assertServiceHeaderPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService", "123");
        text.assertTextSequence(locator.page(), "popserver:", "fake server");
        text.assertTextSequence(locator.page(), "handler.params:", "project=hsp, issuetype=1, catchemail=sam@atlassian.com");
        text.assertTextSequence(locator.page(), "forwardEmail:", "fake@example.com");
        text.assertTextSequence(locator.page(), "handler:", "Create a new issue or add a comment to an existing issue");

        //check that non-logged in users cannot see the service params
        navigation.logout();
        setDevMode("false");
        tester.gotoPage("/internal-error");

        //check listeners
        text.assertTextNotPresent(locator.page(), "ParamListeners");
        text.assertTextNotPresent(locator.page(), "Param1:");
        text.assertTextNotPresent(locator.page(), "paramKey");
        text.assertTextNotPresent(locator.page(), "Param2:");
        text.assertTextNotPresent(locator.page(), "paramValue");

        //check services
        assertServiceHeaderNotPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService");
        assertServiceParamsNotVisible();

        //check that users with no global permission cannot see the service params
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/internal-error");

        //check listeners
        text.assertTextNotPresent(locator.page(), "ParamListeners");
        text.assertTextNotPresent(locator.page(), "Param1:");
        text.assertTextNotPresent(locator.page(), "paramKey");
        text.assertTextNotPresent(locator.page(), "Param2:");
        text.assertTextNotPresent(locator.page(), "paramValue");

        //check services
        assertServiceHeaderNotPresent("Pop Service", "com.atlassian.jira.service.services.mail.MailFetcherService");
        assertServiceParamsNotVisible();

        setDevMode("true");
    }

    private void setDevMode(String value)
    {
        backdoor.systemProperties().setProperty("jira.dev.mode", value);
        backdoor.systemProperties().setProperty("atlassian.dev.mode", value);
    }

    public void test500PageIsShownToRegularUserInDevMode(){
        setDevMode("true");
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/internal-error");

        text.assertTextPresent(locator.page(), "Server ID");
        text.assertTextPresent(locator.page(), "File Paths");
        text.assertTextPresent(locator.page(), "JVM Input Arguments");
        text.assertTextPresent(locator.page(), "-Xmx");

    }

    public void test500PageContainsMemoryAndInputArgsInfo()
    {
        tester.gotoPage("/internal-error");

        if(isJvmWithPermGen())
        {
            text.assertTextPresent(locator.page(), "Used PermGen Memory");
            text.assertTextPresent(locator.page(), "Free PermGen Memory");
        }
        text.assertTextPresent(locator.page(), "JVM Input Arguments");

        if (isBeforeJdk)
        {
            // Make sure the warning message are present
            text.assertTextPresent(locator.page(), "Unable to determine, this requires running JDK 1.5 and higher.");
        }
        else
        {
            // Make sure the warning message are not present
            text.assertTextNotPresent(locator.page(), "Unable to determine, this requires running JDK 1.5 and higher.");
        }
    }

    public void testExternalUserManagement()
    {
        tester.gotoPage("/internal-error");
        text.assertTextPresent(locator.page(), "External user management");
    }

    /*
     * JRA-14105 inserting some escaped HTML in the command name of an action will show up on the 500 page in the
     * stack trace - need to ensure this is escaped to prevent XSS attacks
     */
    public void testHtmlEscapingOfErrors() throws Exception
    {
        String badHtml = "<body onload=alert('XSSATTACK')>";
        String badHtmlEscaped = "%3Cbody%20onload=alert('XSSATTACK')%3E";

        GetMethodWebRequest request = new GetMethodWebRequest(getEnvironmentData().getBaseUrl() + "/secure/Dashboard!default" + badHtmlEscaped + ".jspa");
        final TestContext testContext = tester.getTestContext();
        final WebClient webClient = testContext.getWebClient();

        // set this flag so that test doesn't blow up when we get a 500 error
        // note: no need to reset this flag as it is automatically reset for the next test
        webClient.setExceptionsThrownOnErrorStatus(false);

        final WebResponse response = webClient.sendRequest(request);

        String responseText = response.getText();
        assertFalse("Found bad HTML in the response", responseText.indexOf(badHtml) >= 0);
    }

    public void testAdministratorDoesntSeeContactYourAdmin()
    {
        // as admin
        tester.gotoPage("/internal-error");
        text.assertTextPresent(locator.page(), "Raise an issue for the Support team");
        text.assertTextNotPresent(locator.page(), "JIRA Administrator contact form");
    }

    public void testNonAdministratorSeesContactYourAdmin()
    {
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        tester.gotoPage("/internal-error");
        text.assertTextPresent(locator.page(), "to your JIRA Administrator");
        text.assertTextNotPresent(locator.page(), "Raise an issue for the Support team");
    }

    public void testSystemAdministratorCanSeeSysAdminOnlyProperties()
    {
        tester.gotoPage("/internal-error");
        text.assertTextNotPresent(locator.page(), "Contact your System Administrator to discover file path information.");
        text.assertTextNotPresent(locator.page(), "Contact your System Administrator to discover this property value.");
        text.assertTextSequence(locator.page(), "Server ID", "ABN9-RZYJ-WI2T-37UF");

        // assert that they can see the step 3 file path
        text.assertTextSequence(locator.page(),"server logs", "Support ZIP", " technical details");

        text.assertTextSequence(locator.page(), "File Paths", "atlassian-jira.log", "entityengine.xml");
        text.assertTextPresent(locator.page(), "JVM Input Arguments");
        if (!isBeforeJdk)
        {
            text.assertTextPresent(locator.page(), "-D");
        }
        text.assertTextPresent(locator.page(), "Current Working Directory");
    }

    public void testNonSystemAdministratorDoesntSeeFilePaths()
    {
        setDevMode("false");
        try {
            navigation.login(BOB_USERNAME, BOB_PASSWORD);
            tester.gotoPage("/internal-error");

            // assert that they CANT see the step 3 file path
            text.assertTextNotPresent(locator.page(), "attach the application server log file");
            text.assertTextNotPresent(locator.page(), "atlassian-jira.log");
            text.assertTextNotPresent(locator.page(), "Server ID");
            text.assertTextNotPresent(locator.page(), "File Paths");
            text.assertTextNotPresent(locator.page(), "Current Working Directory");
            text.assertTextNotPresent(locator.page(), "JVM Input Arguments");



            text.assertTextNotPresent(locator.page(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
            navigation.login("admin_non_sysadmin", "admin_non_sysadmin");
            tester.gotoPage("/internal-error");
            text.assertTextNotPresent("File Paths");

            text.assertTextSequence(locator.page(),
                    "Server ID",
                    "ABN9-RZYJ-WI2T-37UF", // admins can see server ids
                    "Current Working Directory",
                    "Contact your System Administrator to discover this property value.",
                    "JVM Input Arguments",
                    "Contact your System Administrator to discover this property value.");
            text.assertTextNotPresent(locator.page(), "-Xmx"); // this shouldn't be present during tests for non sysadmin user
        } finally {
            setDevMode("true");
        }

    }

    private void assertServiceHeaderPresent(String serviceName, String serviceClass, String delay)
    {
        text.assertTextSequence(locator.page(), "Services", serviceName, serviceClass, delay, "minutes");
    }
    private void assertServiceHeaderNotPresent(String serviceName, String serviceClass)
    {
        text.assertTextNotPresent(serviceName);
        text.assertTextNotPresent(serviceClass);
    }

    private void assertServiceParamsNotVisible()
    {
        text.assertTextNotPresent(locator.page(), "usessl:");
        text.assertTextNotPresent(locator.page(), "No SSL");
        text.assertTextNotPresent(locator.page(), "popserver:");
        text.assertTextNotPresent(locator.page(), "fake server");
        text.assertTextNotPresent(locator.page(), "handler.params:");
        text.assertTextNotPresent(locator.page(), "project=hsp, issuetype=1, catchemail=sam@atlassian.com");
        text.assertTextNotPresent(locator.page(), "forwardEmail:");
        text.assertTextNotPresent(locator.page(), "fake@example.com");
        text.assertTextNotPresent(locator.page(), "handler:");
        text.assertTextNotPresent(locator.page(), "Create a new issue or add a comment to an existing issue");
    }

    private boolean isJvmWithPermGen()
    {
        return new EnvironmentUtils(tester, getEnvironmentData(), navigation).isJvmWithPermGen();
    }

}
