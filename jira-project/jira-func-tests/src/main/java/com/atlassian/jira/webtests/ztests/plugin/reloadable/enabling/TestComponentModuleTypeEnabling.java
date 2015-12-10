package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * <p>Responsible for verifying that a component defined withing a plugin works as expected after the plugin has been
 * enabled.
 * This scenario assumes that the plugin has never been enabled and that it is loaded in a disabled state when
 * JIRA starts up.</p>
 * <br/>
 * <p>This is also what we call the from ZERO to ON scenario.</p>
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestComponentModuleTypeEnabling extends AbstractReloadablePluginsTest
{
    public void testShouldExistAndBeAccessibleAfterEnablingThePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        tester.gotoPage("/ReferenceComponentAction.jspa");
        text.assertTextPresent(locator.id("reference-component-message"), "Storm the JIRA!!!");            
    }
}
