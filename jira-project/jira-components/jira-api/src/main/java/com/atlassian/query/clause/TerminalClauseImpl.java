package com.atlassian.query.clause;

import java.util.Collections;
import java.util.List;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

/**
 * Used to represent a terminal node in the query tree.
 *
 * @since v4.0
 */
public class TerminalClauseImpl implements TerminalClause
{
    private final String name;
    private final Operator operator;
    private final Operand operand;
    private final Option<Property> property;

    /**
     * Creates a terminal clause with the specified name, operator and turns the string value into a
     * {@link com.atlassian.query.operand.SingleValueOperand} populated with a string value.
     *
     * @param name the name for the clause.
     * @param operator the operator for the clause.
     * @param operand the string value that will be wrapped in a SingleValueOperand.
     */
    public TerminalClauseImpl(final String name, final Operator operator, final String operand)
    {
        this(name, operator, new SingleValueOperand(operand), Option.<Property>none());
    }

    /**
     * Creates a terminal clause with the specified name, operator and turns the long value into a
     * {@link com.atlassian.query.operand.SingleValueOperand} populated with a long value.
     *
     * @param name the name for the clause.
     * @param operator the operator for the clause.
     * @param operand the long value that will be wrapped in a SingleValueOperand.
     */
    public TerminalClauseImpl(final String name, final Operator operator, final long operand)
    {
        this(name, operator, new SingleValueOperand(operand), Option.<Property>none());
    }

    /**
     * Creates a terminal clause with the specified name, operator and operand.
     *
     * @param name the name for the clause.
     * @param operator the operator for the clause.
     * @param operand the right-hand-side value of the clause.
     */
    public TerminalClauseImpl(String name, Operator operator, Operand operand)
    {
        this(name, operator, operand, Option.<Property>none());
    }

    /**
     * Creates a terminal clause with the specified name, operator, operand and property.
     *
     * @param name the name for the clause.
     * @param operator the operator for the clause.
     * @param operand the right-hand-side value of the clause.
     * @param property the name of the property.
     */
    public TerminalClauseImpl(String name, Operator operator, Operand operand, Option<Property> property)
    {
        this.operator = Assertions.notNull("operator", operator);
        this.operand = Assertions.notNull("operand", operand);
        this.name = Assertions.notNull("name", name);
        this.property = Assertions.notNull("property", property);
    }

    /**
     * A convienience constructor that will create a clause with the {@link com.atlassian.query.operator.Operator#EQUALS}
     * operator if there is only one value in the array and with the {@link com.atlassian.query.operator.Operator#IN}
     * operator if there are more than one value in the array.
     *
     * @param name the name for the clause.
     * @param values the string values that will be turned into {@link com.atlassian.query.operand.SingleValueOperand}'s
     * containing a string value.
     */
    public TerminalClauseImpl(String name, String... values)
    {
        Assertions.notNull("values", values);
        Assertions.not("values is empty", values.length == 0);
        this.name = Assertions.notNull("name", name);
        if (values.length == 1)
        {
            this.operator = Operator.EQUALS;
            this.operand = new SingleValueOperand(values[0]);
        }
        else
        {
            this.operator = Operator.IN;
            this.operand = new MultiValueOperand(values);
        }
        this.property = Option.none();
    }

    /**
     * A convienience constructor that will create a clause with the {@link com.atlassian.query.operator.Operator#EQUALS}
     * operator if there is only one value in the array and with the {@link com.atlassian.query.operator.Operator#IN}
     * operator if there are more than one value in the array.
     *
     * @param name the name for the clause.
     * @param values the long values that will be turned into {@link com.atlassian.query.operand.SingleValueOperand}'s
     * containing a long value.
     */
    public TerminalClauseImpl(String name, Long... values)
    {
        Assertions.notNull("values", values);
        Assertions.not("values", values.length == 0);
        this.name = Assertions.notNull("name", name);
        if (values.length == 1)
        {
            this.operator = Operator.EQUALS;
            this.operand = new SingleValueOperand(values[0]);
        }
        else
        {
            this.operator = Operator.IN;
            this.operand = new MultiValueOperand(values);
        }
        this.property = Option.none();
    }

    public Operand getOperand()
    {
        return operand;
    }

    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public Option<Property> getProperty()
    {
        return property;
    }

    public String getName()
    {
        return name;
    }

    public List<Clause> getClauses()
    {
        return Collections.emptyList();
    }

    public <R> R accept(final ClauseVisitor<R> visitor)
    {
        return visitor.visit(this);
    }

    public String toString()
    {
        //The '{' brackets in this method are designed to make this method return invalid JQL so that we know when
        //we call this method. This method is only here for debugging and should not be used in production.
        final StringBuilder sb = new StringBuilder("{").append(getName());
        property.foreach(new Effect<Property>()
        {
            @Override
            public void apply(final Property property)
            {
                sb.append(property.toString());
            }
        });
        sb.append(" ");
        sb.append(operator.getDisplayString());
        sb.append(" ");
        sb.append(operand.getDisplayString()).append("}");
        return sb.toString();
    }

    ///CLOVER:OFF
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

        final TerminalClauseImpl that = (TerminalClauseImpl) o;

        if (!name.equals(that.name))
        {
            return false;
        }
        if (!operand.equals(that.operand))
        {
            return false;
        }
        if (operator != that.operator)
        {
            return false;
        }
        //noinspection RedundantIfStatement
        if (!property.equals(that.property))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = operand.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
    ///CLOVER:ON
}
