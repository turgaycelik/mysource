package com.atlassian.jira.jql.util;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.operand.Operand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Utility class for processing {@link com.atlassian.jira.jql.operand.QueryLiteral} objects when dealing with
 * {@link com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType} custom field classes.
 *
 * @see com.atlassian.jira.jql.query.CascadingSelectCustomFieldClauseQueryFactory
 * @see com.atlassian.jira.jql.validator.CascadingSelectCustomFieldValidator
 * @see com.atlassian.jira.jql.context.CascadingSelectCustomFieldClauseContextFactory
 * @since v4.0
 */
@InjectableComponent
public class JqlCascadingSelectLiteralUtil
{
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

    public JqlCascadingSelectLiteralUtil(final JqlSelectOptionsUtil jqlSelectOptionsUtil)
    {
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    }

    /**
     * <P>Given a list of {@link Option}s that should be in the result (positive) and a list of {@link Option}s that shouldn't (negative)
     * creates a list of {@link com.atlassian.jira.jql.operand.QueryLiteral}s that represent the positive and negative options and be used
     * for generating the query.
     *
     * @param sourceOperand the operand that the literals are being preduced from
     * @param positiveOptions the options that should be included
     * @param negativeOptions the options that should be excluded
     * @return a list of query literals representing the options to be included and excluded. All QueryLiterals will be
     * Long values. Negative options will be represented by negative Long values. Never null.
     */
    public List<QueryLiteral> createQueryLiteralsFromOptions(final Operand sourceOperand, final Collection<Option> positiveOptions, final Collection<Option> negativeOptions)
    {
        final List<QueryLiteral> result = new ArrayList<QueryLiteral>();
        for (Option option : positiveOptions)
        {
            result.add(new QueryLiteral(sourceOperand, option.getOptionId()));
        }
        for (Option option : negativeOptions)
        {
            result.add(new QueryLiteral(sourceOperand, -option.getOptionId()));
        }
        return result;
    }

    /**
     * <p>Processes the input literals into two groups: positive literals and negative literals.
     *
     * <p>Negative literals are those which have a negative Long value, which when inverted correctly maps to the id of an
     * option in the system.
     *
     * <p>All other literals, including EMPTY literals, are positive literals.
     *
     * <p>If a negative literal is encountered, a new {@link com.atlassian.jira.jql.operand.QueryLiteral} with the
     * inverse Long value is added to the list.
     *
     * @param inputLiterals the literals to process
     * @param positiveLiterals a list of positive literals to add to when processing
     * @param negativeLiterals a list of negative literals to add to when processing
     */
    public void processPositiveNegativeOptionLiterals(final List<QueryLiteral> inputLiterals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
    {
        for (final QueryLiteral literal : inputLiterals)
        {
            if (!literal.isEmpty() && isNegativeLiteral(literal))
            {
                negativeLiterals.add(new QueryLiteral(literal.getSourceOperand(), -literal.getLongValue()));
            }
            else
            {
                positiveLiterals.add(literal);
            }
        }
    }

    /**
     * Determines if the QueryLiteral represents a {@link Option} that should be excluded from the result.
     *
     * @param literal the QueryLiteral to check is negative.
     * @return true if the {@link com.atlassian.jira.jql.operand.QueryLiteral} represents an {@link Option} to be
     * excluded from the results. False otherwise.
     */
    public boolean isNegativeLiteral(final QueryLiteral literal)
    {
        if (literal.getLongValue() != null && literal.getLongValue() < 0)
        {
            final Long actualOptionId = -literal.getLongValue();
            if (jqlSelectOptionsUtil.getOptionById(actualOptionId) != null)
            {
                return true;
            }
        }
        return false;
    }
}
