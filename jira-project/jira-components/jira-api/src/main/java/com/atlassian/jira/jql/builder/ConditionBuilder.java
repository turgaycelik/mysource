package com.atlassian.jira.jql.builder;

import com.atlassian.query.operand.Operand;

import java.util.Collection;
import java.util.Date;

/**
 * An interface that is used to build JQL conditions for a particular JQL field in a fluent programming style. It is
 * created and returned from the factory methods on the {@link JqlClauseBuilder}. For
 * example, the {@link JqlClauseBuilder#affectedVersion()} method creates a new ConditionBuilder for the affectes version
 * system field in JIRA.
 * <p/>
 * The object's main job is to specify the operator for the JQL current condition being generated. The value of the JQL
 * condition can be specified when the operator is specified, or later using the {@link
 * com.atlassian.jira.jql.builder.ValueBuilder}. For example, the JQL clause {@code affectedVersion = "1.2"} can be
 * generated using {@code JqlQueryBuilder.affectedVersion().eq("1.2").build()} or {@code
 * JqlQueryBuilder.affectedVersion().eq().string("1.2").build()}.
 * <p>Generally, it is not possible to passs nulls, empty collections, empty arrays, collections that contain nulls, or arrays
 * that contain nulls to the method on the interface. Any exceptions to these argument conditions are documented on the method concern.
 * Passing a method a bad argument will result in a {@link IllegalArgumentException}.
 * <p/>
 * JQL values are of two types {@link String} and {@link Long}. For fields that are resolvable by both Id's and Names (e.g.
 * projects, versions, issue types, components, options etc), the order of resolution depends on the value type. If the JQL
 * value type is long, JIRA will first attempt to find the domain object by Id, if that fails, it will attempt to find
 * the domain object by name with the string value of the long. If the JQL value type is a String, JIRA will first try to find
 * the domain object by name, if that fails AND the string can be parsed into a number, JIRA attempts to find the domain object by
 * id with that number.
 * 
 * @see com.atlassian.jira.jql.builder.JqlClauseBuilder
 * @see com.atlassian.jira.jql.builder.ValueBuilder
 * @see com.atlassian.jira.jql.builder.JqlQueryBuilder
 * @since v4.0
 */
