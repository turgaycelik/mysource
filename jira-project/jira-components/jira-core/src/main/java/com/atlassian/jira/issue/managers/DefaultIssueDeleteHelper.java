package com.atlassian.jira.issue.managers;

import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssuePreDeleteEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.AttachmentCleanupException;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class DefaultIssueDeleteHelper implements IssueDeleteHelper
{
    private static final Logger log = Logger.getLogger(DefaultIssueDeleteHelper.class);

    private final IssueIndexManager indexManager;
    private final SubTaskManager subTaskManager;
    private final IssueLinkManager issueLinkManager;
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final MailThreadManager mailThreadManager;
    private final CustomFieldManager customFieldManager;
    private final IssueManager issueManager;
    private final NodeAssociationStore nodeAssociationStore;
    private final UserAssociationStore userAssociationStore;
    private final WorkflowManager workflowManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final IssueEventManager issueEventManager;
    private final EventPublisher eventPublisher;
    private final MovedIssueKeyStore movedIssueKeyStore;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private final CommentManager commentManager;
    private final IssueAttachmentDeleteHelper attachmentDeleteHelper;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public DefaultIssueDeleteHelper(IssueIndexManager indexManager, SubTaskManager subTaskManager,
            IssueLinkManager issueLinkManager, RemoteIssueLinkManager remoteIssueLinkManager, MailThreadManager mailThreadManager, CustomFieldManager customFieldManager,
            IssueManager issueManager, NodeAssociationStore nodeAssociationStore,
            WorkflowManager workflowManager, ChangeHistoryManager changeHistoryManager, IssueEventManager issueEventManager,
            UserAssociationStore userAssociationStore, EventPublisher eventPublisher, MovedIssueKeyStore movedIssueKeyStore,
            JsonEntityPropertyManager jsonEntityPropertyManager, CommentManager commentManager, IssueAttachmentDeleteHelper attachmentDeleteHelper,
            IssueEventBundleFactory issueEventBundleFactory)
    {
        this.indexManager = indexManager;
        this.subTaskManager = subTaskManager;
        this.issueLinkManager = issueLinkManager;
        this.remoteIssueLinkManager = remoteIssueLinkManager;
        this.mailThreadManager = mailThreadManager;
        this.customFieldManager = customFieldManager;
        this.issueManager = issueManager;
        this.nodeAssociationStore = nodeAssociationStore;
        this.userAssociationStore = userAssociationStore;
        this.workflowManager = workflowManager;
        this.changeHistoryManager = changeHistoryManager;
        this.issueEventManager = issueEventManager;
        this.eventPublisher = eventPublisher;
        this.movedIssueKeyStore = movedIssueKeyStore;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.commentManager = commentManager;
        this.attachmentDeleteHelper = attachmentDeleteHelper;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        try
        {
            final Long issueId = issue.getId();
            final GenericValue issueGV = issue.getGenericValue();
            if (issueGV == null)
            {
                throw new IllegalArgumentException("The provided issue has a null GenericValue.");
            }

            DeletedIssueEventData eventData = new DeletedIssueEventData(issueManager, customFieldManager, sendMail,
                    eventDispatchOption.isEventBeingSent() ? issueId : null);

            if (eventDispatchOption.isEventBeingSent())
            {
                dispatchPreDeleteEvent(user, eventDispatchOption, eventData);
            }

            Transaction txn = Txn.begin();
            try
            {
                removeChildActions(issue);
                // TODO: move this into the worklog manager remove worklogs
                issueGV.removeRelated("ChildWorklog");

                // Remove issue's sub-tasks (if any exist)
                removeSubTasks(user, issue, eventDispatchOption, sendMail);
                removeIssueLinks(user, issue);

                changeHistoryManager.removeAllChangeItems(issue);
                deleteMovedIssueKeyHistory(issue);
                removeIssueProperties(issue);
                removeAttachments(issue);
                nodeAssociationStore.removeAssociationsFromSource(issueGV);
                userAssociationStore.removeUserAssociationsFromSink(issueGV.getEntityName(), issueGV.getLong("id"));
                customFieldManager.removeCustomFieldValues(issueGV);
                workflowManager.removeWorkflowEntries(issueGV);
                issueGV.remove();
                removeNotifications(issueId);

                txn.commit();

                deindex(issue);
                dispatchDeleteEvent(user, eventDispatchOption, eventData);
            }
            finally
            {
                txn.finallyRollbackIfNotCommitted();
            }
        }
        catch (GenericEntityException e)
        {
            throw new RemoveException(e);
        }
    }

    private void removeChildActions(final Issue issue) throws GenericEntityException
    {
        for (Comment comment : commentManager.getComments(issue))
        {
            commentManager.delete(comment);
        }
        // remove actions
        issue.getGenericValue().removeRelated("ChildAction");
    }

    private void deleteMovedIssueKeyHistory(Issue issue)
    {
        final Long issueId = issue.getId();
        movedIssueKeyStore.deleteMovedIssueKeyHistory(issueId);
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
        deleteIssue(null, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
    }

    private void removeIssueLinks(User user, Issue issue) throws RemoveException
    {
        // test if the issue is a sub-task
        // NOTE: This has to be done BEFORE removing the issue link as the sub-task issue link is
        // used to determine if the issue is a sub-task
        if (issue.isSubTask())
        {
            // Get the parent issue before removing the links, as we need the link to determine the parent issue
            Issue parentIssue = issue.getParentObject();
            // Remove the links
            issueLinkManager.removeIssueLinksNoChangeItems(issue);
            // We need to reorder the parent's links as the its sub-task link for this issue has been removed
            subTaskManager.resetSequences(parentIssue);
        }
        else
        {
            // If there are no sub-tasks so all we need to do is delete the issue's links
            issueLinkManager.removeIssueLinksNoChangeItems(issue);
        }

        for (RemoteIssueLink remoteIssueLink : remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue))
        {
            remoteIssueLinkManager.removeRemoteIssueLink(remoteIssueLink.getId(), user);
        }
    }

    private void removeIssueProperties(final Issue issue)
    {
        jsonEntityPropertyManager.deleteByEntity(EntityPropertyType.ISSUE_PROPERTY.getDbEntityName(), issue.getId());
    }

    private void removeAttachments(Issue issue) throws RemoveException
    {
        try
        {
            attachmentDeleteHelper.deleteAttachmentsForIssue(issue).claim();
        }
        catch (AttachmentCleanupException e)
        {
            throw new RemoveException(e);
        }
    }

    private void removeNotifications(Long issueId)
    {
        if (issueId != null)
        {
            try
            {
                mailThreadManager.removeAssociatedEntries(issueId);
            }
            catch (DataAccessException e)
            {
                log.error("Error removing Notification Instance records for issue with id '" + issueId + "': " + e, e);
            }
        }
    }

    protected void removeSubTasks(User user, Issue parentIssue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        for (IssueLink subTaskIssueLink : subTaskManager.getSubTaskIssueLinks(parentIssue.getId()))
        {
            Issue subTaskIssue = subTaskIssueLink.getDestinationObject();
            log.debug("Deleting sub-task issue with key: " + subTaskIssue.getKey());
            deleteIssue(user, subTaskIssue, eventDispatchOption, sendMail);
            log.debug("Deleted sub-task issue with key: " + subTaskIssue.getKey());
        }
    }

    private void deindex(Issue issue)
    {
        try
        {
            indexManager.deIndex(issue);
        }
        catch (Exception issueDeIndexException)
        {
            log.error("Error deindexing issue: [" + issue.getKey() + "] " + issue.getSummary() + ":" + issueDeIndexException, issueDeIndexException);
        }
    }

    private void dispatchPreDeleteEvent(User user, EventDispatchOption eventDispatchOption, DeletedIssueEventData eventData)
    {
        if (eventDispatchOption.isEventBeingSent())
        {
            eventPublisher.publish(new IssuePreDeleteEvent(eventData.issue, user));
        }
    }

    private void dispatchDeleteEvent(User user, EventDispatchOption eventDispatchOption, DeletedIssueEventData eventData)
    {
        if (eventDispatchOption.isEventBeingSent())
        {
            issueEventManager.dispatchRedundantEvent(eventDispatchOption.getEventTypeId(), eventData.issue,
                    eventData.paramsMap(), user, eventData.isSendMail());
            // Publish new events
            IssueEventBundle issueEventBundle = issueEventBundleFactory.createIssueDeleteEventBundle(eventData.issue,
                    eventData, ApplicationUsers.from(user));
            issueEventManager.dispatchEvent(issueEventBundle);
        }
    }

    /**
     * <p/>
     * Holds the state of the deleted issue object more consistent before it gets deleted.
     * Makes parentId (and thus information, if the issue is a sub-task) accessible in thread local cache and
     * collects custom fields values (also making them accessible in thread-local cache).
     *
     * <p/>
     * See also:<br>
     * http://jira.atlassian.com/browse/JRA-12091<br>
     * http://jira.atlassian.com/browse/JRA-24331<br>
     * http://jira.atlassian.com/browse/JRA-21646
     */
    public static class DeletedIssueEventData
    {
        private final Issue issue;
        private final Map<String, Object> customFieldValues;
        private final List<User> watchers;
        private final boolean sendMail;

        DeletedIssueEventData(IssueManager issueManager, CustomFieldManager customFieldManager, boolean sendMail, Long issueId)
        {
            if (issueId == null)
            {
                issue = null;
                customFieldValues = null;
                watchers = null;
            }
            else
            {
                issue = issueManager.getIssueObject(issueId);
                // don't remove, this inits thread-local cache!
                issue.getParentId();
                customFieldValues = collectCustomFieldValues(customFieldManager);
                watchers = issueManager.getWatchers(issue);
            }
            this.sendMail = sendMail;
        }

        DeletedIssueEventData(IssueManager issueManager, CustomFieldManager customFieldManager, boolean sendMail)
        {
            this(issueManager, customFieldManager, sendMail, null);
        }

        private Map<String, Object> collectCustomFieldValues(CustomFieldManager customFieldManager)
        {
            ImmutableMap.Builder<String, Object> answerBuilder = ImmutableMap.builder();
            for (CustomField customField : customFieldManager.getCustomFieldObjects(issue))
            {
                Object value = customField.getValue(issue);
                if (value != null)
                {
                    answerBuilder.put(customField.getId(), value);
                }

            }
            return answerBuilder.build();
        }

        public Map<String, Object> paramsMap()
        {
            final Map<String, Object> builder = Maps.newHashMap();
            if (customFieldValues != null)
            {
                builder.put(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, customFieldValues);
            }
            if (watchers != null)
            {
                builder.put(IssueEvent.WATCHERS_PARAM_NAME, watchers);
            }
            return !builder.isEmpty() ? ImmutableMap.copyOf(builder) : null;
        }

        public boolean isSendMail()
        {
            return sendMail;
        }
    }
}
