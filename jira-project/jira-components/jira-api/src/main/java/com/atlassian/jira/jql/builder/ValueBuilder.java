package com.atlassian.jira.jql.builder;

import com.atlassian.query.operand.Operand;

import java.util.Collection;
import java.util.Date;

/**
 * A builder used during the construction of JQL conditions for a particular JQL field in a fluent programming style. It is used
 * to specify the value or values of a JQL condition whose operator has already been specified using a {@link ConditionBuilder}. For example,
 * a call to {@link ConditionBuilder#eq()} will return a ValueBuilder that can be used to specify which value should be matched
 * by the equals operator.
 * <p> Each JQL condition is essentially structured as {@code name operator operand}. When this object is created, it has an
 * implied "name" and "operator". This object can be used to complete the current JQL condition by creating the operand. For example,
 * {@code JqlQueryBuilder.affectedVersion().eq()} creates a ValueBuilder whose implied name is "affectedVersion" and whose operator
 * is "=" that can used to complete the JQL condition by filling in the operand.
 * <p>Generally, it is not possible to passs nulls, empty collections, empty arrays, collections that contain nulls, or arrays
 * that contain nulls to the methods on this interface. Any exceptions to these argument conditions are documented on the method concerned.
 * Passing a method a bad argument will result in a {@link IllegalArgumentException}.
 * <p/>
 JQL values are of two types {@link String} and {@link Long}. For fields that are resolvable by both Id's and Names (e.g.
 * projects, versions, issue types, components, options etc), the order of resolution depends on the value type. If the JQL
 * value type is long, JIRA will first attempt to find the domain object by Id, if that fails, it will attempt to find
 * the domain object by name with the string value of the long. If the JQL value type is a String, JIRA will first try to find
 * the domain object by name, if that fails AND the string can be parsed into a number, JIRA attempts to find the domain object by
 * id with that number.
 *
 * @see JqlClauseBuilder
 * @see ConditionBuilder
 * @see JqlQueryBuilder
 * @since v4.0
 */
