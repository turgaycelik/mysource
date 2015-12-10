package com.atlassian.jira.mention;

import java.util.Collections;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.MentionIssueCommentEvent;
import com.atlassian.jira.event.issue.MentionIssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentImpl;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMentionServiceImpl
{
    private User remoteUser;
    private User fred;
    private User bob;

    private EventPublisher eventPublisher;
    private PermissionManager permissionManager;
    private MentionServiceImpl service;
    private UserManager userManager;
    private ChangeHistoryManager changeHistoryManager;


    @Before
    public void setup()
    {
        remoteUser = new MockUser("user");
        fred = new MockUser("fred");
        bob = new MockUser("bob");
        eventPublisher = mock(EventPublisher.class);
        userManager = mock(UserManager.class);
        permissionManager = mock(PermissionManager.class);
        changeHistoryManager = mock(ChangeHistoryManager.class);
        service = new MentionServiceImpl(new MentionFinderImpl(), userManager, permissionManager, eventPublisher, changeHistoryManager);
    }

    @Test
    public void testCommentWithNoMentions()
    {
        Comment comment = getComment("This is a test comment with no mentions!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        service.sendCommentMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), comment, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testCommentNoBrowseUsersPermission()
    {
        Comment comment = getComment("This is a test comment mentioning [~fred]!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(false);
        service.sendCommentMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), comment, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testCommentMention()
    {
        String commentBody = "This is a test comment mentioning [~fred]!";
        Comment comment = getComment(commentBody);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, (Issue) null, fred)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendCommentMentions(remoteUser, currentRecipients, comment, null);

        verify(eventPublisher).publish(new MentionIssueCommentEvent(null, remoteUser, Sets.newHashSet(fred), commentBody, IssueFieldConstants.COMMENT, currentRecipients, comment));
    }

    @Test
    public void testCommentMentionWithUserWithNoBrowsePermission()
    {
        String commentBody = "This is a test comment mentioning [~fred] and [~bob] who can't browse issues!";
        Comment comment = getComment(commentBody);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(userManager.getUser("fred")).thenReturn(fred);
        when(userManager.getUser("bob")).thenReturn(bob);
        when(permissionManager.hasPermission(10, (Issue) null, fred)).thenReturn(true);
        when(permissionManager.hasPermission(10, (Issue) null, bob)).thenReturn(false);

        final Set<NotificationRecipient> currentRecipients = Sets.newHashSet(new NotificationRecipient("admin@example.com"));
        service.sendCommentMentions(remoteUser, currentRecipients, comment, null);

        verify(eventPublisher).publish(new MentionIssueCommentEvent(null, remoteUser, Sets.newHashSet(fred), commentBody, IssueFieldConstants.COMMENT, currentRecipients, comment));
    }

    @Test
    public void testCommentMentionAfterEdit()
    {
        String commentBody = "This is a test comment mentioning [~fred]!";
        String editedBody = "This is a test comment mentioning [~fred]. I thin [~bob] should know about this too!";
        Comment comment = getComment(editedBody);
        Comment originalComment = getComment(commentBody);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(userManager.getUser("fred")).thenReturn(fred);
        when(userManager.getUser("bob")).thenReturn(bob);
        when(permissionManager.hasPermission(10, (Issue) null, fred)).thenReturn(true);
        when(permissionManager.hasPermission(10, (Issue) null, bob)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendCommentMentions(remoteUser, currentRecipients, comment, originalComment);

        verify(eventPublisher).publish(new MentionIssueCommentEvent(null, remoteUser, Sets.newHashSet(bob), editedBody, IssueFieldConstants.COMMENT, currentRecipients, comment));
    }

    @Test
    public void testIssueCreatedMentionNoBrowseUsersPermission()
    {
        Issue issue = getIssue("This is a test description mentioning [~fred]!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(false);
        service.sendIssueCreateMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), issue);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueCreateWithNoMentions()
    {
        Issue issue = getIssue("This is a test description with no mentions!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        service.sendIssueCreateMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), issue);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueCreateMentions()
    {
        String description = "This is a test description mentioning [~fred]!";
        Issue issue = getIssue(description);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendIssueCreateMentions(remoteUser, currentRecipients, issue);

        verify(eventPublisher).publish(new MentionIssueEvent(issue, remoteUser, Sets.newHashSet(fred), description, IssueFieldConstants.DESCRIPTION, currentRecipients));
    }

    @Test
    public void testIssueCreateMentionsUserNoPermissions()
    {
        String description = "This is a test description mentioning [~fred] and [~bob]!";
        Issue issue = getIssue(description);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(userManager.getUser("fred")).thenReturn(fred);
        when(userManager.getUser("bob")).thenReturn(bob);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);
        when(permissionManager.hasPermission(10, issue, bob)).thenReturn(false);

        Set<NotificationRecipient> currentRecipients = Sets.newHashSet(new NotificationRecipient("admin@example.com"));
        service.sendIssueCreateMentions(remoteUser, currentRecipients, issue);

        verify(eventPublisher).publish(new MentionIssueEvent(issue, remoteUser, Sets.newHashSet(fred), description, IssueFieldConstants.DESCRIPTION, currentRecipients));
    }

    @Test
    public void testIssueEditedMentionNoBrowseUsersPermission()
    {
        Issue issue = getIssue("This is a test description mentioning [~fred]!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(false);
        service.sendIssueEditMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), issue, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueEditWithNoMentions()
    {
        Issue issue = getIssue("This is a test description with no mentions!");
        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(changeHistoryManager.getChangeHistories(issue)).thenReturn(Collections.<ChangeHistory>emptyList());

        service.sendIssueEditMentions(remoteUser, Collections.<NotificationRecipient>emptySet(), issue, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueEditMentionsWhenNoEditOccurred()
    {
        String description = "This is a test description mentioning [~fred]!";
        Issue issue = getIssue(description);

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(changeHistoryManager.getChangeHistories(issue)).thenReturn(Collections.<ChangeHistory>emptyList());

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendIssueEditMentions(remoteUser, currentRecipients, issue, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueEditMentionsWhenEditedAnotherField()
    {
        String description = "This is a test description mentioning [~fred]!";
        Issue issue = getIssue(description);

        ChangeHistory changeHistory1 = mock(ChangeHistory.class);
        ChangeHistory changeHistory2 = mock(ChangeHistory.class);

        when(changeHistory2.getChangeItemBeans()).thenReturn(CollectionBuilder.list(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "priority", null, null)));

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(changeHistoryManager.getChangeHistories(issue)).thenReturn(CollectionBuilder.list(changeHistory1, changeHistory2));

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendIssueEditMentions(remoteUser, currentRecipients, issue, null);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    public void testIssueEditMentions()
    {
        String description = "This is a test description mentioning [~fred] and now we should also let [~bob] know!";
        Issue issue = getIssue(description);

        ChangeHistory changeHistory1 = mock(ChangeHistory.class);
        ChangeHistory changeHistory2 = mock(ChangeHistory.class);

        when(changeHistory2.getChangeItemBeans()).thenReturn(CollectionBuilder.list(
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "description", null, "This is a test description mentioning [~fred]", null, description)));

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(changeHistoryManager.getChangeHistories(issue)).thenReturn(CollectionBuilder.list(changeHistory1, changeHistory2));

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);
        when(userManager.getUser("bob")).thenReturn(bob);
        when(permissionManager.hasPermission(10, issue, bob)).thenReturn(true);

        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendIssueEditMentions(remoteUser, currentRecipients, issue, null);

        verify(eventPublisher).publish(new MentionIssueEvent(issue, remoteUser, Sets.newHashSet(bob), description, IssueFieldConstants.DESCRIPTION, currentRecipients));
    }


    @Test
    public void testIssueEditMentionsWithComment()
    {
        String description = "This is a test description mentioning [~fred] and now we should also let [~bob] know!";
        Issue issue = getIssue(description);
        String commentBody = "Hey you know what [~admin] should see this too!";
        Comment comment = getComment(commentBody);

        ChangeHistory changeHistory1 = mock(ChangeHistory.class);
        ChangeHistory changeHistory2 = mock(ChangeHistory.class);

        when(changeHistory2.getChangeItemBeans()).thenReturn(CollectionBuilder.list(
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "description", null, "This is a test description mentioning [~fred]", null, description)));

        when(permissionManager.hasPermission(27, remoteUser)).thenReturn(true);
        when(changeHistoryManager.getChangeHistories(issue)).thenReturn(CollectionBuilder.list(changeHistory1, changeHistory2));

        when(userManager.getUser("fred")).thenReturn(fred);
        when(permissionManager.hasPermission(10, issue, fred)).thenReturn(true);
        when(userManager.getUser("bob")).thenReturn(bob);
        when(permissionManager.hasPermission(10, issue, bob)).thenReturn(true);

        when(userManager.getUser("admin")).thenReturn(remoteUser);
        when(permissionManager.hasPermission(10, (Issue) null, remoteUser)).thenReturn(true);


        Set<NotificationRecipient> currentRecipients = Collections.emptySet();
        service.sendIssueEditMentions(remoteUser, currentRecipients, issue, comment);

        verify(eventPublisher).publish(new MentionIssueEvent(issue, remoteUser, Sets.newHashSet(bob), description, IssueFieldConstants.DESCRIPTION, currentRecipients));
        verify(eventPublisher).publish(new MentionIssueCommentEvent(null, remoteUser, Sets.newHashSet(remoteUser), commentBody, IssueFieldConstants.COMMENT, currentRecipients, comment));
    }

    private Issue getIssue(String description)
    {
        Issue issue = mock(Issue.class);
        when(issue.getDescription()).thenReturn(description);

        return issue;
    }

    private Comment getComment(String commentBody)
    {
        return new CommentImpl(null, null, null, commentBody, null, null, null, null, null);
    }
}
