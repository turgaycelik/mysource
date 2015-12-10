package com.atlassian.jira.webtests.ztests.issue.security.xss;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.opensymphony.util.TextUtils;

/**
 * Test case for XSS exploits in the CreateIssueDetails page
 *
 * @since 5.2
 */
@WebTest({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY })
public class TestXssOnCreateIssueDetailsPage extends FuncTestCase
{
    private static final int PROJECT_HOMOSAP_ID = 10000;
    private static final String XSS_ID = "__xss_script_injected_into_the_page__";
    private static final String XSS = "\"/><script id='" + XSS_ID + "'></script>";
    private static final String XSS_ESCAPED = "&quot;/&gt;&lt;script id=&#39;__xss_script_injected_into_the_page__&#39;&gt;&lt;/script&gt;";

    // JRA-30039
    public void testXssReporterNameOnCreateIssueDetailsPage()
    {
        /* the xss'able field is only available with the frother.reporter.field */
        backdoor.restoreBlankInstance();
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        navigation.dashboard();
        String atl_token = new HtmlPage(tester).getXsrfToken();
        String url = "/secure/CreateIssueDetails.jspa?issuetype=2&pid=" + PROJECT_HOMOSAP_ID +
                "&atl_token=" + atl_token +
                "&reporter=" + XSS;
        navigation.gotoPage(url);
        tester.assertElementPresent("reporter");
        tester.assertTextPresent(XSS_ESCAPED);
        tester.assertElementNotPresent(XSS_ID);
    }

}
