package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.Error404;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

import static org.junit.Assert.assertThat;

/**
 * <p>Holds tests that verify the resolution of non-web actions.</p>
 *
 * <p>These are actions that should only be available to Java code, and should not be instantiated when hitting a URL.</p>
 *
 * @since v5.0.7
 */
@WebTest({Category.FUNC_TEST, Category.SECURITY})
public class TestBackendActionResolution extends FuncTestCase
{
    private static class ListenerCreateAction
    {
        private static class Views
        {
            public static final String SUCCESS = "success";
        }
    }

    public void testBackendActionCanNotBeInvokedByAUrlGivenAFullyQualifiedClassName() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage("com.atlassian.jira.action.admin.ListenerCreate.jspa?name=SuperHackyEmail+Listener&clazz=com.atlassian.jira.event.listeners.mail.MailListener");

        assertListenerCreateActionWasNotInvoked();
        assertUnresolvedActionErrorIsReturned();

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }

    public void testBackendActionCanNotBeInvokedByAUrlGivenAClassName() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage("ListenerCreate.jspa?name=SuperHackyEmail+Listener&clazz=com.atlassian.jira.event.listeners.mail.MailListener");

        assertListenerCreateActionWasNotInvoked();
        assertUnresolvedActionErrorIsReturned();

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }

    private void assertListenerCreateActionWasNotInvoked() throws IOException
    {
        assertFalse(tester.getDialog().getResponse().getText().contains(ListenerCreateAction.Views.SUCCESS));
    }

    private void assertUnresolvedActionErrorIsReturned() throws IOException
    {
        assertThat(new Error404(tester), Error404.isOn404Page());
    }
}
