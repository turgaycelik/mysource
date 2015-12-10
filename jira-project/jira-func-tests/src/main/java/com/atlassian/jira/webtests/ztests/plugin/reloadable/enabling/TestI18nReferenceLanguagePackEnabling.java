package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * Test the enabling of the language pack modules.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestI18nReferenceLanguagePackEnabling  extends AbstractReloadablePluginsTest
{
    public void testReferenceLanguagePackLoadedAndContainsProziekty()
    {
        try
        {
            administration.generalConfiguration().setJiraLocale("Polish (Poland)");
        }
        catch (Exception e)
        {
            // arse. Locale is set to Polish on my box and I can't seem
            // to be able to make JIRA speak English to me by default
            administration.generalConfiguration().setJiraLocale("polski (Polska)");
        }

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // "Projects" string is intentionally misspelled
        // in the reference language pack (Polish) 
        // and correct in the V1 Polish language pack

        administration.plugins().referenceLanguagePack().disable();
        goToAdminPageAndCheckText("Projekty", "Proziekty");

        administration.plugins().referenceLanguagePack().enable();
        goToAdminPageAndCheckText("Proziekty", "Projekty");

        navigation.logout();
    }

    private void goToAdminPageAndCheckText(final String toFind, final String toNotFind)
    {
        tester.gotoPage("/secure/project/ViewProjects.jspa");
        text.assertTextPresent(toFind);
        text.assertTextNotPresent(toNotFind);
    }
}
