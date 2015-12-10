package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSystemFieldDoesItFitSingleManipulateData extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
    }

    public void testResolution() throws Exception
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String resolution1 = administration.resolutions().addResolution("unRESOLVED");
        final String resolution2 = administration.resolutions().addResolution("\"UNresolved\"");

        assertFitsFilterForm("resolution in (\"\\\"unRESOLVED\\\"\", \"\\\"\\\"UNresolved\\\"\\\"\")", createFilterFormParam("resolution", resolution1, resolution2));
    }
}
