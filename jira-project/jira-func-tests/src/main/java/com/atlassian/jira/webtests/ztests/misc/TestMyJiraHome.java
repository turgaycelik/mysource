package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

@WebTest ( { Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestMyJiraHome extends FuncTestCase
{

    public void testHome() throws SAXException
    {
        administration.restoreBlankInstance();

        // turn on the dark feature
        backdoor.darkFeatures().enableForSite("atlassian.darkfeature.jira.myjirahome");
        navigation.login("admin");

        // set home to issue navigator and then login again
        tester.clickLink("set_my_jira_home_issuenav");
        assertTrue("Expected to be on issue navigator", tester.getDialog().getResponsePageTitle().contains("Issue Navigator"));

        navigation.logout();
        navigation.login("admin");
        // assert we are on the isue nav page
        assertTrue("Expected to be on issue navigator", tester.getDialog().getResponsePageTitle().contains("Issue Navigator"));


        navigation.logout();
        navigation.login("fred");
        // No home set so assert we are on the dashboard
        tester.assertElementPresent("dashboard");


        // Assert can't set home to admin
        tester.assertLinkNotPresent("set_my_jira_home_admin");
        // set home to issue navigator and then login again
        tester.clickLink("set_my_jira_home_issuenav");
        assertTrue("Expected to be on issue navigator", tester.getDialog().getResponsePageTitle().contains("Issue Navigator"));

        navigation.logout();
        navigation.login("fred");
        // assert we are on the isue nav page
        assertTrue("Expected to be on issue navigator", tester.getDialog().getResponsePageTitle().contains("Issue Navigator"));


    }
}
