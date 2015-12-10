package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.page.Error500;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static org.junit.Assert.assertThat;

/**
 * @since v4.1.1
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestLogout extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testInvokingLogOutJspDirectlyResultsInAnError()
    {
        allowUnhandledExceptionsToBeShownInA500Page();
        //short=true forces simplified 500 error page to be shown
        tester.gotoPage("logout.jsp?short=true");
        assertThat(new Error500(getTester()), Error500.isShort500Page());
    }

    /**
     * Allows unhandled exceptions to be thrown.
     * Note: There is no need to reset this flag as it is reset in {@link com.atlassian.jira.functest.framework.FuncTestCase#setUp()}
     */
    private void allowUnhandledExceptionsToBeShownInA500Page()
    {
        getTester().getTestContext().getWebClient().setExceptionsThrownOnErrorStatus(false);
    }
}