package com.atlassian.jira.plugin.jql.function;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import javax.annotation.Nonnull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

/**
 * <p>Functions in JQL can be used to provide values for search criteria. For example, the <tt>membersOf("myGroup")</tt>
 * JQL function returns a list of the usernames who are members of the group "myGroup". This function can then be used
 * in any JQL clause that operates on a list of usernames. For example, the JQL clause
 * <tt>assignee in membersOf("myGroup")</tt> returns all issues assigned to a member of the JIRA group "myGroup". This
 * is very powerful, as it removes the need to enumerate over all the members of the group manually.
 *
 * <p>Implementations of JQL functions need to know how to validate a {@link com.atlassian.query.operand.FunctionOperand}
 * (which contains their arguments), and also need to know how to produce {@link com.atlassian.jira.jql.operand.QueryLiteral}
 * values from that operand. They must also specify whether or not the function produces a list of values or a single
 * value.
 *
 * <p>The validate and getValues method take the {@link com.atlassian.query.clause.TerminalClause} that contained the
 * {@link com.atlassian.query.operand.FunctionOperand} on its left-hand side. This can be used to create advanced functionality,
 * such as adjusting the functions result or validation based on the clauses right-hand side value or operator.
 *
 * <p>For plugin developers wishing to write their own JQL functions - you may find it useful to extend from our
 * provided {@link com.atlassian.jira.plugin.jql.function.AbstractJqlFunction}. In addition to implementing this
 * interface, you must also provide an XML descriptor for your function. For an example, see {@link
 * com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor}.
 *
 * <p>{@link QueryLiteral}s returned by the {@link #getValues(com.atlassian.jira.jql.query.QueryCreationContext,
 * com.atlassian.query.operand.FunctionOperand, com.atlassian.query.clause.TerminalClause)} method must have the operand
 * source of the passed in {@link com.atlassian.query.operand.FunctionOperand}.
 *
 * <p>The function must be thread safe. Only one instance of the function is created to service all JQL queries. As a
 * result the function may have multiple threads calling it at the same time.
 *
 * <p>The function will be executed each time a query using it is run. A query is only going to run as fast as its
 * slowest part, thus the function must be very fast to ensure that queries run as quickly as possible. The function also
 * needs to perform well under concurrent load.
 *
 * @see com.atlassian.jira.plugin.jql.function.AbstractJqlFunction
 * @see com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor
 * @since v4.0
 */
@PublicSpi
public interface JqlFunction
{
    /**
     * Initialises this pluggable function with it's module descriptor.
     *
     * @param moduleDescriptor the module descriptor; will not be null.
     */
    void init(@Nonnull JqlFunctionModuleDescriptor moduleDescriptor);

    /**
     * Will validate the function operand's arguments and report back any errors.
     *
     * @param searcher the user performing the search
     * @param operand the operand to validate
     * @param terminalClause the terminal clause that contains the operand
     * @return a MessageSet which will contain any validation errors or warnings or will be empty if there is nothing to
     *         report; never null.
     */
    @Nonnull
    MessageSet validate(User searcher, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause);

    /**
     * <p>Gets the unexpanded values provided by the user on input. This is the output values that will later be
     * transformed into index values.
     *
     * <p>For example, a function who returns all the released versions of a specified project should return {@link
     * com.atlassian.jira.jql.operand.QueryLiteral}s representing the ids of those versions. For correctness, always opt
     * to return the most specific identifier for the object; if you can return either the id (which is stored in the
     * index) or a string name (that would require resolving to get the index value), choose the id.
     *
     * @param queryCreationContext the context of query creation
     * @param operand the operand to get values from
     * @param terminalClause the terminal clause that contains the operand
     * @return a List of objects that represent this Operands raw values. Cannot be null.
     */
    @Nonnull
    List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause);

    /**
     * This method should return true if the function is meant to be used with the IN or NOT IN operators, that is, if
     * the function should be viewed as returning a list. The method should return false when it is to be used with the
     * other relational operators (e.g. =, !=, <, >, ...) that only work with single values.
     * <p/>
     * As a general rule, if a function is going to return more than one value then it should return true here,
     * otherwise it should return false. This does not necessarily need to be the case. For example, it is possible for
     * function that returns false here to return more than one value when it is run.
     *
     * @return true if the function can should be considered a list (i.e. work with IN and NOT IN), or false otherwise.
     *         In this case it is considered to return a single value (i.e. work with =, !=, <, >, ...).
     */
    boolean isList();

    /**
     * This method must return the number of arguments that the function expects to perform its operation correctly. If
     * the function can accept a variable number of arguments this value should be the lower limit. It is perfectly
     * legal for a function to take no arguments and return 0 for this method.
     *
     * @return the number of arguments that the function expects to perform its operation correctly. Must be >=0.
     */
    int getMinimumNumberOfExpectedArguments();

    /**
     * The name of the function. Multiple calls to this method must return the same result. This means that the function
     * name cannot be internationalised with respect to the searcher.
     *
     * @return the name of the function. Cannot be null.
     */
    @Nonnull
    String getFunctionName();

    /**
     * Provides the {@link com.atlassian.jira.JiraDataType} that this function handles and creates values for. This
     * allows us to infer some information about how it will interact with other elements in the system.
     * <p/>
     * For example, if this returns {@link com.atlassian.jira.JiraDataTypes#DATE} then we know that we can provide
     * values for any clauses that also specify a data type of DATE.
     *
     * @return the JiraDataType that this function produces values for. Cannot be null.
     * @see com.atlassian.jira.JiraDataTypes
     */
    @Nonnull
    JiraDataType getDataType();
}