public interface ValueBuilder
{
    /**
     * Finish the current condition such that it matches the passed value. It essentially creates the condition
     * {@code currentName currentOperator value}.
     *
     * @param value the value of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder string(String value);

    /**
     * Finish the current condition such that it matches the passed values. It essentially creates the condition
     * {@code currentName currentOperator (values)}.
     *
     * @param values the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder strings(String... values);

    /**
     * Finish the current condition such that it matches the passed values. It essentially creates the condition
     * {@code currentName currentOperator (values)}.
     *
     * @param values the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder strings(Collection<String> values);

    /**
     * Finish the current condition such that it matches the passed value. It essentially creates the condition
     * {@code current Namecurrent Operator value}.
     *
     * @param value the value of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder number(Long value);

    /**
     * Finish the current condition such that it matches the passed values. It essentially creates the condition
     * {@code currentName currentOperator (values)}.
     *
     * @param values the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder numbers(Long... values);

    /**
     * Finish the current condition such that it matches the passed values. It essentially creates the condition
     * {@code currentName currentOperator (values)}.
     *
     * @param values the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder numbers(Collection<Long> values);

    /**
     * Finish the current condition such that it matches the passed operand. It essentially creates the condition
     * {@code currentName currentOperator operand}.
     *
     * @param operand the value of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder operand(Operand operand);

    /**
     * Finish the current condition such that it matches the passed operands. It essentially creates the condition
     * {@code currentName currentOperator (operands)}.
     *
     * @param operands the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder operands(Operand ... operands);

    /**
     * Finish the current condition such that it matches the passed operands. It essentially creates the condition
     * {@code currentName currentOperator (operands)}.
     *
     * @param operands the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder operands(Collection<? extends Operand> operands);

    /**
     * Finish the current condition such that it looks for empty values. It essentially creates the condition
     * {@code currentName currentOperator EMPTY}.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder empty();

    /**
     * Finish the current condition such that it matches the value(s) returned from the passed function. It essentially creates the condition
     * {@code currentName currentOperator funcName()}.
     *
     * @param funcName the name of the function whose value(s) must be matched.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder function(String funcName);

    /**
     * Finish the current condition such that it matches the value(s) returned from the passed function. It essentially creates the condition
     * {@code currentName currentOperator funcName(arg1, arg2, arg3, ..., arg4)}.
     *
     * @param funcName the name of the function whose value(s) must be matched.
     * @param args the arguments to be passed to the function. Cannot be null or contain null values.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder function(String funcName, String... args);

    /**
     * Finish the current condition such that it matches the value(s) returned from the passed function. It essentially creates the condition
     * {@code currentName currentOperator funcName(arg1, arg2, arg3, ..., arg4)}.
     *
     * @param funcName the name of the function whose value(s) must be matched.
     * @param args the arguments to be passed to the function. Cannot be null or contain null values.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder function(String funcName, Collection<String> args);

    /**
     * Finish the current condition such that it matches the values returned from the "standardIssueTypes" function. This function returns all
     * JIRA issue types that can be associated with top level issues. Only works when sub-tasks are enabled in JIRA.
     * It essentially adds the condition {@code currentName currentOperator standardIssueTypes()}.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionStandardIssueTypes();

    /**
     * Finish the current condition such that it matches the values returned from the "subTaskIssueTypes" function. This function returns all
     * JIRA issue types that can associated with sub-tasks. Only works when sub-tasks are enabled in JIRA. It essentially adds
     * the condition {@code currentName currentOperator subTaskIssueTypes()}.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionSubTaskIssueTypes();

    /**
     * Finish the current condition such that it matches the values returned from the "membersOfFunction" function. This function returns all JIRA
     * users that are a members of the passed group. It essentially adds the condition
     * {@code currentName currentOperator membersOf("groupName")} to the query.
     *
     * @param groupName the name of the group to search. Cannot be null.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionMembersOf(String groupName);

    /**
     * Finish the current condition such that it matches the value returned from the "currentUser" function. This function returns the currently
     * logged in JIRA user. It essentially adds the condition {@code currentName currentOperator currentUser()} to the query.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionCurrentUser();

    /**
     * Finish the current condition such that it matches the values returned from the "issueHistory" function. This function returns the user
     * history for the currently logged in user. It essentially adds the condition {@code currentName currentOperator isssueHistory()} to the query.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionIssueHistory();

    /**
     * Finish the current condition such that it matches the values returned from the "releasedVersions" function. This function returns all the
     * released versions for the specified projects. Passing no projects will return all released versions in JIRA. It essentially
     * adds the condition {@code currentName currentOperator releasedVersions(projects)} to the query.
     *
     * @param projects the projects to add to the condition. Each value can be a project's name (e.g. "JIRA), its key ("e.g. JRA") or its
     * internal JIRA id (e.g. "10000"). Must not be null or contain any null values.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionReleasedVersions(String... projects);

    /**
     * Finish the current condition such that it matches the values returned from the "unreleasedVersions" function. This function returns all the
     * unreleased versions for the specified projects. Passing no projects will return all unreleased versions in JIRA. It essentially
     * adds the condition {@code currentName currentOperator unreleasedVersions(projects)} to the query.
     *
     * @param projects the projects to add to the condition. Each value can be project's name (e.g. "JIRA), its key ("e.g. JRA") or its
     * internal JIRA id (e.g. "10000"). Must not be null or contain any null values.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionUnreleasedVersions(String... projects);

    /**
     * Finish the current condition such that it matches the value returned from the "now" function. This function returns the time when
     * it is run. It essentially adds the condition {@code currentName currentOperator now()} to the query.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionNow();

    /**
     * Finish the current condition such that it matches the value(s) returned from the "watchedIssues" function. This function returns
     * all the issues the current user is watching. It essentially adds the condition {@code currentName currentOperator watchedIssues()}
     * to the query.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionWatchedIssues();

    /**
     * Finish the current condition such that it matches the value(s) returned from the "votedIssues" function. This function returns
     * all the issues the current user has voted for. It essentially adds the condition {@code currentName currentOperator votedIssues()}
     * to the query.
     *
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionVotedIssues();

    /**
     * Finish the current condition such that it matches the value(s) returned from the "linkedIssues" function. This function returns
     * the issues that are linked from the passed issue. It essentially adds the condition {@code currentName currentOperator issueType(issue, issueLikeTyes)}
     * to the query.
     *
     * @param issue the issue whose links should be followed. Can be an issue key or issue id as a string. Cannot be null.
     * @param issueLinkTypes the list of issueLinkTypes to follow. An empty value indicates that all links should be followed. Cannot contain nulls.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionLinkedIssues(String issue, String... issueLinkTypes);

    /**
     * Finish the current condition such that it matches the value(s) returned from the "remoteLinksByGlobalId" function. This function returns
     * the issues that are associated with remote issue links whose globalId is equals to any of the passed {@code globalIds}.
     * It essentially adds the condition {@code currentName currentOperator remoteLinksByGlobalId(globalIds)} to the query.
     *
     * @param globalIds the globalIds to be used to search for remote issue links and then the issues that are associated with those links.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionRemoteLinksByGlobalId(String... globalIds);

    /**
     * Finish the current condition such that it matches the value(s) returned from the "cascadingOption" function.
     * It essentially adds the condition {@code currentName currentOperator cascadingOption(parent).}
     *
     * @param parent the first argument to the "cascadingOption" function. Cannot be null.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionCascaingOption(String parent);

    /**
     * Finish the current condition such that it matches the value(s) returned from the "cascadingOption" function.
     * It essentially adds the condition {@code currentName currentOperator cascadingOption(parent, child).}
     *
     * @param parent the first argument to the "cascadingOption" function. Cannot be null.
     * @param child the second argument to the "cascadingOption" function. Cannot be null.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionCascaingOption(String parent, String child);

    /**
     * Finish the current condition such that it matches the value(s) returned from the "cascadingOption" function.
     * It essentially adds the condition {@code currentName currentOperator cascadingOption(parent, "none").}
     *
     * @param parent parent the first argument to the "cascadingOption" function. Cannot be null.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionCascaingOptionParentOnly(String parent);

    /**
     * Finish the current condition such that it matches the value returned from the "lastLogin" function. This functions
     * returns the time when the current user previously logged in.
     *
     * @return the builder of the overall JQL query
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionLastLogin();

    /**
     * Finish the current condition such that it matches the value returned from the "currentLogin" function. This functions
     * returns the time when the current user logged in.
     *
     * @return the builder of the overall JQL query
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder functionCurrentLogin();

    /**
     * Specify the value that must me matched using the current operator. It essentially creates the condition
     * {@code currentName currentOperator date}.
     *
     * @param date the value of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder date(Date date);

    /**
     * Specify the values that must be matched using the current operator. It essentially creates the condition
     * {@code currentName currentOperator (dates)}.
     *
     * @param dates the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder dates(Date... dates);

    /**
     * Specify the values that must be matched using the current operator. It essentially creates the condition
     * {@code currentName currentOperator (dates)}.
     *
     * @param dates the values of the JQL condition.
     * @return the bulder of the overall JQL query.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */        
    JqlClauseBuilder dates(Collection<Date> dates);
}
