package com.atlassian.jira.jql.builder;

import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link ConditionBuilder}.
 *
 * @since v4.0
 */
class DefaultConditionBuilder implements ConditionBuilder
{
    private final JqlClauseBuilder builder;
    private final String clauseName;

    DefaultConditionBuilder(final String clauseName, final JqlClauseBuilder builder)
    {
        this.builder = notNull("builder", builder);
        this.clauseName = notNull("clauseName", clauseName);
    }

    public ValueBuilder eq()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.EQUALS);
    }

    public JqlClauseBuilder eq(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.EQUALS, value);
    }

    public JqlClauseBuilder eq(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.EQUALS, value);
    }

    public JqlClauseBuilder eq(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.EQUALS, date);
    }

    public JqlClauseBuilder eq(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.EQUALS, operand);
    }

    public JqlClauseBuilder eqEmpty()
    {
        return builder.addCondition(clauseName, Operator.EQUALS, EmptyOperand.EMPTY);
    }

    public JqlClauseBuilder eqFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.EQUALS, funcName);
    }

    public JqlClauseBuilder eqFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.EQUALS, funcName, args);
    }

    public JqlClauseBuilder eqFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.EQUALS, funcName, args);
    }

    public ValueBuilder notEq()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.NOT_EQUALS);
    }

    public JqlClauseBuilder notEq(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.NOT_EQUALS, value);
    }

    public JqlClauseBuilder notEq(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.NOT_EQUALS, value);
    }

    public JqlClauseBuilder notEq(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.NOT_EQUALS, operand);
    }

    public JqlClauseBuilder notEq(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.NOT_EQUALS, date);
    }

    public JqlClauseBuilder notEqEmpty()
    {
        return builder.addCondition(clauseName, Operator.NOT_EQUALS, EmptyOperand.EMPTY);
    }

    public JqlClauseBuilder notEqFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_EQUALS, funcName);
    }

    public JqlClauseBuilder notEqFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_EQUALS, funcName, args);
    }

    public JqlClauseBuilder notEqFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_EQUALS, funcName, args);
    }

    public ValueBuilder like()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.LIKE);
    }

    public JqlClauseBuilder like(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.LIKE, value);
    }

    public JqlClauseBuilder like(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.LIKE, value);
    }

    public JqlClauseBuilder like(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.LIKE, operand);
    }

    public JqlClauseBuilder like(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.LIKE, date);
    }

    public JqlClauseBuilder likeFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.LIKE, funcName);
    }

    public JqlClauseBuilder likeFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LIKE, funcName, args);
    }

    public JqlClauseBuilder likeFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LIKE, funcName, args);
    }

    public ValueBuilder notLike()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.NOT_LIKE);
    }

    public JqlClauseBuilder notLike(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.NOT_LIKE, value);
    }

    public JqlClauseBuilder notLike(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.NOT_LIKE, value);
    }

    public JqlClauseBuilder notLike(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.NOT_LIKE, operand);
    }

    public JqlClauseBuilder notLike(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.NOT_LIKE, date);
    }

    public JqlClauseBuilder notLikeFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_LIKE, funcName);
    }

    public JqlClauseBuilder notLikeFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_LIKE, funcName, args);
    }

    public JqlClauseBuilder notLikeFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_LIKE, funcName, args);
    }

    public ValueBuilder is()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.IS);
    }

    public JqlClauseBuilder isEmpty()
    {
        return builder.addCondition(clauseName, Operator.IS, EmptyOperand.EMPTY);
    }

    public ValueBuilder isNot()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.IS_NOT);
    }

    public JqlClauseBuilder isNotEmpty()
    {
        return builder.addCondition(clauseName, Operator.IS_NOT, EmptyOperand.EMPTY);
    }

    public ValueBuilder lt()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.LESS_THAN);
    }

    public JqlClauseBuilder lt(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.LESS_THAN, value);
    }

    public JqlClauseBuilder lt(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.LESS_THAN, value);
    }

    public JqlClauseBuilder lt(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.LESS_THAN, operand);
    }

    public JqlClauseBuilder lt(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.LESS_THAN, date);
    }

    public JqlClauseBuilder ltFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN, funcName);
    }

    public JqlClauseBuilder ltFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN, funcName, args);
    }

    public JqlClauseBuilder ltFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN, funcName, args);
    }

    public ValueBuilder ltEq()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.LESS_THAN_EQUALS);
    }

    public JqlClauseBuilder ltEq(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.LESS_THAN_EQUALS, value);
    }

    public JqlClauseBuilder ltEq(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.LESS_THAN_EQUALS, value);
    }

    public JqlClauseBuilder ltEq(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.LESS_THAN_EQUALS, operand);
    }

    public JqlClauseBuilder ltEq(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.LESS_THAN_EQUALS, date);
    }

    public JqlClauseBuilder ltEqFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN_EQUALS, funcName);
    }

    public JqlClauseBuilder ltEqFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN_EQUALS, funcName, args);
    }

    public JqlClauseBuilder ltEqFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.LESS_THAN_EQUALS, funcName, args);
    }

    public ValueBuilder gt()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.GREATER_THAN);
    }

    public JqlClauseBuilder gt(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.GREATER_THAN, value);
    }

    public JqlClauseBuilder gt(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.GREATER_THAN, value);
    }

    public JqlClauseBuilder gt(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.GREATER_THAN, operand);
    }

    public JqlClauseBuilder gt(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.GREATER_THAN, date);
    }

    public JqlClauseBuilder gtFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN, funcName);
    }

    public JqlClauseBuilder gtFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN, funcName, args);
    }

    public JqlClauseBuilder gtFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN, funcName, args);
    }

    public ValueBuilder gtEq()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.GREATER_THAN_EQUALS);
    }

    public JqlClauseBuilder gtEq(final String value)
    {
        return builder.addStringCondition(clauseName, Operator.GREATER_THAN_EQUALS, value);
    }

    public JqlClauseBuilder gtEq(final Long value)
    {
        return builder.addNumberCondition(clauseName, Operator.GREATER_THAN_EQUALS, value);
    }

    public JqlClauseBuilder gtEq(final Operand operand)
    {
        return builder.addCondition(clauseName, Operator.GREATER_THAN_EQUALS, operand);
    }

    public JqlClauseBuilder gtEq(final Date date)
    {
        return builder.addDateCondition(clauseName, Operator.GREATER_THAN_EQUALS, date);
    }

    public JqlClauseBuilder gtEqFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN_EQUALS, funcName);
    }

    public JqlClauseBuilder gtEqFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN_EQUALS, funcName, args);
    }

    public JqlClauseBuilder gtEqFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.GREATER_THAN_EQUALS, funcName, args);
    }

    public ValueBuilder in()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.IN);
    }

    public JqlClauseBuilder in(final String... values)
    {
        return builder.addStringCondition(clauseName, Operator.IN, values);
    }

    public JqlClauseBuilder inStrings(final Collection<String> values)
    {
        return builder.addStringCondition(clauseName, Operator.IN, values);
    }

    public JqlClauseBuilder in(final Long... values)
    {
        return builder.addNumberCondition(clauseName, Operator.IN, values);
    }

    public JqlClauseBuilder inNumbers(final Collection<Long> values)
    {
        return builder.addNumberCondition(clauseName, Operator.IN, values);
    }

    public JqlClauseBuilder in(final Operand... operands)
    {
        return builder.addCondition(clauseName, Operator.IN, operands);
    }

    public JqlClauseBuilder inOperands(final Collection<Operand> operands)
    {
        return builder.addCondition(clauseName, Operator.IN, operands);
    }

    public JqlClauseBuilder in(final Date... dates)
    {
        return builder.addDateCondition(clauseName, Operator.IN, dates);
    }

    public JqlClauseBuilder inDates(final Collection<Date> dates)
    {
        return builder.addDateCondition(clauseName, Operator.IN, dates);
    }

    public JqlClauseBuilder inFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.IN, funcName);
    }

    public JqlClauseBuilder inFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.IN, funcName, args);
    }

    public JqlClauseBuilder inFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.IN, funcName, args);
    }

    public ValueBuilder notIn()
    {
        return new DefaultValueBuilder(builder, clauseName, Operator.NOT_IN);
    }

    public JqlClauseBuilder notIn(final String... values)
    {
        return builder.addStringCondition(clauseName, Operator.NOT_IN, values);
    }

    public JqlClauseBuilder notInStrings(final Collection<String> values)
    {
        return builder.addStringCondition(clauseName, Operator.NOT_IN, values);
    }

    public JqlClauseBuilder notIn(final Long... values)
    {
        return builder.addNumberCondition(clauseName, Operator.NOT_IN, values);
    }

    public JqlClauseBuilder notInNumbers(final Collection<Long> values)
    {
        return builder.addNumberCondition(clauseName, Operator.NOT_IN, values);
    }

    public JqlClauseBuilder notIn(final Operand... operands)
    {
        return builder.addCondition(clauseName, Operator.NOT_IN, operands);
    }

    public JqlClauseBuilder notIn(final Date... dates)
    {
        return builder.addDateCondition(clauseName, Operator.NOT_IN, dates);
    }

    public JqlClauseBuilder notInDates(final Collection<Date> dates)
    {
        return builder.addDateCondition(clauseName, Operator.NOT_IN, dates);
    }

    public JqlClauseBuilder notInOperands(final Collection<Operand> operands)
    {
        return builder.addCondition(clauseName, Operator.NOT_IN, operands);
    }

    public JqlClauseBuilder notInFunc(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_IN, funcName);
    }

    public JqlClauseBuilder notInFunc(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_IN, funcName, args);
    }

    public JqlClauseBuilder notInFunc(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, Operator.NOT_IN, funcName, args);
    }

    public JqlClauseBuilder range(final Date start, final Date end)
    {
        return builder.addDateRangeCondition(clauseName, start, end);
    }

    public JqlClauseBuilder range(final String start, final String end)
    {
        return builder.addStringRangeCondition(clauseName, start, end);
    }

    public JqlClauseBuilder range(final Long start, final Long end)
    {
        return builder.addNumberRangeCondition(clauseName, start, end);
    }

    public JqlClauseBuilder range(final Operand start, final Operand end)
    {
        return builder.addRangeCondition(clauseName, start, end);
    }
}
