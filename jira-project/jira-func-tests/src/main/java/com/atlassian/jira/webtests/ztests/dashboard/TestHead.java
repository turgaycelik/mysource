package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;


@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestHead extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("blankprojects.xml");
    }

    public void testDashboard() throws Exception
    {
        tester.gotoPage("secure/Dashboard.jspa");
        String pageText = tester.getTestContext().getWebClient().getCurrentPage().getText();
        int uaIndex = pageText.indexOf("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=Edge\"/>");
        int scriptIndex = pageText.indexOf("<script");

        assertTrue("Could not find X-UA-Compatible meta tag", uaIndex > -1);
        if (scriptIndex > -1)
            assertTrue("At least one script tag appears before the first X-UA-Compatible meta tag", uaIndex < scriptIndex);
    }
}
