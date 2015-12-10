package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Condition to check whether there are any link types available.
 *
 * @since v4.1
 */
public class HasLinkTypesAvailableCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(HasLinkTypesAvailableCondition.class);
    private final IssueLinkTypeManager issueLinkTypeManager;

    public HasLinkTypesAvailableCondition(IssueLinkTypeManager issueLinkTypeManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final Collection<IssueLinkType> linkTypes = issueLinkTypeManager.getIssueLinkTypes();
        return linkTypes != null && !linkTypes.isEmpty();
    }

}