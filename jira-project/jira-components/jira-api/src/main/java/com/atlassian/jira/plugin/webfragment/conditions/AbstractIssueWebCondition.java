package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.web.Condition;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Convenient abstraction for {@link Condition}s that are aware of JIRA's
 * authentication and project- or issue-related contexts.  These can be
 * used in action configurations to guard conditionally displayed content.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v6.0
 */
@PublicSpi
public abstract class AbstractIssueWebCondition extends AbstractWebCondition
{
    private static final Logger log = Logger.getLogger(AbstractIssueWebCondition.class);

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        final Issue issue = (Issue)params.get("issue");

        if (issue == null)
        {
            log.warn("Trying to run condition on an issue, but no issue exists");
            return false;
        }

        return shouldDisplay(user, issue, jiraHelper);
    }

    /**
     * Should we display this item for this issue?
     *
     * @param user       The current user
     * @param issue      The issue we are displaying against
     * @param jiraHelper The JiraHelper
     * @return true if we should display this item, false otherwise
     */
    public abstract boolean shouldDisplay(ApplicationUser user, Issue issue, JiraHelper jiraHelper);
}
