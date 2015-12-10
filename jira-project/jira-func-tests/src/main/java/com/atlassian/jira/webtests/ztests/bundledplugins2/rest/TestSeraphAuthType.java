package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.WebTester;

/**
 * This *should* live in com.atlassian.jira.webtests.ztests.misc.TestSeraphAuthType but those tests don't run
 * with bundled plugins enabled and this depends on the atlassian-rest plugin existing.
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestSeraphAuthType extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        navigation.logout();

        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
    }

    public void testDefault_REST() throws Exception
    {
        WebTester webTester = WebTesterFactory.createNewWebTester(environmentData);
        webTester.getTestContext().addCookie("JSESSIONID", "bad-cookie");
        
        // Under /rest/ it should be treated as ANY even if you don't specify anything.
        tester.beginAt("/rest/api/latest/user?username=admin");
        final WebResponse response = tester.getDialog().getResponse();
        assertEquals(401, response.getResponseCode());
    }

}
