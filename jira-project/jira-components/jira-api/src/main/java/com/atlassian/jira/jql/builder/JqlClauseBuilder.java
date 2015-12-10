package com.atlassian.jira.jql.builder;

import java.util.Collection;
import java.util.Date;

import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import net.jcip.annotations.NotThreadSafe;

/**
 * A builder used to construct the Where {@link Clause} portion of a JQL {@link com.atlassian.query.Query} in a fluent programming
 * structure. JQL queries can be defined as one or more terminal clauses, seperated by logical operators, where terminal clauses
 * define value conditions on specific fields.
 * <p/>
 * This builder provided methods to build terminal clauses specific to system fields (e.g. {@link #reporter()})
 * and short cuts for common terminal clauses  (e.g. {@link #unresolved() which produce the terminal clause {@code resolution = Unresolved}.
 * But also allows the programmer to define his terminal clause components manually, for example
 * {@code builder.field("cf[100]").in().strings("jql", "rocks").buildQuery()}, this is useful for custom fields.
 * <p/>
 * To build Where Clauses with more than one terminal clause, the logical operators must be defined by the programmer between
 * each call to a terminal clause method, or a default operator must be set. For example to produce the JQL {@code project = HSP and issuetype = bug}
 * the builder would be used as such {@code builder.project("HSP").and().issueType("bug").buildQuery()} or
 * {@code builder.defaultAnd().project("HSP").issueType("bug").buildQuery()}. Not defining the operator, such as
 * {@code builder.project("HSP").issueType("bug").buildQuery()} will cause an illegal state exception.
 * <p/>
 * Different logical operators can be specified by the programmer by using the {@link com.atlassian.jira.jql.builder.ConditionBuilder} returned by the field
 * level methods such as {@link #project()}. For instance to create the terminal clause {@code component != searching} the programmer would use
 * the builder as such {@code builder.component().notEq().string("searching")}.
 * <p/>
 * By default the builder uses the standard order of precedence. However if the programmer wishes to define their own order,
 * they can make use of the {@link #sub()} and {@link #endsub()} methods, which effectively add opening and closing parenthesis to
 * the JQL respectively. For instance to create the JQL {@code (resolution is unresolved and assignee is empty) or resolution = fixed}
 * the programmer would use the builder as such {@code builder.sub().field("resolution").and.assigneeIsEmpty().endsub().or().resolution().eq("fixed")}
 * <p/>
 * Generally, it is not possible to passs nulls, empty collections, empty arrays, collections that contain nulls, or arrays
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
 * @since v4.0
 */
@NotThreadSafe
public interface JqlClauseBuilder
{
    /**
     * Call this to get a handle on the associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}.
     *
     * @return the associated {@link com.atlassian.jira.jql.builder.JqlQueryBuilder}. Null may be returned to indicate
     * there is no associated builder.
     */
    JqlQueryBuilder endWhere();

    /**
     * Call this method to build a {@link Query} using the current builder. When {@link #endWhere()} is not null, this
     * equates to calling {@code endWhere().buildQuery()}. When {@code endWhere()} is null, this equates to calling
     * {@code new QueryImpl(buildClause())}.
     *
     * @throws IllegalStateException if it is not possible to build the current query given the state of the builder.
     * @return the newly generated query query.
     */
    Query buildQuery();

    /**
     * Reset the builder to its empty state.
     *
     * @return the reset builder.
     */
    JqlClauseBuilder clear();

    /**
     * Tell the builder to combine JQL conditions using the "AND" operator when none has been specified. Normally the
     * caller must ensure that a call to either {@link #and()} or {@link #or()} is placed between calls to create JQL
     * conditions. Calling this method on the builder tells it to automatically add a JQL "AND"
     * between JQL conditions when no calls to either {@code and} or {@code or} have been made. This mode will remain
     * active until one of {@link #defaultNone()}, {@code defaultOr()} or {@link #clear()} is called.
     * <p/>
     * While in this mode it is still possible to explicitly call {@code and} or {@code or} to overide the default
     * operator for the current condition.
     *<p/>
     * For example {@code builder.where().assigneeIsEmpty().or().defaultAnd().reporterIsCurrentUser().affectedVersion("10.5").defaultOr().issueType("bug").buildQuery()}
     * will build the JQL query "assignee is empty or reporter = currentUser() and affectedVersion = '10.5' or issuetype = bug".
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    JqlClauseBuilder defaultAnd();

    /**
     * Tell the builder to combine JQL conditions using the "OR" operator when none has been specified. Normally the
     * caller must ensure that a call to either {@link #and()} or {@link #or()} is placed between calls to create JQL
     * conditions. Calling this method on the builder tells it to automatically add a JQL "OR"
     * between JQL conditions when no calls to either {@code and} or {@code or} have been made. This mode will remain
     * active until one of {@link #defaultNone()}, {@code defaultAnd()} or {@link #clear()} is called.
     * <p/>
     * While in this mode it is still possible to explicitly call {@code and} or {@code or} to overide the default
     * operator for the current condition.
     * <p/>
     * For example {@code builder.where().assigneeIsEmpty().and().defaultOr().reporterIsCurrentUser().affectedVersion("10.5").defaultOr().issueType("bug").buildQuery()}
     * will build the JQL query "assignee is empty and reporter = currentUser() or affectedVersion = '10.5' or issuetype = bug". 
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    JqlClauseBuilder defaultOr();

    /**
     * Tell the builder to stop injecting JQL "AND" or "OR" operators automatically between the generated JQL
     * conditions. This essentially turns off the behaviour started by calling either
     * {@link #defaultAnd()} or {@link #defaultOr()}.
     *
     * @return a builder that can be used to further extends the current JQL expression.
     */
    JqlClauseBuilder defaultNone();

