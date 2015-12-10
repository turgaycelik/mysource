package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.jql.context.ClauseContextFactory;

/**
 * Can be provided as a CustomFieldSearcherClauseHandler if your custom field wants to participate in the
 * generation of the {@link com.atlassian.jira.jql.context.QueryContext} for a search request.
 *
 * This should only be necessary if the custom fields values are related to a projects context (like version
 * custom fields) or are related to the custom field context (like select list custom fields).
 *
 * If there is a {@link com.atlassian.jira.jql.context.ClauseContextFactory} provided by this then the
 * result will be intersected with the results of the {@link com.atlassian.jira.jql.context.CustomFieldClauseContextFactory}
 * which generates the context for the custom fields configuration and the context for the field visibility, based
 * on all the projects that the user running the query can see.
 *
 * NOTE: You should not need to implement this interface, see {@link com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler}
 * instead.
 *
 * @since v4.0
 */
public interface CustomFieldClauseContextHandler
{
    /**
     * @return provides a clause context factory that will be used to give the custom field a chance to
     * inspect its values so that it can infer some context based on the value.
     */
    ClauseContextFactory getClauseContextFactory();
}
