package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 * Test for the REST view of issues
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.REST })
public class TestViewIssueWithRest extends FuncTestCase
{
    public void setUpTest()
    {
        administration.restoreData("TestAssignToMe.xml");
    }

    public void testRestViewIsTranslated() throws IOException
    {
        changeLanguageViewToFrench();
        translateBugToFrench();
        changeUserLanguage();
        tester.gotoPage("/rest/api/2/issue/MKY-1");
        tester.assertTextPresent("French_Bug");
        tester.assertTextPresent("French_Desc");
    }

    private void changeLanguageViewToFrench() throws IOException
    {
        tester.gotoPage("/secure/admin/ViewTranslations!default.jspa?issueConstantType=issuetype");
        tester.setWorkingForm("changeTranslationLocale");
        tester.setFormElement("selectedLocale", "fr_FR");
        tester.submit("view");
        tester.assertTextPresent("Translation Language: French (France)");
    }

    private void translateBugToFrench() throws IOException
    {
        tester.setWorkingForm("update");
        tester.setFormElement("jira.translation.Issue Type.1.name", "French_Bug");
        tester.setFormElement("jira.translation.Issue Type.1.desc", "French_Desc");
        tester.submit("update");
        tester.assertTextPresent("French_Bug");
        tester.assertTextPresent("French_Desc");
    }

    private void changeUserLanguage() throws IOException
    {
        tester.gotoPage("/secure/ViewProfile.jspa");
        tester.clickLink("edit_prefs_lnk");
        tester.setWorkingForm("update-user-preferences");
        tester.setFormElement("userLocale", "fr_FR");
        tester.submit();
        tester.assertTextPresent("fran\u00e7ais (France)");
    }
}
