package com.atlassian.jira.issue.search.optimizers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.OperandVisitor;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Iterables.transform;

/**
 * @since v6.3
 *
 * A visitor that checks if we can determine any projects from the given query.
 * It visits the given query recursively and each of the visits returns true if any projects could be determined.
 *
 * For 'or' clause each of its subclauses visits must return true in order to determine project(s) from the whole clause.
 * For 'and' at least one of its subclauses visits must return true in order to determine project(s).
 *
 * All subclauses always need to be visited no matter what the results is so all projects are collected.
 */

public class DeterminedProjectsInQueryVisitor implements ClauseVisitor<Boolean>
{
    public static final String PROJECT_CLAUSE_NAME = "project";
    public static final ImmutableList<Operator> ALLOWED_OPERATORS =
            ImmutableList.of(Operator.EQUALS, Operator.IN, Operator.IS);

    public final ImmutableSet.Builder<String> projectsSetBuilder = ImmutableSet.builder();

    @Override
    public Boolean visit(final AndClause andClause)
    {
        for (final Clause clause : andClause.getClauses())
        {
            if (clause.accept(this))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public Boolean visit(final OrClause orClause)
    {
        for (final Clause clause : orClause.getClauses())
        {
            if (!clause.accept(this))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public Boolean visit(final TerminalClause terminalClause)
    {
        if (PROJECT_CLAUSE_NAME.equalsIgnoreCase(terminalClause.getName()) &&
                ALLOWED_OPERATORS.contains(terminalClause.getOperator()))
        {
            final List<String> extractedProjects = terminalClause.getOperand().accept(new ArgumentExtractingOperandVisitor());
            addAllWithQuotationMarksRemoved(extractedProjects);

            return !extractedProjects.isEmpty();
        }
        return false;
    }

    @Override
    public Boolean visit(final WasClause wasClause)
    {
        return false;
    }

    @Override
    public Boolean visit(final ChangedClause changedClause)
    {
        return false;
    }

    @Override
    public Boolean visit(final NotClause notClause)
    {
        return false;
    }

    public Set<String> getDeterminedProjects()
    {
        return projectsSetBuilder.build();
    }

    private void addAllWithQuotationMarksRemoved(final Iterable<String> projects)
    {
        for (final String project : projects)
        {
            projectsSetBuilder.add(project.replaceAll("\"", ""));
        }
    }

    private static class ArgumentExtractingOperandVisitor implements OperandVisitor<List<String>>
    {
        @Override
        public List<String> visit(final EmptyOperand emptyOperand)
        {
            return Collections.emptyList();
        }

        @Override
        public List<String> visit(final FunctionOperand functionOperand)
        {
            return Collections.emptyList();
        }

        @Override
        public List<String> visit(final MultiValueOperand multiValueOperand)
        {
            final Iterable<String> operands = transform(multiValueOperand.getValues(), new Function<Operand, String>()
            {
                @Override
                public String apply(Operand input)
                {
                    return input.getDisplayString();
                }
            });

            return ImmutableList.copyOf(operands);
        }

        @Override
        public List<String> visit(final SingleValueOperand singleValueOperand)
        {
            return ImmutableList.of(singleValueOperand.getDisplayString());
        }
    }
}