    /**
     * Add the JQL "AND" operator to the JQL expression currently being built. The builder takes into account operator
     * precendence when generating the JQL expression, and as such, the caller may need to group JQL conditions using
     * the {@link #sub()} and {@link #endsub()} calls. For example, {@code builder.not().affectedVersion("11").and().effectedVersion("12")}
     * produces the JQL {@code NOT (affectedVersion = "11") and affectedVersion = "12"} as the {@link #not() "NOT"
     * operator} has a higher precedence than "AND". On the other hand, {@code builder.not().sub().affectedVersion("11").and().effectedVersion("12").endsub()}
     * produces the JQL {@code NOT(affectedVersion = "11" andaffectedVersion = "12")}.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add the AND operator given the current state of the
     * builder.
     */
    JqlClauseBuilder and();

    /**
     * Add the JQL "OR" operator to the JQL expression currently being built. The builder takes into account operator
     * precendence when generating the JQL expression, and as such, the caller may need to group JQL conditions using
     * the {@link #sub()} and {@link #endsub()} calls. For example, {@code builder.issueType("bug").and().affectedVersion("11").or().affectedVersion("12")}
     * produces the JQL {@code (issueType = "bug" andaffectedVersion = "11") or affectedVersion = "12"} as the {@link
     * #and() "AND" operator} has a higher precedence than "OR". On the other hand, {@code
     * builder.issueType("bug").and().sub().affectedVersion("11").or().affectedVersion("12").endsub()} produces the JQL
     * {@code issueType = "bug" and (affectedVersion = "11" or affectedVersion = "12")}.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add the OR operator given the current state of the
     * builder.
     */
    JqlClauseBuilder or();

    /**
     * Add the JQL "NOT" operator to the JQL expression currently being built. The builder takes into account operator
     * precendence when generating the JQL expression, and as such, the caller may need to group JQL conditions using
     * the {@link #sub()} and {@link #endsub()} calls. For example, {@code builder.not().affectedVersion("11").and().effectedVersion("12")}
     * produces the JQL {@code NOT (affectedVersion = "11") and affectedVersion = "12"} as the {@link #and()} "AND"
     * operator} has a lower precedence than "NOT". On the other hand, {@code builder.not().sub().affectedVersion("11").and().effectedVersion("12").endsub()}
     * produces the JQL {@code NOT(affectedVersion = "11" andaffectedVersion = "12")}.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add the NOT operator given the current state of the
     * builder.
     */
    JqlClauseBuilder not();

    /**
     * Create a new sub expression in the current JQL. This essentialy opens a bracket in the JQL query such that all
     * the JQL expressions from now until the next matching {@link #endsub() close bracket} are grouped together. This
     * can be used to override JQL's precedence rules. For example, {@code builder.sub().affectedVersion("12").or().affectedVersion("11").endsub().and().issueType("bug")}
     * will produce the JQL query {@code (affectedVersion = "12" oraffectedVersion = "12") and type = "bug"}.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to create a sub JQL expression given the current state of the
     * builder.
     */
    JqlClauseBuilder sub();

    /**
     * End the current sub JQL expression. This essentially adds a close bracket to the JQL query which will close the
     * last {@link #sub() open bracket}.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if there is not current sub expression to close, that is, there is no matching call
     * to {@link #sub}.
     * @see #sub()
     */
    JqlClauseBuilder endsub();

    /**
     * Add a condtion to the query that finds the issues associated with a particular affected version. This essentially
     * adds the JQL condition {@code affectedVersion = "value"} to the query being built.
     *
     * @param version the version to search for. Can be passed as its name (e.g. "1.2") or the string representation of
     * its internal JIRA ID (e.g. "102020"). Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder affectedVersion(String version);

    /**
     * Add a condition to the query that finds the issues associated particular set of affected versions. This
     * essentially adds the JQL condition {@code affectedVersion in (versions)} to the query being built.
     *
     * @param versions the affected versions to search for. Each version can be specified either by its name (e.g.
     * "1.2") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder affectedVersion(String... versions);

    /**
     * Adds adds a condition to the query to find issues with no assigned affected version. This essentially adds the
     * JQL condition {@code affectedVersion IS EMPTY} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder affectedVersionIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the affected version.
     *
     * @return a reference to a condition builder for affected version.
     */
    ConditionBuilder affectedVersion();

    /**
     * Add a condtion to the query that finds the issues associated with a particular fix version. This essentially adds
     * the JQL condition {@code fixVersion = "value"} to the query being built.
     *
     * @param version the version to search for. Can be passed as its name (e.g. "1.2") or the string representation of
     * its internal JIRA ID (e.g. "102020"). Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder fixVersion(String version);

    /**
     * Add a condition to the query that finds the issues associated particular set of fix versions. This essentially
     * adds the JQL condition {@code fixVersion in (versions)} to the query being built.
     *
     * @param versions the fix versions to search for. Each version can be specified either by its name (e.g. "1.2") or
     * by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder fixVersion(String... versions);

    /**
     * Add a condtion to the query that finds the issues associated with a particular fix version. This essentially adds
     * the JQL condition {@code fixVersion = version} to the query being built.
     *
     * @param version the version to search for. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder fixVersion(Long version);

    /**
     * Add a condition to the query that finds the issues associated particular set of fix versions. This essentially
     * adds the JQL condition {@code fixVersion in (versions)} to the query being built.
     *
     * @param versions the fix versions to search for. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder fixVersion(Long... versions);

    /**
     * Adds adds a condition to the query to find issues with no assigned fix version. This essentially adds the JQL
     * condition {@code fixVersion IS EMPTY} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder fixVersionIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the fix version.
     *
     * @return a reference to a ConditionBuilder for fix version.
     */
    ConditionBuilder fixVersion();

