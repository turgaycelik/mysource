package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_SERVLET_KEY;

/**
 * <p>
 * Test case verifying that a servlet defined within a plugin module works as expected after the module has been
 * enabled and disabled any number of times.
 *
 * <p>
 * This scenario assumes that the module has never been enabled and that the plugin is loaded in a disabled state when
 * JIRA starts up.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestServletModuleReloadability extends AbstractReloadablePluginsTest
{
    private final static int PAGE_NOT_FOUND = 404;
    private static final String USER_FRED = "fred";
    private static final String USER_BOB = "bob";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
    }

    public void testShouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        goToReferenceServlet(USER_FRED);
        assertServletNotAccessible(USER_FRED);
    }

    public void testShouldBeAccessibleAfterEnablingTheReferencePlugin()
    {
        administration.plugins().referencePlugin().enable();
        goToReferenceServlet(USER_FRED);
        assertServletAccessible(USER_FRED);
    }

    public void testShouldBeAccessibleAfterMultipleEnablingAndDisablingTheReferencePlugin()
    {
        administration.plugins().referencePlugin().enable();
        goToReferenceServlet(USER_FRED);
        assertServletAccessible(USER_FRED);
        administration.plugins().referencePlugin().disable();
        goToReferenceServlet(USER_BOB);
        assertServletNotAccessible(USER_BOB);
        administration.plugins().referencePlugin().enable();
        goToReferenceServlet(USER_BOB);
        assertServletAccessible(USER_BOB);
    }

    public void testShouldBeAccessibleAfterEnablingTheServletModule()
    {
        administration.plugins().referencePlugin().enable();
        for (int i=0; i<3; i++)
        {
            goToReferenceServlet(USER_FRED);
            assertServletAccessible(USER_FRED);
            disableReferenceServletModule();
            goToReferenceServlet(USER_BOB);
            assertServletNotAccessible(USER_BOB);
            enableReferenceServletModule();
        }
    }

    private void goToReferenceServlet(String username)
    {
        tester.gotoPage("/plugins/servlet/reference-servlet?user=" + username);
    }

    private void assertServletAccessible(String expectedUsername)
    {
        text.assertTextPresent("Hello World Australia from: " + expectedUsername);
    }

    private void assertServletNotAccessible(String expectedUsername)
    {
        assertEquals(tester.getDialog().getResponse().getResponseCode(), PAGE_NOT_FOUND);
        text.assertTextNotPresent("Hello World Australia from: " + expectedUsername);
    }

    private void enableReferenceServletModule()
    {
        administration.plugins().referencePlugin().enableModule(REFERENCE_SERVLET_KEY);
    }

    private void disableReferenceServletModule()
    {
        administration.plugins().referencePlugin().disableModule(REFERENCE_SERVLET_KEY);
    }

}
