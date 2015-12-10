package com.atlassian.jira.issue.managers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkImpl;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.transaction.MockTransactionSupport;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.util.concurrent.Promises;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper}.
 *
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultIssueDeleteHelper
{
    private static final long PROJECT_ID = 1L;
    private static final long ISSUE_ID = 1L;
    private static final long PARENT_ISSUE_ID = 20000L;
    private static final RemoteIssueLink REMOTE_ISSUE_LINK = new RemoteIssueLinkBuilder().id(10000L).build();

    @Mock public NodeAssociationStore nodeAssociationStore;
    @Mock public UserAssociationStore userAssociationStore;
    @Mock public CustomFieldManager customFieldManager;
    @Mock public IssueAttachmentDeleteHelper attachmentDeleteHelper;
    @Mock public MailThreadManager mailThreadManager;
    @Mock public SubTaskManager subTaskManager;
    @Mock public IssueManager issueManager;
    @Mock public IssueLinkManager mockIssueLinkManager;
    @Mock public RemoteIssueLinkManager mockRemoteIssueLinkManager;
    @Mock public WorkflowManager workflowManager;
    @Mock public IssueIndexManager indexManager;
    @Mock public ChangeHistoryManager changeHistoryManager;
    @Mock public IssueEventManager issueEventManager;
    @Mock public EventPublisher eventPublisher;
    @Mock public MutableIssue issue;
    @Mock public MovedIssueKeyStore movedIssueKeyStore;
    @Mock public JsonEntityPropertyManager jsonEntityPropertyManager;
    @Mock public CommentManager commentManager;
    @Mock public IssueEventBundleFactory issueEventBundleFactory;

    @AvailableInContainer private TransactionSupport transactionSupport = new MockTransactionSupport();

    @Rule public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    private DbIndependentMockGenericValue issueGenericValue;
    private Attachment attachment;
    private Map<String,Object> capturedEventParams;
    private boolean capturedSendMailFlag;

    @Before
    public void initMocks() throws Exception
    {
        issueGenericValue = createIssueGV(ISSUE_ID, PROJECT_ID, "Test issue", "Test-1", "Test Assignee", "Test Resolution");
        attachment = new Attachment(null, new MockGenericValue("Attachment"), null);
    }

    private IssueDeleteHelper getIssueDeleteHelper() throws Exception
    {
        setUpIssue();
        setUpMocks();
        return new DefaultIssueDeleteHelper(indexManager, subTaskManager,  mockIssueLinkManager, mockRemoteIssueLinkManager, mailThreadManager,
                customFieldManager, issueManager, nodeAssociationStore, workflowManager, changeHistoryManager,
                issueEventManager, userAssociationStore, eventPublisher, movedIssueKeyStore, jsonEntityPropertyManager, commentManager, attachmentDeleteHelper,
                issueEventBundleFactory);
    }

    @Test
    public void shouldDeleteSubTaskWithLinks() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        MutableIssue parentIssue = mock(MutableIssue.class);
        when(parentIssue.getId()).thenReturn(PARENT_ISSUE_ID);
        when(issue.isSubTask()).thenReturn(true);
        when(issue.getParentObject()).thenReturn(parentIssue);

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);

        assertTrue(issueGenericValue.isRemoved());
        assertTrue(capturedSendMailFlag);

        verify(subTaskManager).resetSequences(eq(parentIssue));
        verifyMocks();
    }

    @Test
    public void shouldDeleteNotSubtask() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        when(issue.isSubTask()).thenReturn(false);

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, false);
        assertTrue(issueGenericValue.isRemoved());
        assertFalse(capturedSendMailFlag);
    }

    @Test
    public void removeSubTasks() throws Exception
    {
        setUpIssue();
        final AtomicInteger deleteIssueCalled = new AtomicInteger(0);
        DefaultIssueDeleteHelper tested = newMockDeleteIssueHelper(deleteIssueCalled);

        final MockGenericValue subTask1 = createIssueGV(2L, PROJECT_ID, "sub task 1", "TST-10", null, null);
        final MutableIssue subTaskIssue1 = mock(MutableIssue.class);
        when(subTaskIssue1.getId()).thenReturn(2L);
        when(subTaskIssue1.getGenericValue()).thenReturn(subTask1);
        final MockGenericValue subTask2 = createIssueGV(3L, PROJECT_ID, "sub task 2", "TST-11", null, null);
        final MutableIssue subTaskIssue2 = mock(MutableIssue.class);
        when(subTaskIssue2.getId()).thenReturn(3L);
        when(subTaskIssue2.getGenericValue()).thenReturn(subTask2);

        when(issueManager.getIssueObject(2L)).thenReturn(subTaskIssue1);
        when(issueManager.getIssueObject(3L)).thenReturn(subTaskIssue2);

        final MockGenericValue mockLinkGV1 = new DbIndependentMockGenericValue("IssueLink",
                ImmutableMap.<String,Object>of("destination", 2L));
        final IssueLink issueLink1 = new IssueLinkImpl(mockLinkGV1, null, issueManager);
        final MockGenericValue mockLinkGV2 = new DbIndependentMockGenericValue("IssueLink",
                ImmutableMap.<String,Object>of("destination", 3L));
        final IssueLink issueLink2 = new IssueLinkImpl(mockLinkGV2, null, issueManager);
        when(subTaskManager.getSubTaskIssueLinks(eq(ISSUE_ID))).thenReturn(ImmutableList.of(issueLink1, issueLink2));

        tested.removeSubTasks(null, issue, EventDispatchOption.ISSUE_DELETED, true);

        assertEquals(2, deleteIssueCalled.get());
    }

    @Test
    public void shouldAddCustomFieldParamsOnDelete() throws Exception
    {
        final CustomField customField1 = mock(CustomField.class);
        when(customField1.getId()).thenReturn("customfield_10000");
        when(customField1.getValue(issue)).thenReturn("Value1");

        final CustomField customField2 = mock(CustomField.class);
        when(customField2.getId()).thenReturn("customfield_10001");
        when(customField2.getValue(issue)).thenReturn("Value2");

        when(customFieldManager.getCustomFieldObjects(issue)).thenReturn(ImmutableList.of(customField1, customField2));
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        when(issue.isSubTask()).thenReturn(false);

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);
        assertTrue(issueGenericValue.isRemoved());

        final Map<String,Object> expected = ImmutableMap.<String, Object>of("customfield_10000", "Value1", "customfield_10001", "Value2");
        assertEquals(expected, capturedEventParams.get(IssueEvent.CUSTOM_FIELDS_PARAM_NAME));
        assertTrue(capturedSendMailFlag);
    }

    @Test
    public void shouldAddWatchersParamOnDelete() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        when(issueManager.getWatchers(eq(issue))).thenReturn(ImmutableList.<User>of(new MockUser("one"), new MockUser("two")));
        when(issue.isSubTask()).thenReturn(false);

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);

        assertTrue(issueGenericValue.isRemoved());
        final List<User> expected = ImmutableList.<User>of(new MockUser("one"), new MockUser("two"));
        assertEquals(expected, capturedEventParams.get(IssueEvent.WATCHERS_PARAM_NAME));
        assertTrue(capturedSendMailFlag);
    }

    private void verifyMocks() throws Exception
    {
        verify(nodeAssociationStore).removeAssociationsFromSource(eq(issueGenericValue));
        verify(mockRemoteIssueLinkManager).removeRemoteIssueLink(eq(REMOTE_ISSUE_LINK.getId()), isNull(User.class));
        verify(userAssociationStore).removeUserAssociationsFromSink(eq(issueGenericValue.getEntityName()), eq(issueGenericValue.getLong("id")));
        verify(customFieldManager).removeCustomFieldValues(eq(issueGenericValue));
        verify(attachmentDeleteHelper).deleteAttachmentsForIssue(eq(issue));
        verify(workflowManager).removeWorkflowEntries(eq(issueGenericValue));
        verify(indexManager).deIndex(eq(issue));
        verify(changeHistoryManager).removeAllChangeItems(eq(issue));
        verify(issueEventManager).dispatchRedundantEvent(eq(EventDispatchOption.ISSUE_DELETED.getEventTypeId()),
                eq(issue), Mockito.anyMapOf(String.class, Object.class), isNull(User.class), anyBoolean());
        verify(jsonEntityPropertyManager).deleteByEntity(eq(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName()), eq(ISSUE_ID));
    }


    private void setUpIssue()
    {
        when(issue.getId()).thenReturn(ISSUE_ID);
        when(issue.getGenericValue()).thenReturn(issueGenericValue);
    }

    private void setUpEmptyCustomFields()
    {
        when(customFieldManager.getCustomFieldObjects(eq(issue))).thenReturn(Collections.<CustomField>emptyList());
    }

    private void setUpEmptyWatchers()
    {
        when(issueManager.getWatchers(issue)).thenReturn(Collections.<User>emptyList());
    }

    private void setUpMocks() throws GenericEntityException, RemoveException, IndexException
    {
        when(mailThreadManager.removeAssociatedEntries(eq(ISSUE_ID))).thenReturn(1);
        when(mockIssueLinkManager.removeIssueLinksNoChangeItems(eq(issue))).thenReturn(1);
        when(mockRemoteIssueLinkManager.getRemoteIssueLinksForIssue(eq(issue))).thenReturn(ImmutableList.of(REMOTE_ISSUE_LINK));
        when(attachmentDeleteHelper.deleteAttachmentsForIssue(eq(issue))).thenReturn(Promises.<Void>promise(null));
        when(issueManager.getIssueObject(eq(ISSUE_ID))).thenReturn(issue);
        doAnswer(new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                capturedEventParams = (Map) invocation.getArguments()[2];
                capturedSendMailFlag = (Boolean) invocation.getArguments()[4];
                return null;
            }
        }).when(issueEventManager).dispatchRedundantEvent(eq(EventDispatchOption.ISSUE_DELETED.getEventTypeId()),
                eq(issue), Mockito.anyMapOf(String.class, Object.class), isNull(User.class), anyBoolean());
        when(subTaskManager.getSubTaskIssueLinks(eq(ISSUE_ID))).thenReturn(Collections.<IssueLink>emptyList());
        when(changeHistoryManager.getAllChangeItems(eq(issue))).thenReturn(Collections.<ChangeHistoryItem>emptyList());
    }

    private DefaultIssueDeleteHelper newMockDeleteIssueHelper(final AtomicInteger deleteIssueCalled)
    {
        return new DefaultIssueDeleteHelper(indexManager, subTaskManager,
                mockIssueLinkManager, mockRemoteIssueLinkManager, mailThreadManager, customFieldManager, issueManager,
                nodeAssociationStore, workflowManager, changeHistoryManager, issueEventManager, userAssociationStore,
                eventPublisher, movedIssueKeyStore, jsonEntityPropertyManager, commentManager, attachmentDeleteHelper,
                issueEventBundleFactory)
        {
            @Override
            public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
                    throws RemoveException
            {
                deleteIssueCalled.incrementAndGet();
            }
        };
    }

    static DbIndependentMockGenericValue createIssueGV(Long id, Long projectId, String summary, String key, String assignee, String resolution)
    {
        return new DbIndependentMockGenericValue("Issue", MapBuilder.<String,Object>newBuilder()
                .add("id", id)
                .add("project", projectId)
                .add("assignee", assignee)
                .add("summary", summary)
                .add("key", key)
                .add("resolution", resolution)
                .toMap()
        );
    }

    private static class DbIndependentMockGenericValue extends MockGenericValue
    {

        public DbIndependentMockGenericValue(String entityName)
        {
            super(entityName);
        }

        public DbIndependentMockGenericValue(String entityName, Map<String,Object> fields)
        {
            super(entityName, fields);
        }

        public DbIndependentMockGenericValue(String entityName, Long id)
        {
            super(entityName, id);
        }

        @Override
        public void store() throws GenericEntityException
        {
            stored = true;
        }

        @Override
        public void remove() throws GenericEntityException
        {
            removed = true;
        }

        @Override
        public void refresh() throws GenericEntityException
        {
            refreshed = true;
        }
    }

}
