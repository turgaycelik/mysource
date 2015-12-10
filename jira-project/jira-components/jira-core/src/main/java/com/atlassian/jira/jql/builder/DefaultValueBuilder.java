package com.atlassian.jira.jql.builder;

import com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.plugin.jql.function.CurrentLoginFunction;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.IssueHistoryFunction;
import com.atlassian.jira.plugin.jql.function.LastLoginFunction;
import com.atlassian.jira.plugin.jql.function.LinkedIssuesFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.plugin.jql.function.NowFunction;
import com.atlassian.jira.plugin.jql.function.RemoteLinksByGlobalIdFunction;
import com.atlassian.jira.plugin.jql.function.VotedIssuesFunction;
import com.atlassian.jira.plugin.jql.function.WatchedIssuesFunction;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link ValueBuilder}.
 *
 * @since v4.0
 */
class DefaultValueBuilder implements ValueBuilder
{
    private final JqlClauseBuilder builder;
    private final String clauseName;
    private final Operator operator;

    DefaultValueBuilder(final JqlClauseBuilder builder, final String clauseName, final Operator operator)
    {
        this.builder = notNull("builder", builder);
        this.clauseName = notNull("clauseName", clauseName);
        this.operator = notNull("operator", operator);
    }

    public JqlClauseBuilder string(final String value)
    {
        return builder.addStringCondition(clauseName, operator, value);
    }

    public JqlClauseBuilder strings(final String... values)
    {
        return builder.addStringCondition(clauseName, operator, values);
    }

    public JqlClauseBuilder strings(final Collection<String> values)
    {
        return builder.addStringCondition(clauseName, operator, values);
    }

    public JqlClauseBuilder number(final Long value)
    {
        return builder.addNumberCondition(clauseName, operator, value);
    }

    public JqlClauseBuilder numbers(final Long... value)
    {
        return builder.addNumberCondition(clauseName, operator, value);
    }

    public JqlClauseBuilder numbers(final Collection<Long> value)
    {
        return builder.addNumberCondition(clauseName, operator, value);
    }

    public JqlClauseBuilder operand(final Operand operand)
    {
        return builder.addCondition(clauseName, operator, operand);
    }

    public JqlClauseBuilder operands(final Operand... operands)
    {
        return builder.addCondition(clauseName, operator, operands);
    }

    public JqlClauseBuilder operands(final Collection<? extends Operand> operands)
    {
        return builder.addCondition(clauseName, operator, operands);
    }

    public JqlClauseBuilder empty()
    {
        return builder.addCondition(clauseName, operator, EmptyOperand.EMPTY);
    }

    public JqlClauseBuilder function(final String funcName)
    {
        return builder.addFunctionCondition(clauseName, operator, funcName);
    }

    public JqlClauseBuilder function(final String funcName, final String... args)
    {
        return builder.addFunctionCondition(clauseName, operator, funcName, args);
    }

    public JqlClauseBuilder function(final String funcName, final Collection<String> args)
    {
        return builder.addFunctionCondition(clauseName, operator, funcName, args);
    }

    public JqlClauseBuilder functionStandardIssueTypes()
    {
        return function(AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES);
    }

    public JqlClauseBuilder functionSubTaskIssueTypes()
    {
        return function(AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES);
    }

    public JqlClauseBuilder functionMembersOf(final String groupName)
    {
        notNull("groupName", groupName);

        return function(MembersOfFunction.FUNCTION_MEMBERSOF, groupName);
    }

    public JqlClauseBuilder functionCurrentUser()
    {
        return function(CurrentUserFunction.FUNCTION_CURRENT_USER);
    }

    public JqlClauseBuilder functionIssueHistory()
    {
        return function(IssueHistoryFunction.FUNCTION_ISSUE_HISTORY);
    }

    public JqlClauseBuilder functionReleasedVersions(String... projects)
    {
        return function(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS, projects);
    }

    public JqlClauseBuilder functionUnreleasedVersions(String... projects)
    {
        return function(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS, projects);
    }

    public JqlClauseBuilder functionNow()
    {
        return function(NowFunction.FUNCTION_NOW);
    }

    public JqlClauseBuilder functionWatchedIssues()
    {
        return function(WatchedIssuesFunction.FUNCTION_WATCHED_ISSUES);
    }

    public JqlClauseBuilder functionVotedIssues()
    {
        return function(VotedIssuesFunction.FUNCTION_VOTED_ISSUES);
    }

    public JqlClauseBuilder functionLinkedIssues(final String issue, final String... issueLinkTypes)
    {
        Assertions.notNull("issue", issue);
        Assertions.notNull("issueLinkTypes", issueLinkTypes);

        final List<String> args;
        if (issueLinkTypes.length == 0)
        {
            args = Collections.singletonList(issue);
        }
        else
        {
            args = new ArrayList<String>(issueLinkTypes.length + 1);
            args.add(issue);
            args.addAll(Arrays.asList(issueLinkTypes));
        }

        return function(LinkedIssuesFunction.FUNCTION_LINKED_ISSUES, args);
    }

    @Override
    public JqlClauseBuilder functionRemoteLinksByGlobalId(final String... globalIds)
    {
        Assertions.notNull("globalIds", globalIds);
        Assertions.stateFalse("empty globalIds", globalIds.length == 0);
        return function(RemoteLinksByGlobalIdFunction.FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID, globalIds);
    }

    public JqlClauseBuilder functionCascaingOption(final String parent)
    {
        return function(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, parent);
    }

    public JqlClauseBuilder functionCascaingOption(final String parent, final String child)
    {
        return function(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, parent, child);
    }

    public JqlClauseBuilder functionCascaingOptionParentOnly(final String parent)
    {
        return function(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, parent, CascadeOptionFunction.QUOTED_EMPTY_VALUE);
    }

    public JqlClauseBuilder functionLastLogin()
    {
        return function(LastLoginFunction.FUNCTION_LAST_LOGIN);
    }

    public JqlClauseBuilder functionCurrentLogin()
    {
        return function(CurrentLoginFunction.FUNCTION_CURRENT_LOGIN);
    }

    public JqlClauseBuilder date(final Date date)
    {
        return builder.addDateCondition(clauseName, operator, date);
    }

    public JqlClauseBuilder dates(final Date... dates)
    {
        return builder.addDateCondition(clauseName, operator, dates);
    }

    public JqlClauseBuilder dates(final Collection<Date> dates)
    {
        return builder.addDateCondition(clauseName, operator, dates);
    }
}
