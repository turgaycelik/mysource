package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;

/**
 * Represents the the reference issue tab panel in the reference plugin.
 *
 * @since 4.4
 */
public class IssueTabPanel extends ReferencePluginModule
{
    private static final String MODULE_KEY = "reference-issue-tab-panel";
    private static final String MODULE_NAME = "Reference Issue Tab Panel";

    private final Navigation navigation;
    private final LocatorFactory locators;

    public IssueTabPanel(Administration administration, Navigation navigation, LocatorFactory locators)
    {
        super(administration);
        this.navigation = navigation;
        this.locators = locators;
    }

    @Override
    public String moduleKey()
    {
        return MODULE_KEY;
    }

    @Override
    public String moduleName()
    {
        return MODULE_NAME;
    }

    /**
     * Check if the reference panel is present at given view issue page.
     *
     * @param issueKey issue key
     * @return <code>true</code>, if reference issue tab panel is present for given issue
     */
    public boolean isPresent(String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);
        return locators.id(MODULE_KEY).exists();
    }
}
