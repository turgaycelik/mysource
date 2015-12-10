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
public class TestMultiSelectCustomFieldOptions extends JIRAWebTest
{
    private static final String CUSTOM_FIELD_ID = "10004";
    private static final String OPTION_00 = "10015";
    private static final String OPTION_01 = "10016";
    private static final String OPTION_02 = "10017";
    private static final String OPTION_03 = "10018";

    public TestMultiSelectCustomFieldOptions(String name)
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
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_03, "Suzuki");

        // We should be on the custom field edit page and should be able to see the option text
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + CUSTOM_FIELD_ID);
        assertTextPresent("Honda");
        assertTextPresent("Kawasaki");
        assertTextPresent("Yamaha");
        assertTextPresent("Suzuki");

        // Should be able to change these again
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Fiat");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Lambretta");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Vespa");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_03, "Aprilia");

        // We should be on the custom field edit page and should be able to see the option text
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + CUSTOM_FIELD_ID);
        assertTextPresent("Fiat");
        assertTextPresent("Lambretta");
        assertTextPresent("Vespa");
        assertTextPresent("Aprilia");
    }

    public void testDefaultShowsUpdatedOption() throws SAXException
    {
        // Set the default
        administration.customFields().setDefaultValue(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + CUSTOM_FIELD_ID);
        // Check the default is set
        locator.id("customfield_" + CUSTOM_FIELD_ID + "-value-default").getText().contains("option 00");

        // Edit options
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Fiat");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Lambretta");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Vespa");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_03, "Aprilia");
        // Check the default now says Honda
        locator.id("customfield_" + CUSTOM_FIELD_ID + "-value-default").getText().contains("Honda");
    }

    public void testDisable() throws SAXException
    {
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + CUSTOM_FIELD_ID);
        assertTextSequence(new String[] { "option 00", "(disabled)" });
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + CUSTOM_FIELD_ID);
        assertTextNotPresent("(disabled)");
    }

    public void testSearchNewValues() throws SAXException
    {
        // Edit options
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_00, "Honda");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_01, "Kawasaki");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_02, "Yamaha");
        administration.customFields().editOptionValue(CUSTOM_FIELD_ID, OPTION_03, "Suzuki");
        // Check the default now says Honda
        locator.id("customfield_" + CUSTOM_FIELD_ID + "-value-default").getText().contains("Honda");

        administration.reIndex();
        // Search for Hondas, there should be 3
        createSearchAndAssertIssues("MultiSelectField = Honda ORDER BY key ASC", "HSP-10", "HSP-11", "HSP-12");
        // Search for Kawasaki, there should be 2
        createSearchAndAssertIssues("MultiSelectField = Kawasaki ORDER BY key ASC", "HSP-12");
    }

    public void testDisabledNotValidForCreate() throws SAXException
    {
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        // Check selectList values
        tester.assertOptionsEqual("customfield_" + CUSTOM_FIELD_ID, new String[] { "None", "option 01", "option 02", "option 03" });

        // Enable "option 00"
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to create issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        // Check selectList values
        tester.assertOptionsEqual("customfield_" + CUSTOM_FIELD_ID, new String[] { "None", "option 00", "option 01", "option 02", "option 03"  });
    }

    public void testDisabledNotValidForEdit() throws SAXException
    {
        // Disable "option 01"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_01);
        // go to edit issue option 00 not already the selected value.
        navigation.issue().gotoEditIssue("HSP-11");
        // Check selectList values
        tester.assertOptionsEqual("customfield_" + CUSTOM_FIELD_ID, new String[] { "None", "option 00", "option 02", "option 03"  });
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to edit issue option 00 not already the selected value.
        navigation.issue().gotoEditIssue("HSP-11");
        // Check selectList values
        tester.assertOptionsEqual("customfield_" + CUSTOM_FIELD_ID, new String[] { "None", "option 00", "option 02", "option 03"  });

        // Enable "option 01"
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_00);
        administration.customFields().enableOptions(CUSTOM_FIELD_ID, OPTION_01);
        // go to edit issue option 00 not already the selected value.
        navigation.issue().gotoEditIssue("HSP-11");
        // Check selectList values
        tester.assertOptionsEqual("customfield_" + CUSTOM_FIELD_ID, new String[] { "None", "option 00", "option 01", "option 02", "option 03"  });
    }

    public void testDisabledValidForSearch() throws SAXException
    {
        // Disable "option 00"
        administration.customFields().disableOptions(CUSTOM_FIELD_ID, OPTION_00);
        // go to edit issue option 00 not already the selected value.
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        // Check selectList values
        tester.gotoPage("secure/QueryComponentRendererEdit!Default.jspa?fieldId=customfield_" + CUSTOM_FIELD_ID + "&decorator=none&jqlContext=");
        assertTextPresent("option 00");
        assertTextPresent("option 01");
        assertTextPresent("option 02");
        assertTextPresent("option 03");
    }

    private void createSearchAndAssertIssues(String jqlQuery, String...keys)
    {
        //Make sure we find the issues in the past.
        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(keys);
    }

}
