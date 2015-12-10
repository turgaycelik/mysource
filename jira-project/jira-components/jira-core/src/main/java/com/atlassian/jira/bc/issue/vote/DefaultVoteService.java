package com.atlassian.jira.bc.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultVoteService implements VoteService
{
    private final VoteManager voteManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultVoteService(final VoteManager voteManager, final I18nHelper.BeanFactory beanFactory,
            final ApplicationProperties applicationProperties, final PermissionManager permissionManager,
            final I18nHelper.BeanFactory i18nFactory)
    {
        this.voteManager = voteManager;
        this.beanFactory = beanFactory;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public VoteValidationResult validateAddVote(final User remoteUser, final User voter, final Issue issue)
    {
        notNull("voter", voter);
        notNull("issue", issue);

        return validateVoting(remoteUser, voter, issue);
    }

    @Override
    public int addVote(final User remoteUser, final VoteValidationResult validationResult)
    {
        notNull("remoteUser", remoteUser);
        notNull("validationResult", validationResult);

        User voter = validationResult.getVoter();
        Issue issue = validationResult.getIssue();

        voteManager.addVote(voter, issue);
        return voteManager.getVoteCount(issue);
    }

    @Override
    public VoteValidationResult validateRemoveVote(final User remoteUser, final User voter, final Issue issue)
    {
        notNull("voter", voter);
        notNull("issue", issue);

        return validateVoting(remoteUser, voter, issue);
    }

    @Override
    public int removeVote(final User remoteUser, final VoteValidationResult validationResult)
    {
        notNull("remoteUser", remoteUser);
        notNull("validationResult", validationResult);

        User voter = validationResult.getVoter();
        Issue issue = validationResult.getIssue();

        voteManager.removeVote(voter, issue);
        return voteManager.getVoteCount(issue);
    }

    @Override
    public ServiceOutcome<Collection<User>> viewVoters(final Issue issue, final User remoteUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(remoteUser);

        if (!hasPermissionToViewVoters(issue, remoteUser))
        {
            return ServiceOutcomeImpl.error(i18n.getText("voters.no.permission"));
        }
        else
        {
            if (voteManager.isVotingEnabled())
            {
                final Collection<User> voters = voteManager.getVoters(issue, i18n.getLocale());
                return ServiceOutcomeImpl.ok(voters);
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("issue.operations.voting.disabled"));
            }
        }
    }

    @Override
    public ServiceOutcome<List<VoteHistoryEntry>> getVoterHistory(Issue issue, User remoteUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(remoteUser);

        if (!hasPermissionToViewVoters(issue, remoteUser))
        {
            return ServiceOutcomeImpl.error(i18n.getText("voters.no.permission"));
        }
        else
        {
            if (voteManager.isVotingEnabled())
            {
                return ServiceOutcomeImpl.ok(voteManager.getVoteHistory(issue));
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("issue.operations.voting.disabled"));
            }
        }
    }

    private boolean hasPermissionToViewVoters(@Nonnull Issue issue, @Nonnull User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, remoteUser);
    }

    @Override
    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    @Override
    public boolean hasVoted(final Issue issue, final User user)
    {
        return voteManager.hasVoted(user, issue);
    }

    private VoteValidationResult validateVoting(final User remoteUser, final User voter, final Issue issue)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        final VoteValidationResult result = new VoteValidationResult(errors, voter, issue);
        if (isAnonymous(remoteUser) || isAnonymous(voter))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.not.loggedin"));
        }
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, voter))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.error.vote.issue.permission"));
        }
        final String reporterId = issue.getReporterId();
        if (StringUtils.isNotBlank(reporterId) && voter != null && reporterId.equals(ApplicationUsers.getKeyFor(voter)))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.novote"));
        }
        if (issue.getResolution() != null)
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.resolved"));
        }
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.disabled"));
        }
        return result;
    }
}
