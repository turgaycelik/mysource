package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests that plugin state is successfully migrated from ApplicationProperties to the PluginState table
 *
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestPluginStateMigration extends FuncTestCase
{

    private static final String PLUGIN_KEY = "com.atlassian.jira.jira-monitoring-plugin";;

    public void testUpgrade()
    {
        administration.restoreDataSlowOldWay("TestPluginStateUpgrade.xml");
        checkPluginStateRespected();
    }

    public void testPluginStateImportedCorrectly()
    {

        administration.plugins().enablePlugin(PLUGIN_KEY);
        administration.restoreData("TestPluginImport.xml");
        checkPluginStateRespected();
    }

    public void testPluginStateImportsDotPrefixCorrectly()
    {
        administration.plugins().enablePlugin(PLUGIN_KEY);
        administration.restoreDataSlowOldWay("TestPluginDotPrefixImport.xml");
        checkPluginStateRespected();
    }

    @Override
    protected void tearDownTest()
    {
        administration.plugins().enablePlugin(PLUGIN_KEY);
    }

    private void checkPluginStateRespected()
    {
        assertTrue("Plugin should be disabled.", administration.plugins().isPluginDisabled("com.atlassian.jira.jira-monitoring-plugin"));
    }


}