public interface ConditionBuilder
{
    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#EQUALS equals}. The value of
     * the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder eq();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed value. It essentially creates the JQL condition {@code name = "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eq(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed value. It essentially creates the JQL condition {@code name = value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eq(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed date. It essentially creates the JQL condition {@code name = date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eq(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed value. It essentially creates the JQL condition {@code name = operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eq(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * EMPTY value. It essentially creates the JQL condition {@code name = EMPTY}.
     *
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eqEmpty();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed function. It essentially creates the JQL condition {@code name = funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eqFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed function. It essentially creates the JQL condition {@code name = funcName(arg1, arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eqFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#EQUALS equals operator} and the
     * passed function. It essentially creates the JQL condition {@code name = funcName(arg1, arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder eqFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals}. The
     * value of the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder notEq();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed value. It essentially creates the JQL condition {@code name != "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEq(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed value. It essentially creates the JQL condition {@code name != value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEq(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed value. It essentially creates the JQL condition {@code name != operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEq(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator} and the
     * passed date. It essentially creates the JQL condition {@code name != date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEq(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the EMPTY value. It essentially creates the JQL condition {@code name != EMPTY}.
     *
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEqEmpty();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed function. It essentially creates the JQL condition {@code name != funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEqFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed function. It essentially creates the JQL condition {@code name != funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEqFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_EQUALS not equals operator}
     * and the passed function. It essentially creates the JQL condition {@code name != funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notEqFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#LIKE like}. The value of the
     * condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder like();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * value. It essentially creates the JQL condition {@code name ~ "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder like(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * value. It essentially creates the JQL condition {@code name ~ value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder like(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * value. It essentially creates the JQL condition {@code name ~ operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder like(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the
     * passed date. It essentially creates the JQL condition {@code name ~ date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder like(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * function. It essentially creates the JQL condition {@code name ~ funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder likeFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * function. It essentially creates the JQL condition {@code name ~ funcName(arg1, arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder likeFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LIKE like operator} and the passed
     * function. It essentially creates the JQL condition {@code name != funcName(arg1, arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder likeFunc(String funcName, Collection<String> args);


    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#NOT_LIKE not like}. The
     * value of the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder notLike();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed value. It essentially creates the JQL condition {@code name !~ "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLike(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed value. It essentially creates the JQL condition {@code name !~ value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLike(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed value. It essentially creates the JQL condition {@code name !~ operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLike(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and the
     * passed date. It essentially creates the JQL condition {@code name !~ date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLike(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed function. It essentially creates the JQL condition {@code name !~ funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLikeFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed function. It essentially creates the JQL condition {@code name !~ funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLikeFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_LIKE not like operator} and
     * the passed function. It essentially creates the JQL condition {@code name !~ funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notLikeFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#IS is}. The value of the
     * condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder is();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IS is operator} and the EMPTY
     * value. It essentially creates the JQL condition {@code name IS EMPTY}.
     *
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder isEmpty();

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#IS_NOT is not}. The value of
     * the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder isNot();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IS_NOT is not operator} and the
     * EMPTY value. It essentially creates the JQL condition {@code name IS NOT EMPTY}.
     *
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder isNotEmpty();

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#LESS_THAN less than}. The
     * value of the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder lt();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN less than operator} and
     * the passed value. It essentially creates the JQL condition {@code name &lt; "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder lt(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN less than operator} and
     * the passed value. It essentially creates the JQL condition {@code name &lt; value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder lt(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN less than operator} and
     * the passed value. It essentially creates the JQL condition {@code name &lt; operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder lt(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN less than operator} and the
     * passed date. It essentially creates the JQL condition {@code name &lt; date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder lt(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN "&lt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name < funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN "&lt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name < funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN "&lt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name < funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS less than
     * equals}. The value of the condition can be specified using the returned {@link
     * com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder ltEq();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS less than equals
     * operator} and the passed value. It essentially creates the JQL condition {@code name &lt;= "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEq(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS less than equals
     * operator} and the passed value. It essentially creates the JQL condition {@code name &lt;= value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEq(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS less than equals
     * operator} and the passed value. It essentially creates the JQL condition {@code name &lt;= operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEq(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS less than or equals operator} and the
     * passed date. It essentially creates the JQL condition {@code name &lt;= date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEq(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS "&lt;="
     * operator} and the passed function. It essentially creates the JQL condition {@code name &lt;= funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEqFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS "&lt;="
     * operator} and the passed function. It essentially creates the JQL condition {@code name &lt;= funcName(arg1,
     * arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEqFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#LESS_THAN_EQUALS "&lt;="
     * operator} and the passed function. It essentially creates the JQL condition {@code name &lt;= funcName(arg1,
     * arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder ltEqFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#GREATER_THAN greater than}.
     * The value of the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder gt();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN greater than
     * operator} and the passed value. It essentially creates the JQL condition {@code name &gt; "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gt(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN greater than
     * operator} and the passed value. It essentially creates the JQL condition {@code name &gt; value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gt(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN greater than
     * operator} and the passed value. It essentially creates the JQL condition {@code name &gt; operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gt(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN greater than operator} and the
     * passed date. It essentially creates the JQL condition {@code name &gt; date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gt(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN "&gt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name &gt; funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN "&gt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name &gt; funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN "&gt;" operator} and
     * the passed function. It essentially creates the JQL condition {@code name &gt; funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater
     * than equals}. The value of the condition can be specified using the returned {@link
     * com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder gtEq();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed value. It essentially creates the JQL condition {@code name &gt;= "str"}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEq(String value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed value. It essentially creates the JQL condition {@code name &gt;= value}.
     *
     * @param value the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEq(Long value);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed value. It essentially creates the JQL condition {@code name &gt;= operand}.
     *
     * @param operand the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEq(Operand operand);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than or equals operator} and the
     * passed date. It essentially creates the JQL condition {@code name &gt;= date}.
     *
     * @param date the value of the condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEq(Date date);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed function. It essentially creates the JQL condition {@code name &gt;= funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEqFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed function. It essentially creates the JQL condition {@code name &gt;= funcName(arg1,
     * arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEqFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#GREATER_THAN_EQUALS greater than
     * equals operator} and the passed function. It essentially creates the JQL condition {@code name &gt;= funcName(arg1,
     * arg2, arg3, ... argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder gtEqFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#IN in}. The values of the
     * condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder in();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder in(String... values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inStrings(Collection<String> values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder in(Long... values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inNumbers(Collection<Long> values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (operands)}.
     *
     * @param operands the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder in(Operand... operands);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (operands)}.
     *
     * @param operands the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inOperands(Collection<Operand> operands);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (dates)}.
     *
     * @param dates the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder in(Date... dates);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name IN (dates)}.
     *
     * @param dates the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inDates(Collection<Date> dates);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN "in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name in funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN "in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name in funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN "in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name in funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder inFunc(String funcName, Collection<String> args);

    /**
     * Make the operator for the JQL condition {@link com.atlassian.query.operator.Operator#NOT_IN not in}. The values
     * of the condition can be specified using the returned {@link com.atlassian.jira.jql.builder.ValueBuilder}.
     *
     * @return a builder that can be used to specify the value of the condition.
     */
    ValueBuilder notIn();

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the
     * passed values. It essentially creates the JQL condition {@code name NOT IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notIn(String... values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the
     * passed values. It essentially creates the JQL condition {@code name NOT IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInStrings(Collection<String> values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the
     * passed values. It essentially creates the JQL condition {@code name NOT IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notIn(Long... values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the
     * passed values. It essentially creates the JQL condition {@code name NOT IN (values)}.
     *
     * @param values the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInNumbers(Collection<Long> values);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the
     * passed values. It essentially creates the JQL condition {@code name NOT IN (operands)}.
     *
     * @param operands the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notIn(Operand... operands);

     /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN not in operator} and the passed
     * values. It essentially creates the JQL condition {@code name NOT IN (dates)}.
     *
     * @param dates the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notIn(Date... dates);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN in operator} and the passed
     * values. It essentially creates the JQL condition {@code name NOT IN (dates)}.
     *
     * @param dates the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInDates(Collection<Date> dates);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#IN not in operator} and the passed
     * values. It essentially creates the JQL condition {@code name NOT IN (operands)}.
     *
     * @param operands the values of the condition. Cannot be null, empty or contain any null value.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInOperands(Collection<Operand> operands);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN "not in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name not in funcName()}.
     *
     * @param funcName the name of the function in the new condition. Cannot be null.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInFunc(String funcName);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN "not in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name not in funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInFunc(String funcName, String... args);

    /**
     * Create the JQL condition with the {@link com.atlassian.query.operator.Operator#NOT_IN "not in" operator} and
     * the passed function. It essentially creates the JQL condition {@code name not in funcName(arg1, arg2, arg3, ...
     * argN)}.
     *
     * @param funcName the name of the function in the new condition.
     * @param args the arguments for the function. Cannot be null or contain any null values.
     * @return the {@link JqlClauseBuilder} that created the condition.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder notInFunc(String funcName, Collection<String> args);

    /**
     * Add a condition range condition to the current query for the passed dates. This essentially
     * adds the query {@code clauseName &gt;= start AND clauseName &lt;= end} to the query being built.
     * </p>
     * It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code start} with a null {@code end} will add the condition {@code clauseName &gt;= start}. Passing
     * a non-null {@code end} with a null {@code start} will add the condition {@code clauseName &lt;= end}.
     * Passing a null {@code start} and null {@code end} is illegal.
     *
     * @param start the date for the start of the range. May be null if {@code end} is not null.
     * @param end the date for the end of the range. May be null if {@code start} is not null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     * @return a reference to the current builder.
     */
    JqlClauseBuilder range(Date start, Date end);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially
     * adds the query {@code clauseName &gt;= start AND clauseName &lt;= end} to the query being built.
     * </p>
     * It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code start} with a null {@code end} will add the condition {@code clauseName &gt;= start}. Passing
     * a non-null {@code end} with a null {@code start} will add the condition {@code clauseName &lt;= end}.
     * Passing a null {@code start} and null {@code end} is illegal.
     *
     * @param start the value for the start of the range. May be null if {@code end} is not null.
     * @param end the value for the end of the range. May be null if {@code start} is not null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     * @return a reference to the current builder.
     */
    JqlClauseBuilder range(String start, String end);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially
     * adds the query {@code clauseName &gt;= start AND clauseName &lt;= end} to the query being built.
     * </p>
     * It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code start} with a null {@code end} will add the condition {@code clauseName &gt;= start}. Passing
     * a non-null {@code end} with a null {@code start} will add the condition {@code clauseName &lt;= end}.
     * Passing a null {@code start} and null {@code end} is illegal.
     *
     * @param start the value for the start of the range. May be null if {@code end} is not null.
     * @param end the value for the end of the range. May be null if {@code start} is not null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     * @return a reference to the current builder.
     */
    JqlClauseBuilder range(Long start, Long end);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially
     * adds the query {@code clauseName &gt;= start AND clauseName &lt;= end} to the query being built.
     * </p>
     * It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code start} with a null {@code end} will add the condition {@code clauseName &gt;= start}. Passing
     * a non-null {@code end} with a null {@code start} will add the condition {@code clauseName &lt;= end}.
     * Passing a null {@code start} and null {@code end} is illegal.
     *
     * @param start the value for the start of the range. May be null if {@code end} is not null.
     * @param end the value for the end of the range. May be null if {@code start} is not null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     * @return a reference to the current builder.
     */
    JqlClauseBuilder range(Operand start, Operand end);
}
