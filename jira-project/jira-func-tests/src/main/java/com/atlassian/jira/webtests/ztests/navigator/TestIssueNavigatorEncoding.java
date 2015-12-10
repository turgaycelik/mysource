package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.ISSUES })
public class TestIssueNavigatorEncoding extends FuncTestCase
{
    public void testCustomFieldValuesThatShouldBeEncoded()
    {
        // data contains a filter called "Fields that should be encoded" that has the following criteria:
        // My Free Text: <xxx>freetext</xxx>
        // My Group: <xxx>delta</xxx>
        // My Multi Group: <xxx>gamma</xxx>
        // My Multi User: <xxx>alpha</xxx>
        // My Text: <xxx>smalltext</xxx>
        // My User: <xxx>beta</xxx>
        // these values come largely from user input, and so should be encoded by the view when displayed
        // e.g. currently <xxx>alpha</xxx> is a valid user name
        administration.restoreData("TestXssCustomFields.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //Assertions on searchers have been migrated to KA

        // Refresh search request by submitting an empty quick search and check that the value is encoded
        // in the search history
        navigation.issueNavigator().createSearch("");
        Locator pageLocator = new WebPageLocator(tester);
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>delta</xxx>");
        text.assertTextPresent(pageLocator.getHTML(), "&lt;xxx&gt;delta&lt;/xxx&gt;");

        // check the view issue page
        tester.clickLinkWithText("HSP-1");
        pageLocator = new WebPageLocator(tester);
        text.assertTextNotPresent(pageLocator.getHTML(), "<xxx>delta</xxx>");
        text.assertTextPresent(pageLocator.getHTML(), "&lt;xxx&gt;delta&lt;/xxx&gt;");
    }

    //JRADEV-1042 - custom field labels should be encoded properly
    public void testCustomFieldLabelsEncoded()
    {
        administration.restoreData("TestIssueNavigatorCustomFieldLabelXss.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        //test filter summary and header row
        navigation.gotoPage("/secure/IssueNavigator.jspa?mode=hide&requestId=10030");
        assertCustomFieldLabelEncoded(new WebPageLocator(tester));

        //custom field label in the navigator on left and header row
        navigation.issueNavigator().displayAllIssues();
        assertCustomFieldLabelEncoded(new WebPageLocator(tester));
    }

    private void assertCustomFieldLabelEncoded(final Locator locator)
    {
        text.assertTextPresent(locator.getHTML(), "&quot;&gt;&lt;iframe src=&quot;http://www.google.com&quot;&gt;&lt;/iframe&gt;&lt;a href=&quot;#&quot; rel=&quot;");
        text.assertTextNotPresent(locator.getHTML(), "\"><iframe src=\"http://www.google.com\"></iframe><a href=\"#\" rel=\"");
    }
}
