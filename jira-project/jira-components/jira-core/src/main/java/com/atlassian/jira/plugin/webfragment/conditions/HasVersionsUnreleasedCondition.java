package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.version.VersionManager;

/**
 * Checks if there are any unreleased versions for the selected project.
 * <p>
 * By default, archived versions are not included. This can be changed by mapping
 * "includedArchived" to "true" in the params passed in to {@link #init(java.util.Map params)}
 */
public class HasVersionsUnreleasedCondition extends AbstractHasVersionsCondition
{
    private final VersionManager versionManager;

    public HasVersionsUnreleasedCondition(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        if (jiraHelper.getProject() != null)
        {
            return versionManager.getVersionsUnreleased(jiraHelper.getProjectObject().getId(), includeArchived.booleanValue()).isEmpty();
        }
        return false;
    }
}
