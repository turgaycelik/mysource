package com.atlassian.jira.jql;

import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A container for all the objects needed to process a Jql clause.
 *
 * @since v4.0
 */
public final class DefaultClauseHandler implements ClauseHandler
{
    private final ClauseQueryFactory factory;
    private final ClauseValidator validator;
    private final ClausePermissionHandler permissionHandler;
    private final ClauseContextFactory contextFactory;
    private final ClauseInformation clauseInformation;

    public DefaultClauseHandler(final ClauseInformation information, final ClauseQueryFactory factory, final ClauseValidator validator,
            final ClausePermissionHandler permissionHandler, final ClauseContextFactory contextFactory)
    {
        this.permissionHandler = notNull("permissionHandler", permissionHandler);
        this.factory = notNull("factory", factory);
        this.validator = notNull("validator", validator);
        this.contextFactory = notNull("contextFactory", contextFactory);
        this.clauseInformation = notNull("information", information);
    }

    public ClauseInformation getInformation()
    {
        return clauseInformation;
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

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DefaultClauseHandler that = (DefaultClauseHandler) o;

        if (!clauseInformation.equals(that.clauseInformation))
        {
            return false;
        }
        if (!contextFactory.equals(that.contextFactory))
        {
            return false;
        }
        if (!factory.equals(that.factory))
        {
            return false;
        }
        if (!permissionHandler.equals(that.permissionHandler))
        {
            return false;
        }
        if (!validator.equals(that.validator))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = factory.hashCode();
        result = 31 * result + validator.hashCode();
        result = 31 * result + permissionHandler.hashCode();
        result = 31 * result + contextFactory.hashCode();
        result = 31 * result + clauseInformation.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
