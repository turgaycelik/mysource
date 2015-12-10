package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestEditCustomFieldDescription extends FuncTestCase
{
    private static class Data
    {
        static class IssueTypes
        {
            static class Bug
            {
                static String id()
                {
                    return "1";
                }
            }
        }
    }
    public void testEditFieldConfigurations() throws Exception
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");

        // assert initial descriptions
        assertFieldDescription("homosapien", "Bug", "The default description of MY TEXT FIELD");
        assertFieldDescription("monkey", "Bug", "A customised description for Monkey");

        // modify the custom field's description directly
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("edit_MY TEXT FIELD");
        tester.setFormElement("description", "A new description");
        tester.submit("Update");

        // assert homosapien (which uses default field configuration) has new custom field description
        assertFieldDescription("homosapien", "Bug", "A new description");
        // assert monkey (which uses custom field configuration) retains its old description
        assertFieldDescription("monkey", "Bug", "A customised description for Monkey");

        // modify custom field description through default field configuration
        editDescription("Default Field Configuration", "A newer description");

        // assert homosapien (which uses default field configuration) has new custom field description
        assertFieldDescription("homosapien", "Bug", "A newer description");
        // assert monkey (which uses custom field configuration) retains its old description
        assertFieldDescription("monkey", "Bug", "A customised description for Monkey");

        // modify custom field description for non default field configuration
        editDescription("Field Configuration 1", "Fantastic new description");

        // assert homosapien (which uses default field configuration) has not changed
        assertFieldDescription("homosapien", "Bug", "A newer description");
        // assert monkey (which uses custom field configuration) has changed
        assertFieldDescription("monkey", "Bug", "Fantastic new description");

        // clear description for non default field configuration
        editDescription("Field Configuration 1", "");

        // assert homosapien (which uses default field configuration) has not changed
        assertFieldDescription("homosapien", "Bug", "A newer description");
        // assert monkey (which uses custom field configuration) has changed
        assertNoFieldDescription("monkey", "Bug", "Fantastic new description");

        // copy the default field configuration
        navigation.gotoAdminSection("field_configuration");
        tester.clickLink("copy-Default Field Configuration");
        tester.setFormElement("fieldLayoutName", "Copy of Default Field Configuration");
        tester.submit("Copy");

        // configure it for Non Standard Field Configuration Scheme with Bug issue type
        administration.fieldConfigurationSchemes().
                fieldConfigurationScheme("Non Standard Field Configuration Scheme").
                addAssociation(Data.IssueTypes.Bug.id(), "Copy of Default Field Configuration");

        // change description for copy of default field configuration
        editDescription("Copy of Default Field Configuration", "The newest description");
        editDescription("Field Configuration 1", "Something completely different");

        // assert homosapien (which uses default field configuration) has not changed
        assertFieldDescription("homosapien", "Bug", "A newer description");
        // assert monkey/Bug (which uses Copy of Default Field Configuration) has changed
        assertFieldDescription("monkey", "Bug", "The newest description");
        // assert monkey/Task (which uses Field Configuration 1) has not changed
        assertFieldDescription("monkey", "Task", "Something completely different");

        // Remove the description in the default field configuration
        editDescription("Default Field Configuration", "");
        // assert homosapien (which uses default field configuration) displays teh description of the field
        // as there is no field configuration specific description
        assertFieldDescription("homosapien", "Bug", "A new description");

    }

    private void editDescription(final String fieldConfigName, final String description)
    {
        navigation.gotoAdminSection("field_configuration");
        tester.clickLink("configure-" + fieldConfigName);
        tester.clickLink("edit_10");
        tester.setFormElement("description", description);
        tester.submit("Update");
    }

    private void assertFieldDescription(final String projectName, final String issueType, final String description)
    {
        final String key = navigation.issue().createIssue(projectName, issueType, "Summary");
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        text.assertTextSequence(new WebPageLocator(tester), new String[] {"MY TEXT FIELD", description });
    }

    private void assertNoFieldDescription(final String projectName, final String issueType, final String oldDescription)
    {
        final String key = navigation.issue().createIssue(projectName, issueType, "Summary");
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        text.assertTextPresent(new WebPageLocator(tester), "MY TEXT FIELD");
        text.assertTextNotPresent(new WebPageLocator(tester), oldDescription);
    }
}
