package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Sanitises clauses which have issue keys or ids as their values.
 *
 * @since v4.0
 */
public class IssueClauseValueSanitiser implements ClauseSanitiser
{
    private static final Logger log = Logger.getLogger(IssueClauseValueSanitiser.class);

    private final PermissionManager permissionManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final JqlIssueSupport jqlIssueSupport;

    public IssueClauseValueSanitiser(final PermissionManager permissionManager, final JqlOperandResolver jqlOperandResolver, final JqlIssueSupport jqlIssueSupport)
    {
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
        this.jqlIssueSupport = notNull("jqlIssueSupport", jqlIssueSupport);
    }

    /**
     * Note: we cannot assume that the {@link com.atlassian.jira.jql.permission.IssueClauseValueSanitiser.IssueOperandSanitisingVisitor}
     * returns the same type of operand that went in, because issues can expand to more than one literal.
     *
     * @param clause the clause to sanitise
     * @return the sanitised clause; never null.
     */
    public Clause sanitise(final User user, final TerminalClause clause)
    {
        final String clauseName = clause.getName();
        final Operator operator = clause.getOperator();

        final IssueOperandSanitisingVisitor visitor = createOperandVisitor(user, clause);
        final Operand originalOperand = clause.getOperand();
        final Operand sanitisedOperand = originalOperand.accept(visitor);

        if (originalOperand.equals(sanitisedOperand))
        {
            return clause;
        }
        else
        {
            // if we have the same type of operand, we can reuse the operator
            if (originalOperand.getClass().equals(sanitisedOperand.getClass()))
            {
                return new TerminalClauseImpl(clauseName, operator, sanitisedOperand);
            }
            // if we had a SingleValueOperand and now a MultiValueOperand, we need to do some magic the operator
            else if (originalOperand instanceof SingleValueOperand && sanitisedOperand instanceof MultiValueOperand)
            {
                // if the operator was positive equality, we just need to change it into "IN"
                if (OperatorClasses.POSITIVE_EQUALITY_OPERATORS.contains(operator))
                {
                    return new TerminalClauseImpl(clauseName, Operator.IN, sanitisedOperand);
                }
                // if the operator was negative equality, we just need to change it into "NOT IN"
                else if (OperatorClasses.NEGATIVE_EQUALITY_OPERATORS.contains(operator))
                {
                    return new TerminalClauseImpl(clauseName, Operator.NOT_IN, sanitisedOperand);
                }
                // if the operator was relational, we need to build up multiple clauses and OR them together
                // Note: there is a known issue with sanitising clauses with relational operators, since the issue id
                // cannot be used in a relational clause. See JRA-17163.
                else if (OperatorClasses.RELATIONAL_ONLY_OPERATORS.contains(operator))
                {
                    MultiValueOperand multiOperand = (MultiValueOperand) sanitisedOperand;
                    List<Clause> clauses = new ArrayList<Clause>();
                    for (Operand operand : multiOperand.getValues())
                    {
                        clauses.add(new TerminalClauseImpl(clauseName, operator, operand));
                    }
                    return new OrClause(clauses);
                }
            }
        }

        // if we got here, we have no idea how to sanitise this properly - just return the original
        log.debug(String.format("Could not figure out how to reconcile original operand '%s' and sanitised operand '%s'", originalOperand.getDisplayString(), sanitisedOperand.getDisplayString()));
        return clause;
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

        final IssueClauseValueSanitiser that = (IssueClauseValueSanitiser) o;

        if (jqlIssueSupport != null ? !jqlIssueSupport.equals(that.jqlIssueSupport) : that.jqlIssueSupport != null)
        {
            return false;
        }
        if (jqlOperandResolver != null ? !jqlOperandResolver.equals(that.jqlOperandResolver) : that.jqlOperandResolver != null)
        {
            return false;
        }
        if (permissionManager != null ? !permissionManager.equals(that.permissionManager) : that.permissionManager != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = permissionManager != null ? permissionManager.hashCode() : 0;
        result = 31 * result + (jqlOperandResolver != null ? jqlOperandResolver.hashCode() : 0);
        result = 31 * result + (jqlIssueSupport != null ? jqlIssueSupport.hashCode() : 0);
        return result;
    }

    static class IssueOperandSanitisingVisitor extends AbstractLiteralSanitisingVisitor
    {
        private final PermissionManager permissionManager;
        private final User user;
        private final JqlIssueSupport jqlIssueSupport;

        IssueOperandSanitisingVisitor(final JqlOperandResolver jqlOperandResolver, final PermissionManager permissionManager, final User user, final TerminalClause terminalClause, final JqlIssueSupport jqlIssueSupport)
        {
            super(jqlOperandResolver, user, terminalClause);
            this.permissionManager = permissionManager;
            this.user = user;
            this.jqlIssueSupport = jqlIssueSupport;
        }

        /// CLOVER:OFF
        protected LiteralSanitiser createLiteralSanitiser()
        {
            return new IssueLiteralSanitiser(permissionManager, jqlIssueSupport, user);
        }
        /// CLOVER:ON
    }

    /// CLOVER:OFF
    IssueOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
    {
        return new IssueOperandSanitisingVisitor(jqlOperandResolver, permissionManager, user, terminalClause, jqlIssueSupport);
    }
    /// CLOVER:ON
}
