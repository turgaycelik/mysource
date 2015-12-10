package com.atlassian.jira.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.ApplicationUserBestNameComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.dbc.Null;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultVoteManager implements VoteManager
{
    private static final Logger log = Logger.getLogger(DefaultVoteManager.class);
    private static final String ASSOCIATION_TYPE = "VoteIssue";

    private final ApplicationProperties applicationProperties;
    private final UserAssociationStore userAssociationStore;
    private final VoteHistoryStore voteHistoryStore;
    private final IssueIndexManager indexManager;
    private final IssueManager issueManager;


    public DefaultVoteManager(final ApplicationProperties applicationProperties, final UserAssociationStore userAssociationStore, final IssueIndexManager indexManager, VoteHistoryStore voteHistoryStore, IssueManager issueManager)
    {
        this.applicationProperties = applicationProperties;
        this.userAssociationStore = userAssociationStore;
        this.indexManager = indexManager;
        this.voteHistoryStore = voteHistoryStore;
        this.issueManager = issueManager;
    }

    public boolean addVote(final User user, final Issue issue)
    {
        return addVote(ApplicationUsers.from(user), issue);
    }

    @Override
    public boolean addVote(ApplicationUser user, Issue issue)
    {
        return updateVote(true, user, issue.getGenericValue());
    }

    public boolean addVote(final User user, final GenericValue issue)
    {
        return updateVote(true, ApplicationUsers.from(user), issue);
    }

    public boolean removeVote(final User user, final Issue issue)
    {
        return updateVote(false, ApplicationUsers.from(user), issue.getGenericValue());
    }

    @Override
    public boolean removeVote(ApplicationUser user, Issue issue)
    {
        return updateVote(false, user, issue.getGenericValue());
    }

    public boolean removeVote(final User user, final GenericValue issue)
    {
        return updateVote(false, ApplicationUsers.from(user), issue);
    }

    public Collection<String> getVoterUsernames(final Issue issue)
    {
        return getVoterUsernames(issue.getGenericValue());
    }

    public Collection<String> getVoterUsernames(final GenericValue issue)
    {
        Collection<String> userkeys = userAssociationStore.getUserkeysFromSink(ASSOCIATION_TYPE, Entity.Name.ISSUE, issue.getLong("id"));
        Collection<String> usernames = new ArrayList<String>(userkeys.size());
        UserKeyService userKeyService = ComponentAccessor.getComponent(UserKeyService.class);
        for (String userkey : userkeys)
        {
            usernames.add(userKeyService.getUsernameForKey(userkey));
        }
        return usernames;
    }

    @Override
    public Collection<String> getVoterUserkeys(Issue issue)
    {
        return userAssociationStore.getUserkeysFromSink(ASSOCIATION_TYPE, Entity.Name.ISSUE, issue.getId());
    }

    @Override
    public int getVoteCount(Issue issue)
    {
        return getVoterUsernames(issue).size();
    }

    public List<VoteHistoryEntry> getVoteHistory(Issue issue)
    {
        return voteHistoryStore.getHistory(issue.getId());
    }

    public List<User> getVoters(final Issue issue, final Locale usersLocale)
    {
        return ApplicationUsers.toDirectoryUsers(getVoters(issue.getGenericValue(), usersLocale));
    }

    @Override
    public List<ApplicationUser> getVotersFor(Issue issue, Locale usersLocale)
    {
        return getVoters(issue.getGenericValue(), usersLocale);
    }

    private List<ApplicationUser> getVoters(final GenericValue issueGV, final Locale usersLocale)
    {
        // Find the associated voters for this issue
        final List<ApplicationUser> voters = userAssociationStore.getUsersFromSink(ASSOCIATION_TYPE, issueGV);
        // Sort by User DisplayName in the preferred Locale
        Collections.sort(voters, new ApplicationUserBestNameComparator(usersLocale));
        return voters;
    }

    private boolean updateVote(final boolean isVoting, final ApplicationUser user, final GenericValue issue)
    {
        if (validateUpdate(user, issue))
        {
            try
            {
                if (isVoting)
                {
                    if (!hasVoted(user, issue))
                    {
                        userAssociationStore.createAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustVoteCount(issue, 1);
                        return true;
                    }
                }
                else
                {
                    if (hasVoted(user, issue))
                    {
                        userAssociationStore.removeAssociation(ASSOCIATION_TYPE, user.getKey(), Entity.Name.ISSUE, issue.getLong("id"));
                        adjustVoteCount(issue, -1);
                        return true;
                    }
                }
            }
            catch (final GenericEntityException e)
            {
                log.error("Error changing vote association", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Adjusts the vote count for an issue.
     *
     * @param originalIssue       the issue to change count for
     * @param adjustValue the value to change it by
     * @throws GenericEntityException If there wasa persitence problem
     */

    private void adjustVoteCount(final GenericValue originalIssue, final int adjustValue) throws GenericEntityException
    {
        final long votes = recalculateVoters(originalIssue, adjustValue);
        final Long issueId = originalIssue.getLong("id");

        final GenericValue clonedIssue = (GenericValue) originalIssue.clone();
        clonedIssue.clear();
        clonedIssue.set("id", originalIssue.getLong("id"));
        clonedIssue.set("votes", votes);
        clonedIssue.store();

        // The issue reference is kept in the VoteOrWatchIssue action, need to update original issue too
        originalIssue.set("votes", votes);

        final Timestamp now = new Timestamp(new Date().getTime());
        voteHistoryStore.add(new VoteHistoryEntryImpl(originalIssue.getLong("id"), now ,votes));

        try
        {
            // indexing doesn't handles incremental updates, re-indexing the updated object from db
            final GenericValue updatedIssue = issueManager.getIssue(issueId);
            indexManager.reIndex(updatedIssue);
        }
        catch (final IndexException e)
        {
            log.error("Exception re-indexing issue " + e, e);
        }
    }

    private Long recalculateVoters(final GenericValue issue, final int adjustValue)
    {
        Long votes = issue.getLong("votes");

        if (votes == null)
        {
            votes = 0L;
        }
        votes = votes + adjustValue;

        if (votes < 0)
        {
            votes = 0L;
        }
        return votes;
    }

    /**
     * Validates that the params and the system are in a correct state to change a vote
     *
     * @param user  The user who is voting
     * @param issue the issue the user is voting for
     * @return whether or not to go ahead with the vote.
     */
    private boolean validateUpdate(final ApplicationUser user, final GenericValue issue)
    {
        if (issue == null)
        {
            log.error("You must specify an issue.");
            return false;
        }

        if (!isVotingEnabled())
        {
            log.error("Voting is not enabled - the change vote on issue " + issue.getString("key") + " by user " + user.getUsername() + " was unsuccessful.");

            return false;
        }

        if (issue.getString("resolution") != null)
        {
            log.error("Cannot change vote on issue that has been resolved.");
            return false;
        }

        if (user == null)
        {
            log.error("You must specify a user.");
            return false;
        }
        return true;
    }

    /**
     * Check if voting has been enabled
     */
    public boolean isVotingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }

    public boolean hasVoted(User user, Issue issue)
    {
        return hasVoted(ApplicationUsers.from(user), issue);
    }

    @Override
    public boolean hasVoted(ApplicationUser user, Issue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no votes for the issue then this dude didn't vote for it.
        if (issue.getVotes() == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, Entity.Name.ISSUE, issue.getId());
    }

    public boolean hasVoted(final User user, final GenericValue issue)
    {
        return hasVoted(ApplicationUsers.from(user), issue);
    }

    private boolean hasVoted(final ApplicationUser user, final GenericValue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no votes for the issue then this dude didn't vote for it.
        if (issue.getLong("votes") == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, Entity.Name.ISSUE, issue.getLong("id"));
    }

    public void removeVotesForUser(final User user)
    {
        removeVotesForUser(ApplicationUsers.from(user));
    }

    @Override
    public void removeVotesForUser(ApplicationUser user)
    {
        notNull("user", user);
        // Get all the issues
        List<GenericValue> issueGvs = userAssociationStore.getSinksFromUser(ASSOCIATION_TYPE, user, "Issue");
        for (GenericValue issueGv : issueGvs)
        {
            updateVote(false, user, issueGv);
        }
    }
}
