package com.atlassian.jira.webtests.ztests.plugin.reloadable.enabling;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.plugin.reloadable.AbstractReloadablePluginsTest;

/**
 * <p>
 * Test case verifying that a view profile panel plugin module behaves correctly correct when going from 'never enabled'
 * to enabled state.
 *
 * <p>
 * Also referred to as the 'ZERO to ON' scenario.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestViewProfilePanelModuleTypeEnabling extends AbstractReloadablePluginsTest
{

    private static final String PROFILE_TAB_PANEL_ID = "up_reference-view-profile-panel_li";

    public void testShouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        goToViewProfilePage();
        tester.assertElementNotPresent(PROFILE_TAB_PANEL_ID);
    }

    private void goToViewProfilePage()
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        tester.gotoPage("/secure/ViewProfile.jspa");
    }

    public void testShouldBeReachableAfterEnablingTheReferencePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        goToViewProfilePage();
        tester.assertElementPresent(PROFILE_TAB_PANEL_ID);
    }
}
