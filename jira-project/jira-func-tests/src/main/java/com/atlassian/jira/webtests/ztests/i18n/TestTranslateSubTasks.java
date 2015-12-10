package com.atlassian.jira.webtests.ztests.i18n;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1.1
 */
@WebTest ({ Category.FUNC_TEST, Category.I18N })
public class TestTranslateSubTasks extends FuncTestCase
{

    @Override
    public void tearDownTest()
    {
        administration.generalConfiguration().setJiraLocaleToSystemDefault();
        super.tearDownTest();
    }

    /**
     * A user with no predefined language gets the language options in the system's default language
     */
    public void testShowsLanguageListInDefaultLanguage()
    {
        administration.restoreData("TestTranslateSubTasks.xml");

        administration.subtasks().enable();

        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.gotoAdminSection("subtasks");
        tester.clickLink("translate_sub_tasks");

        // assert that the page defaults to German
        text.assertTextPresent("Sprache: Deutsch (Deutschland)");

        // assert that the list of languages has German selected by default
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("selectedLocale", "Deutsch (Deutschland)");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestTranslateSubTasks.xml");

        administration.subtasks().enable();

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.login(FRED_USERNAME);

        navigation.gotoAdminSection("subtasks");
        tester.clickLink("translate_sub_tasks");

        // assert that the page defaults to Spanish
        text.assertTextPresent("Lenguaje: espa\u00f1ol (Espa\u00f1a)");

        // assert that the list of languages has Spanish selected by default
        // note that there is a difference with how HttpUnit handles unicode for getResponseText (above) and getSelectedOption()!
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("selectedLocale", "espa\u00f1ol (Espa\u00f1a)");
    }
}
