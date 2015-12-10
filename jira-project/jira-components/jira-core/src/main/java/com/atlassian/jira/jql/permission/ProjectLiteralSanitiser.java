package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
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
 * Sanitise the project keys, names or ids stored in {@link com.atlassian.jira.jql.operand.QueryLiteral}s.
 * The strategy is to sanitise only those projects which both exist and the user does not have permission to browse.
 * The sanitised form of the operand replaces the name or key form with the id representation.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class ProjectLiteralSanitiser implements LiteralSanitiser
{
    private final NameResolver<Project> projectResolver;
    private final PermissionManager permissionManager;
    private final IndexInfoResolver<Project> projectIndexInfoResolver;
    private final User user;

    public ProjectLiteralSanitiser(final NameResolver<Project> projectResolver, final PermissionManager permissionManager, final User user)
    {
        this(projectResolver, permissionManager, new ProjectIndexInfoResolver(projectResolver), user);
    }

    ProjectLiteralSanitiser(final NameResolver<Project> projectResolver, final PermissionManager permissionManager, final IndexInfoResolver<Project> indexInfoResolver, final User user)
    {
        this.projectResolver = notNull("projectResolver", projectResolver);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectIndexInfoResolver = notNull("indexInfoResolver", indexInfoResolver);
        this.user = user;
    }

    /**
     * We make a big assumption here that a single project literal will never expand out into more than one project id,
     * because of the rules around project names and resolving. This means that we should always get the same number of
     * literals returned as that are passed in.
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
            final List<String> stringIds = getIndexValues(literal);
            for (String stringId : stringIds)
            {
                Long projectId = new Long(stringId);
                Project project = projectResolver.get(projectId);

                // the only instance in which we sanitise is if the project existed, but the user does not have permission to see it.
                if (project != null && !permissionManager.hasPermission(Permissions.BROWSE, project, user))
                {
                    resultantLiterals.add(new QueryLiteral(literal.getSourceOperand(), projectId));
                    isModified = true;
                }
                else
                {
                    resultantLiterals.add(literal);
                }
            }
        }

        return new Result(isModified, new ArrayList<QueryLiteral>(resultantLiterals));
    }

    List<String> getIndexValues(final QueryLiteral literal)
    {
        if (literal.getStringValue() != null)
        {
            return projectIndexInfoResolver.getIndexedValues(literal.getStringValue());
        }
        else if (literal.getLongValue() != null)
        {
            return projectIndexInfoResolver.getIndexedValues(literal.getLongValue());
        }
        else
        {
            // must have got an Empty literal - but empty projects do not make sense, so just return an empty list.
            return Collections.emptyList();
        }
    }
}
