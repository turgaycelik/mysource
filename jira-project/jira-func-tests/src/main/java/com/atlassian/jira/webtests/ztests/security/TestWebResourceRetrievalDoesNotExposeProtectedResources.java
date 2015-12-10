package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for testing the retrieval of static web-resources does not expose protected resources.
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY})
public class TestWebResourceRetrievalDoesNotExposeProtectedResources extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testWebInfResourceCannotBeRetrieved() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage("s/1519/3/1.0/_/WEB-INF/classes/seraph-config.xml");

        assertEquals(tester.getDialog().getResponse().getResponseCode(), 404);
        assertTrue(tester.getDialog().getResponse().getText().contains("dead link"));

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }

    public void testWebInfResourceCannotBeRetrievedWithParentTransversal() throws Exception
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);

        tester.gotoPage("s/1519/3/1.0/_/WEB-INF/images/../classes/seraph-config.xml");

        assertEquals(tester.getDialog().getResponse().getResponseCode(), 404);
        assertTrue(tester.getDialog().getResponse().getText().contains("dead link"));

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
    }
}
