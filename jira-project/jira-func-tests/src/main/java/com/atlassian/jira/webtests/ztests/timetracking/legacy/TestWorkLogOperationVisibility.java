package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestWorkLogOperationVisibility extends FuncTestCase
{
    private static final String BUG = "HSP-1";
    private static final String NEW_FEATURE = "HSP-2";

    public TestWorkLogOperationVisibility(String name)
    {
        this.setName(name);
    }

    public void setUpTest()
    {
        // restore a backup that hides time tracking field for all issue types except bugs in Homosapien project
        administration.restoreData("TestWorkLogVisibility.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testWorkLogOperationVisibility() throws Exception
    {
        navigation.issue().viewIssue(BUG);
        assertLogWorkOperationPresent();

        navigation.issue().viewIssue(NEW_FEATURE);
        assertLogWorkOperationNotPresent();

        flipVisibilityInFieldConfigEnterprise();

        navigation.issue().viewIssue(BUG);
        assertLogWorkOperationNotPresent();

        navigation.issue().viewIssue(NEW_FEATURE);
        assertLogWorkOperationPresent();
    }

    public void testCreateWorkActionVisibility() throws Exception
    {
        navigation.issue().viewIssue(BUG);

        assertLogWorkOperationPresent();
        getTester().clickLink("log-work");
        assertWorkLogFormPresent();

        // go to new feature HSP-2 create worklog directly as there is no link to click
        getTester().gotoPage("/secure/CreateWorklog!default.jspa?id=10010");
        assertWorkLogFormNotPresent();

        // go to new feature HSP-2 create worklog directly as there is no link to click
        getTester().gotoPage(page.addXsrfToken("/secure/CreateWorklog.jspa?id=10010&timeLogged=1h&startDate=26/Sep/07%2005:41%20PM"));
        assertWorkLogFormNotPresent();

        flipVisibilityInFieldConfigEnterprise();

        // go to bug HSP-1 create worklog directly as there is no link to click
        getTester().gotoPage("/secure/CreateWorklog!default.jspa?id=10000");
        assertWorkLogFormNotPresent();

        // go to bug HSP-1 create worklog directly as there is no link to click
        getTester().gotoPage(page.addXsrfToken("/secure/CreateWorklog.jspa?id=10000&timeLogged=1h&startDate=26/Sep/07%2005:41%20PM"));
        assertWorkLogFormNotPresent();

        navigation.issue().viewIssue(NEW_FEATURE);

        assertLogWorkOperationPresent();
        getTester().clickLink("log-work");
        assertWorkLogFormPresent();
    }

    private void flipVisibilityInFieldConfigEnterprise()
    {
        navigation.gotoAdmin();
        getTester().clickLink("field_configuration");
        getTester().clickLink("configure-Default Field Configuration");
        getTester().clickLink("show_18"); // show time tracking
        getTester().clickLink("view_fieldlayouts");
        getTester().clickLink("configure-Bug Field Configuration");
        getTester().clickLink("hide_18");   // hide time tracking
    }

    private void assertWorkLogFormPresent()
    {
        text.assertTextPresent("Log Work");
        text.assertTextNotPresent("Access Denied");
        text.assertTextNotPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    private void assertWorkLogFormNotPresent()
    {
        text.assertTextNotPresent("This form allows you to log work that you have done on this issue.");
        text.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    private void assertLogWorkOperationNotPresent()
    {
        getTester().assertLinkNotPresent("log-work");
        getTester().assertLinkNotPresentWithText("Log work");
    }

    private void assertLogWorkOperationPresent()
    {
        getTester().assertLinkPresent("log-work");
        getTester().assertLinkPresentWithText("Log work");
    }
}