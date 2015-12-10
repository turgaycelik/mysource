package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.Error404;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static org.junit.Assert.assertThat;

/**
 * Holds the tests that verify the resolution of web actions when they are specified by an alias in a URL and that
 * the appropiate role checks are applied.
 *
 * @since v5.0.7
 */
@WebTest({Category.FUNC_TEST, Category.SECURITY})
public class TestWebActionResolution extends FuncTestCase
{
    private static class ViewUserIssueColumnsAction
    {
        private static class Views
        {
            public static final String SECURITY_BREACH = "securitybreach";
        }
    }

    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testFullyQualifiedClassNameInUrlCanNotBeResolvedToAnAction() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage("com.atlassian.jira.web.action.user.ViewUserIssueColumns.jspa");

        assertFalse(tester.getDialog().getResponse().getText().contains(ViewUserIssueColumnsAction.Views.SECURITY_BREACH));
        assertThat(new Error404(tester), Error404.isOn404Page());

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }

    public void testCanResolveARootActionGivenThatAnAliasHasBeenDefinedForIt() throws Exception
    {
        navigation.login(ADMIN_USERNAME);
        tester.gotoPage("SchemeTools.jspa");

        assertTrue(locator.id("scheme-tools-list").exists());
    }

    public void testCanResolveARootActionGivenThatNoExplicitAliasHasBeenDefinedForIt() throws Exception
    {
        tester.gotoPage("Dashboard.jspa");

        assertTrue(locator.id("dashboard").exists());
    }

    public void testCanResolveACommandActionGivenAnAliasHasBeenDefinedForIt() throws Exception
    {
        navigation.login(ADMIN_USERNAME);
        tester.gotoPage("AddNewWorkflow.jspa");

        assertTrue(locator.id("add-workflow").exists());
    }

    public void testCanNotResolveARootActionGivenThatTheUserDoesNotHaveTheRolesRequiredForIt() throws Exception
    {
        navigation.login(FRED_USERNAME);
        tester.gotoPage("ImportWorkflowFromXml!default.jspa");

        assertTrue(locator.id("login-form").exists());
    }

    public void testCanNotResolveACommandActionGivenThatTheUserDoesNotHaveTheRolesRequiredForItByInheritance() throws Exception
    {
        navigation.login(FRED_USERNAME);
        tester.gotoPage("IssueLinkingActivate.jspa?" + XsrfCheck.ATL_TOKEN + "=" + page.getFreshXsrfToken());

        assertTrue(locator.id("login-form").exists());
    }
}
