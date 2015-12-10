package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCurrentUser extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestCurrentUser.xml");
    }

    public void testXmlViewWithCurrentUser() throws Exception
    {
        navigation.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?reporterSelect=issue_current_user&tempMax=1000");
        text.assertTextPresent(tester.getDialog().getResponseText(), "jqlQuery=reporter+%3D+currentUser");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-3");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-2");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-1");

        navigation.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=reporter+%3D+currentUser()&tempMax=1000");
        text.assertTextPresent(tester.getDialog().getResponseText(), "jqlQuery=reporter+%3D+currentUser");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-3");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-2");
        text.assertTextPresent(tester.getDialog().getResponseText(), "HSP-1");

        navigation.logout();
        navigation.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?reporterSelect=issue_current_user&tempMax=1000");
        text.assertTextPresent(tester.getDialog().getResponseText(), "jqlQuery=reporter+%3D+currentUser");
        text.assertTextNotPresent(tester.getDialog().getResponseText(), "<item>");
        assertEquals(200, tester.getDialog().getResponse().getResponseCode());
        
        navigation.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=reporter+%3D+currentUser()&tempMax=1000");
        text.assertTextPresent(tester.getDialog().getResponseText(), "jqlQuery=reporter+%3D+currentUser");
        text.assertTextNotPresent(tester.getDialog().getResponseText(), "<item>");
        assertEquals(200, tester.getDialog().getResponse().getResponseCode());
    }
}
