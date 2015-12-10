package com.atlassian.jira.hints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import java.util.List;

/**
 * Hint manager responsible for providing JIRA usage hints displayed
 * to the users.
 *
 * @since v4.2
 */
public interface HintManager
{
    /**
     * A number of different contexts that hints may be displayed for.
     */
    static enum Context
    {
        CLONE, DELETE_FILTER, COMMENT, ASSIGN, ATTACH, TRANSITION, DELETE_ISSUE, LABELS, LINK, LOG_WORK
    }

    /**
     * Get random hint visible in given user context.
     *
     * @param user current user
     * @param jiraHelper JIRA helper
     * @return random hint
     */
    Hint getRandomHint(User user, JiraHelper jiraHelper);

    /**
     * Get all hints visible in given user context.
     *
     * @param user current user
     * @param helper JIRA helper
     * @return all hints accessible to the user
     */
    List<Hint> getAllHints(User user, JiraHelper helper);

    /**
     * Given a valid {@link com.atlassian.jira.hints.HintManager.Context} this method returns a random
     * link for that context.  May be null if no hints exist for the context specified.
     *
     * @param remoteUser The current user
     * @param jiraHelper JIRA helper
     * @param context The context to display a hint for
     * @return A random hint for the context specified or null if none exist
     */
    Hint getHintForContext(final User remoteUser, final JiraHelper jiraHelper, final Context context);
}
