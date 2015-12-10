package com.atlassian.query.operand;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to represent a multiple constant values as an Operand.
 *
 * @since v4.0
 */
public final class MultiValueOperand implements Operand
{
    public static final String OPERAND_NAME = "MultiValueOperand";

    private final List<Operand> values;
    private static final String LEFT_PAREN = "(";
    private static final String COMMA_SPACE = ", ";
    private static final String RIGHT_PAREN = ")";
    private final int hashcode;

    public static MultiValueOperand ofQueryLiterals(Collection<QueryLiteral> literals)
    {
        return new MultiValueOperand(literals.toArray(new QueryLiteral[literals.size()]));
    }

    public MultiValueOperand(String... stringValues)
    {
        notNull("stringValues", stringValues);
        not("stringValues is empty", stringValues.length == 0);
        ArrayList<Operand> tmpValues = new ArrayList<Operand>(stringValues.length);
        for (String stringValue : stringValues)
        {
            final SingleValueOperand singleValueOperand = new SingleValueOperand(stringValue);
            tmpValues.add(singleValueOperand);
        }
        values = Collections.unmodifiableList(tmpValues);
        this.hashcode = calculateHashCode(this.values);
    }

    public MultiValueOperand(List<Long> longs)
    {
        notNull("longs", longs);
        not("longs not empty", longs.size() == 0);
        values = Collections.unmodifiableList(getLongOperands(longs));
        this.hashcode = calculateHashCode(this.values);
    }

    public MultiValueOperand(Long... longs)
    {
        notNull("longs", longs);
        not("longs not empty", longs.length == 0);
        values = Collections.unmodifiableList(getLongOperands(Arrays.asList(longs)));
        this.hashcode = calculateHashCode(this.values);
    }

    public MultiValueOperand(Operand... operands)
    {
        this(Arrays.asList(notNull("operands", operands)));
    }

    public MultiValueOperand(QueryLiteral... literals)
    {
        notNull("literals", literals);
        not("literals not empty", literals.length == 0);
        ArrayList<Operand> tmpValues = new ArrayList<Operand>(literals.length);
        for (QueryLiteral literal : literals)
        {
            if (literal.isEmpty())
            {
                tmpValues.add(EmptyOperand.EMPTY);
            }
            else
            {
                final SingleValueOperand singleValueOperand = new SingleValueOperand(literal);
                tmpValues.add(singleValueOperand);
            }
        }

        values = Collections.unmodifiableList(tmpValues);
        this.hashcode = calculateHashCode(this.values);
    }

    public MultiValueOperand(Collection<? extends Operand> values)
    {
        containsNoNulls("values", values);
        not("values is empty", values.isEmpty());
        this.values = CollectionUtil.copyAsImmutableList(values);
        this.hashcode = calculateHashCode(this.values);
    }

    private static int calculateHashCode(List<Operand> values)
    {
        return values != null ? values.hashCode() : 0;
    }

    public List<Operand> getValues()
    {
        return values;
    }

    public String getName()
    {
        return OPERAND_NAME;
    }

    public String getDisplayString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(LEFT_PAREN);
        for (Iterator<? extends Operand> iterator = values.iterator(); iterator.hasNext(); )
        {
            Operand value = iterator.next();
            sb.append(value.getDisplayString());
            if (iterator.hasNext())
            {
                sb.append(COMMA_SPACE);
            }
        }
        sb.append(RIGHT_PAREN);

        return sb.toString();
    }

    private List<Operand> getLongOperands(List<Long> operands)
    {
        ArrayList<Operand> tmpValues = new ArrayList<Operand>(operands.size());
        for (Long longValue : operands)
        {
            final SingleValueOperand singleValueOperand = new SingleValueOperand(longValue);
            tmpValues.add(singleValueOperand);
        }

        return tmpValues;
    }

    public <R> R accept(final OperandVisitor<R> visitor)
    {
        return visitor.visit(this);
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

        final MultiValueOperand that = (MultiValueOperand) o;

        if (values != null ? !values.equals(that.values) : that.values != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        // Since the enclosed list is immutable, we can pre-calculate the hashCode.
        return hashcode;
    }
}
