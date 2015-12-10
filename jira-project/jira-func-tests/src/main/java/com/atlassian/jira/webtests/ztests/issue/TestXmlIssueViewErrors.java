package com.atlassian.jira.webtests.ztests.issue;

import java.io.IOException;

import com.atlassian.jira.functest.framework.page.Error404;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;

import static org.junit.Assert.assertThat;

/**
 * This class tests the various error conditions encountered when trying to view an issue using the IssueViewURLHandler.
 *
 * @since v3.13.3
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestXmlIssueViewErrors extends JIRAWebTest
{
    private static final String SAMPLE_PATH_MESSAGE_1 = "Invalid path format.";
    private static final String SAMPLE_PATH_MESSAGE_2 = "Path should be of format";
    private static final String SAMPLE_PATH_MESSAGE_3 = "/si/jira.issueviews:xml/JRA-10/JRA-10.xml";
    private static final String BAD_PLUGIN_MESSAGE = "java.lang.IllegalArgumentException: Invalid complete key specified: unknownplugin";

    public TestXmlIssueViewErrors(String name)
    {
        super(name);
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        log("not running normal test setup for " + getName());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);
    }

    public void tearDown()
    {
        log("not running normal test teardown for " + getName());
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
    }

    public void testMalformedUrls() throws Exception
    {
        assert400SamplePathMessage("/si");
        assert400SamplePathMessage("/si/");
        assert400SamplePathMessage("/si/unknownplugin");
        assert400SamplePathMessage("/si/unknownplugin/");
        assert400SamplePathMessage("/si/unknownplugin/blah");
        assert400BadPlugin("/si/unknownplugin/blah/");
        assert404BadKey("/si/jira.issueviews:issue-xml/badkey/");

        // TODO: add more cases here
    }

    private void assert404BadKey(final String url) throws IOException
    {
        beginAt(url);
        assertThat(new Error404(tester), Error404.isOn404Page());
    }

    private void assert400SamplePathMessage(String url)
    {
        assertResponseCode(url, 400);
        assertTextPresent(SAMPLE_PATH_MESSAGE_1 + " " + SAMPLE_PATH_MESSAGE_2 + " " + SAMPLE_PATH_MESSAGE_3);
    }

    private void assert400BadPlugin(String url)
    {
        assertResponseCode(url, 400);
        assertTextPresent("Could not find any enabled plugin with key");
    }

    private void assertResponseCode(String url, final int code)
    {
        beginAt(url);
        final WebResponse webResponse = tester.getDialog().getResponse();
        assertEquals(code, webResponse.getResponseCode());
    }
}