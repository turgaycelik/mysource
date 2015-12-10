package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import com.meterware.httpunit.WebResponse;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test that redirects from the old to the new issue navigator work.
 * Based on <a href="https://jira.atlassian.com/browse/JRA-36281">JRA-36281</a>.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestIssueNavigatorRedirects extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
    }

    @Override
    protected void tearDownTest()
    {
        // Clear out the user's last search...
        navigation.issueNavigator().createSearch("");
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }

    public void testRedirectWithNewQuery()
    {
        assertRedirectionsTo("/secure/IssueNavigator.jspa?createNew=true",
                "/issues/?jql=",
                "/issues/?jql");
    }

    public void testRedirectWithSimpleTextQuery()
    {
        assertRedirectionsTo("/secure/IssueNavigator.jspa?searchString=summary text",
                "/issues/?jql=text%20~%20%22summary%20text%22",
                "/issues/?jql=text%20%7E%20%22summary%20text%22");
    }

    public void testRedirectWithSimpleJQLQuery()
    {
        assertRedirectionsTo("/secure/IssueNavigator.jspa?jqlQuery=project in (MKY)",
                "/issues/?jql=project%20in%20(MKY)",
                "/issues/?jql=project+in+%28MKY%29");
    }

    // JRA-36281
    public void testRedirectWithJQLQueryWithTimestamp()
    {
        assertRedirectionsTo("/secure/IssueNavigator.jspa?jqlQuery=createdDate <= '2014-01-01 9:30'",
                "/issues/?jql=createdDate%20%3C%3D%20%222014-01-01%209%3A30%22",
                "/issues/?jql=createdDate+%3C%3D+%272014-01-01+9%3A30%27");
    }

    /**
     * Check all the various ways a user might be redirected from the old to new urls.
     * @param startUrl The original url
     * @param expectedUrl Where the start url should be redirected to
     * @param otherExpectedUrl An alternate encoding yet equivalent url to expectedUrl. See <a href="https://jdog.jira-dev.com/browse/JDEV-28018">JDEV-28018</a>.
     */
    private void assertRedirectionsTo(final String startUrl, final String expectedUrl, final String otherExpectedUrl)
    {
        WebResponse response;

        // Simple part: go straight there.
        response = goTo(startUrl);
        assertThat("Explicit redirection to user's search should work", response.getURL().toString(), anyOf(
                endsWith(expectedUrl),
                endsWith(otherExpectedUrl)
        ));
        assertThat(response.getResponseCode(), equalTo(200));

        // More complicated part: implicit redirection.
        response = goTo("/secure/IssueNavigator.jspa");
        assertThat("Implicit redirection to user's last JQL search should work", response.getURL().toString(), anyOf(
                endsWith(expectedUrl),
                endsWith(otherExpectedUrl)
        ));
        assertThat(response.getResponseCode(), equalTo(200));

        // Sinister: my JIRA home redirect
        tester.clickLink("set_my_jira_home_issuenav");
        assertThat("Redirection when setting JIRA Home should work", navigation.getCurrentPage(), anyOf(
                endsWith(expectedUrl),
                endsWith(otherExpectedUrl)
        ));

        navigation.logout();
        navigation.login("admin");
        assertThat("Redirection on login should work", navigation.getCurrentPage(), anyOf(
                endsWith(expectedUrl),
                endsWith(otherExpectedUrl)
        ));
    }

    private WebResponse goTo(final String url)
    {
        tester.gotoPage(url);
        return tester.getDialog().getResponse();
    }

}