    /**
     * Add a condition to the query that finds the issues associated particular set of priorities. This essentially adds
     * the JQL condition {@code priority in (priorities)} to the query being built.
     *
     * @param priorities the JIRA priorities to search for. Each priority can be specified either by its name (e.g.
     * "Major") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder priority(String... priorities);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the priority.
     *
     * @return a reference to a ConditionBuilder for priority.
     */
    ConditionBuilder priority();

    /**
     * Add a condition to the query that finds the issues associated particular set of resolutions. This essentially
     * adds the JQL condition {@code resolution in (resultions)} to the query being built.
     *
     * @param resolutions the JIRA resolutions to search for. Each resolution can be specified  either by its name (e.g.
     * "Resolved") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder resolution(String... resolutions);

    /**
     * Add a condition to query that finds the issues that have not been resolved. This essentially adds the JQL
     * condition {@code resolution IS EMPTY} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder unresolved();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * resolution.
     *
     * @return a reference to a ConditionBuilder for resolution.
     */
    ConditionBuilder resolution();

    /**
     * Add a condition to the query that finds the issues associated particular set of statuses. This essentially adds
     * the JQL condition {@code status in (statuses)} to the query being built.
     *
     * @param statuses the JIRA statuses to search for. Each status can be specified  either by its name (e.g. "Won't
     * Fix") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder status(String... statuses);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * status.
     *
     * @return a reference to a ConditionBuilder for status.
     */
    ConditionBuilder status();

    /**
     * Add a condition to the query that finds the issues associated particular set of statuses of given category. This essentially adds
     * the JQL condition {@code statusCategory in (categories)} to the query being built.
     *
     * @param categories the JIRA status categories to search for. Each category can be specified  either by its name (e.g. "new")
     * or by its JIRA ID as a string (e.g. "2"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     *
     * @since v6.2
     */
    JqlClauseBuilder statusCategory(final String... categories);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * statusCategory.
     *
     * @return a reference to a ConditionBuilder for statusCategory.
     *
     * @since v6.2
     */
    ConditionBuilder statusCategory();


    /**
     * Add a condition to the query that finds the issues of a particular type. This essentially adds the JQL condition
     * {@code issuetype in (types)} to the query being built.
     *
     * @param types the JIRA issue types to search for. Each type can be specified either by its name (e.g. "Bug") or by
     * its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueType(String... types);

    /**
     * Add a condition to the query that finds the "standard" issue types. Standard issues types are those that are not
     * sub-tasks.  This essentially adds the JQL condition {@code issuetype in standardIssueTypes() } to the query being
     * built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueTypeIsStandard();

    /**
     * Add a condition to the query that finds the "sub-task" issue types. This essentially adds the JQL condition
     * {@code issuetype in subTaskIssueTypes() } to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueTypeIsSubtask();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue types.
     *
     * @return a reference to a ConditionBuilder for issue types.
     */
    ConditionBuilder issueType();

    /**
     * Add a condition to the query that finds the issues that match the passed description. This essentially adds the
     * condition {@code description ~ "value"} to the query being built.
     * <p/>
     * NOTE: The description field performs apporximate text matching not exact text matching.
     *
     * @param value the value of the condition.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder description(String value);

    /**
     * Add a condition to the query that finds the issues that have no description. This essentially adds the condition
     * {@code description IS EMPTY} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder descriptionIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue descriptions.
     *
     * @return a reference to a ConditionBuilder for issue descriptions.
     */
    ConditionBuilder description();

    /**
     * Add a condition to the query that finds the issues match the passed summary. This essentially adds the condition
     * {@code summary ~ "value"} to the query being built.
     * <p/>
     * NOTE: The summary field performs apporximate text matching not exact text matching.
     *
     * @param value the value of the condition.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder summary(String value);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue summaries.
     *
     * @return a reference to a ConditionBuilder for issue summaries.
     */
    ConditionBuilder summary();

    /**
     * Add a condition to the query that finds the issues that match the passed environment. This essentially adds the
     * condition {@code environment ~ "value"} to the query being built.
     * <p/>
     * NOTE: The environment field performs apporximate text matching not exact text matching.
     *
     * @param value the value of the condition.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder environment(String value);

    /**
     * Add a condition to the query that finds the issues that have no environment. This essentially adds the condition
     * {@code environment IS EMPTY} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder environmentIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue environments.
     *
     * @return a reference to a ConditionBuilder for issue environments.
     */
    ConditionBuilder environment();

    /**
     * Add a condition to the query that finds the issues that match the passed comment. This essentially adds the
     * condition {@code comment ~ "value"} to the query being built.
     * <p/>
     * NOTE: The comment field performs apporximate text matching not exact text matching.
     *
     * @param value the value of the condition.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder comment(String value);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue comments.
     *
     * @return a reference to a ConditionBuilder for issue comments.
     */
    ConditionBuilder comment();

    /**
     * Add a condition to the query that finds the issues within a particular project. This essentially adds the JQL
     * condition {@code project in (projects)} to the query being built.
     *
     * @param projects the JIRA projects to search for. Each project can be specified by its name (e.g. "JIRA"), its key
     * (e.g. "JRA") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder project(String... projects);

    /**
     * Add a condition to the query that finds the issues within a particular project. This essentially adds the JQL
     * condition {@code project in (pids)} to the query being built.
     *
     * @param pids the JIRA id's of the projects to search for. Cannot be null, empty or contain null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder project(Long... pids);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * an issue's project.
     *
     * @return a reference to a ConditionBuilder for projects
     */
    ConditionBuilder project();

