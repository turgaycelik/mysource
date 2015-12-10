package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.CUSTOM_FIELDS })
public class TestCustomFieldMultiUserPicker extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testCreateAndSetDefaultValue()
    {
        navigation.gotoAdminSection("view_custom_fields");

        // add Multi User Picker
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker");
        tester.submit("nextBtn");

        // set Name to 'Developers'
        tester.setFormElement("fieldName", "Developers");
        tester.submit("nextBtn");

        // associate with 'Default' screen
        tester.checkCheckbox("associatedScreens", "1");
        tester.submit("Update");

        // test the new field is present on the page
        tester.assertTextPresent("Custom Fields");
        text.assertTextPresent(locator.id("custom-fields-customfield_10000-name"),"Developers");
        text.assertTextPresent(locator.id("custom-fields-customfield_10000-type"),"User Picker (multiple users)");

        tester.clickLink("config_customfield_10000");

        // configure - set empty default value - already set by default
        tester.clickLink("customfield_10000-edit-default");
        tester.submit("Set Default");
        tester.assertTextPresent("Default Configuration Scheme for Developers");
        tester.assertTextNotPresent(FRED_USERNAME);

        // configure - set default value to Fred
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_10000", "");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent("Default Configuration Scheme for Developers");
        tester.assertTextPresent(FRED_FULLNAME);

        // configure - set default value to Admin and Fred
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_10000", FRED_USERNAME);
        tester.setFormElement("customfield_10000", "fred, admin");
        tester.submit("Set Default");
        text.assertTextPresent(locator.xpath("//*[@id='configscheme10010']//*[@class='formtitle']"),
                "Default Configuration Scheme for Developers");
        text.assertTextSequence(locator.id("customfield_10000-field"), ADMIN_FULLNAME, FRED_FULLNAME);

        // configure - set default value to Admin
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_" + "10000", "admin, fred"); // is sorted
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for Developers");

        // configure - clear default value
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_" + "10000", ADMIN_USERNAME);
        tester.setFormElement("customfield_" + "10000", "");
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for Developers");

        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_10000", "");
    }

}
