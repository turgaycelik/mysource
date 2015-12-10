package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.io.IOException;

/**
 * Test some basic operations in JIRA in German.
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestBasic extends JIRAWebTest
{
    private static final String NEW_FEATURE_I18N_KEY="jira.translation.issuetype.newfeature.name";
    private static final String GERMAN_LOCALE = "de_DE";
    private static final String IMPROVEMENT_I18N_KEY = "jira.translation.issuetype.improvement.name";
    private static final String TASK_I18N_KEY = "jira.translation.issuetype.task.name";

    public TestBasic(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void tearDown()
    {
        navigation.userProfile().changeUserLanguageToJiraDefault();
        super.tearDown();
    }

    public void testI18NDates() throws IOException
    {
        setLocaleTo("Deutsch (Deutschland)");

        // create an issue with a valid due date
        createIssueInGermanWithDueDate("25/Dez/05");
        assertTextPresent("Es liegen noch keine Kommentare zu diesem Vorgang vor.");

        // create an issue with an invalid due date
        createIssueInGermanWithDueDate("25/Dec/05");
        assertTextPresent("Datum eingegeben. Geben Sie das Datum im Format");
    }

    public void testIssueConstantTranslations()
    {
        setLocaleTo("Deutsch (Deutschland)");
        // reset the translation to blank
        updateBugTranslationWith("", "");

        // browse to the admin section and make sure that we see the issue constants as translated
        browseToCustomFieldAdd();

        assertTextPresent("Alle Vorgangstypen");
        assertTextPresent("Bug");
        assertTextPresent(getBackdoor().i18n().getText(IMPROVEMENT_I18N_KEY, GERMAN_LOCALE));
        assertTextPresent(getBackdoor().i18n().getText(NEW_FEATURE_I18N_KEY, GERMAN_LOCALE));
        assertTextPresent(getBackdoor().i18n().getText(TASK_I18N_KEY, GERMAN_LOCALE));

        // add a translation via the GUI, confirm present in place of the default properties
        updateBugTranslationWith("bugenzee", "bugenzee desc");

        browseToCustomFieldAdd();
        text.assertTextPresent(locator.css(".jiraform"), "bugenzee");
        text.assertTextNotPresent(locator.css(".jiraform"), "Fehler");
    }

    private void updateBugTranslationWith(String name, String desc)
    {
        gotoAdmin();
        clickLink("issue_types");
        clickLink("translate_link");
        setWorkingForm("update");
        setFormElement("jira.translation.Vorgangstyp.1.name", name);
        setFormElement("jira.translation.Vorgangstyp.1.desc", desc);
        submit();
    }

    private void browseToCustomFieldAdd()
    {
        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textarea");
        submit(BUTTON_NAME_NEXT);
    }

    private void createIssueInGermanWithDueDate(String dueDate)
    {
        navigation.issue().
                goToCreateIssueForm(PROJECT_HOMOSAP, getBackdoor().i18n().getText(NEW_FEATURE_I18N_KEY, GERMAN_LOCALE));

        setWorkingForm("issue-create");
        setFormElement("duedate", dueDate);
        setFormElement("summary", "test issue");
        assertFormElementHasValue("issue-create", "issue-create-submit", "Erstellen");
        submit("Create");
    }

    private void setLocaleTo(String localeName)
    {
        navigation.userProfile().changeUserLanguage(localeName);
    }
}