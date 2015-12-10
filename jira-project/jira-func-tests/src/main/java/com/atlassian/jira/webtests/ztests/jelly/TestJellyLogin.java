package com.atlassian.jira.webtests.ztests.jelly;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.JELLY })
public class TestJellyLogin extends FuncTestCase
{
    private static final String THIS_IS_A_FIRST_COMMENT = "This is a first comment";
    private static final String BILL_PASSWORD = "bill";
    private static final String BILL_USERNAME = "bill";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestJellyLogin.xml");
    }

    /**
     * This tests that the user Bill cant create comments when logged in via the LoginTag but that
     * the user Fred can.
     *
     */
    public void testBillCanLoginButCantCreateACommentTagButFredCan()
    {
        assertIssueInitialState();

        runLoginAndAddCommentScript(BILL_USERNAME, BILL_PASSWORD, THIS_IS_A_FIRST_COMMENT);

        assertIssueInitialState();

        runLoginAndAddCommentScript(FRED_USERNAME, FRED_PASSWORD, THIS_IS_A_FIRST_COMMENT);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), THIS_IS_A_FIRST_COMMENT);
    }

    private void runLoginAndAddCommentScript(String userName, String password, String comment)
    {
        String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:Login username=\"" + userName + "\" password=\"" + password + "\">\n"
                + "     <jira:AddComment issue-key=\"HSP-1\" comment=\"" + comment + ".\"/>\n"
                + "  </jira:Login>"
                + "</JiraJelly>";
        administration.runJellyScript(script);
    }

    private void assertIssueInitialState()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), THIS_IS_A_FIRST_COMMENT);
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");
    }


}
