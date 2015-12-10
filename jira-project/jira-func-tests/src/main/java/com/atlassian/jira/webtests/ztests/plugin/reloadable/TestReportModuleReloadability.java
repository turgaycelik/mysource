package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * <p>
 * Test verifying that a report defined within a module can be enabled and disabled any number of times.
 *
 * <p>
 * This scenario assumes that the module has never been enabled and that the plugin is loaded in a disabled state when
 * JIRA starts up.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT })
public class TestReportModuleReloadability extends AbstractReloadablePluginsTest
{
    private static final String ISSUE_COUNT_BY_PROJECT = "Issue count by Project";

    public void testReportModulePluginZeroToOn() throws Exception
    {
        shouldNotExistAndBeAccessibleBeforeEnablingThePlugin();
        shouldBeAccessibleAfterEnablingTheReferencePlugin();
    }

    private void shouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        assertPluginReportNotAccessible();
    }

    private void shouldBeAccessibleAfterEnablingTheReferencePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        assertPluginReportAccessible();
    }

    public void testReportModulePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        navigation.issue().createIssue("homosapien",ISSUE_TYPE_BUG,"Just a simple bug");
        goToProject();
        tester.clickLinkWithText(ISSUE_COUNT_BY_PROJECT);
        text.assertTextPresent(locator.css("td.fieldLabelArea"),"Project Key:");
        tester.submit("Next");
        text.assertTextPresent(locator.css("div.report"),"HSP has 1 issues");
    }

    public void testReportPluginModuleAccessibilityAfterMultipleReferencePluginEnablingAndDisabling()
    {
        assertPluginReportNotAccessible();
        administration.plugins().referencePlugin().enable();
        assertPluginReportAccessible();
        administration.plugins().referencePlugin().disable();
        assertPluginReportNotAccessible();
        administration.plugins().referencePlugin().enable();
        assertPluginReportAccessible();
    }

    public void testAccessibilityAfterReportPluginModuleEnablingAndDisabling()
    {
        administration.plugins().referencePlugin().enable();
        assertPluginReportAccessible();
        for (int i=0; i<3; i++)
        {
            disableReportPluginModule();
            assertPluginReportNotAccessible();
            enableReportPluginModule();
            assertPluginReportAccessible();
        }
    }

    private void assertPluginReportAccessible()
    {
        goToProject();
        text.assertTextPresent(ISSUE_COUNT_BY_PROJECT);
    }

    private void assertPluginReportNotAccessible()
    {
        goToProject();
        text.assertTextNotPresent(ISSUE_COUNT_BY_PROJECT);
    }

    private void goToProject()
    {
        navigation.browseProject("HSP");
    }

    private void enableReportPluginModule()
    {
        administration.plugins().referencePlugin().enableModule(ReferencePluginConstants.REFERENCE_REPORT_KEY);
    }

    private void disableReportPluginModule()
    {
        administration.plugins().referencePlugin().disableModule(ReferencePluginConstants.REFERENCE_REPORT_KEY);
    }
}