    /**
     * Add a condition to the query that finds the issues from projects within a list of project categories. This
     * essentially adds the JQL condition {@code category in (categories)} to the query being built.
     *
     * @param categories the JIRA project categories for the condition. Each project can be specified by its name (e.g.
     * "Atlassian Products") or by its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null
     * values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder category(String... categories);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's in a particular project category. An issue is in a project category only when it is in a project that is
     * part of the category.
     *
     * @return a reference to a ConditionBuilder for project category.
     */
    ConditionBuilder category();

    /**
     * Add a condition to the query that finds the issues that were created after the passed date. This essentially
     * adds the query {@code created &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be created after. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder createdAfter(Date startDate);

    /**
     * Add a condition to the query that finds the issues that were created after the passed date. This essentially
     * adds the query {@code created &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be created after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder createdAfter(String startDate);

    /**
     * Add a condition to the query that finds the issues that where created between the passed dates. This essentially
     * adds the query {@code created &gt;= startDate AND created &lt;= endDate} to the query being built. </p> 
     * It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code startDate} with a null {@code endDate} will add the condition {@code created &gt;= startDate}. Passing a
     * non-null {@code endDate} with a null {@code startDate} will add the condition {@code created &lt;= endDate}.
     * Passing a null {@code startDate} and null {@code endDate} is illegal.
     *
     * @param startDate the date that issues must be created on or after. May be null if {@code endDate} is not null.
     * @param endDate the date that issues must be created on or before. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder createdBetween(Date startDate, Date endDate);

    /**
     * Add a condition to the query that finds the issues that where created between the passed dates. This essentially
     * adds the query {@code created &gt;= startDateString AND created &lt;= endDateString} to the query being built.
     * </p> It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a
     * non-null {@code startDateString} with a null {@code endDateString} will add the condition {@code created &gt;=
     * startDateString}. Passing a non-null {@code endDateString} with a null {@code startDateString} will add the
     * condition {@code created &lt;= endDateString}. Passing a null {@code startDateString} and null {@code
     * endDateString} is illegal.
     *
     * @param startDateString the date that issues must be created on or after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code endDateString} is not null.
     * @param endDateString the date that issues must be created on or before. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code startDateString} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDateString} and {@code endDateString} are null.
     */
    JqlClauseBuilder createdBetween(String startDateString, String endDateString);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's creation date.
     *
     * @return a reference to a ConditionBuilder for created date.
     */
    ConditionBuilder created();

    /**
     * Add a condition to the query that finds the issues that were updated after the passed date. This essentially
     * adds the query {@code updated &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be updated after. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder updatedAfter(Date startDate);

    /**
     * Add a condition to the query that finds the issues that were updated after the passed date. This essentially
     * adds the query {@code updated &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be updated after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder updatedAfter(String startDate);

    /**
     * Add a condition to the query that finds the issues that where updated between the passed dates. This essentially
     * adds the query {@code updated &gt;= startDate AND updated &lt;= endDate} to the query being built. </p> It is
     * also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code startDate} with a null {@code endDate} will add the condition {@code updated &gt;= startDate}. Passing a
     * non-null {@code endDate} with a null {@code startDate} will add the condition {@code updated &lt;= endDate}.
     * Passing a null {@code startDate} and null {@code endDate} is illegal.
     *
     * @param startDate the date that issues must be updated on or after. May be null if {@code endDate} is not null.
     * @param endDate the date that issues must be updated on or before. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder updatedBetween(Date startDate, Date endDate);

    /**
     * Add a condition to the query that finds the issues that where updated between the passed dates. This essentially
     * adds the query {@code updated &gt;= startDateString AND updated &lt;= endDateString} to the query being built.
     * </p> It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a
     * non-null {@code startDateString} with a null {@code endDateString} will add the condition {@code updated &gt;=
     * startDateString}. Passing a non-null {@code endDateString} with a null {@code startDateString} will add the
     * condition {@code updated &lt;= endDateString}. Passing a null {@code startDateString} and null {@code
     * endDateString} is illegal.
     *
     * @param startDateString the date that issues must be updated on or after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code endDateString} is not null.
     * @param endDateString the date that issues must be updated on or before. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code startDateString} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDateString} and {@code endDateString} are null.
     */
    JqlClauseBuilder updatedBetween(String startDateString, String endDateString);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's updated date.
     *
     * @return a reference to a ConditionBuilder for updated date.
     */
    ConditionBuilder updated();

    /**
      * Add a condition to the query that finds the issues that are due after the passed date. This essentially
      * adds the query {@code duedate &gt;= startDate} to the query being built.
      *
      * @param startDate the date that issues must be due after. Cannot be null.
      * @return a reference to the current builder.
      * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
      * builder.
      */
    JqlClauseBuilder dueAfter(Date startDate);

    /**
     * Add a condition to the query that finds the issues that are due after the passed date. This essentially
     * adds the query {@code duedate &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be due after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder dueAfter(String startDate);

    /**
     * Add a condition to the query that finds the issues that where due between the passed dates. This essentially adds
     * the query {@code duedate &gt;= startDate AND duedate &lt;= endDate} to the query being built. </p> It is also
     * possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code
     * startDate} with a null {@code endDate} will add the condition {@code duedate &gt;= startDate}. Passing a non-null
     * {@code endDate} with a null {@code startDate} will add the condition {@code duedate &lt;= endDate}. Passing a
     * null {@code startDate} and null {@code endDate} is illegal.
     *
     * @param startDate the date that issues must be due on or after. May be null if {@code endDate} is not null.
     * @param endDate the date that issues must be due on or before. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder dueBetween(Date startDate, Date endDate);

    /**
     * Add a condition to the query that finds the issues that where due between the passed dates. This essentially adds
     * the query {@code duedate &gt;= startDateString AND duedate &lt;= endDateString} to the query being built. </p> It
     * is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code startDateString} with a null {@code endDateString} will add the condition {@code duedate &gt;=
     * startDateString}. Passing a non-null {@code endDateString} with a null {@code startDateString} will add the
     * condition {@code duedate &lt;= endDateString}. Passing a null {@code startDateString} and null {@code
     * endDateString} is illegal.
     *
     * @param startDateString the date that issues must be due on or after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code endDateString} is not null.
     * @param endDateString the date that issues must be due on or before. Can be a date (e.g. "2008-10-23") or a period
     * (e.g. "-3w"). May be null if {@code startDateString} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDateString} and {@code endDateString} are null.
     */
    JqlClauseBuilder dueBetween(String startDateString, String endDateString);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's due date.
     *
     * @return a reference to a ConditionBuilder for due date.
     */
    ConditionBuilder due();


