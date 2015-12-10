package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestReleaseNotes extends FuncTestCase
{
    public void testHtmlEscaping() throws IOException
    {
        administration.restoreData("TestReleaseNotes.xml");

        tester.gotoPage("/ConfigureReleaseNote.jspa?projectId=10000");
        tester.selectOption("version", "New Version 4");
        tester.selectOption("styleName", "Html");
        tester.submit("Create");
        //text in the bottom text area should be doubly escaped so users can copy/paste straight from the web browser (JRA-12184)
        text.assertTextPresent("Release Notes - &amp;quot;homosapien&amp;quot; - Version New Version 4");
        text.assertTextPresent("Bugs &amp;amp; Things");

        String baseUrl = getEnvironmentData().getBaseUrl().toString();
        if (!baseUrl.endsWith("/"))
        {
            baseUrl += "/";
        }
        text.assertTextPresent("<li>[<a href='" + baseUrl + "browse/HSP-1'>HSP-1</a>] -         Chevrons - &amp;gt;&amp;gt;&amp;gt; &amp;lt;&amp;lt;&amp;lt;");
        text.assertTextPresent("<li>[<a href='" + baseUrl + "browse/HSP-2'>HSP-2</a>] -         Ampersands - &amp;amp; &amp;amp; &amp;amp; &amp;amp; &amp;amp;");
        text.assertTextPresent("<li>[<a href='" + baseUrl + "browse/HSP-3'>HSP-3</a>] -         Quotes - &amp;quot; &amp;quot; &amp;quot; &amp;#39; &amp;#39; &amp;#39;  &amp;quot; &amp;quot; &amp;quot;");
    }
}
