package com.atlassian.query.operand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Collection;

/**
 * Factory class for creating operands.
 *
 * @since v4.0
 */
public final class Operands
{
    //Don't let people construct me.
    private Operands()
    {

    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Create an operand that represents the passed string.
     *
     * @param value the value to wrap as an operand. Cannot be null.
     * @return the operand that represents the passed value.
     */
    public static Operand valueOf(String value)
    {
        return new SingleValueOperand(value);
    }

    /**
     * Create an operands that represents a list of passed string values.
     *
     * @param values the list of values to represent. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of values.
     */
    public static Operand valueOf(String ... values)
    {
        return new MultiValueOperand(values);
    }

    /**
     * Create an operands that represents a list of passed string values.
     *
     * @param values the list of values to represent. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of values.
     */
    public static Operand valueOfStrings(Collection<String> values)
    {
        notNull("values", values);
        return new MultiValueOperand(values.toArray(new String[values.size()]));
    }

     /**
     * Create an operand that represents the passed number.
     *
     * @param value the value to wrap as an operand. Cannot be null.
     * @return the operand that represents the passed value.
     */
    public static Operand valueOf(Long value)
    {
        return new SingleValueOperand(value);
    }

    /**
     * Create an operands that represents a list of passed numbers.
     *
     * @param values the list of values to represent. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of values.
     */
    public static Operand valueOf(Long ... values)
    {
        return new MultiValueOperand(values);
    }

    /**
     * Create an operands that represents a list of passed numbers.
     *
     * @param values the list of values to represent. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of values.
     */
    public static Operand valueOfNumbers(Collection<Long> values)
    {
        notNull("values", values);
        return new MultiValueOperand(values.toArray(new Long[values.size()]));
    }

    /**
     * Create an operand that represents a list of the passed operands.
     *
     * @param operands the list of value to convert. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of operands.
     */
    public static Operand valueOf(Operand ... operands)
    {
        return new MultiValueOperand(operands);
    }

    /**
     * Create an operand that represents a list of the passed operands.
     *
     * @param operands the list of value to convert. Cannot be null, empty or contain any null values.
     * @return the operand that represents the list of operands.
     */
    public static Operand valueOfOperands(Collection<Operand> operands)
    {
        return new MultiValueOperand(operands);
    }

    /**
     * Return an operand that represents the JQL EMPTY value.
     *
     * @return the operand that represents the JQL EMPTY value.
     */
    public static Operand getEmpty()
    {
        return EmptyOperand.EMPTY;
    }
}
