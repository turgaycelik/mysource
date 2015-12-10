package com.atlassian.jira.jql.builder;

import java.util.Collection;
import java.util.Date;

import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.IssueHistoryFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.plugin.jql.function.VotedIssuesFunction;
import com.atlassian.jira.plugin.jql.function.WatchedIssuesFunction;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import net.jcip.annotations.NotThreadSafe;

import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forAffectedVersion;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forAssignee;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forAttachments;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forComments;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forComponent;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forCreatedDate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forCurrentEstimate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forDescription;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forDueDate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forEnvironment;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forFixForVersion;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forIssueKey;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forIssueParent;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forIssueType;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forLabels;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forLastViewedDate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forOriginalEstimate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forPriority;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forProject;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forProjectCategory;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forReporter;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forResolution;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forResolutionDate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forSavedFilter;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forSecurityLevel;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forStatus;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forStatusCategory;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forSummary;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forTimeSpent;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forUpdatedDate;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forVoters;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forVotes;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forWatchers;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forWatches;
import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forWorkRatio;
import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.jql.builder.JqlClauseBuilder}.
 *
 * @since v4.0
 */
@NotThreadSafe
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_NULL_PARAM_DEREF", justification="TODO Brenden needs to fix this")
class DefaultJqlClauseBuilder implements JqlClauseBuilder
{
    private final JqlQueryBuilder parent;
    private final JqlDateSupport jqlDateSupport;
    private SimpleClauseBuilder builder;

    DefaultJqlClauseBuilder(final JqlQueryBuilder parent, final SimpleClauseBuilder builder, final JqlDateSupport support)
    {
        this.builder = notNull("builder", builder);
        this.jqlDateSupport = notNull("support", support);
        this.parent = parent;
    }

    DefaultJqlClauseBuilder(final JqlQueryBuilder parent, final TimeZoneManager timeZoneManager)
    {
        this(parent, new PrecedenceSimpleClauseBuilder(), new JqlDateSupportImpl(timeZoneManager));
    }

    DefaultJqlClauseBuilder(TimeZoneManager timeZoneManager)
    {
        this(null, timeZoneManager);
    }

    public JqlClauseBuilder clear()
    {
        builder = builder.clear();
        return this;
    }

    public JqlQueryBuilder endWhere()
    {
        return parent;
    }

    public Query buildQuery()
    {
        if (parent != null)
        {
            return parent.buildQuery();
        }
        else
        {
            return new QueryImpl(buildClause());
        }
    }

    public JqlClauseBuilder defaultAnd()
    {
        builder = builder.defaultAnd();
        return this;
    }

    public JqlClauseBuilder defaultOr()
    {
        builder = builder.defaultOr();
        return this;
    }

    public JqlClauseBuilder defaultNone()
    {
        builder = builder.defaultNone();
        return this;
    }

    public JqlClauseBuilder and()
    {
        builder = builder.and();
        return this;
    }

    public JqlClauseBuilder or()
    {
        builder = builder.or();
        return this;
    }

    public JqlClauseBuilder not()
    {
        builder = builder.not();
        return this;
    }

    public JqlClauseBuilder sub()
    {
        builder = builder.sub();
        return this;
    }

    public JqlClauseBuilder endsub()
    {
        builder = builder.endsub();
        return this;
    }

    public JqlClauseBuilder affectedVersion(final String version)
    {
        return this.addStringCondition(forAffectedVersion().getJqlClauseNames().getPrimaryName(), version);
    }

    public JqlClauseBuilder affectedVersion(final String... versons)
    {
        return this.addStringCondition(forAffectedVersion().getJqlClauseNames().getPrimaryName(), versons);
    }

