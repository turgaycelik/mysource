package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of a {@link com.atlassian.jira.jql.permission.ClausePermissionHandler}. To fulfill the
 * responsibilities of the composite interfaces {@link com.atlassian.jira.jql.permission.ClauseSanitiser} and
 * {@link com.atlassian.jira.jql.permission.ClausePermissionChecker}, this class simply holds one reference to each
 * interface, and delegates to those instances.
 *
 * @since v4.0
 */
public class DefaultClausePermissionHandler implements ClausePermissionHandler
{
    public static final DefaultClausePermissionHandler NOOP_CLAUSE_PERMISSION_HANDLER = new DefaultClausePermissionHandler(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER);

    private final ClausePermissionChecker permissionChecker;
    private final ClauseSanitiser sanitiser;

    public DefaultClausePermissionHandler(final ClausePermissionChecker permissionChecker)
    {
        this(permissionChecker, NoOpClauseSanitiser.NOOP_CLAUSE_SANITISER);
    }

    public DefaultClausePermissionHandler(ClauseSanitiser sanitiser)
    {
        this(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER, sanitiser);
    }

    public DefaultClausePermissionHandler(final ClausePermissionChecker permissionChecker, final ClauseSanitiser sanitiser)
    {
        this.permissionChecker = notNull("permissionChecker", permissionChecker);
        this.sanitiser = notNull("sanitiser", sanitiser);
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return permissionChecker.hasPermissionToUseClause(user);
    }

    @Override
    public boolean hasPermissionToUseClause(User searcher, Set<FieldLayout> fieldLayouts)
    {
        return permissionChecker.hasPermissionToUseClause(searcher, fieldLayouts);
    }

    public Clause sanitise(final User user, final TerminalClause clause)
    {
        return sanitiser.sanitise(user, clause);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DefaultClausePermissionHandler))
        {
            return false;
        }

        final DefaultClausePermissionHandler that = (DefaultClausePermissionHandler) o;

        if (permissionChecker != null ? !permissionChecker.equals(that.permissionChecker) : that.permissionChecker != null)
        {
            return false;
        }
        if (sanitiser != null ? !sanitiser.equals(that.sanitiser) : that.sanitiser != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = sanitiser != null ? sanitiser.hashCode() : 0;
        result = 31 * result + (permissionChecker != null ? permissionChecker.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
