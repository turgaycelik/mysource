package com.atlassian.jira.webtests.ztests.i18n;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.I18N })
public class TestI18n500Page extends FuncTestCase
{
    public static final String USERNAME_NON_SYS_ADMIN = "admin_non_sysadmin";
    public static final String PASSWORD_NON_SYS_ADMIN = "admin_non_sysadmin";

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreI18nData("TestI18n.xml");
    }

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
        administration.restoreData("TestUserProfileI18n.xml");

        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        tester.gotoPage("/internal-error");

        // assert that the page defaults to German
        final int lastRow = page.getHtmlTable("language-info").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow, 1), "Deutsch (Deutschland)");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.login(FRED_USERNAME);

        tester.gotoPage("/internal-error");

        // assert that the page defaults to Spanish
        final int lastRow = page.getHtmlTable("language-info").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow, 1), "alem\u00e1n (Alemania)");
        text.assertTextPresent(new TableCellLocator(tester, "language-info", lastRow - 1, 0), "espa\u00f1ol (Espa\u00f1a)");
    }
}

