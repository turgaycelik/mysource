package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.NonInjectableComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Sanitise the issue keys or ids stored in {@link com.atlassian.jira.jql.operand.QueryLiteral}s.
 * The strategy is to sanitise only those issues which both exist and the user does not have permission to browse.
 * The sanitised form of the operand replaces the key representation with the id representation.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class IssueLiteralSanitiser implements LiteralSanitiser
{
    private final PermissionManager permissionManager;
    private final JqlIssueSupport jqlIssueSupport;
    private final User user;

    public IssueLiteralSanitiser(final PermissionManager permissionManager, final JqlIssueSupport jqlIssueSupport, final User user)
    {
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.jqlIssueSupport = notNull("jqlIssueSupport", jqlIssueSupport);
        this.user = user;
    }

    /**
     * Issue keys are not guaranteed to be 1-1, so this method might actually return more QueryLiterals than what we started with.
     * Therefore, callers of this method need to be aware that the type of Operand to use might need to change.
     *
     * @param literals the literals to sanitise; must not be null.
     * @return the result object containing the modification status and the resulting literals
     */
    public Result sanitiseLiterals(List<QueryLiteral> literals)
    {
        notNull("literals", literals);

        boolean isModified = false;

        // keep a set of literals: if we're going to sanitise the literal, we may as well optimise and remove duplicates.
        final Set<QueryLiteral> resultantLiterals = new LinkedHashSet<QueryLiteral>();
        for (QueryLiteral literal : literals)
        {
            final List<Issue> issues = getIssues(literal);
            final List<Long> badIds = new ArrayList<Long>();
            for (Issue issue : issues)
            {
                if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
                {
                    badIds.add(issue.getId());
                }
            }

            // if every issue had no permission, then we need to sanitise the literal to be all the ids resolved
            // if there were no resolved issues, then the literals didnt exist anyway, so we dont need to sanitise
            if ((issues.size() == badIds.size()) && !issues.isEmpty())
            {
                for (Long badId : badIds)
                {
                    resultantLiterals.add(new QueryLiteral(literal.getSourceOperand(), badId));
                }
                isModified = true;
            }
            else
            {
                resultantLiterals.add(literal);
            }
        }

        return new Result(isModified, new ArrayList<QueryLiteral>(resultantLiterals));
    }

    List<Issue> getIssues(final QueryLiteral literal)
    {
        if (literal.getStringValue() != null)
        {
            final Issue issue = jqlIssueSupport.getIssue(literal.getStringValue());
            if (issue != null)
            {
                return Collections.singletonList(issue);
            }
        }
        else if (literal.getLongValue() != null)
        {
            final Issue issue = jqlIssueSupport.getIssue(literal.getLongValue());
            if (issue != null)
            {
                return Collections.singletonList(issue);
            }
        }

        return Collections.emptyList();
    }
}
