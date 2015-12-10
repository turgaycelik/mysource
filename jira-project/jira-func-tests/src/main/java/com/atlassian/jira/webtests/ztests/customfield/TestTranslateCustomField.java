package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestTranslateCustomField extends FuncTestCase
{

    public static final String NAME_EN = "MY TEXT FIELD";
    public static final String NAME_FR = "Mon champ de texte";
    public static final String METTRE_A_JOUR = "Mettre \u00E0 jour";

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
        administration.restoreData("TestTranslateCustomField.xml");

        backdoor.userProfile().changeUserLanguage(ADMIN_USERNAME, "fr_FR");

        // assert initial descriptions
        assertFieldDescription("homosapien", "Bogue", "The default description of MY TEXT FIELD", NAME_EN);
        assertFieldDescription("monkey", "Bogue", "A customised description for Monkey", NAME_EN);

        // Translate the custom field description
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("translate_customfield_10000");

        tester.setFormElement("description", "Une nouvelle description");
        tester.submit(METTRE_A_JOUR);

        // assert homosapien (which uses default field configuration) has new custom field description
        assertFieldDescription("homosapien", "Bogue", "Une nouvelle description", NAME_EN);
        // assert monkey (which uses custom field configuration) retains its old description
        assertFieldDescription("monkey", "Bogue", "A customised description for Monkey", NAME_EN);

        // Translate the custom field name also description
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("translate_customfield_10000");

        tester.setFormElement("name", NAME_FR);
        tester.submit(METTRE_A_JOUR);

        // assert homosapien (which uses default field configuration) has new custom field description
        assertFieldDescription("homosapien", "Bogue", "Une nouvelle description", NAME_FR);
        // assert monkey (which uses custom field configuration) retains its old description
        assertFieldDescription("monkey", "Bogue", "A customised description for Monkey", NAME_FR);

        // Test the english is still default
        backdoor.userProfile().changeUserLanguage(ADMIN_USERNAME, "en_UK");

        // assert initial descriptions
        assertFieldDescription("homosapien", "Bug", "The default description of MY TEXT FIELD", NAME_EN);
        assertFieldDescription("monkey", "Bug", "A customised description for Monkey", NAME_EN);

        // Test the german is still default
        backdoor.userProfile().changeUserLanguage(ADMIN_USERNAME, "de_DE");

        // assert initial descriptions
        assertFieldDescription("homosapien", "Bug", "The default description of MY TEXT FIELD", NAME_EN);
        assertFieldDescription("monkey", "Bug", "A customised description for Monkey", NAME_EN);

        // Now clear the French values
        backdoor.userProfile().changeUserLanguage(ADMIN_USERNAME, "fr_FR");

        // Translate the custom field description
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("translate_customfield_10000");

        tester.setFormElement("name", "");
        tester.setFormElement("description", "");
        tester.submit(METTRE_A_JOUR);

        // assert initial descriptions
        assertFieldDescription("homosapien", "Bogue", "The default description of MY TEXT FIELD", NAME_EN);
        assertFieldDescription("monkey", "Bogue", "A customised description for Monkey", NAME_EN);

        backdoor.userProfile().changeUserLanguage(ADMIN_USERNAME, "en_UK");

    }

    private void editDescription(final String fieldConfigName, final String description)
    {
        navigation.gotoAdminSection("field_configuration");
        tester.clickLink("configure-" + fieldConfigName);
        tester.clickLink("edit_10");
        tester.setFormElement("description", description);
        tester.submit("Update");
    }

    private void assertFieldDescription(final String projectName, final String issueType, final String description, final String name)
    {
        final String key = navigation.issue().createIssue(projectName, issueType, "Summary");
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { name, description });
    }

    private void assertNoFieldDescription(final String projectName, final String issueType, final String oldDescription)
    {
        final String key = navigation.issue().createIssue(projectName, issueType, "Summary");
        navigation.issue().viewIssue(key);
        tester.clickLink("edit-issue");
        text.assertTextPresent(new WebPageLocator(tester), NAME_EN);
        text.assertTextNotPresent(new WebPageLocator(tester), oldDescription);
    }
}
