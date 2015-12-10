package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestCrowdServerConfiguration extends FuncTestCase
{
    private static final String MY_APP = "My App";
    private static final String MY_APP_ADDRESS = "192.168.0.1";

    public void testAddApplication() throws Exception
    {
        tester.clickLink("crowd-add-application");

        final String appName = "FishEye";
        final String appAddress = "127.0.0.1";

        // first try to create with invalid values
        submitEditForm("", "", "not_an_address");
        text.assertTextPresent(locator.css("#edit-crowd-application-name-error"), "You must provide an application name.");
        text.assertTextPresent(locator.css("#edit-crowd-application-credential-error"), "A password is required.");
        text.assertTextPresent(locator.css("#edit-crowd-application-remoteAddresses-error"), "'not_an_address' is not a valid IP address.");

        // fill in the form, then check that the app has been created and is shown in the list
        submitEditForm(appName, "pass", appAddress);
        assertAppPresent(appName, appAddress);
    }

    /*
     * Tests that we can add an app, then edit it, and then delete it.
     */
    public void testEditApplication() throws Exception
    {
        final String newAppName = "MyNewApp";
        final String newAppAddress = "127.0.0.127";

        // try to edit with invalid values
        tester.clickLink("crowd-edit-application-" + MY_APP);
        submitEditForm("", "", "not_an_address");
        text.assertTextPresent(locator.css("#edit-crowd-application-name-error"), "You must provide an application name.");
        text.assertTextNotPresent(locator.css("#edit-crowd-application-credential-error"), "A password is required.");
        text.assertTextPresent(locator.css("#edit-crowd-application-remoteAddresses-error"), "'not_an_address' is not a valid IP address.");

        // rename the application, then check that the app has been renamed (even if no password provided)
        submitEditForm(newAppName, "", newAppAddress);
        assertAppPresent(newAppName, newAppAddress);
        assertAppNotPresent(MY_APP, MY_APP_ADDRESS);
    }

    /*
     * Tests that we can add an app, then edit it, and then delete it.
     */
    public void testDeleteApplication() throws Exception
    {
        tester.clickLink("crowd-delete-application-" + MY_APP);
        assertAppNotPresent(MY_APP, MY_APP_ADDRESS);
    }

    protected void setUpTest()
    {
        administration.restoreData("TestCrowdServerConfiguration.xml");
        navigation.gotoPage("secure/project/ConfigureCrowdServer.jspa");
    }

    private void submitEditForm(String name, String credential, String remoteAddresses)
    {
        tester.setWorkingForm("edit-crowd-application");
        tester.setFormElement("name", name);
        tester.setFormElement("credential", credential);
        tester.setFormElement("remoteAddresses", remoteAddresses);
        tester.clickButton("edit-crowd-application-submit");
    }

    private void assertAppPresent(String appName, String appAddress)
    {
        String appRowLocator = String.format("#crowd-app-%s", appName);
        text.assertTextPresent(locator.css(appRowLocator + " td[headers=application]"), appName);
        text.assertTextPresent(locator.css(appRowLocator + " td[headers=address]"), appAddress);
    }

    private void assertAppNotPresent(String appName, String appAddress)
    {
        String appRowLocator = String.format("#crowd-app-%s", appName);
        text.assertTextNotPresent(locator.css(appRowLocator + " td[headers=application]"), appName);
        text.assertTextNotPresent(locator.css(appRowLocator + " td[headers=address]"), appAddress);
    }
}
