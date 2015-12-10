package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some simple tests to make sure the services admin pages do not explode.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestServices extends FuncTestCase
{

    private static final String SERVICE_CLASS = "com.atlassian.jira.service.services.DebugService";
    private static final String SERVICE_NAME = "Test Debug Service";
    private static final String SERVICE_PARAM_DESC_1 = "How long to run";
    private static final String SERVICE_PARAM_DESC_2 = "Debug param one";
    private static final String HELLO_WORLD_1 = "hello world 1";
    private static final String HELLO_WORLD_2 = "hello world 2";
    private static final String GOODBYE_WORLD_1 = "goodbye world 1";

    public void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testServiceRunner()
    {
        administration.utilities().runServiceNow(10000);
    }

    public void testAddServiceWithParams()
    {
        navigation.gotoAdmin();
        tester.clickLink("services");
        tester.setFormElement("name", SERVICE_NAME);
        tester.setFormElement("clazz", SERVICE_CLASS);
        tester.submit("Add Service");

        tester.assertTextPresent(SERVICE_PARAM_DESC_1);
        tester.assertTextPresent(SERVICE_PARAM_DESC_2);

        final String urlString = tester.getDialog().getResponse().getURL().toExternalForm();
        final Matcher matcher = Pattern.compile("EditService!default.jspa?.*id=(\\d+)").matcher(urlString);

        if (!matcher.find())
        {
            fail("Did not get redirected to EditService!default.jspa");
        }

        final long id = Long.parseLong(matcher.group(1));        

        tester.setFormElement("debug param one", HELLO_WORLD_1);
        tester.setFormElement("debug param two", HELLO_WORLD_2);
        tester.setFormElement("delay", "4");
        tester.submit("Update");

        tester.assertTextPresent(HELLO_WORLD_2);
        tester.assertTextPresent(HELLO_WORLD_1);

        // Now test the edit
        tester.clickLink("edit_" + id);
        tester.setFormElement("debug param one", GOODBYE_WORLD_1);
        tester.submit("Update");

        tester.assertTextPresent(GOODBYE_WORLD_1);
        tester.assertTextNotPresent(HELLO_WORLD_1);

        // Now delete the service we just added.
        tester.clickLink("del_" + id);

        // Now make sure the service is not there anymore
        tester.assertTextNotPresent(HELLO_WORLD_2);
        tester.assertTextNotPresent(HELLO_WORLD_1);
    }

    public void testZeroOrNegativeDelayIsInvalid() throws Exception
    {
        navigation.gotoAdmin();
        tester.clickLink("services");

        // assert we are still on the Services page
        tester.assertTextPresent("Add Service");
        tester.assertTextPresent("Add a new service by entering a name and class below. You can then edit it to set properties.");
        tester.assertTextNotPresent("Please specify a delay longer than one minute.");

        // check with 0 delay
        tester.setFormElement("name", SERVICE_NAME);
        tester.setFormElement("clazz", SERVICE_CLASS);
        tester.setFormElement("delay", "0");
        tester.submit("Add Service");

        // assert we are still on the Services page
        tester.assertTextPresent("Add Service");
        tester.assertTextPresent("Add a new service by entering a name and class below. You can then edit it to set properties.");
        tester.assertTextPresent("Please specify a delay longer than one minute.");

        // check with negative delay
        tester.setFormElement("delay", "-11");
        tester.submit("Add Service");

        // assert we are still on the Services page
        tester.assertTextPresent("Add Service");
        tester.assertTextPresent("Add a new service by entering a name and class below. You can then edit it to set properties.");
        tester.assertTextPresent("Please specify a delay longer than one minute.");

        // ok, now a good one
        tester.setFormElement("delay", "10");
        tester.submit("Add Service");

        // assert we are on edit service page
        tester.assertTextPresent("Edit Service");
        tester.assertTextPresent(SERVICE_NAME);
        tester.assertTextNotPresent("Please specify a delay longer than one minute.");

        // check with 0 delay
        tester.setFormElement("delay", "0");
        tester.submit("Update");

        // assert we are still on the Services page
        tester.assertTextPresent("Edit Service");
        tester.assertTextPresent(SERVICE_NAME);
        tester.assertTextPresent("Please specify a delay longer than one minute.");

        // check with negative delay
        tester.setFormElement("delay", "-1");
        tester.submit("Update");

        // assert we are still on the Services page
        tester.assertTextPresent("Edit Service");
        tester.assertTextPresent(SERVICE_NAME);
        tester.assertTextPresent("Please specify a delay longer than one minute.");
    }
}
