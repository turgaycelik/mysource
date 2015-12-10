package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.version.VersionManager;

/**
 * Checks if there are any released versions for the selected project.
 * <p>
 * By default, archived versions are not included. This can be changed by mapping
 * "includedArchived" to "true" in the params passed in to {@link #init(java.util.Map params)}
 */
public class HasVersionsReleasedCondition extends AbstractHasVersionsCondition
{
    private final VersionManager versionManager;

    public HasVersionsReleasedCondition(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        if (jiraHelper.getProject() != null)
        {
            return versionManager.getVersionsReleased(jiraHelper.getProjectObject().getId(), includeArchived.booleanValue()).isEmpty();
        }
        return false;
    }
}
