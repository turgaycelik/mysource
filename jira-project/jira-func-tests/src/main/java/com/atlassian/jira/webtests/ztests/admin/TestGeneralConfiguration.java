package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.w3c.dom.Node;

/**
 * Func test of editing application properties.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestGeneralConfiguration extends JIRAWebTest
{
    public TestGeneralConfiguration(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
    }

    @Override
    public void tearDown()
    {
        administration.generalConfiguration().setJiraLocaleToSystemDefault();
        super.tearDown();
    }

    public void testAjaxIssuePicker()
    {
        navigation.gotoAdminSection("general_configuration");
        //enabled by default
        assertTableCellHasText("options_table", 12, 1, "ON");

        //lets disable it
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("ajaxIssuePicker", "false");
        tester.submit("Update");
        assertTableCellHasText("options_table", 12, 1, "OFF");
    }

    public void testBaseUrlValidation()
    {
        String[] invalidURLs = {
                "",
                "http",
                "http://",
                "http://*&^%$#@",
                "http://example url.com:8090",
                "ldap://example.url.com:8090",
                "http://example.url.com:not_a_port",
                "http://example.url.com:8090/invalid path"
        };

        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");

        for (String URL : invalidURLs)
        {
            tester.setFormElement("baseURL", URL);
            tester.submit("Update");
            tester.assertTextPresent("You must set a valid base URL.");
        }
    }

    public void testMimeSnifferOptions()
    {
        navigation.gotoAdminSection("general_configuration");
        tester.assertTextPresent("Work around Internet Explorer security hole");

        tester.clickLink("edit-app-properties");
        tester.setFormElement("ieMimeSniffer", "secure");
        tester.submit("Update");
        tester.assertTextPresent("Secure: forced download of attachments for all browsers");

        tester.clickLink("edit-app-properties");
        tester.setFormElement("ieMimeSniffer", "insecure");
        tester.submit("Update");
        tester.assertTextPresent("Insecure: inline display of attachments");

        tester.clickLink("edit-app-properties");
        tester.setFormElement("ieMimeSniffer", "workaround");
        tester.submit("Update");
        tester.assertTextPresent("Work around Internet Explorer security hole");

        // hack url
        tester.gotoPage(page.addXsrfToken("/secure/admin/jira/EditApplicationProperties.jspa?title=jWebTest+JIRA+installation&mode=public&captcha=false&baseURL=http%3A%2F%2Flocalhost%3A8080%2Fjira&emailFromHeaderFormat=%24%7Bfullname%7D+%28JIRA%29&introduction=&encoding=UTF-8&language=english&defaultLocale=-1&voting=true&watching=true&allowUnassigned=false&externalUM=false&logoutConfirm=never&useGzip=false&allowRpc=false&emailVisibility=show&groupVisibility=true&excludePrecedenceHeader=false&ajaxIssuePicker=true&ajaxUserPicker=true&Update=Update"));
        tester.assertTextPresent("The MIME sniffing policy option is required.");
        tester.gotoPage(page.addXsrfToken("/secure/admin/jira/EditApplicationProperties.jspa?title=jWebTest+JIRA+installation&mode=public&captcha=false&baseURL=http%3A%2F%2Flocalhost%3A8080%2Fjira&emailFromHeaderFormat=%24%7Bfullname%7D+%28JIRA%29&introduction=&encoding=UTF-8&language=english&defaultLocale=-1&voting=true&watching=true&allowUnassigned=false&externalUM=false&logoutConfirm=never&useGzip=false&allowRpc=false&emailVisibility=show&groupVisibility=true&excludePrecedenceHeader=false&ajaxIssuePicker=true&ajaxUserPicker=true&ieMimeSniffer=_WRONGARSE%26copy;&Update=Update"));
        tester.assertTextPresent("The given value for MIME sniffing policy is invalid: _WRONGARSE&amp;copy;");
    }

    /**
     * Items in the language list should be localised to that same language
     * (e.g. "German" should be in German, "Japanese" in Japanese, etc.).
     */
    public void testLocalisesLanguageListItems()
    {
        administration.restoreData("TestUserProfileI18n.xml");
        tester.gotoPage("secure/admin/jira/EditApplicationProperties!default.jspa");

        tester.setWorkingForm("jiraform");
        final String[] languageOptions = tester.getDialog().getOptionsFor("defaultLocale");
        final String[] expectedOptions = new String[] {
                "English (Australia)",
                "Deutsch (Deutschland)",
                "\u65e5\u672c\u8a9e (\u65e5\u672c)"
        };

        // Just checking for equality won't work as the JVM's default language
        // is marked as the "default" and that could change between systems.
        for (String expectedOption : expectedOptions)
        {
            boolean found = false;
            for (String languageOption : languageOptions)
            {
                if (languageOption.indexOf(expectedOption) == 0)
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                fail();
            }
        }
    }

    public void testMaxAuthattempts()
    {
        navigation.gotoAdminSection("general_configuration");
        //enabled by default to 3
        text.assertTextSequence(xpath("//table//tr[@id='maximumAuthenticationAttemptsAllowed']"), "Maximum Authentication Attempts Allowed", "3");        

        //lets disable it
        tester.clickLink("edit-app-properties");
        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "xzl");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You must specify a number or leave it blank");

        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "0");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You cannot set the maximum authentication attempts to zero or less");
        
        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "-1");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You cannot set the maximum authentication attempts to zero or less");

        tester.setFormElement("maximumAuthenticationAttemptsAllowed", "10");
        tester.submit("Update");
        text.assertTextSequence(xpath("//table//tr[@id='maximumAuthenticationAttemptsAllowed']"), "Maximum Authentication Attempts Allowed", "10");
    }

    public void testMaxProjectNameLength()
    {
        navigation.gotoAdminSection("general_configuration");
        //enabled by default to 80
        text.assertTextSequence(xpath("//table//tr[@id='maximumLengthProjectNames']"), "Maximum project name size", "80");

        tester.clickLink("edit-app-properties");
        tester.setFormElement("maximumLengthProjectNames", "xzl");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You must specify a number or leave it blank");

        tester.setFormElement("maximumLengthProjectNames", "0");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "The maximum project name length must be greater than 1.");

        tester.setFormElement("maximumLengthProjectKeys", "-1");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "The maximum project name length must be greater than 1.");
    }

    public void testMaxProjectKeyLength()
    {
        navigation.gotoAdminSection("general_configuration");
        //enabled by default to 10
        text.assertTextSequence(xpath("//table//tr[@id='maximumLengthProjectKeys']"), "Maximum project key size", "10");

        tester.clickLink("edit-app-properties");
        tester.setFormElement("maximumLengthProjectKeys", "xzl");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "You must specify a number or leave it blank");

        tester.setFormElement("maximumLengthProjectKeys", "0");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "The maximum project key length must be greater than 1.");

        tester.setFormElement("maximumLengthProjectKeys", "-1");
        tester.submit("Update");
        text.assertTextPresent(xpath("//form[@name='jiraform']//span[@class='errMsg']"), "The maximum project key length must be greater than 1.");
    }

    /**
     * JRA-23891 Test that a language not in the defaultLocales fails validation
     */
    public void testLocaleWhitelistValidation()
    {
        String[] invalidLocales = new String[] {
            "cs_CZ",
            "AF_ZA",
            "<script>alert()</script>"
        };
        administration.restoreData("TestUserProfileI18n.xml");
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        // need to use FormParameterUtils as you can't submit invalid options through WebUnit
        for (String locale : invalidLocales)
        {
            FormParameterUtil formHelper = new FormParameterUtil(tester, "jiraform", "Update");
            formHelper.setFormElement("defaultLocale", locale);
            Node document = formHelper.submitForm();
            Locator locator = new XPathLocator(document,"//*[@class='errMsg']" );

            assertTrue(locator.getText().contains(String.format("Locale '%s' is not a valid locale.",locale)));
            tester.reset();
        }
    }
}