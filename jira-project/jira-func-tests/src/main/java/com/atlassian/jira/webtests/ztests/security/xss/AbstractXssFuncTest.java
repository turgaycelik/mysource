package com.atlassian.jira.webtests.ztests.security.xss;

import com.atlassian.jira.functest.framework.FuncTestCase;

/**
 * Some common methods for the tests which are testing Xss.
 *
 * @since v5.1
 */
public abstract class AbstractXssFuncTest extends FuncTestCase
{
    public static final String XSS_ID = "__xss_script_injected_into_the_page__";
    public static final String XSS = "\"/><script id='" + XSS_ID + "'></script>";
    public static final String XSS_ENCODED = "&quot;/&gt;&lt;script id=&#39;__xss_script_injected_into_the_page__&#39;&gt;&lt;/script&gt;";

    /**
     * Assert that there the xss is not in the page
     * @param url the url to navigate to
     * @param xssId the id of the injected xss element
     * @param xss the injected xss string e.g. <script id='123'>alert(3)</script>
     * @param xssEncoded the encoded (html) version of the xss string
     */
    public void assertXssNotInPage(String url, String xssId, String xss, String xssEncoded)
    {
        tester.gotoPage(url);
        tester.assertElementNotPresent(XSS_ID);
        tester.assertTextNotPresent(XSS);
        tester.assertTextPresent(XSS_ENCODED);
    }

    /**
     * A shortcut to assertXssNotInPage using the built in XSS_ID XSS and XSS_ENCODED variables
     * @param url the url to navigate to
     */
    public void assertXssNotInPage(String url)
    {
        assertXssNotInPage(url, XSS_ID, XSS, XSS_ENCODED);
    }

}