    public JqlClauseBuilder affectedVersionIsEmpty()
    {
        return addEmptyCondition(forAffectedVersion().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder affectedVersion()
    {
        return new DefaultConditionBuilder(forAffectedVersion().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder fixVersion(final String version)
    {
        return this.addStringCondition(forFixForVersion().getJqlClauseNames().getPrimaryName(), version);
    }

    public JqlClauseBuilder fixVersion(final String... versions)
    {
        return this.addStringCondition(forFixForVersion().getJqlClauseNames().getPrimaryName(), versions);
    }

    public JqlClauseBuilder fixVersion(final Long version)
    {
        return this.addNumberCondition(forFixForVersion().getJqlClauseNames().getPrimaryName(), version);
    }

    public JqlClauseBuilder fixVersion(final Long... versions)
    {
        return this.addNumberCondition(forFixForVersion().getJqlClauseNames().getPrimaryName(), versions);
    }

    public JqlClauseBuilder fixVersionIsEmpty()
    {
        return addEmptyCondition(forFixForVersion().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder fixVersion()
    {
        return new DefaultConditionBuilder(forFixForVersion().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder priority(final String... priorities)
    {
        return addStringCondition(forPriority().getJqlClauseNames().getPrimaryName(), priorities);
    }

    public ConditionBuilder priority()
    {
        return new DefaultConditionBuilder(forPriority().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder resolution(final String... resolutions)
    {
        return addStringCondition(forResolution().getJqlClauseNames().getPrimaryName(), resolutions);
    }

    public JqlClauseBuilder unresolved()
    {
        return addCondition(forResolution().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new SingleValueOperand(
                ResolutionSystemField.UNRESOLVED_OPERAND));
    }

    public ConditionBuilder resolution()
    {
        return new DefaultConditionBuilder(forResolution().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder status(final String... statuses)
    {
        return addStringCondition(forStatus().getJqlClauseNames().getPrimaryName(), statuses);
    }

    public ConditionBuilder status()
    {
        return new DefaultConditionBuilder(forStatus().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder statusCategory(final String... categories)
    {
        return addStringCondition(forStatusCategory().getJqlClauseNames().getPrimaryName(), categories);
    }

    public ConditionBuilder statusCategory()
    {
        return new DefaultConditionBuilder(forStatusCategory().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder issueType(final String... types)
    {
        return addStringCondition(forIssueType().getJqlClauseNames().getPrimaryName(), types);
    }

    public JqlClauseBuilder issueTypeIsStandard()
    {
        return addFunctionCondition(forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IN,
                AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES);
    }

    public JqlClauseBuilder issueTypeIsSubtask()
    {
        return addFunctionCondition(forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IN,
            AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES);
    }

    public ConditionBuilder issueType()
    {
        return new DefaultConditionBuilder(forIssueType().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder description(final String value)
    {
        return addStringCondition(forDescription().getJqlClauseNames().getPrimaryName(), Operator.LIKE, value);
    }

    public JqlClauseBuilder descriptionIsEmpty()
    {
        return addEmptyCondition(forDescription().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder description()
    {
        return new DefaultConditionBuilder(forDescription().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder summary(final String value)
    {
        return addStringCondition(forSummary().getJqlClauseNames().getPrimaryName(), Operator.LIKE, value);
    }

    public ConditionBuilder summary()
    {
        return new DefaultConditionBuilder(forSummary().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder environment(final String value)
    {
        return addStringCondition(forEnvironment().getJqlClauseNames().getPrimaryName(), Operator.LIKE, value);
    }

    public JqlClauseBuilder environmentIsEmpty()
    {
        return addEmptyCondition(forEnvironment().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder environment()
    {
        return new DefaultConditionBuilder(forEnvironment().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder comment(final String value)
    {
        return addStringCondition(forComments().getJqlClauseNames().getPrimaryName(), Operator.LIKE, value);
    }

    public ConditionBuilder comment()
    {
        return new DefaultConditionBuilder(forComments().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder project(final String... projects)
    {
        return addStringCondition(forProject().getJqlClauseNames().getPrimaryName(), projects);
    }

    public JqlClauseBuilder project(final Long... pids)
    {
        return addNumberCondition(forProject().getJqlClauseNames().getPrimaryName(), pids);
    }

    public ConditionBuilder project()
    {
        return new DefaultConditionBuilder(forProject().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder category(final String... values)
    {
        return addStringCondition(forProjectCategory().getJqlClauseNames().getPrimaryName(), values);
    }

    public ConditionBuilder category()
    {
        return new DefaultConditionBuilder(forProjectCategory().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder createdAfter(final Date startDate)
    {
        return addDateCondition(forCreatedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder createdAfter(final String startDate)
    {
        return addStringCondition(forCreatedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder createdBetween(final Date startDate, final Date endDate)
    {
        return addDateRangeCondition(forCreatedDate().getJqlClauseNames().getPrimaryName(), startDate, endDate);
    }

    public JqlClauseBuilder createdBetween(final String startDateString, final String endDateString)
    {
        return addStringRangeCondition(forCreatedDate().getJqlClauseNames().getPrimaryName(), startDateString, endDateString);
    }

    public ConditionBuilder created()
    {
        return new DefaultConditionBuilder(forCreatedDate().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder updatedAfter(final Date startDate)
    {
        return addDateCondition(forUpdatedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder updatedAfter(final String startDate)
    {
        return addStringCondition(forUpdatedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder updatedBetween(final Date startDate, final Date endDate)
    {
        return addDateRangeCondition(forUpdatedDate().getJqlClauseNames().getPrimaryName(), startDate, endDate);
    }

    public JqlClauseBuilder updatedBetween(final String startDateString, final String endDateString)
    {
        return addStringRangeCondition(forUpdatedDate().getJqlClauseNames().getPrimaryName(), startDateString, endDateString);
    }

    public ConditionBuilder updated()
    {
        return new DefaultConditionBuilder(forUpdatedDate().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder dueAfter(final Date startDate)
    {
        return addDateCondition(forDueDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder dueAfter(final String startDate)
    {
        return addStringCondition(forDueDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder dueBetween(final Date startDate, final Date endDate)
    {
        return addDateRangeCondition(forDueDate().getJqlClauseNames().getPrimaryName(), startDate, endDate);
    }

    public JqlClauseBuilder dueBetween(final String startDateString, final String endDateString)
    {
        return addStringRangeCondition(forDueDate().getJqlClauseNames().getPrimaryName(), startDateString, endDateString);
    }

    public ConditionBuilder due()
    {
        return new DefaultConditionBuilder(forDueDate().getJqlClauseNames().getPrimaryName(), this);
    }

    @Override
    public JqlClauseBuilder lastViewedAfter(Date startDate)
    {
        return addDateCondition(forLastViewedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    @Override
    public JqlClauseBuilder lastViewedAfter(String startDate)
    {
        return addStringCondition(forLastViewedDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    @Override
    public JqlClauseBuilder lastViewedBetween(Date startDate, Date endDate)
    {
        return addDateRangeCondition(forLastViewedDate().getJqlClauseNames().getPrimaryName(), startDate, endDate);
    }

    @Override
    public JqlClauseBuilder lastViewedBetween(String startDateString, String endDateString)
    {
        return addStringRangeCondition(forLastViewedDate().getJqlClauseNames().getPrimaryName(), startDateString, endDateString);
    }

    @Override
    public ConditionBuilder lastViewed()
    {
        return new DefaultConditionBuilder(forLastViewedDate().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder resolutionDateAfter(final Date startDate)
    {
        return addDateCondition(forResolutionDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder resolutionDateAfter(final String startDate)
    {
        return addStringCondition(forResolutionDate().getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, startDate);
    }

    public JqlClauseBuilder resolutionDateBetween(final Date startDate, final Date endDate)
    {
        return addDateRangeCondition(forResolutionDate().getJqlClauseNames().getPrimaryName(), startDate, endDate);
    }

    public JqlClauseBuilder resolutionDateBetween(final String startDateString, final String endDateString)
    {
        return addStringRangeCondition(forResolutionDate().getJqlClauseNames().getPrimaryName(), startDateString, endDateString);
    }

    public ConditionBuilder resolutionDate()
    {
        return new DefaultConditionBuilder(forResolutionDate().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder reporterUser(final String userName)
    {
        Assertions.notNull("userName", userName);

        return addStringCondition(forReporter().getJqlClauseNames().getPrimaryName(), userName);
    }

    public JqlClauseBuilder reporterInGroup(final String groupName)
    {
        Assertions.notNull("groupName", groupName);

        return addCondition(forReporter().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, groupName));
    }

    public JqlClauseBuilder reporterIsCurrentUser()
    {
        return addCondition(forReporter().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new FunctionOperand(
            CurrentUserFunction.FUNCTION_CURRENT_USER));
    }

    public JqlClauseBuilder reporterIsEmpty()
    {
        return addEmptyCondition(forReporter().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder reporter()
    {
        return new DefaultConditionBuilder(forReporter().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder assigneeUser(final String userName)
    {
        Assertions.notNull("userName", userName);

        return addStringCondition(forAssignee().getJqlClauseNames().getPrimaryName(), userName);
    }

    public JqlClauseBuilder assigneeInGroup(final String groupName)
    {
        Assertions.notNull("groupName", groupName);

        return addCondition(forAssignee().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, groupName));
    }

    public JqlClauseBuilder assigneeIsCurrentUser()
    {
        return addCondition(forAssignee().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new FunctionOperand(
            CurrentUserFunction.FUNCTION_CURRENT_USER));
    }

    public JqlClauseBuilder assigneeIsEmpty()
    {
        return addEmptyCondition(forAssignee().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder assignee()
    {
        return new DefaultConditionBuilder(forAssignee().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder component(final String... components)
    {
        return addStringCondition(forComponent().getJqlClauseNames().getPrimaryName(), components);
    }

    public JqlClauseBuilder component(final Long... components)
    {
        return addNumberCondition(forComponent().getJqlClauseNames().getPrimaryName(), components);
    }

    public JqlClauseBuilder componentIsEmpty()
    {
        return addEmptyCondition(forComponent().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder component()
    {
        return new DefaultConditionBuilder(forComponent().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder labels()
    {
        return new DefaultConditionBuilder(forLabels().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder labels(final String... labels)
    {
        return addStringCondition(forLabels().getJqlClauseNames().getPrimaryName(), labels);
    }

    public JqlClauseBuilder labelsIsEmpty()
    {
        return addEmptyCondition(forLabels().getJqlClauseNames().getPrimaryName());
    }

    public JqlClauseBuilder issue(final String... keys)
    {
        return addStringCondition(forIssueKey().getJqlClauseNames().getPrimaryName(), Operator.IN, keys);
    }

    public JqlClauseBuilder issueInHistory()
    {
        return addFunctionCondition(forIssueKey().getJqlClauseNames().getPrimaryName(), Operator.IN, IssueHistoryFunction.FUNCTION_ISSUE_HISTORY);
    }

    public JqlClauseBuilder issueInWatchedIssues()
    {
        return addFunctionCondition(forIssueKey().getJqlClauseNames().getPrimaryName(), Operator.IN, WatchedIssuesFunction.FUNCTION_WATCHED_ISSUES);
    }

    public JqlClauseBuilder issueInVotedIssues()
    {
        return addFunctionCondition(forIssueKey().getJqlClauseNames().getPrimaryName(), Operator.IN, VotedIssuesFunction.FUNCTION_VOTED_ISSUES);
    }

    public ConditionBuilder issue()
    {
        return new DefaultConditionBuilder(forIssueKey().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder issueParent(final String... keys)
    {
        return addStringCondition(forIssueParent().getJqlClauseNames().getPrimaryName(), Operator.IN, keys);
    }

    public ConditionBuilder issueParent()
    {
        return new DefaultConditionBuilder(forIssueParent().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder currentEstimate()
    {
        return new DefaultConditionBuilder(forCurrentEstimate().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder originalEstimate()
    {
        return new DefaultConditionBuilder(forOriginalEstimate().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder timeSpent()
    {
        return new DefaultConditionBuilder(forTimeSpent().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder workRatio()
    {
        return new DefaultConditionBuilder(forWorkRatio().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder level(final String... levels)
    {
        return addStringCondition(forSecurityLevel().getJqlClauseNames().getPrimaryName(), Operator.IN, levels);
    }

    public ConditionBuilder level()
    {
        return new DefaultConditionBuilder(forSecurityLevel().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder savedFilter(final String... filters)
    {
        return addStringCondition(forSavedFilter().getJqlClauseNames().getPrimaryName(), Operator.IN, filters);
    }

    public ConditionBuilder savedFilter()
    {
        return new DefaultConditionBuilder(forSavedFilter().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder votes()
    {
        return new DefaultConditionBuilder(forVotes().getJqlClauseNames().getPrimaryName(), this);
    }

    public ConditionBuilder watches()
    {
        return new DefaultConditionBuilder(forWatches().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder voterUser(final String userName)
    {
        Assertions.notNull("userName", userName);

        return addStringCondition(forVoters().getJqlClauseNames().getPrimaryName(), userName);
    }

    public JqlClauseBuilder voterInGroup(final String groupName)
    {
        Assertions.notNull("groupName", groupName);

        return addCondition(forVoters().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF,
            groupName));
    }

    public JqlClauseBuilder voterIsCurrentUser()
    {
        return addCondition(forVoters().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new FunctionOperand(
            CurrentUserFunction.FUNCTION_CURRENT_USER));
    }

    public JqlClauseBuilder voterIsEmpty()
    {
        return addEmptyCondition(forVoters().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder voter()
    {
        return new DefaultConditionBuilder(forVoters().getJqlClauseNames().getPrimaryName(), this);
    }

    public JqlClauseBuilder watcherUser(final String userName)
    {
        Assertions.notNull("userName", userName);

        return addStringCondition(forWatchers().getJqlClauseNames().getPrimaryName(), userName);
    }

    public JqlClauseBuilder watcherInGroup(final String groupName)
    {
        Assertions.notNull("groupName", groupName);

        return addCondition(forWatchers().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(
            MembersOfFunction.FUNCTION_MEMBERSOF, groupName));
    }

    public JqlClauseBuilder watcherIsCurrentUser()
    {
        return addCondition(forWatchers().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new FunctionOperand(
            CurrentUserFunction.FUNCTION_CURRENT_USER));
    }

    public JqlClauseBuilder watcherIsEmpty()
    {
        return addEmptyCondition(forWatchers().getJqlClauseNames().getPrimaryName());
    }

    public ConditionBuilder watcher()
    {
        return new DefaultConditionBuilder(forWatchers().getJqlClauseNames().getPrimaryName(), this);
    }

    @Override
    public JqlClauseBuilder attachmentsExists(boolean hasAttachment)
    {
        return addStringCondition(forAttachments().getJqlClauseNames().getPrimaryName(), hasAttachment ? Operator.IS_NOT : Operator.IS, EmptyOperand.OPERAND_NAME);
    }

    public ConditionBuilder field(final String jqlName)
    {
        notNull("jqlName", jqlName);
        return new DefaultConditionBuilder(jqlName, this);
    }

    public ConditionBuilder customField(final Long id)
    {
        notNull("id", id);
        return new DefaultConditionBuilder(JqlCustomFieldId.toString(id), this);
    }

    public JqlClauseBuilder addClause(final Clause clause)
    {
        notNull("clause", clause);

        builder = builder.clause(clause);
        return this;
    }

    public JqlClauseBuilder addDateCondition(final String clauseName, final Operator operator, final Date date)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("date", date);

        return addTerminalClause(clauseName, operator, new SingleValueOperand(jqlDateSupport.getDateString(date)));
    }

    public JqlClauseBuilder addDateCondition(final String clauseName, final Date... dates)
    {
        if ((dates != null) && (dates.length == 1))
        {
            return addDateCondition(clauseName, Operator.EQUALS, dates[0]);
        }
        else
        {
            return addDateCondition(clauseName, Operator.IN, dates);
        }
    }

    public JqlClauseBuilder addDateCondition(final String clauseName, final Operator operator, final Date... dates)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("dates", dates);
        Assertions.not("dates is empty", dates.length == 0);

        final String[] args = new String[dates.length];
        int position = 0;
        for (final Date date : dates)
        {
            args[position++] = jqlDateSupport.getDateString(date);
        }

        return addTerminalClause(clauseName, operator, new MultiValueOperand(args));
    }

    public JqlClauseBuilder addDateCondition(final String clauseName, final Collection<Date> dates)
    {
        if ((dates != null) && (dates.size() == 1))
        {
            return addDateCondition(clauseName, Operator.EQUALS, dates.iterator().next());
        }
        else
        {
            return addDateCondition(clauseName, Operator.IN, dates);
        }
    }

    public JqlClauseBuilder addDateCondition(final String clauseName, final Operator operator, final Collection<Date> dates)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("dates", dates);
        Assertions.not("dates is empty", dates.isEmpty());

        final String[] args = new String[dates.size()];
        int position = 0;
        for (final Date date : dates)
        {
            args[position++] = jqlDateSupport.getDateString(date);
        }

        return addTerminalClause(clauseName, operator, new MultiValueOperand(args));
    }

    public JqlClauseBuilder addDateRangeCondition(final String clauseName, final Date startDate, final Date endDate)
    {
        final Operand startOperand = startDate == null ? null : new SingleValueOperand(jqlDateSupport.getDateString(startDate));
        final Operand endOperand = endDate == null ? null : new SingleValueOperand(jqlDateSupport.getDateString(endDate));

        return addRangeCondition(clauseName, startOperand, endOperand);
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final String functionName)
    {
        return addFunctionCondition(clauseName, Operator.EQUALS, functionName);
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final String functionName, final String... args)
    {
        return addFunctionCondition(clauseName, Operator.EQUALS, functionName, args);
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final String functionName, final Collection<String> args)
    {
        return addFunctionCondition(clauseName, Operator.EQUALS, functionName, args);
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final Operator operator, final String functionName)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("functionName", functionName);

        return addTerminalClause(clauseName, operator, new FunctionOperand(functionName));
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final Operator operator, final String functionName, final String... args)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("functionName", functionName);
        containsNoNulls("args", args);

        return addTerminalClause(clauseName, operator, new FunctionOperand(functionName, args));
    }

    public JqlClauseBuilder addFunctionCondition(final String clauseName, final Operator operator, final String functionName, final Collection<String> args)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("functionName", functionName);
        containsNoNulls("args", args);

        return addTerminalClause(clauseName, operator, new FunctionOperand(functionName, args));
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final String clauseValue)
    {
        return addStringCondition(clauseName, Operator.EQUALS, clauseValue);
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final String... clauseValues)
    {
        if ((clauseValues != null) && (clauseValues.length == 1))
        {
            return addStringCondition(clauseName, Operator.EQUALS, clauseValues[0]);
        }
        else
        {
            return addStringCondition(clauseName, Operator.IN, clauseValues);
        }
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final Collection<String> clauseValues)
    {
        if ((clauseValues != null) && (clauseValues.size() == 1))
        {
            return addStringCondition(clauseName, Operator.EQUALS, clauseValues.iterator().next());
        }
        else
        {
            return addStringCondition(clauseName, Operator.IN, clauseValues);
        }
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final Operator operator, final String clauseValue)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("clauseValue", clauseValue);

        return addTerminalClause(clauseName, operator, new SingleValueOperand(clauseValue));
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final Operator operator, final String... clauseValues)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("clauseValues", clauseValues);
        Assertions.not("clauseValues is empty", clauseValues.length == 0);

        return addTerminalClause(clauseName, operator, new MultiValueOperand(clauseValues));
    }

    public JqlClauseBuilder addStringCondition(final String clauseName, final Operator operator, final Collection<String> clauseValues)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("clauseValues", clauseValues);
        Assertions.not("clauseValues is empty", clauseValues.isEmpty());

        return addTerminalClause(clauseName, operator, new MultiValueOperand(clauseValues.toArray(new String[clauseValues.size()])));
    }

    public JqlClauseBuilder addStringRangeCondition(final String clauseName, final String start, final String end)
    {
        final Operand startClause = start == null ? null : new SingleValueOperand(start);
        final Operand endClause = end == null ? null : new SingleValueOperand(end);

        return addRangeCondition(clauseName, startClause, endClause);
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Long clauseValue)
    {
        return addNumberCondition(clauseName, Operator.EQUALS, clauseValue);
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Long... clauseValues)
    {
        if ((clauseValues != null) && (clauseValues.length == 1))
        {
            return addNumberCondition(clauseName, Operator.EQUALS, clauseValues[0]);
        }
        else
        {
            return addNumberCondition(clauseName, Operator.IN, clauseValues);
        }
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Collection<Long> clauseValues)
    {
        if ((clauseValues != null) && (clauseValues.size() == 1))
        {
            return addNumberCondition(clauseName, Operator.EQUALS, clauseValues.iterator().next());
        }
        else
        {
            return addNumberCondition(clauseName, Operator.IN, clauseValues);
        }
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Operator operator, final Long clauseValue)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("clauseValue", clauseValue);

        return addTerminalClause(clauseName, operator, new SingleValueOperand(clauseValue));
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Operator operator, final Long... clauseValues)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("clauseValues", clauseValues);
        Assertions.not("clauseValues is empty", clauseValues.length == 0);

        return addTerminalClause(clauseName, operator, new MultiValueOperand(clauseValues));
    }

    public JqlClauseBuilder addNumberCondition(final String clauseName, final Operator operator, final Collection<Long> clauseValues)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("clauseValues", clauseValues);
        Assertions.not("clauseValues is empty", clauseValues.isEmpty());

        return addTerminalClause(clauseName, operator, new MultiValueOperand(clauseValues.toArray(new Long[clauseValues.size()])));
    }

    public JqlClauseBuilder addNumberRangeCondition(final String clauseName, final Long start, final Long end)
    {
        final Operand startClause = start == null ? null : new SingleValueOperand(start);
        final Operand endClause = end == null ? null : new SingleValueOperand(end);

        return addRangeCondition(clauseName, startClause, endClause);
    }

    public ConditionBuilder addCondition(final String clauseName)
    {
        notNull("clauseName", clauseName);

        return new DefaultConditionBuilder(clauseName, this);
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Operand operand)
    {
        return addCondition(clauseName, Operator.EQUALS, operand);
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Operand... operands)
    {
        return addCondition(clauseName, Operator.IN, operands);
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Collection<? extends Operand> operands)
    {
        return addCondition(clauseName, Operator.IN, operands);
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Operator operator, final Operand operand)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        notNull("operand", operand);

        return addTerminalClause(clauseName, operator, operand);
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Operator operator, final Operand... operands)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("operands", operands);

        return addTerminalClause(clauseName, operator, new MultiValueOperand(operands));
    }

    public JqlClauseBuilder addCondition(final String clauseName, final Operator operator, final Collection<? extends Operand> operands)
    {
        notNull("clauseName", clauseName);
        notNull("operator", operator);
        containsNoNulls("operands", operands);

        return addTerminalClause(clauseName, operator, new MultiValueOperand(operands));
    }

    public JqlClauseBuilder addRangeCondition(final String clauseName, final Operand start, final Operand end)
    {
        Assertions.not("start and end are both null.", (start == null) && (end == null));
        Assertions.notNull("clauseName", clauseName);

        final Clause clause;
        if (start != null)
        {
            if (end != null)
            {
                final TerminalClause startClause = new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start);
                final TerminalClause endClause = new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, end);

                clause = new AndClause(startClause, endClause);
            }
            else
            {
                clause = new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start);
            }
        }
        else
        {
            clause = new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, end);
        }

        return addClause(clause);
    }

    public Clause buildClause()
    {
        return builder.build();
    }

    private JqlClauseBuilder addTerminalClause(final String clauseName, final Operator operator, final Operand clauseValue)
    {
        builder = builder.clause(new TerminalClauseImpl(clauseName, operator, clauseValue));
        return this;
    }

    public JqlClauseBuilder addEmptyCondition(final String clauseName)
    {
        notNull("clauseName", clauseName);
        return addTerminalClause(clauseName, Operator.IS, EmptyOperand.EMPTY);
    }
}
