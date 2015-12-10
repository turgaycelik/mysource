package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestAutoWatches extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testAutowatchIsEnabledByDefault() throws Exception
    {
        String key = createIssueAndGotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);
    }

    public void testAutowatchDisabledInUserSettings() throws Exception
    {
        setUserAutoWatchValue(false);
        createIssueAndGotoWatched();
        tester.assertElementNotPresent("issuetable");
    }

    public void testAutowatchDisabledGlobally()
    {
        setGlobalAutoWatchValue(false);
        navigation.issue().createIssue(PROJECT_HOMOSAP, null, "I am not watching this");
        createIssueAndGotoWatched();
        tester.assertElementNotPresent("issuetable");
    }

    public void testCommentIssueWithAutowatchDisabled()
    {
        setGlobalAutoWatchValue(false);
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "Not watching this issue");
        navigation.issue().addComment(key, "I have autowatch disabled, i wouldn't like to watch this");
        gotoWatched();
        tester.assertElementNotPresent("issuetable");
    }

    public void testCommentIssueWithAutowatchEnabled()
    {
        setGlobalAutoWatchValue(false);
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "Not watching this issue");
        gotoWatched();
        tester.assertElementNotPresent("issuetable");

        setGlobalAutoWatchValue(true);
        navigation.issue().addComment(key, "Autowatch settings has changed, i am watching this issue after this comment");
        gotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);
    }

    public void testAutowatchEnabledGloballyDisabedAtUserProfile()
    {
        setGlobalAutoWatchValue(true);
        setUserAutoWatchValue(false);
        createIssueAndGotoWatched();
        tester.assertElementNotPresent("issuetable");
    }

    public void testAutowatchReturnedToGlobalEnabledSettings()
    {
        setGlobalAutoWatchValue(true);
        setUserAutoWatchValue(true);
        final String key = createIssueAndGotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);

        setUserAutoWatchValue(null);
        String secondIssueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "Second test bug");
        gotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(secondIssueKey, key);
    }

    public void testAutowatchReturnedToGlobalDisabledSettings()
    {
        setGlobalAutoWatchValue(false);
        setUserAutoWatchValue(true);
        final String key = createIssueAndGotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);

        setUserAutoWatchValue(null);
        navigation.issue().createIssue(PROJECT_HOMOSAP, null, "Second test bug");
        gotoWatched();
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(key);
    }


    private String createIssueAndGotoWatched()
    {
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, null, "First test bug");
        gotoWatched();
        return key;
    }

    private void gotoWatched()
    {
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("watched");
    }

    private void setGlobalAutoWatchValue(boolean autoWatchValue)
    {
        navigation.gotoAdminSection("user_defaults");
        tester.clickLink("user-defaults-edit");
        tester.setWorkingForm("edit_user_defaults");
        if (autoWatchValue)
        {
            tester.checkCheckbox("autoWatch", "true");
        }
        else
        {
            tester.uncheckCheckbox("autoWatch");
        }
        tester.submit();
    }

    private void setUserAutoWatchValue(final Boolean autoWatchValue)
    {
        backdoor.userProfile().changeUserAutoWatch(autoWatchValue, "admin");
    }

}
