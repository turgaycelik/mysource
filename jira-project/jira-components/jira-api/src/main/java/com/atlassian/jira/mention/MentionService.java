package com.atlassian.jira.mention;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.notification.NotificationRecipient;

import java.util.Set;

/**
 * Responsible for publishing {@link com.atlassian.jira.event.issue.MentionIssueEvent} when a user has mentioned
 * other users on an issue.
 *
 * @since v5.0
 */
@PublicApi
public interface MentionService
{
    /**
     * Given a comment object this method will look for any mentions using the {@link MentionFinder} and send e-mails to
     * all users mentioned.
     * <p/>
     * Sending mentions can not be performed by anonymous users.  The user sending the mentions must have browse users
     * permission and all users mentioned must have permission to browse the issue.  Otherwise no e-mails will be sent.
     *
     * @param remoteUser The currently logged in user performing this operation.
     * @param currentRecipients A set of recipients already being notified for this mention.
     * @param comment the comment to scan for text.
     * @param originalComment If a comment was edited provied the original comment so that a comparison can be carried out to only send mentions to new users. May be null
     */
    void sendCommentMentions(final User remoteUser, final Set<NotificationRecipient> currentRecipients, final Comment comment, final Comment originalComment);

    /**
     * Given an issue object this method will look for any mentions in the description field using the {@link MentionFinder} and send e-mails to
     * all users mentioned.
     * <p/>
     * Sending mentions can not be performed by anonymous users.  The user sending the mentions must have browse users
     * permission and all users mentioned must have permission to browse the issue.  Otherwise no e-mails will be sent.
     *
     * @param remoteUser The currently logged in user performing this operation.
     * @param currentRecipients A set of recipients already being notified for this mention.
     * @param issue the issue whose description will be scanned for metions.
     */
    void sendIssueCreateMentions(final User remoteUser, final Set<NotificationRecipient> currentRecipients, final Issue issue);

    /**
     * Given an issue that has just been edited and an optional edit comment this method sends mention e-mails
     * to all users mentioned in either the new issue description or option edit comment.
     *
     * Mentions will only be sent for users if the description field was edited (as determined by the latest
     * change history for this issue) and only to users that weren't mentioned in the description text previously.
     *
     *
     * @param remoteUser The currently logged in user performing this operation.
     * @param currentRecipients A set of recipients already being notified for this mention.
     * @param issue the issue whose description will be scanned for metions.
     * @param comment An optional comment for the edit
     */
    void sendIssueEditMentions(final User remoteUser, final Set<NotificationRecipient> currentRecipients, final Issue issue, final Comment comment);

    /**
     * Whether the specified user is able to mention other users in a JIRA issue.
     *
     * @param remoteUser The user to check mention permissions for.
     * @return true if the user is able to mention other users; false otherwise.
     */
    boolean isUserAbleToMention(final User remoteUser);
}
