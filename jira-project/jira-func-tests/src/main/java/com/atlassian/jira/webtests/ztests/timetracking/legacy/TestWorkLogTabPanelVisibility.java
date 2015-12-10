package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestWorkLogTabPanelVisibility extends FuncTestCase
{
    private static final String BUG = "HSP-1";
    private static final String NEW_FEATURE = "HSP-2";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        // restore a backup that hides time tracking field for all issue types except bugs in Homosapien project
        administration.restoreData("TestWorkLogVisibility.xml");
    }

    public void testWorkLogTabPanelVisibility()
    {
        navigation.issue().viewIssue(BUG);
        assertTabLinkPresent();

        navigation.issue().viewIssue(NEW_FEATURE);
        assertTabLinkNotPresent();

        flipVisibilityInFieldConfigEnterprise();

        navigation.issue().viewIssue(BUG);
        assertTabLinkNotPresent();

        navigation.issue().viewIssue(NEW_FEATURE);
        assertTabLinkPresent();
    }

    public void testDirectUrlAccessTakesVisibilityIntoAccount()
    {
        assertTabPresent(BUG);
        assertTabNotPresent(NEW_FEATURE);

        flipVisibilityInFieldConfigEnterprise();

        assertTabNotPresent(BUG);
        assertTabPresent(NEW_FEATURE);
    }

    private void flipVisibilityInFieldConfigEnterprise()
    {
        navigation.gotoAdmin();
        tester.clickLink("field_configuration");
        tester.clickLink("configure-Default Field Configuration");
        tester.clickLink("show_18"); // time tracking
        tester.clickLink("view_fieldlayouts");
        tester.clickLink("configure-Bug Field Configuration");
        tester.clickLink("hide_18");    // hide time tracking
    }

    private void assertTabPresent(String issueKey)
    {
        tester.gotoPage("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
        text.assertTextSequence(locator.page(), new String[]{"Time Spent", BUG.equals(issueKey) ? "2 hours" : "1 hour", "No comment"});
    }

    private void assertTabNotPresent(String issueKey)
    {
        tester.gotoPage("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
        tester.assertTextNotPresent("Time Spent");
        tester.assertTextNotPresent(BUG.equals(issueKey) ? "2 hours" : "1 hour");
        tester.assertTextNotPresent("No comment");
    }

    private void assertTabLinkNotPresent()
    {
        tester.assertLinkNotPresentWithText("Work Log");
    }

    private void assertTabLinkPresent()
    {
        tester.assertLinkPresentWithText("Work Log");
    }
}
