/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.CUSTOM_FIELDS })
public class TestRadioSelectCustomFieldOptions extends JIRAWebTest
{
    private static final String CUSTOM_FIELD_ID = "10006";
    private static final String CUSTOMFIELD_LONG_ID = "customfield_" + CUSTOM_FIELD_ID;
    private static final String OPTION_00 = "10019";
    private static final String OPTION_01 = "10020";
    private static final String OPTION_02 = "10021";

    public TestRadioSelectCustomFieldOptions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        // Clean JIRA instance
        administration.restoreData("TestSelectCustomFieldOptions.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testEditOptions() throws SAXException
    {
        // Edit options
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Honda");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Kawasaki");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Yamaha");

        // We should be on the custom field edit page and should be able to see the option text
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=10006");
        assertTextPresent("Honda");
        assertTextPresent("Kawasaki");
        assertTextPresent("Yamaha");

        // Should be able to change these again
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Fiat");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Lambretta");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Vespa");

        // We should be on the custom field edit page and should be able to see the option text
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=10006");
        assertTextPresent("Fiat");
        assertTextPresent("Lambretta");
        assertTextPresent("Vespa");
    }

    public void testDefaultShowsUpdatedOption() throws SAXException
    {
        // Set the default
        administration.customFields().setDefaultValue(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=10006");
        // Check the default is set
        locator.id(CUSTOMFIELD_LONG_ID + "-value-default").getText().contains("option 00");

        // Edit options
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Honda");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Kawasaki");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Yamaha");
        // Check the default now says Honda
        locator.id(CUSTOMFIELD_LONG_ID + "-value-default").getText().contains("Honda");
    }

    public void testDisable() throws SAXException
    {
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=10006");
        assertTextSequence(new String[] { "option 00", "(disabled)" });
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=10006");
        assertTextNotPresent("(disabled)");
    }

    public void testSearchNewValues() throws SAXException
    {
        // Edit options
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Honda");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Kawasaki");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Yamaha");
        // Check the default now says Honda
        locator.id(CUSTOMFIELD_LONG_ID + "-value-default").getText().contains("Honda");

        administration.reIndex();
        // Search for Hondas, there should be 1
        createSearchAndAssertIssues("RadioButtonsField = Honda ORDER BY key ASC", "HSP-11");
        // Search for Kawasaki, there should be 2
        createSearchAndAssertIssues("RadioButtonsField = Kawasaki ORDER BY key ASC", "HSP-10");
    }

    public void testDisabledNotValidForCreate() throws SAXException
    {
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        // Check radio buttons present
        assertTrue(!locator.id(CUSTOMFIELD_LONG_ID + "-1").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-2").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-3").exists());

        // Enable "option 00"
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        // Check radio buttons present
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-1").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-2").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-3").exists());
    }

    public void testDisabledNotValidForEdit() throws SAXException
    {
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to edit issue option 00 not already the selected value.
        navigation.issue().gotoEditIssue("HSP-10");
        // Check selectList values
        // Check radio buttons present
        assertTrue(!locator.id(CUSTOMFIELD_LONG_ID + "-1").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-2").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-3").exists());
        // go to edit issue option 00 is already the selected value.
        navigation.issue().gotoEditIssue("HSP-11");
        // Check radio buttons present
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-1").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-2").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-3").exists());

        // Enable "option 00"
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to edit issue option 00 not already the selected value.
        navigation.issue().gotoEditIssue("HSP-10");
        // Check radio buttons present
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-1").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-2").exists());
        assertTrue(locator.id(CUSTOMFIELD_LONG_ID + "-3").exists());
    }

    public void testDisabledValidForSearch() throws SAXException
    {
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to edit issue option 00 not already the selected value.
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        // Check radio buttons present
        assertTextInElement("jqlFieldz", "cf[" + CUSTOM_FIELD_ID + "]");
    }

    private void createSearchAndAssertIssues(String jqlQuery, String...keys)
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(keys);
    }

}
