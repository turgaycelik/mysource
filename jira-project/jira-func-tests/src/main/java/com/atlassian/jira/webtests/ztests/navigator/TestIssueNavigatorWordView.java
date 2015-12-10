/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponseUtil;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorWordView extends TestIssueNavigatorFullContentView
{
    public TestIssueNavigatorWordView(String name)
    {
        super(name);
    }

    public void testWordFilenameWithNonAsciiCharacters()
    {
        final String encodedFilename = "%D0%B5%D1%81%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F+%28jWebTest+JIRA+installation%29.doc";
        final String oldUserAgent = getDialog().getWebClient().getClientProperties().getUserAgent();

        try
        {
            log("Issue Navigator: Test that the word view generates the correct filename when the search request has non-ASCII characters");
            restoreData("TestSearchRequestViewNonAsciiSearchName.xml");

            // first test "IE"
            getDialog().getWebClient().getClientProperties().setUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
            gotoPage("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc?tempMax=1000");
            String contentDisposition = getDialog().getResponse().getHeaderField("content-disposition");
            assertFalse(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertTrue("Expected the content disposition to contain '" + encodedFilename + "' but got '" + contentDisposition + "'!",
                    contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);

            // next test "Mozilla"
            getDialog().getWebClient().getClientProperties().setUserAgent("Mozilla/5.001 (windows; U; NT4.0; en-US; rv:1.0) Gecko/25250101");
            gotoPage("/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc?tempMax=1000");
            contentDisposition = getDialog().getResponse().getHeaderField("content-disposition");
            assertTrue(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertFalse(contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);
        }
        finally
        {
            // restore old user agent
            getDialog().getWebClient().getClientProperties().setUserAgent(oldUserAgent);
        }
    }


}
