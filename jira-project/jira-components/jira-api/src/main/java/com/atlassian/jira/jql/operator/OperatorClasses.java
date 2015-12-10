package com.atlassian.jira.jql.operator;

import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Contains classes for operators.
 *
 * @since v4.0
 */
public final class OperatorClasses
{
    /**
     * A set of all non-relational operators. Contains NOT_EQUALS, EQUALS, NOT_LIKE, LIKE, NOT_IN, IN, IS_NOT, and IS.
     */
    public static final Set<Operator> NON_RELATIONAL_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.NOT_EQUALS,
            Operator.EQUALS,
            Operator.NOT_LIKE,
            Operator.LIKE,
            Operator.NOT_IN,
            Operator.IN,
            Operator.IS_NOT,
            Operator.IS));

    /**
     * A set of operators that work exclusively on the EMPTY clause. Contains IS_NOT and IS.
     */
    public static final Set<Operator> EMPTY_ONLY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.IS_NOT,
            Operator.IS));

    /**
     * A set of operators that can work on the EMPTY clause. Contains NO_EQUALS, EQUALS, NOT_LIKE, LIKE, IS_NOT, and IS.
     * Also contains the history operators was and was not
     */
    public static final Set<Operator> EMPTY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.NOT_EQUALS,
            Operator.EQUALS,
            Operator.NOT_LIKE,
            Operator.LIKE,
            Operator.IS_NOT,
            Operator.IS,
            Operator.WAS,
            Operator.WAS_NOT));

    /**
     * A set of operators that can work on text clauses. Contains NOT_LIKE, LIKE, IS_NOT, and IS.
     */
    public static final Set<Operator> TEXT_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.NOT_LIKE,
            Operator.LIKE,
            Operator.IS_NOT,
            Operator.IS));

    /**
     * Contains EQUALS, IN, and IS.
     */
    public static final Set<Operator> POSITIVE_EQUALITY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.EQUALS,
            Operator.IN,
            Operator.IS));

    /**
     * Contains NOT_EQUALS, NOT_IN, and IS_NOT.
     */
    public static final Set<Operator> NEGATIVE_EQUALITY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.NOT_EQUALS,
            Operator.NOT_IN,
            Operator.IS_NOT));

    /**
     * A set of operators that a clause needs to support if it suppors the EQUALS operator and the EMPTY operand. Contains
     * EQUALS, NOT_EQUALS, NOT_IN, IN, IS_NOT and IS.
     */
    public static final Set<Operator> EQUALITY_OPERATORS_WITH_EMPTY = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.NOT_IN,
            Operator.IN,
            Operator.IS_NOT,
            Operator.IS));

    /**
     * The set of operators that a clause needs to support if it supports the EQUALS operator. Cotains EQUALS, NOT_EQUALS,
     * NOT_IN and IN.
     */
    public static final Set<Operator> EQUALITY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.NOT_IN,
            Operator.IN));

    /**
     * Set of operators that work with lists. Cotnains NOT_IN and IN.
     */
    public static final Set<Operator> LIST_ONLY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.NOT_IN,
            Operator.IN,
            Operator.WAS_NOT_IN,
            Operator.WAS_IN));

    /**
     * Contains GREATER_THAN, GREATER_THAN_EQUALS, LESS_THAN and LESS_THAN_EQUALS.
     */
    public static final Set<Operator> RELATIONAL_ONLY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.GREATER_THAN,
            Operator.GREATER_THAN_EQUALS,
            Operator.LESS_THAN,
            Operator.LESS_THAN_EQUALS));

    /**
     * A set of {@link #EQUALITY_OPERATORS} and {@link #RELATIONAL_ONLY_OPERATORS}.
     */
    public static final Set<Operator> EQUALITY_AND_RELATIONAL= Collections.unmodifiableSet(
        EnumSet.of(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.NOT_IN,
            Operator.IN,
            Operator.GREATER_THAN,
            Operator.GREATER_THAN_EQUALS,
            Operator.LESS_THAN,
            Operator.LESS_THAN_EQUALS));

    /**
     * A set of change history predicates.
     */
    public static final Set<Operator> CHANGE_HISTORY_PREDICATES = Collections.unmodifiableSet(
        EnumSet.of(
                Operator.AFTER,
                Operator.BEFORE,
                Operator.BY,
                Operator.DURING,
                Operator.ON,
                Operator.FROM,
                Operator.TO));

     /**
     * A set of change history predicates.
     */
    public static final Set<Operator> CHANGE_HISTORY_VALUE_PREDICATES = Collections.unmodifiableSet(
        EnumSet.of(
                Operator.FROM,
                Operator.TO));

     /**
     * A set of change history predicates that support date searching.
     */
    public static final Set<Operator> CHANGE_HISTORY_DATE_PREDICATES = Collections.unmodifiableSet(
        EnumSet.of(
                Operator.AFTER,
                Operator.BEFORE,
                Operator.DURING,
                Operator.ON));
     /**
     * A set of change history operators
     */
    public static final Set<Operator> CHANGE_HISTORY_OPERATORS = Collections.unmodifiableSet(
        EnumSet.of(
                Operator.WAS,
                Operator.WAS_NOT,
                Operator.WAS_IN,
                Operator.WAS_NOT_IN,
                Operator.CHANGED));

    /**
     * A set of {@link #EQUALITY_OPERATORS_WITH_EMPTY} and {@link #RELATIONAL_ONLY_OPERATORS}.
     */
    public static final Set<Operator> EQUALITY_AND_RELATIONAL_WITH_EMPTY = Collections.unmodifiableSet(
        EnumSet.of(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.NOT_IN,
            Operator.IN,
            Operator.IS_NOT,
            Operator.IS,
            Operator.GREATER_THAN,
            Operator.GREATER_THAN_EQUALS,
            Operator.LESS_THAN,
            Operator.LESS_THAN_EQUALS));

    //This class should not be constructed.
    private OperatorClasses()
    {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
}
