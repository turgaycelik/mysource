package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;

/**
 * This class tests the various error conditions encountered when trying to view a search request using the
 * SearchRequestURLHandler.
 *
 * @since v3.13.3
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestSearchXmlViewErrors extends FuncTestCase
{
    private static final String SAMPLE_PATH_MESSAGE_1 = "Invalid path format.";
    private static final String SAMPLE_PATH_MESSAGE_2 = "Path should be of format";
    private static final String SAMPLE_PATH_MESSAGE_3 = "/sr/jira.issueviews:searchrequest-xml/10010/SearchRequest-10010.xml";
    private static final String SAMPLE_PATH_MESSAGE_4 = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?param1=abc";
    private static final String BAD_PLUGIN_MESSAGE = "Could not find any enabled plugin with key";

    public void testMalformedUrls() throws Exception
    {
        assert400SamplePathMessage("/sr");
        assert400SamplePathMessage("/sr/");
        assert400SamplePathMessage("/sr/unknownplugin");
        assert400SamplePathMessage("/sr/unknownplugin/");
        assert400SamplePathMessage("/sr/unknownplugin/blah");
        assert400BadPlugin("/sr/unknownplugin/blah/", "unknownplugin");
    }

    private void assert400SamplePathMessage(String url)
    {
        assert400(url);
        text.assertTextSequence(locator.page(), SAMPLE_PATH_MESSAGE_1, SAMPLE_PATH_MESSAGE_2, SAMPLE_PATH_MESSAGE_3, SAMPLE_PATH_MESSAGE_4);
    }

    private void assert400BadPlugin(String url, final String message)
    {
        assert400(url);
        text.assertTextSequence(locator.page(), BAD_PLUGIN_MESSAGE, message);
    }

    private void assert400(String url)
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        try
        {
            tester.gotoPage(url);
            final WebResponse webResponse = tester.getDialog().getResponse();
            assertEquals(400, webResponse.getResponseCode());
        }
        finally
        {
            tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(true);
        }
    }
}
