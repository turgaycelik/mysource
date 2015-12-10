package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.NavigationImpl.PROJECT_PLUGIN_PREFIX;

/**
 * We moved local velocity macros in a global file: global.vm,
 * because velocity does have some problems in multi-threaded environments when two
 * user access the same local template at the same point in time.
 *
 * This test verifies that after the move all places still render correctly using now the macro from
 * the global file.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT, Category.COMMENTS, Category.DASHBOARDS,
        Category.ISSUE_NAVIGATOR, Category.ISSUES, Category.TIME_TRACKING })
public class TestReplacedLocalVelocityMacros extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestReplacedLocalVelocityMacros.xml");
    }

    public void testEditCommentDropDownBoxViewableBy()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("footer-comment-button");
        tester.assertFormElementPresent("commentLevel");
        tester.assertOptionsEqual("commentLevel", new String[] { "All Users", "jira-administrators", "jira-developers", "jira-users" });
    }

    public void testWorklogTab()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Work Log");
        tester.assertLinkPresentWithText(ADMIN_FULLNAME);
        tester.assertLinkPresent("worklogauthor_10000");
        tester.assertTextPresent("logged work  -");
        tester.assertTextPresent("2 hours");
    }

    public void testPrintableIssueView()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Printable");
        tester.assertTextPresent("The banana&#39;s are gone...");
        tester.assertTextPresent("duplicates");
        tester.assertTextPresent("Duplicate");
        tester.assertLinkPresentWithText("New Version 1");
    }

    public void testXMLIssueView()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("XML");
        tester.assertTextPresent("Someone stole the monkey&apos;s banana  &amp;  OOOHH NOOO");
        // This seems to be a legitmate double-encoding: we are storing encoded HTML inside XML (which also needs to be encoded).
        tester.assertTextPresent("<description>Bring the monkey&amp;#39;s banana back!&lt;br/&gt;\n&lt;br/&gt;\nOtherwise he will start to swear:&amp;#39;&amp;amp;@#$%^*()&amp;#39;</description>");
        tester.assertTextPresent("<issuelink>");
        tester.assertTextPresent("<issuekey id=\"10010\">HSP-2</issuekey>");
        tester.assertTextPresent("</issuelink>");

        tester.gotoPage("si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml?rssMode=raw");
        tester.assertTextPresent("Someone stole the monkey&apos;s banana  &amp;  OOOHH NOOO");
        tester.assertTextPresent("<description><![CDATA[Bring the monkey's banana back!\n\nOtherwise he will start to swear:'&@#$%^*()']]></description>");
        tester.assertTextPresent("<issuelink>");
        tester.assertTextPresent("<issuekey id=\"10010\">HSP-2</issuekey>");
        tester.assertTextPresent("</issuelink>");
    }

    public void testWikiMarkupHelp()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("footer-comment-button");
        tester.clickLink("viewHelp");
        assertions.assertNodeHasText(new CssLocator(tester, "h2"), "Text Effects");
        assertions.assertNodeHasText(new CssLocator(tester, "th"), "Notation");
        tester.assertTextPresent("aui-nav-selected");
    }

    public void testRSSCommentAllIssues()
    {
        navigation.issueNavigator().displayRssAllComments();
        tester.assertTextPresent("IssueNavigator.jspa?reset=true&amp;jqlQuery=");
        tester.assertTextPresent("<title>RE: [HSP-1] Someone stole the monkey&apos;s banana  &amp;  OOOHH NOOO</title>");
    }

    public void testRSSViewAllIssues()
    {
        navigation.issueNavigator().displayRssAllIssues();
        tester.assertTextPresent("IssueNavigator.jspa?reset=true&amp;jqlQuery=");
        tester.assertTextNotPresent("IssueNavigator.jspa?reset=true&jqlQuery=");
        tester.assertTextPresent("<title>[HSP-1] Someone stole the monkey&apos;s banana  &amp;  OOOHH NOOO</title>");
        tester.assertTextPresent("<title>[HSP-2] The banana&apos;s are gone...</title>");
    }

    public void testXMLViewAllIssues()
    {
        navigation.issueNavigator().displayXmlAllIssues();
        tester.assertTextPresent("IssueNavigator.jspa?reset=true&amp;jqlQuery=");
        tester.assertTextNotPresent("IssueNavigator.jspa?reset=true&jqlQuery=");
    }
}
