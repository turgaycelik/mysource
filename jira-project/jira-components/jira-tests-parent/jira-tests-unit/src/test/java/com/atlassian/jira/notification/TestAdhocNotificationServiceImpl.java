package com.atlassian.jira.notification;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestAdhocNotificationServiceImpl
{
    @Mock
    private VoteService voteService;

    private AdhocNotificationServiceImpl notificationService;

    @Before
    public void setUp()
    {
        notificationService = createNotificationService(voteService);
    }

    @Test
    public void getVotersRecipientsReturnsAnIterableWithNotificationRecipientsIfUserCanSeeTheVoters()
    {
        User user = mock(User.class);
        Issue issue = mock(Issue.class);

        Collection<User> voters = ImmutableList.of(mock(User.class), mock(User.class));
        when(voteService.viewVoters(issue, user)).thenReturn(ServiceOutcomeImpl.ok(voters));

        Iterable<NotificationRecipient> votersRecipients = notificationService.getVotersRecipients(user, issue);

        assertThat(votersRecipients.iterator().hasNext(), is(true));
    }

    @Test
    public void getVotersRecipientsReturnsAnEmptyIterableIfUserCanNotViewTheVoters()
    {
        User user = mock(User.class);
        Issue issue = mock(Issue.class);

        when(voteService.viewVoters(issue, user)).thenReturn(ServiceOutcomeImpl.<Collection<User>>error("some error"));

        Iterable<NotificationRecipient> votersRecipients = notificationService.getVotersRecipients(user, issue);

        assertThat(votersRecipients.iterator().hasNext(), is(false));
    }

    private AdhocNotificationServiceImpl createNotificationService(final VoteService voteService)
    {
        return new AdhocNotificationServiceImpl(
                mock(MailService.class),
                mock(UserManager.class),
                mock(GroupManager.class),
                mock(WatcherService.class),
                voteService,
                mock(PermissionManager.class),
                mock(I18nHelper.BeanFactory.class),
                mock(NotificationSchemeManager.class),
                mock(NotificationFilterManager.class),
                mock(UserPreferencesManager.class)
        );
    }
}