    /**
      * Add a condition to the query that finds the issues that where last viewed after the passed date (if issue is
      * stored in history kept). This essentially adds the query {@code lastViewed &gt;= startDate} to the query being built.
      *
      * @param startDate the date that issues must be viewed after. Cannot be null.
      * @return a reference to the current builder.
      * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
      * builder.
      */
    JqlClauseBuilder lastViewedAfter(Date startDate);

    /**
     * Add a condition to the query that finds the issues that where viewed after the passed date (if issue is
     * stored in history kept). This essentially adds the query {@code lastViewed &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be viewed after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder lastViewedAfter(String startDate);

    /**
     * Add a condition to the query that finds the issues that where viewed between the passed dates. This essentially adds
     * the query {@code lastViewed &gt;= startDate AND lastViewed &lt;= endDate} to the query being built. </p> It is also
     * possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code
     * startDate} with a null {@code endDate} will add the condition {@code lastViewed &gt;= startDate}. Passing a non-null
     * {@code endDate} with a null {@code startDate} will add the condition {@code lastViewed &lt;= endDate}. Passing a
     * null {@code startDate} and null {@code endDate} is illegal. This will only return issues that are stored in the
     * user's history ~ 50 issues.
     *
     * @param startDate the date that issues must be viewed on or after. May be null if {@code endDate} is not null.
     * @param endDate the date that issues must be viewed on or before. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder lastViewedBetween(Date startDate, Date endDate);

    /**
     * Add a condition to the query that finds the issues that where last viewed between the passed dates. This essentially adds
     * the query {@code lastViewed &gt;= startDateString AND lastViewed &lt;= endDateString} to the query being built. </p> It
     * is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null
     * {@code startDateString} with a null {@code endDateString} will add the condition {@code lastViewed &gt;=
     * startDateString}. Passing a non-null {@code endDateString} with a null {@code startDateString} will add the
     * condition {@code lastViewed &lt;= endDateString}. Passing a null {@code startDateString} and null {@code
     * endDateString} is illegal.
     *
     * @param startDateString the date that issues must be last viewed on or after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code endDateString} is not null.
     * @param endDateString the date that issues must be last viewed on or before. Can be a date (e.g. "2008-10-23") or a period
     * (e.g. "-3w"). May be null if {@code startDateString} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDateString} and {@code endDateString} are null.
     */
    JqlClauseBuilder lastViewedBetween(String startDateString, String endDateString);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's last viewed date.
     *
     * @return a reference to a ConditionBuilder for last viewed date.
     */
    ConditionBuilder lastViewed();

    /**
      * Add a condition to the query that finds the issues that were reolved after the passed date. This essentially
      * adds the query {@code resolutiondate &gt;= startDate} to the query being built.
      *
      * @param startDate the date that issues must be resolved after. Cannot be null.
      * @return a reference to the current builder.
      * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
      * builder.
      */
    JqlClauseBuilder resolutionDateAfter(Date startDate);

    /**
     * Add a condition to the query that finds the issues that were resolved after the passed date. This essentially
     * adds the query {@code resolutiondate &gt;= startDate} to the query being built.
     *
     * @param startDate the date that issues must be resolved after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder resolutionDateAfter(String startDate);

    /**
     * Add a condition to the query that finds the issues that where resolved between the passed dates. This essentially
     * adds the query {@code resolutiondate &gt;= startDate AND resolutiondate &lt;= endDate} to the query being built.
     * </p> It is also possible to create an open interval by passing one of the arguments as {@code null}. Passing a
     * non-null {@code startDate} with a null {@code endDate} will add the condition {@code resolutiondate &gt;=
     * startDate}. Passing a non-null {@code endDate} with a null {@code startDate} will add the condition {@code
     * resolutiondate &lt;= endDate}. Passing a null {@code startDate} and null {@code endDate} is illegal.
     *
     * @param startDate the date that issues must be resolved on or after. May be null if {@code endDate} is not null.
     * @param endDate the date that issues must be resolved on or before. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder resolutionDateBetween(Date startDate, Date endDate);

    /**
     * Add a condition to the query that finds the issues that where resolved between the passed dates. This essentially
     * adds the query {@code resolutiondate &gt;= startDateString AND resolutiondate &lt;= endDateString} to the query
     * being built. </p> It is also possible to create an open interval by passing one of the arguments as {@code null}.
     * Passing a non-null {@code startDateString} with a null {@code endDateString} will add the condition {@code
     * resolutiondate &gt;= startDateString}. Passing a non-null {@code endDateString} with a null {@code
     * startDateString} will add the condition {@code resolutiondate &lt;= endDateString}. Passing a null {@code
     * startDateString} and null {@code endDateString} is illegal.
     *
     * @param startDateString the date that issues must be resolved on or after. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code endDateString} is not null.
     * @param endDateString the date that issues must be resolved on or before. Can be a date (e.g. "2008-10-23") or a
     * period (e.g. "-3w"). May be null if {@code startDateString} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDateString} and {@code endDateString} are null.
     */
    JqlClauseBuilder resolutionDateBetween(String startDateString, String endDateString);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * issue's resolution date.
     *
     * @return a reference to a ConditionBuilder for resolution date.
     */
    ConditionBuilder resolutionDate();

