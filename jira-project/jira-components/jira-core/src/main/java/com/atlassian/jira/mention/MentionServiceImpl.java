package com.atlassian.jira.mention;

import com.atlassian.jira.event.issue.MentionIssueCommentEvent;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.MentionIssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Iterables;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class MentionServiceImpl implements MentionService
{
    private final MentionFinder finder;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final EventPublisher eventPublisher;
    private final ChangeHistoryManager changeHistoryManager;

    public MentionServiceImpl(MentionFinder finder, UserManager userManager, PermissionManager permissionManager,
            EventPublisher eventPublisher, ChangeHistoryManager changeHistoryManager)
    {
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.eventPublisher = eventPublisher;
        this.changeHistoryManager = changeHistoryManager;
        this.finder = notNull("finder", finder);
    }

    @Override
    public void sendCommentMentions(User remoteUser, Set<NotificationRecipient> currentRecipients, Comment comment, Comment originalComment)
    {
        if (!isUserAbleToMention(remoteUser))
        {
            return;
        }

        final String mentionText = comment.getBody();
        final Issue issue = comment.getIssue();
        final Set<User> mentionedUsers = getMentionedUsers(mentionText, issue);
        if (originalComment != null)
        {
            //if a comment was edited strip out any users that were already mentioned previously in this comment
            final Set<User> originalMentionedUsers = getMentionedUsers(originalComment.getBody(), issue);
            mentionedUsers.removeAll(originalMentionedUsers);
        }

        if (!mentionedUsers.isEmpty())
        {
            eventPublisher.publish(new MentionIssueCommentEvent(issue, remoteUser, mentionedUsers, mentionText, IssueFieldConstants.COMMENT, currentRecipients, comment));
        }
    }

    @Override
    public void sendIssueEditMentions(User remoteUser, Set<NotificationRecipient> currentRecipients, Issue issue, Comment comment)
    {
        if (!isUserAbleToMention(remoteUser))
        {
            return;
        }

        final String mentionText = issue.getDescription();
        final Set<User> mentionedUsers = getMentionedUsers(mentionText, issue);

        final List<ChangeHistory> allChangeHistories = changeHistoryManager.getChangeHistories(issue);
        boolean containsDescriptionChanges = false;
        if(!allChangeHistories.isEmpty())
        {
            final ChangeHistory last = Iterables.getLast(allChangeHistories);

            final List<ChangeItemBean> changeItemBeans = last.getChangeItemBeans();
            for (ChangeItemBean changeItemBean : changeItemBeans)
            {
                if (changeItemBean.getField().equals(IssueFieldConstants.DESCRIPTION))
                {
                    final Set<User> lastMentionedUsers = getMentionedUsers(changeItemBean.getFromString(), issue);
                    mentionedUsers.removeAll(lastMentionedUsers);
                    containsDescriptionChanges = true;
                    break;
                }
            }
        }

        //Only send mentions if we actually had any users mentioned and we edited the description in our last edit.
        if (!mentionedUsers.isEmpty() && containsDescriptionChanges)
        {
            eventPublisher.publish(new MentionIssueEvent(issue, remoteUser, mentionedUsers, mentionText, IssueFieldConstants.DESCRIPTION, currentRecipients));
        }

        //If we added an optional edit comment when updating this issue make sure we also send mentions for anyone
        //in this comment.
        if(comment != null)
        {
            sendCommentMentions(remoteUser, currentRecipients, comment, null);
        }
    }

    @Override
    public void sendIssueCreateMentions(User remoteUser, Set<NotificationRecipient> currentRecipients, Issue issue)
    {
        if (!isUserAbleToMention(remoteUser))
        {
            return;
        }

        final String mentionText = issue.getDescription();
        final Set<User> mentionedUsers = getMentionedUsers(mentionText, issue);

        if (!mentionedUsers.isEmpty())
        {
            eventPublisher.publish(new MentionIssueEvent(issue, remoteUser, mentionedUsers, mentionText, IssueFieldConstants.DESCRIPTION, currentRecipients));
        }
    }

    @Override
    public boolean isUserAbleToMention(final User remoteUser)
    {
        return !isAnonymous(remoteUser) && isUserAbleToBrowseUsers(remoteUser);
    }

    private boolean isUserAbleToBrowseUsers(User remoteUser)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, remoteUser);
    }

    private Set<User> getMentionedUsers(String mentionText, Issue issue)
    {
        final Set<User> mentionedUsers = new LinkedHashSet<User>();
        Iterable<String> allMentionedUsernames = finder.getMentionedUsernames(mentionText);

        for (String username : allMentionedUsernames)
        {
            final User user = userManager.getUser(username);
            //any user that we find that has the browse permission
            if (user != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
            {
                mentionedUsers.add(user);
            }
        }
        return mentionedUsers;
    }
}
