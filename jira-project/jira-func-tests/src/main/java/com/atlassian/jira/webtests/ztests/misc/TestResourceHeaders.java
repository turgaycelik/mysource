package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v3.13.5
 */
@WebTest ({ Category.FUNC_TEST, Category.HTTP })
public class TestResourceHeaders extends FuncTestCase
{
    public void testResourcesNotPrivate()
    {
        administration.restoreBlankInstance();
        tester.gotoPage("/s/" + administration.getBuildNumber() + "/1/_/images/icons/favicon.png");
        String cache = tester.getDialog().getResponse().getHeaderField("Cache-Control");
        assertTrue((cache == null || cache.indexOf("private") == -1));
    }
}