    /**
     * Add a condition to the query that finds issues that where reported by the passed user. This essentially adds the
     * condition {@code reporter = userName} to the query being built.
     *
     * @param userName the username to search for. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder reporterUser(String userName);

    /**
     * Add a condition to the query that finds all issues that were reported by users in a particular group. This essentially
     * adds the condition {@code reporter in memboersOf("groupName")} to the query being built.
     *
     * @param groupName the group for the condition. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder reporterInGroup(String groupName);

    /**
     * Add a condition to the query that finds all issues that were reported by the current user. This essentially adds the
     * condition {@code reporter = currentUser()} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder reporterIsCurrentUser();

    /**
     * Add a condition to the query to find issues without a reporter. This essentially adds the condition {@code reporter IS EMPTY}
     * to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder reporterIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's reporter.
     *
     * @return a reference to a ConditionBuilder for reporter.
     */
    ConditionBuilder reporter();

    /**
     * Add a condition to the query that finds issues that are assigned to the passed user. This essentially adds the
     * condition {@code assignee = userName} to the query being built.
     *
     * @param userName the username to search for. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder assigneeUser(String userName);

    /**
     * Add a condition to the query that finds all issues that are assigned to users in a particular group. This essentially
     * adds the condition {@code assignee in memboersOf("groupName")} to the query being built.
     *
     * @param groupName the group for the condition. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder assigneeInGroup(String groupName);

    /**
     * Add a condition to the query that finds all issues that are assigned to the current user. This essentially adds the
     * condition {@code assignee = currentUser()} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder assigneeIsCurrentUser();

    /**
     * Add a condition to the query to find all unassigned issues. This essentially adds the condition {@code assignee IS EMPTY}
     * to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder assigneeIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's assignee.
     *
     * @return a reference to a ConditionBuilder for assignee.
     */
    ConditionBuilder assignee();

    /**
     * Add a condition to the query to find all issues with particular components. This essentially adds the JQL
     * condition {@code component in (components)} to the query.
     *
     * @param components the JIRA components to search for. Each component can be specified by its name (e.g. "Web") or by
     * its JIRA ID as a string (e.g. "10000"). Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder component(String... components);

    /**
     * Add a condition to the query to find all issues with particular components. This essentially adds the JQL
     * condition {@code component in (components)} to the query.
     *
     * @param components the ids of the components to search for. Must not be null, contain nulls or be empty.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder component(Long... components);

    /**
     * Add a condition to the query to find all issues that have not component assigned. This essentially adds the JQL
     * condition {@code component IS EMTPY} to the query.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder componentIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's component.
     *
     * @return a reference to a ConditionBuilder for component.
     */
    ConditionBuilder component();

    /**
     * Add a condition to the query to find all issues with particular labels. This essentially adds the JQL condition
     * {@code labels in (labels)} to the query.
     *
     * @param labels the labels to search for. Must not be null, contain nulls or be empty.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder labels(String... labels);

    /**
     * Add a condition to the query to find all issues that have no labels. This essentially adds the JQL condition
     * {@code labels IS EMTPY} to the query.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder labelsIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's labels.
     *
     * @return a reference to a ConditionBuilder for labels.
     */
    ConditionBuilder labels();

    /**
     * Add a condition to the query that will find all issues with the passed key. This essentially adds the JQL condition
     * {@code key IN (keys)} to the query.
     *
     * @param keys the issues keys to search for. Cannot be null, empty or contain any nulls.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issue(String... keys);

    /**
     * Add a condition to the query that will find all issues currently within the user's history. This essentially adds
     * the JQL condition {@code key IN issueHistory()} to the query.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueInHistory();

    /**
     * Add a condition to the query that will find all issues currently watched by the user. This essentially adds
     * the JQL condition {@code key IN watchedIssues()} to the query.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueInWatchedIssues();

    /**
     * Add a condition to the query that will find all issues the user has voted on. This essentially adds
     * the JQL condition {@code key IN votedIssues()} to the query.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueInVotedIssues();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's id or key.
     *
     * @return a reference to a ConditionBuilder for issue id or key.
     */
    ConditionBuilder issue();

    /**
     * Add a condition to the query that will find all issues that have the passed issues as parents. This essentially
     * adds the condition {@code parent IN (keys)} to the query.
     *
     * @param keys the issues keys to search for. Cannot be null, empty or contain any nulls.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder issueParent(String... keys);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's parent.
     *
     * @return a reference to a ConditionBuilder for issue parent.
     */
    ConditionBuilder issueParent();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's current estimate.
     *
     * @return a reference to a ConditionBuilder for current estimate.
     */
    ConditionBuilder currentEstimate();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's original estimate.
     *
     * @return a reference to a ConditionBuilder for original estimate.
     */
    ConditionBuilder originalEstimate();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's timespent field.
     *
     * @return a reference to a ConditionBuilder for timespent.
     */
    ConditionBuilder timeSpent();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's work ratio field.
     *
     * @return a reference to a ConditionBuilder for work ratio.
     */
    ConditionBuilder workRatio();

    /**
     * Add a condition to the query that will find all issues with the passed security levels. This essentially adds
     * the condition {@code level IN (levels)} to the query.
     *
     * @param levels the security levels to search for. Cannot be null, empty or contain nulls.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder level(String... levels);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the issue's security level.
     *
     * @return a reference to a ConditionBuilder for issue level.
     */
    ConditionBuilder level();

