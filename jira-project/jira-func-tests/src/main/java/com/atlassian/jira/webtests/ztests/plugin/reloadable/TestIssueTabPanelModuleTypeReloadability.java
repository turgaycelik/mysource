package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.plugins.IssueTabPanel;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test case verifying that the issue tab panel plugin module is fully reloadable, i.e. can be enabled and disabled at any
 * times without any issues.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.ISSUES, Category.SLOW_IMPORT })
public class TestIssueTabPanelModuleTypeReloadability extends AbstractReloadablePluginsTest
{

    private IssueTabPanel issueTabPanel;
    private String testIssueKey;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueTabPanel = administration.plugins().referencePlugin().issueTabPanel();
        testIssueKey = createTestIssue();
    }

    private String createTestIssue()
    {
        return navigation.issue().createIssue("homosapien", ISSUE_TYPE_BUG, "Just a simple bug");
    }

    public void testShouldNotExistAndBeAccessibleBeforeEnablingThePlugin() throws Exception
    {
        assertNotAccessible();
    }

    public void testShouldBeReachableAfterEnablingTheReferencePlugin() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        assertAccessible();
    }

    public void testShouldBeAccesibleAfterMultipleReferencePluginEnablingAndDisabling() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        assertAccessible();
        administration.plugins().referencePlugin().disable();
        assertNotAccessible();
        administration.plugins().referencePlugin().enable();
        assertAccessible();
    }

    public void testShouldBeAccessibleAfterIssueTabModuleDisablingAndEnabling()
    {
        administration.plugins().referencePlugin().enable();
        assertAccessible();
        for (int i=0; i<3; i++)
        {
            issueTabPanel.disable();
            assertNotAccessible();
            issueTabPanel.enable();
            assertAccessible();
        }
    }

    private void assertAccessible()
    {
        assertTrue(issueTabPanel.isPresent(testIssueKey));
    }

    private void assertNotAccessible()
    {
        assertFalse(issueTabPanel.isPresent(testIssueKey));
    }


}
