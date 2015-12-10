package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_ACTIONS_KEY;

/**
 * <p>
 * Test case verifying that a webwork action plugin module is fully reloadable, i.e. can be enabled and disabled at any
 * times without any issues.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT })
public class TestWebWork1ActionModuleReloadability extends AbstractReloadablePluginsTest
{
    private static final String REFERENCE_ACTION_URI = "/ReferenceAction.jspa";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
    }

    public void testShouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        goToReferenceActionPage();
        assertActionNotAccessible();
    }

    public void testShouldBeReachableAfterEnablingTheReferencePlugin()
    {
        administration.plugins().referencePlugin().enable();
        goToReferenceActionPage();
        assertActionAccessible();
    }

    public void testActionShouldNotBeAccessibleAfterDisablingThePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        goToReferenceActionPage();
        assertActionAccessible();
        administration.plugins().referencePlugin().disable();
        goToReferenceActionPage();
        assertActionNotAccessible();
    }

    public void testActionShouldNotBeAccessibleAfterDisablingItsPluginModule() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        // yes it looks ridiculous but there are caches involved that get populated/cleared on plugins events
        // so we want to make sure that this works back and forth.
        for (int i=0; i<3; i++)
        {
            goToReferenceActionPage();
            assertActionAccessible();
            disableReferenceWebworkActionModule();
            goToReferenceActionPage();
            assertActionNotAccessible();
            enableReferenceWebworkActionModule();
        }
    }

    public void testActionShouldBeAccessibleAfterMultiplePluginDisablingAndEnabling() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        goToReferenceActionPage();
        assertActionAccessible();
        administration.plugins().referencePlugin().disable();
        goToReferenceActionPage();
        assertActionNotAccessible();
        administration.plugins().referencePlugin().enable();
        goToReferenceActionPage();
        assertActionAccessible();
    }

    private void assertActionAccessible()
    {
        text.assertTextPresent(locator.id("reference-action-message"), "Welcome to JIRA");
    }

    private void assertActionNotAccessible()
    {
        assertEquals(DisabledActionResponse.CODE, tester.getDialog().getResponse().getResponseCode());
        text.assertTextNotPresent(locator.id("reference-action-message"), "Welcome to JIRA");
    }

    private void goToReferenceActionPage()
    {
        tester.gotoPage(REFERENCE_ACTION_URI);
    }

    private void enableReferenceWebworkActionModule()
    {
        administration.plugins().referencePlugin().enableModule(REFERENCE_ACTIONS_KEY);
    }

    private void disableReferenceWebworkActionModule()
    {
        administration.plugins().referencePlugin().disableModule(REFERENCE_ACTIONS_KEY);
    }

    static class DisabledActionResponse
    {
        static int CODE = 404;
    }
}
