package com.atlassian.jira.jql;

import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;

/**
 * @since v4.0
 */
public class MockClauseHandler implements ClauseHandler
{
    private ClauseQueryFactory factory;
    private ClauseValidator validator;
    private ClausePermissionHandler permissionHandler;
    private ClauseContextFactory contextFactory;
    private ClauseInformation information;

    public MockClauseHandler()
    {
    }

    public MockClauseHandler(final ClauseQueryFactory factory, final ClauseValidator validator,
            final ClausePermissionHandler permissionHandler, final ClauseContextFactory contextFactory)
    {
        this.factory = factory;
        this.validator = validator;
        this.permissionHandler = permissionHandler;
        this.contextFactory = contextFactory;
    }

    public MockClauseHandler setFactory(final ClauseQueryFactory factory)
    {
        this.factory = factory;
        return this;
    }

    public MockClauseHandler setValidator(final ClauseValidator validator)
    {
        this.validator = validator;
        return this;
    }

    public MockClauseHandler setPermissionHandler(final ClausePermissionHandler permissionHandler)
    {
        this.permissionHandler = permissionHandler;
        return this;
    }

    public MockClauseHandler setContextFactory(final ClauseContextFactory contextFactory)
    {
        this.contextFactory = contextFactory;
        return this;
    }

    public MockClauseHandler setInformation(final ClauseInformation information)
    {
        this.information = information;
        return this;
    }

    public ClauseInformation getInformation()
    {
        return information;
    }

    public ClauseQueryFactory getFactory()
    {
        return factory;
    }

    public ClauseValidator getValidator()
    {
        return validator;
    }

    public ClausePermissionHandler getPermissionHandler()
    {
        return permissionHandler;
    }

    public ClauseContextFactory getClauseContextFactory()
    {
        return contextFactory;
    }
}
