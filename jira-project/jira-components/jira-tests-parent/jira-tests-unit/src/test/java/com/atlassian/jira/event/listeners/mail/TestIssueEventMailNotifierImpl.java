package com.atlassian.jira.event.listeners.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mail.IssueMailQueueItem;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilterContext;
import com.atlassian.jira.notification.NotificationFilterManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.type.NotificationType;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.template.Template;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueEventMailNotifierImpl
{
    @Mock
    private NotificationFilterManager notificationFilterManager;
    @Mock
    private NotificationSchemeManager notificationSchemeManager;
    @Mock
    private TemplateManager templateManager;
    @Mock
    private IssueMailQueueItemFactory issueMailQueueItemFactory;
    @Mock
    private MailQueue mailQueue;

    private IssueEventMailNotifier notifier;

    @Before
    public void setUp()
    {
        notifier = new IssueEventMailNotifierImpl(
                notificationFilterManager,
                notificationSchemeManager,
                templateManager,
                issueMailQueueItemFactory,
                mailQueue
        );
    }

    @Test
    public void noMailItemsAreCreatedIfTheListOfSchemeEntitiesIsEmpty()
    {
        List<SchemeEntity> schemeEntities = Collections.emptyList();

        Set<NotificationRecipient> recipients = notifier.generateNotifications(schemeEntities, anyIssueEvent(), anySetOfRecipients());

        assertThat(recipients.size(), is(0));
        verify(issueMailQueueItemFactory, never()).getIssueMailQueueItem(any(IssueEvent.class), anyLong(), anySetOf(NotificationRecipient.class), anyString());
        verify(mailQueue, never()).addItem(any(MailQueueItem.class));
    }
    
    @Test
    public void mailItemsAreCorrectlyCreatedWhenTheListOfSchemeEntitiesHasOnlyOneElement() throws Exception
    {
        IssueEvent issueEvent = anyIssueEvent();
        SchemeEntity schemeEntity = schemeEntityWithType(NotificationType.CURRENT_ASSIGNEE);

        NotificationFilterContext initialContext = new NotificationFilterContext();
        when(notificationFilterManager.makeContextFrom(JiraNotificationReason.ISSUE_EVENT, issueEvent)).thenReturn(initialContext);
        NotificationFilterContext contextSpecifyingChangeOnIssue = new NotificationFilterContext();
        when(notificationFilterManager.makeContextFrom(initialContext, NotificationType.CURRENT_ASSIGNEE)).thenReturn(contextSpecifyingChangeOnIssue);

        Set<NotificationRecipient> recipientsForSchemeEntity = Sets.newHashSet(new NotificationRecipient("1"), new NotificationRecipient("2"));
        when(notificationSchemeManager.getRecipients(issueEvent, schemeEntity)).thenReturn(recipientsForSchemeEntity);

        Set<NotificationRecipient> recomputedRecipients = Sets.newHashSet(new NotificationRecipient("3"), new NotificationRecipient("4"));
        when(notificationFilterManager.recomputeRecipients(recipientsForSchemeEntity, contextSpecifyingChangeOnIssue)).thenReturn(recomputedRecipients);

        Template template = templateWithId(1L);
        when(templateManager.getTemplate(schemeEntity)).thenReturn(template);

        IssueMailQueueItem mailItem = mock(IssueMailQueueItem.class);
        when(issueMailQueueItemFactory.getIssueMailQueueItem(issueEvent, template.getId(), recomputedRecipients, schemeEntity.getType())).thenReturn(mailItem);

        Set<NotificationRecipient> returnedRecipients = notifier.generateNotifications(Arrays.asList(schemeEntity), issueEvent, anySetOfRecipients());

        assertThat(returnedRecipients, equalTo(recomputedRecipients));
        verify(mailQueue, times(1)).addItem(mailItem);
    }

    @Test
    public void eachRecipientGetsAtMostOneEmailWhenTheListOfSchemeEntitiesHasSeveralElements() throws Exception
    {
        NotificationFilterContext context = new NotificationFilterContext();
        generatedNotificationContextIs(context);
        mockGeneratingTemplate();

        SchemeEntity schemeEntity1 = schemeEntityWithType(NotificationType.CURRENT_ASSIGNEE);
        SchemeEntity schemeEntity2 = schemeEntityWithType(NotificationType.CURRENT_USER);
        SchemeEntity schemeEntity3 = schemeEntityWithType(NotificationType.ALL_WATCHERS);

        NotificationRecipient recipient1 = new NotificationRecipient("1");
        NotificationRecipient recipient2 = new NotificationRecipient("2");
        NotificationRecipient recipient3 = new NotificationRecipient("3");

        IssueEvent issueEvent = anyIssueEvent();
        recipientsForSchemeEntity(issueEvent, context, schemeEntity1, Sets.newHashSet(recipient1, recipient2));
        recipientsForSchemeEntity(issueEvent, context, schemeEntity2, Sets.newHashSet(recipient2, recipient3));
        recipientsForSchemeEntity(issueEvent, context, schemeEntity3, Sets.newHashSet(recipient1, recipient3));

        IssueMailQueueItemFactorySpy mailItemFactory = new IssueMailQueueItemFactorySpy();
        notifier = createGeneratorWith(mailItemFactory);
        Set<NotificationRecipient> returnedRecipients = notifier.generateNotifications(Arrays.asList(schemeEntity1, schemeEntity2, schemeEntity3), issueEvent, anySetOfRecipients());

        // all the recipients must have received an email
        assertThat(returnedRecipients, contains(recipient1, recipient2, recipient3));

        // when processing the schemeEntity1, we generate mail items for both its recipients, as none of them have receive an email yet
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity1), contains(recipient1, recipient2));
        // when processing the schemeEntity2, we generate mail items for recipient3, as recipient2 has already received the email related to schemeEntity1
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity2), contains(recipient3));
        // when processing the schemeEntity3, we don't generate emails, as recipient1 has already received the email related to schemeEntity1 and recipient3 has already received the email related to schemeEntity2
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity3), is(nullValue()));

        verify(mailQueue, times(2)).addItem(any(IssueMailQueueItem.class));
    }

    @Test
    public void recipientsIncludedInTheListOfRecipientsToSkipDoNotReceiveAnyEmails() throws Exception
    {
        NotificationFilterContext context = new NotificationFilterContext();
        generatedNotificationContextIs(context);
        mockGeneratingTemplate();

        SchemeEntity schemeEntity1 = schemeEntityWithType(NotificationType.CURRENT_ASSIGNEE);
        SchemeEntity schemeEntity2 = schemeEntityWithType(NotificationType.CURRENT_USER);
        SchemeEntity schemeEntity3 = schemeEntityWithType(NotificationType.ALL_WATCHERS);

        NotificationRecipient recipient1 = new NotificationRecipient("1");
        NotificationRecipient recipient2 = new NotificationRecipient("2");
        NotificationRecipient recipient3 = new NotificationRecipient("3");

        IssueEvent issueEvent = anyIssueEvent();
        recipientsForSchemeEntity(issueEvent, context, schemeEntity1, Sets.newHashSet(recipient1, recipient2));
        recipientsForSchemeEntity(issueEvent, context, schemeEntity2, Sets.newHashSet(recipient2, recipient3));
        recipientsForSchemeEntity(issueEvent, context, schemeEntity3, Sets.newHashSet(recipient1, recipient3));

        Set<NotificationRecipient> recipientsToSkip = Sets.newHashSet(recipient1, recipient3);

        IssueMailQueueItemFactorySpy mailItemFactory = new IssueMailQueueItemFactorySpy();
        notifier = createGeneratorWith(mailItemFactory);
        Set<NotificationRecipient> returnedRecipients = notifier.generateNotifications(Arrays.asList(schemeEntity1, schemeEntity2, schemeEntity3), issueEvent, recipientsToSkip);

        // only recipient2 can receive emails, since recipient1 and recipient3 are on the list of recipients to skip
        assertThat(returnedRecipients, contains(recipient2));

        // when processing the schemeEntity1, we generate mail items for recipient2, as recipient1 is on the list of recipients to skip
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity1), contains(recipient2));
        // when processing the schemeEntity2, we don't generate emails, as recipient2 has already received the email related to schemeEntity1 and recipient3 is on the list of recipients to skip
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity2), is(nullValue()));
        // when processing the schemeEntity3, we don't generate emails, as all its recipients are on the list of recipients to skip
        assertThat(mailItemFactory.getRecipientsUsedFor(schemeEntity3), is(nullValue()));

        verify(mailQueue, times(1)).addItem(any(IssueMailQueueItem.class));
    }

    private void mockGeneratingTemplate()
    {
        when(templateManager.getTemplate(any(SchemeEntity.class))).thenReturn(mock(Template.class));
    }

    private void generatedNotificationContextIs(NotificationFilterContext context)
    {
        when(notificationFilterManager.makeContextFrom(any(JiraNotificationReason.class), any(IssueEvent.class))).thenReturn(context);
        when(notificationFilterManager.makeContextFrom(any(NotificationFilterContext.class), any(NotificationType.class))).thenReturn(context);
    }

    private void recipientsForSchemeEntity(IssueEvent issueEvent, NotificationFilterContext context, SchemeEntity schemeEntity, Set<NotificationRecipient> recipients) throws Exception
    {
        when(notificationSchemeManager.getRecipients(issueEvent, schemeEntity)).thenReturn(recipients);
        when(notificationFilterManager.recomputeRecipients(recipients, context)).thenReturn(recipients);
    }

    private Template templateWithId(Long id)
    {
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(id);
        return template;
    }

    private SchemeEntity schemeEntityWithType(NotificationType notificationType)
    {
        SchemeEntity schemeEntity = mock(SchemeEntity.class);
        when(schemeEntity.getType()).thenReturn(notificationType.dbCode());
        return schemeEntity;
    }

    private Set<NotificationRecipient> anySetOfRecipients()
    {
        return Collections.emptySet();
    }

    private IssueEvent anyIssueEvent()
    {
        return new IssueEvent(null, null, null, null);
    }

    private IssueEventMailNotifier createGeneratorWith(IssueMailQueueItemFactory issueMailQueueItemFactory)
    {
       return new IssueEventMailNotifierImpl(
                notificationFilterManager,
                notificationSchemeManager,
                templateManager,
                issueMailQueueItemFactory,
                mailQueue
        );
    }

    private static class IssueMailQueueItemFactorySpy implements IssueMailQueueItemFactory
    {
        private Map<String, Set<NotificationRecipient>> recipientsPerNotificationType = new HashMap<String, Set<NotificationRecipient>>();

        @Override
        public IssueMailQueueItem getIssueMailQueueItem(final IssueEvent event, final Long templateId, final Set<NotificationRecipient> recipientList, final String notificationType)
        {
            recipientsPerNotificationType.put(notificationType, recipientList);
            return mock(IssueMailQueueItem.class);
        }

        public Set<NotificationRecipient> getRecipientsUsedFor(SchemeEntity schemeEntity)
        {
            return recipientsPerNotificationType.get(schemeEntity.getType());
        }
    }
}
