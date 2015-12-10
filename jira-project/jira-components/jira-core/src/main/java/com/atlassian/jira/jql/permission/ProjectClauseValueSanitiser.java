package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Sanitises clauses which have Project keys, names or ids as their values.
 *
 * @since v4.0
 */
public class ProjectClauseValueSanitiser implements ClauseSanitiser
{
    private final PermissionManager permissionManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final NameResolver<Project> projectResolver;

    public ProjectClauseValueSanitiser(final PermissionManager permissionManager, final JqlOperandResolver jqlOperandResolver, final NameResolver<Project> projectResolver)
    {
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.projectResolver = notNull("projectResolver", projectResolver);
    }

    /**
     * Important note: we are making a big assumption here that the {@link com.atlassian.jira.jql.permission.ProjectClauseValueSanitiser.ProjectOperandSanitisingVisitor}
     * will always return the same kind of operand back after sanitising. This is because project literals can never
     * expand to more than one index value for a named literal. Therefore, the multiplicity of the operand does not
     * change after sanitising. Because of this, we blindly reuse the original operator from the input clause.
     *
     * If this assumption ever changes, we will need to revisit this code.
     *
     * @param clause the clause to sanitise
     * @return the sanitised clause; never null.
     */
    public Clause sanitise(final User user, final TerminalClause clause)
    {
        final ProjectOperandSanitisingVisitor visitor = createOperandVisitor(user, clause);
        final Operand originalOperand = clause.getOperand();
        final Operand sanitisedOperand = originalOperand.accept(visitor);

        if (originalOperand.equals(sanitisedOperand))
        {
            return clause;
        }
        else
        {
            return new TerminalClauseImpl(clause.getName(), clause.getOperator(), sanitisedOperand);
        }
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

        final ProjectClauseValueSanitiser sanitiser = (ProjectClauseValueSanitiser) o;

        if (!jqlOperandResolver.equals(sanitiser.jqlOperandResolver))
        {
            return false;
        }
        if (!permissionManager.equals(sanitiser.permissionManager))
        {
            return false;
        }
        if (!projectResolver.equals(sanitiser.projectResolver))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = permissionManager.hashCode();
        result = 31 * result + jqlOperandResolver.hashCode();
        result = 31 * result + projectResolver.hashCode();
        return result;
    }

    /// CLOVER:OFF
    ProjectOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
    {
        return new ProjectOperandSanitisingVisitor(jqlOperandResolver, projectResolver, permissionManager, user, terminalClause);
    }
    /// CLOVER:ON

    static class ProjectOperandSanitisingVisitor extends AbstractLiteralSanitisingVisitor
    {
        private final NameResolver<Project> projectResolver;
        private final PermissionManager permissionManager;
        private final User user;

        ProjectOperandSanitisingVisitor(final JqlOperandResolver jqlOperandResolver, final NameResolver<Project> projectResolver, final PermissionManager permissionManager, final User user, final TerminalClause terminalClause)
        {
            super(jqlOperandResolver, user, terminalClause);
            this.projectResolver = projectResolver;
            this.permissionManager = permissionManager;
            this.user = user;
        }

        /// CLOVER:OFF
        protected LiteralSanitiser createLiteralSanitiser()
        {
            return new ProjectLiteralSanitiser(projectResolver, permissionManager, user);
        }
        /// CLOVER:ON
    }
}
