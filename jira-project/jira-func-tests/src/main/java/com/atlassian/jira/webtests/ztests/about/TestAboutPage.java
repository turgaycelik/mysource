package com.atlassian.jira.webtests.ztests.about;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.TestContext;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestAboutPage extends FuncTestCase
{

    @Override
    public void setUpTest()
    {
        super.setUpTest();
    }

    public void test500PageServiceParamVisibility()
    {
        tester.gotoPage("/secure/AboutPage.jspa");

        tester.assertElementPresent("test-about-introduction");
        tester.assertElementPresent("test-about-conclusion");
        tester.assertTextPresent("Atlassian JIRA - Plugins - DevMode - Func Test Plugin");
        tester.assertTextPresent("test-library-ignore");
        tester.assertLinkPresentWithText("thisIsAnExampleIgnoreIt");
        tester.assertTextNotPresent("another-test-library-ignore");
    }
}
