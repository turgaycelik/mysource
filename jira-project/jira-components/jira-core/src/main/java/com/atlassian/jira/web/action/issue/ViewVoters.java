package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.issue.vote.VoteHistoryEntryImpl;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.component.multiuserpicker.UserBean;
import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.ofbiz.core.entity.GenericEntityException;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.Nonnull;

public class ViewVoters extends AbstractIssueSelectAction
{
    private final VoteManager voteManager;
    private final VoteService voteService;
    private final PermissionManager permissionManager;
    private final TimeZoneManager timeZoneManager;

    private Collection<UserBean> voters;
    private Boolean votedAlready;

    public ViewVoters(VoteManager voteManager, VoteService voteService, PermissionManager permissionManager, TimeZoneManager timeZoneManager)
    {
        this.voteManager = voteManager;
        this.voteService = voteService;
        this.permissionManager = permissionManager;
        this.timeZoneManager = timeZoneManager;
    }

    public String doDefault() throws Exception
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!hasPermissionToViewVoters())
        {
            return "securitybreach";
        }

        return super.doDefault();
    }

    public Collection<UserBean> getVoters()
    {
        if (voters == null)
        {
            final Collection<String> usernames = voteManager.getVoterUsernames(getIssue());
            voters = UserBean.convertUsernamesToUserBeans(getLocale(), usernames);
        }
        return voters;
    }

    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    public SimpleVoteHistory getVoteHistory()
    {
        List<VoteHistoryEntry> voteHistory = voteManager.getVoteHistory(getIssueObject());
        return new SimpleVoteHistory(getIssueObject());
    }

    public String getCommaSeperatedDateParts(Date date)
    {
        NumberFormat nf = new DecimalFormat("##00");
        TimeZone timeZone = timeZoneManager.getLoggedInUserTimeZone();
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        String yyyy = nf.format(calendar.get(Calendar.YEAR));
        String mm = nf.format(calendar.get(Calendar.MONTH));
        String dd = nf.format(calendar.get(Calendar.DAY_OF_MONTH));

        return yyyy + "," + mm + "," + dd;
    }

    // Add vote for current user on this issue
    @RequiresXsrfCheck
    public String doAddVote() throws GenericEntityException
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!hasPermissionToViewVoters())
        {
            return "securitybreach";
        }

        // Cannot vote for an issue that is reported by the current user or an issue that has been resolved.
        if (isIssueReportedByMe() || (getIssue().getString("resolution") != null))
        {
            return "securitybreach";
        }

        voteManager.addVote(getLoggedInUser(), getIssue());
        refreshIssueObject();

        return SUCCESS;
    }

    /**
     * Remove the current users vote for this issue
     * @return The name of the view to be rendered. {@link webwork.action.Action#ERROR} is returned if the issue could
     * not be found or if the user does not have permission to see the issue.
     * @throws GenericEntityException
     */
    @RequiresXsrfCheck
    public String doRemoveVote() throws GenericEntityException
    {
        if (!isIssueValid())
        {
            return ISSUE_PERMISSION_ERROR;
        }

        if (!hasPermissionToViewVoters())
        {
            return "securitybreach";
        }

        // Cannot remove vote for an issue that is reported by the current user
        // or an issue that has been resolved.
        if (isIssueReportedByMe() || (getIssue().getString("resolution") != null))
        {
            return "securitybreach";
        }

        voteManager.removeVote(getLoggedInUser(), getIssue());
        refreshIssueObject();

        return SUCCESS;
    }

    /**
     * Determine whether the current user has voted already or not
     *
     * @return true if current user has already voted, false otherwise
     */
    public boolean isVotedAlready()
    {
        if (votedAlready == null)
        {
            if (getLoggedInUser() != null)
            {
                votedAlready = voteManager.hasVoted(getLoggedInUser(), getIssue());
            }
            else
            {
                votedAlready = Boolean.FALSE;
            }
        }
        return votedAlready;
    }

    public boolean isIssueReportedByMe()
    {
        final String reporter = getIssue().getString("reporter");
        final ApplicationUser user = getLoggedInApplicationUser();
        return user != null && reporter != null && reporter.equals(user.getKey());
    }

    public boolean isCanAddVote()
    {
        User user = getLoggedInUser();
        return user != null && voteService.validateAddVote(user, user, getIssueObject()).isValid() && !voteService.hasVoted(getIssueObject(), user);
    }

    public boolean isCanRemoveVote()
    {
        User user = getLoggedInUser();
        return user != null && voteService.validateRemoveVote(user, user, getIssueObject()).isValid() && voteService.hasVoted(getIssueObject(), user);
    }

    public class SimpleVoteHistory
    {
        private final List<VoteHistoryEntry> voteHistory = new ArrayList<VoteHistoryEntry>();
        private final int numberOfDays;
        public SimpleVoteHistory(Issue issue)
        {
            voteHistory.add(new VoteHistoryEntryImpl(issue.getId(), issue.getCreated(), 0));
            voteHistory.addAll(voteManager.getVoteHistory(issue));
            voteHistory.add(new VoteHistoryEntryImpl(issue.getId(), new Timestamp(System.currentTimeMillis()), issue.getVotes()));

            numberOfDays = Days.daysBetween(new DateMidnight(issue.getCreated()), new DateMidnight()).getDays() + 1;
        }

        public List<VoteHistoryEntry> getVoteHistory()
        {
            return voteHistory;
        }

        public int getNumberOfDays()
        {
            return numberOfDays;
        }
    }

    private boolean hasPermissionToViewVoters()
    {
        return permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, getIssueObject(), getLoggedInUser());
    }
}
