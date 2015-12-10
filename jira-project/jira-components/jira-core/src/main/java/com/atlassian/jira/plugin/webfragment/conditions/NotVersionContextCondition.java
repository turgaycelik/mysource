package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.VersionHelper;

/**
 * This condition specifies that the item should only be displayed if the supplied {@link JiraHelper} is not of type
 * {@link VersionHelper} i.e. we are not in a Version context.
 *
 * @since v4.0
 */
public class NotVersionContextCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(final User user, final JiraHelper jiraHelper)
    {
        return !(jiraHelper instanceof VersionHelper);
    }
}
