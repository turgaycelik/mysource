package com.atlassian.jira.jql;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;

/**
 * A container for all the objects needed to process a Jql clause.
 *
 * @since v4.0
 */
@PublicApi
public interface ClauseHandler
{
    /**
     * @return an object that contains some static naming information (clause names, field id, index id) about the clause
     * handler and its associations.
     */
    ClauseInformation getInformation();

    /**
     * @return a factory that can create a lucene query for the clause.
     */
    ClauseQueryFactory getFactory();

    /**
     * @return a validator that will inspect the clause and return any validation errors it encounters.
     */
    ClauseValidator getValidator();

    /**
     * @return a permission handler that will check the users who is executing the queries permission to include
     * the clause.
     */
    ClausePermissionHandler getPermissionHandler();

    /**
     * @return a clause context factory that will be able to generate the clause context
     */
    ClauseContextFactory getClauseContextFactory();
}
