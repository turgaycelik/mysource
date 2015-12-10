package com.atlassian.jira.plugin.jql.function;

import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.exception.GetException;
import com.atlassian.jira.issue.link.DefaultRemoteIssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Returns the issue ids of issues that are associated with remote links with any of the given global ids.
 * <p/>
 * This function can only be used if Issue Linking is enabled.
 * <p/>
 * Function usage:
 * <code>issuesWithRemoteLinksByGlobalId ( globalId [, globalId ]* )</code>
 * <p/>
 * The maximum number of global ids allowed by this function is 100.
 *
 * @since v6.1
 */
public class RemoteLinksByGlobalIdFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_REMOTE_LINKS_BY_GLOBAL_ID = "issuesWithRemoteLinksByGlobalId";

    private final IssueLinkManager issueLinkManager;
    private final RemoteIssueLinkManager remoteIssueLinkManager;

    public RemoteLinksByGlobalIdFunction(final IssueLinkManager issueLinkManager, final RemoteIssueLinkManager remoteIssueLinkManager)
    {
        this.issueLinkManager = issueLinkManager;
        this.remoteIssueLinkManager = remoteIssueLinkManager;
    }

    @Nonnull
    @Override
    public MessageSet validate(final User searcher, @Nonnull final FunctionOperand operand, @Nonnull final TerminalClause terminalClause)
    {
        MessageSet messageSet = new MessageSetImpl();

        if (!issueLinkManager.isLinkingEnabled())
        {
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.issue.linking.disabled", getFunctionName()));
            return messageSet;
        }
        final List<String> args = operand.getArgs();
        final int maxArgSize = DefaultRemoteIssueLinkManager.MAX_GLOBAL_ID_LIST_SIZE_FOR_FIND;
        if (args.size() < 1 || args.size() > maxArgSize)
        {
            messageSet.addErrorMessage(getI18n().getText("jira.jql.function.arg.incorrect.range", getFunctionName(), 1, maxArgSize, args.size())
                    + " " + getI18n().getText("jira.jql.function.remote.link.by.global.id.incorrect.usage", getFunctionName()));
            return messageSet;
        }

        return messageSet;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull final QueryCreationContext queryCreationContext, @Nonnull final FunctionOperand operand, @Nonnull final TerminalClause terminalClause)
    {
        notNull("operand", operand);

        if (!issueLinkManager.isLinkingEnabled())
        {
            return ImmutableList.of();
        }
        final List<String> args = operand.getArgs();
        if (args.isEmpty())
        {
            return ImmutableList.of();
        }

        final List<RemoteIssueLink> remoteIssueLinks;
        try
        {
            remoteIssueLinks = remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(args);
        }
        catch (GetException e)
        {
            return ImmutableList.of();
        }
        final List<Long> issueIds = Lists.transform(remoteIssueLinks, new Function<RemoteIssueLink, Long>()
        {
            @Override
            public Long apply(final RemoteIssueLink remoteIssueLink)
            {
                return remoteIssueLink.getIssueId();
            }
        });
        // remove duplicates in issueIds, as an issue might be matched by more than one global Ids
        final List<Long> issueIdsWithoutDup = ImmutableSet.copyOf(issueIds).asList();
        return Lists.transform(issueIdsWithoutDup, new Function<Long, QueryLiteral>()
        {
            @Override
            public QueryLiteral apply(final Long issueId)
            {
                return new QueryLiteral(operand, issueId);
            }
        });
    }

    /**
     * expects at least one globalId as argument.
     */
    @Override
    public int getMinimumNumberOfExpectedArguments()
    {
        return 1;
    }

    @Nonnull
    @Override
    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }
}