    /**
     * Add a condition to the query that will inclue the results from the passed filters in the search. This essentially
     * adds the condition {@code filter IN (filters)} to the condition.
     *
     * @param filters the filters to include in the search. They can be specified by the name (e.g. "JIRA Unresolved") or
     * by their JIRA id (e.g. "10000"). Cannot be null, empty or contain any nulls.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder savedFilter(String... filters);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to add saved filters as conditions
     * to the query.
     *
     * @return a reference to a ConditionBuilder for saved filters.
     */
    ConditionBuilder savedFilter();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the number of votes on an issue.
     *
     * @return a reference to a ConditionBuilder for votes.
     */
    ConditionBuilder votes();

    /**
     * Add a condition to the query that finds issues that are voted for by the passed user. This essentially adds the
     * condition {@code voter = userName} to the query being built.
     *
     * @param userName the username to search for. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder voterUser(String userName);

    /**
     * Add a condition to the query that finds all issues that were voted for by users in a particular group. This essentially
     * adds the condition {@code voter in membersOf("groupName")} to the query being built.
     *
     * @param groupName the group for the condition. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder voterInGroup(String groupName);

    /**
     * Add a condition to the query that finds all issues that were voted for by the current user. This essentially adds the
     * condition {@code voter = currentUser()} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder voterIsCurrentUser();

    /**
     * Add a condition to the query to find issues without any votes. This essentially adds the condition {@code voter IS EMPTY}
     * to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder voterIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the number of votes on an issue.
     *
     * @return a reference to a ConditionBuilder for votes.
     */
    ConditionBuilder voter();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the number of watches on an issue.
     *
     * @return a reference to a ConditionBuilder for votes.
     */
    ConditionBuilder watches();

    /**
     * Add a condition to the query that finds issues that are watched by the passed user. This essentially adds the
     * condition {@code watcher = userName} to the query being built.
     *
     * @param userName the username to search for. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder watcherUser(String userName);

    /**
     * Add a condition to the query that finds issues which contains/do not contain attachments.
     *
     * @param hasAttachment true if expecting issues with attachments.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder attachmentsExists(boolean hasAttachment);

    /**
     * Add a condition to the query that finds all issues that were watched by users in a particular group. This essentially
     * adds the condition {@code watcher in membersOf("groupName")} to the query being built.
     *
     * @param groupName the group for the condition. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder watcherInGroup(String groupName);

    /**
     * Add a condition to the query that finds all issues that were watched by the current user. This essentially adds the
     * condition {@code watcher = currentUser()} to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder watcherIsCurrentUser();

    /**
     * Add a condition to the query to find issues without any watchers. This essentially adds the condition {@code watcher IS EMPTY}
     * to the query being built.
     *
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder watcherIsEmpty();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the number of watches on an issue.
     *
     * @return a reference to a ConditionBuilder for watcher.
     */
    ConditionBuilder watcher();

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the passed name.
     *
     * @param jqlName the name of the JQL condition. Cannot be null.
     * @return a reference to a ConditionBuilder for the passed name.
     */
    ConditionBuilder field(String jqlName);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * a custom field with the passed id.
     *
     * @param id the ID for the custom field. Cannot be null.
     * @return a reference to a ConditionBuilder for the passed ID.
     */
    ConditionBuilder customField(Long id);

    /**
     * Add the passed JQL condition to the query being built.
     *
     * @param clause the clause to add. Must not be null.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addClause(Clause clause);

    /**
     * Add the JQL condition {@code clausename operator date} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param date the date for the condition. Must not be null.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addDateCondition(String clauseName, Operator operator, Date date);

    /**
     * Add the JQL condition {@code clauseName in (dates)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param dates dates for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addDateCondition(String clauseName, Date... dates);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param dates date values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addDateCondition(String clauseName, Operator operator, Date... dates);

    /**
     * Add the JQL condition {@code clauseName in (dates)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param dates dates for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addDateCondition(String clauseName, Collection<Date> dates);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param dates date values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addDateCondition(String clauseName, Operator operator, Collection<Date> dates);

    /**
     * Add a condition range condition to the current query for the passed dates. This essentially adds the query {@code
     * clauseName &gt;= startDate AND clauseName &lt;= endDate} to the query being built. </p> It is also possible to
     * create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code startDate}
     * with a null {@code endDate} will add the condition {@code clauseName &gt;= startDate}. Passing a non-null {@code
     * endDate} with a null {@code startDate} will add the condition {@code clauseName &lt;= endDate}. Passing a null
     * {@code startDate} and null {@code endDate} is illegal.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param startDate the date for the start of the range. May be null if {@code endDate} is not null.
     * @param endDate the date for the end of the range. May be null if {@code startDate} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code startDate} and {@code endDate} are null.
     */
    JqlClauseBuilder addDateRangeCondition(String clauseName, Date startDate, Date endDate);

