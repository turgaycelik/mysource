package com.atlassian.jira.webtests.ztests.plugin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test that plugins that use wiki rendering are called before TokenRendererComponent
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.PLUGINS })
public class TestPluginWikiRenderer extends FuncTestCase
{
    public void testPluginWikiRendererThatUsesTokenRendererBlock() throws Exception
    {
        administration.restoreData("TestPluginWikiRenderer.xml");

        navigation.issue().setDescription("HSP-1", "Yo stop");

        text.assertTextPresent("<h1>Yo stop</h1><h2>Collaborate and listen</h2>");
    }

    public void testPluginWikiRendererThatUsesTokenRendererInline() throws Exception
    {
        administration.restoreData("TestPluginWikiRenderer.xml");

        navigation.issue().setDescription("HSP-1", "Ice is back");

        text.assertTextPresent("Ice is back with a brand new invention");
    }

}
