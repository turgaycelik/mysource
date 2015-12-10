package com.atlassian.jira.jql.util;

import com.atlassian.fugue.Effect;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClausePrecedence;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.MultiClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.PredicateVisitor;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.OperandVisitor;
import com.atlassian.query.operand.SingleValueOperand;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Turns a JQL search into a valid JQL string. Will perform escaping as necessary during the process.
 *
 * @since v4.0
 */
final class ToJqlStringVisitor implements OperandVisitor<String>, ClauseVisitor<ToJqlStringVisitor.Result> , PredicateVisitor<String>
{
    private final JqlStringSupport support;

    ToJqlStringVisitor(final JqlStringSupport support)
    {
        this.support = notNull("support", support);
    }

    String toJqlString(Clause clause)
    {
        notNull("clause", clause);
        return clause.accept(this).getJql();
    }

    public String visit(final EmptyOperand empty)
    {
        return EmptyOperand.OPERAND_NAME;
    }

    public String visit(final FunctionOperand function)
    {
        final StringBuilder sb = new StringBuilder(support.encodeFunctionName(function.getName()));
        sb.append("(");

        final List<String> args = function.getArgs();
        boolean first = true;
        for (String arg : args)
        {
            if (!first)
            {
                sb.append(", ");
            }
            first = false;

            sb.append(support.encodeFunctionArgument(arg));
        }
        sb.append(")");
        return sb.toString();
    }

    public String visit(final MultiValueOperand multiValue)
    {
        final StringBuilder sb = new StringBuilder("(");
        final List<Operand> operands = multiValue.getValues();
        boolean first = true;
        for (Operand operand : operands)
        {
            if (!first)
            {
                sb.append(", ");
            }
            first = false;

            sb.append(operand.accept(this));
        }
        sb.append(")");
        return sb.toString();
    }

    public String visit(final SingleValueOperand singleValueOperand)
    {
        if (singleValueOperand.getLongValue() != null)
        {
            return singleValueOperand.getLongValue().toString();
        }
        else
        {
            return support.encodeStringValue(singleValueOperand.getStringValue());
        }
    }

    public Result visit(final AndClause andClause)
    {
        return visitMultiClause(andClause, AndClause.AND, ClausePrecedence.AND);
    }

    public Result visit(final OrClause orClause)
    {
        return visitMultiClause(orClause, OrClause.OR, ClausePrecedence.OR);
    }

    public Result visit(final NotClause notClause)
    {
        final Result subResult = notClause.getSubClause().accept(this);
        final boolean brackets = subResult.getPrecedence().compareTo(ClausePrecedence.NOT) < 0;
        final String jql;
        if (brackets)
        {
            jql = String.format("%s (%s)", NotClause.NOT, subResult.getJql());
        }
        else
        {
            jql = String.format("%s %s", NotClause.NOT, subResult.getJql());
        }
        return new Result(jql, ClausePrecedence.NOT);
    }

    public Result visit(final TerminalClause clause)
    {
        return buildJqlString(clause);
    }

    @Override
    public Result visit(WasClause clause)
    {
        return buildJqlString(clause);
    }

    @Override
    public Result visit(ChangedClause clause)
    {
        return  buildJqlString(clause);
    }

    @Override
    public String visit(HistoryPredicate predicate)
    {
        //depending on actual implementation of HistoryPredicate we should print them differently
        if (predicate instanceof TerminalHistoryPredicate)
        {
            return visit((TerminalHistoryPredicate) predicate);
        }
        else if (predicate instanceof AndHistoryPredicate)
        {
            return visit((AndHistoryPredicate) predicate);
        }

        //default fallback for unknown implementation
        return predicate.getDisplayString();
    }

    private String visit(TerminalHistoryPredicate predicate)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(predicate.getOperator().getDisplayString());
        sb.append(" ");
        sb.append(predicate.getOperand().accept(this));
        return sb.toString();
    }

    private String visit(AndHistoryPredicate predicate)
    {
        StringBuilder sb = new StringBuilder();
        for (HistoryPredicate p : predicate.getPredicates())
        {
            sb.append(p.accept(this)).append(" ");
        }
        return sb.toString();
    }

    private Result visitMultiClause(final MultiClause andClause, final String clauseName, final ClausePrecedence clausePrecedence)
    {
        final StringBuilder sb = new StringBuilder();

        for (Iterator<Clause> clauseIterator = andClause.getClauses().iterator(); clauseIterator.hasNext();)
        {
            final Clause clause = clauseIterator.next();
            final Result clauseResult = clause.accept(this);
            final boolean brackets = clauseResult.getPrecedence().compareTo(clausePrecedence) < 0;

            if (brackets)
            {
                sb.append("(");
            }
            sb.append(clauseResult.getJql());
            if (brackets)
            {
                sb.append(")");
            }

            if (clauseIterator.hasNext())
            {
                sb.append(" ").append(clauseName).append(" ");
            }
        }
        return new Result(sb.toString(), clausePrecedence);
    }

    private Result buildJqlString(TerminalClause clause)
    {
        final StringBuilder builder = new StringBuilder(support.encodeFieldName(clause.getName()));
        clause.getProperty().foreach(new Effect<Property>()
        {
            @Override
            public void apply(final Property property)
            {
                builder.append("[")
                        .append(support.encodeFieldName(property.getKeysAsString()))
                        .append("]");

                if (StringUtils.isNotEmpty(property.getObjectReferencesAsString()))
                {
                    builder.append(".")
                            .append(support.encodeFieldName(property.getObjectReferencesAsString()));
                }
            }
        });
        builder.append(" ").append(clause.getOperator().getDisplayString());
        builder.append(" ").append(clause.getOperand().accept(this));
        if (clause instanceof WasClause)
        {
            final HistoryPredicate predicate = ((WasClause) clause).getPredicate();
            if (predicate != null)
            {
                builder.append(" ").append(predicate.accept(this));
            }
        }

        return new Result(builder.toString(), ClausePrecedence.TERMINAL);
    }

    private Result buildJqlString(ChangedClause clause)
    {
        final StringBuilder builder = new StringBuilder(support.encodeFieldName(clause.getField()));
        builder.append(" ").append("changed");
        if (clause.getPredicate() != null)
        {
           builder.append(" ");
           builder.append(clause.getPredicate().accept(this));
        }
        return new Result(builder.toString(), ClausePrecedence.TERMINAL);
    }

    public final static class Result
    {
        private final String jql;
        private final ClausePrecedence precedence;

        private Result(final String jql, final ClausePrecedence precedence)
        {
            this.jql = jql;
            this.precedence = precedence;
        }

        public String getJql()
        {
            return jql;
        }

        public ClausePrecedence getPrecedence()
        {
            return precedence;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