    /**
     * Add the JQL condition {@code clauseName = functionName()} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, String functionName);

    /**
     * Add the JQL condition {@code clauseName = functionName(arg1, arg2, arg3, ..., argN)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @param args the arguments to add to the function. Must not be null or contain any null values.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, String functionName, String... args);

    /**
     * Add the JQL condition {@code clauseName = functionName(arg1, arg2, arg3, ..., argN)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @param args the arguments to add to the function. Must not be null or contain any null values.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, String functionName, Collection<String> args);

    /**
     * Add the JQL condition {@code clauseName operator functionName()} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, Operator operator, String functionName);

    /**
     * Add the JQL condition {@code clauseName operator functionName(arg1, arg2, arg3, ..., argN)} to the query being
     * built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @param args the arguments to add to the function. Must not be null or contain any null values.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, Operator operator, String functionName, String... args);

    /**
     * Add the JQL condition {@code clauseName operator functionName(arg1, arg2, arg3, ..., argN)} to the query being
     * built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param functionName name of the function to call. Must not be null.
     * @param args the arguments to add to the function. Must not be null or contain any null values.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addFunctionCondition(String clauseName, Operator operator, String functionName, Collection<String> args);

    /**
     * Add the JQL condition {@code clauseName = "clauseValue"} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValue string value for the condition. Must not be null.
     * @return a reference to the current builder. Never null.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, String clauseValue);

    /**
     * Add the JQL condition {@code clauseName in (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValues string values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, String... clauseValues);

    /**
     * Add the JQL condition {@code clauseName in (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValues string values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, Collection<String> clauseValues);

    /**
     * Add the JQL condition {@code clauseName operator "clauseValue"} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValue string value for the condition. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, Operator operator, String clauseValue);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValues string values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, Operator operator, String... clauseValues);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValues string values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addStringCondition(String clauseName, Operator operator, Collection<String> clauseValues);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially adds the query {@code
     * clauseName &gt;= start AND clauseName &lt;= end} to the query being built. </p> It is also
     * possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code
     * start} with a null {@code end} will add the condition {@code clauseName &gt;=
     * start}. Passing a non-null {@code end} with a null {@code start} will add the
     * condition {@code clauseName &lt;= end}. Passing a null {@code start} and null {@code
     * end} is illegal.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param start the start of the range. May be null if {@code end} is not null.
     * @param end the end of the range. May be null if {@code start} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     */
    JqlClauseBuilder addStringRangeCondition(String clauseName, String start, String end);

    /**
     * Add the JQL condition {@code clauseName = clauseValue} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValue long value for the condition. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Long clauseValue);

    /**
     * Add the JQL condition {@code clauseName in (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValues long values. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Long... clauseValues);

    /**
     * Add the JQL condition {@code clauseName in (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param clauseValues long values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Collection<Long> clauseValues);

    /**
     * Add the JQL condition {@code clauseName operator clauseValue} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValue long value for the condition. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Operator operator, Long clauseValue);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValues long values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Operator operator, Long... clauseValues);

    /**
     * Add the JQL condition {@code clauseName operator (clauseValues)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param clauseValues long values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addNumberCondition(String clauseName, Operator operator, Collection<Long> clauseValues);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially adds the query {@code
     * clauseName &gt;= start AND clauseName &lt;= end} to the query being built. </p> It is also
     * possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code
     * start} with a null {@code end} will add the condition {@code clauseName &gt;=
     * start}. Passing a non-null {@code end} with a null {@code start} will add the
     * condition {@code clauseName &lt;= end}. Passing a null {@code start} and null {@code
     * end} is illegal.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param start the start of the range. May be null if {@code end} is not null.
     * @param end the end of the range. May be null if {@code start} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     */
    JqlClauseBuilder addNumberRangeCondition(String clauseName, Long start, Long end);

    /**
     * Return a {@link com.atlassian.jira.jql.builder.ConditionBuilder} that can be used to build a JQL condition for
     * the passed JQL name.
     *
     * @param clauseName the name of the JQL condition to add.
     * @return a reference to a condition builder for the passed condition.
     */
    ConditionBuilder addCondition(String clauseName);

    /**
     * Add the JQL condition {@code clauseName = operand} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operand defines an operand that will serve as the clause value. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Operand operand);

    /**
     * Add the JQL condition {@code clauseName in (operands)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operands operands values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Operand... operands);

    /**
     * Add the JQL condition {@code clauseName in (operands)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operands operands values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Collection<? extends Operand> operands);

    /**
     * Add the JQL condition {@code clauseName operator operand} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param operand defines an operand that will serve as the clause value. Must not be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Operator operator, Operand operand);

    /**
     * Add the JQL condition {@code clauseName operator (operands)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param operands values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Operator operator, Operand... operands);

    /**
     * Add the JQL condition {@code clauseName operator (operands)} to the query being built.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param operator one of the enumerated {@link com.atlassian.query.operator.Operator}s. Must not be null.
     * @param operands values for the condition. Must not be null, empty or contain any null values.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     */
    JqlClauseBuilder addCondition(String clauseName, Operator operator, Collection<? extends Operand> operands);

    /**
     * Add a condition range condition to the current query for the passed values. This essentially adds the query {@code
     * clauseName &gt;= start AND clauseName &lt;= end} to the query being built. </p> It is also
     * possible to create an open interval by passing one of the arguments as {@code null}. Passing a non-null {@code
     * start} with a null {@code end} will add the condition {@code clauseName &gt;=
     * start}. Passing a non-null {@code end} with a null {@code start} will add the
     * condition {@code clauseName &lt;= end}. Passing a null {@code start} and null {@code
     * end} is illegal.
     *
     * @param clauseName name of the clause in the condition. Must not be null.
     * @param start the start of the range. May be null if {@code end} is not null.
     * @param end the end of the range. May be null if {@code start} is not null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the
     * builder.
     * @throws IllegalArgumentException if both {@code start} and {@code end} are null.
     */
    JqlClauseBuilder addRangeCondition(String clauseName, Operand start, Operand end);

    /**
     * Add an "IS EMPTY" condition to the current query for the passed JQL clause. This essentially adds the query
     * {@code clauseName IS EMPTY} to the query being built.
     *
     * @param clauseName the clause name for the new condition. Cannot be null.
     * @return a reference to the current builder.
     * @throws IllegalStateException if it is not possible to add a JQL condition given the current state of the builder.
     */
    JqlClauseBuilder addEmptyCondition(String clauseName);

    /**
     * Create the JQL clause the builder has currently constructed. The builder can still be used after this method is
     * called.
     *
     * @return the clause generated by the builder. Can be null if there is no clause to generate.
     * @throws IllegalStateException if it is not possible to build a valid JQL query given the state of the builder.
     */
    Clause buildClause();
}
