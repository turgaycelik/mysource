package com.atlassian.jira.auditing;

import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.fugue.Effect;
import com.atlassian.jira.auditing.handlers.CustomFieldHandler;
import com.atlassian.jira.auditing.handlers.FieldLayoutSchemeChangeHandler;
import com.atlassian.jira.auditing.handlers.GroupEventHandler;
import com.atlassian.jira.auditing.handlers.NotificationChangeHandler;
import com.atlassian.jira.auditing.handlers.PermissionChangeHandler;
import com.atlassian.jira.auditing.handlers.ProjectComponentEventHandler;
import com.atlassian.jira.auditing.handlers.ProjectEventHandler;
import com.atlassian.jira.auditing.handlers.SchemeEventHandler;
import com.atlassian.jira.auditing.handlers.UserEventHandler;
import com.atlassian.jira.auditing.handlers.VersionEventHandler;
import com.atlassian.jira.auditing.handlers.WorkflowEventHandler;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.ProjectUpdatedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowCreatedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.jira.event.WorkflowRenamedEvent;
import com.atlassian.jira.event.WorkflowUpdatedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentCreatedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentDeletedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentMergedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentUpdatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeAddedToProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCopiedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeDeletedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityCreatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityRemovedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeEntityUpdatedEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.fields.layout.FieldLayoutSchemeUpdatedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldCreatedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldDeletedEvent;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeAddedToProjectEvent;
import com.atlassian.jira.event.issue.security.IssueSecuritySchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.notification.NotificationAddedEvent;
import com.atlassian.jira.event.notification.NotificationDeletedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeAddedToProjectEvent;
import com.atlassian.jira.event.notification.NotificationSchemeCopiedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeCreatedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeDeletedEvent;
import com.atlassian.jira.event.notification.NotificationSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.notification.NotificationSchemeUpdatedEvent;
import com.atlassian.jira.event.permission.GlobalPermissionAddedEvent;
import com.atlassian.jira.event.permission.GlobalPermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.event.permission.PermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeAddedToProjectEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCopiedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCreatedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.permission.PermissionSchemeUpdatedEvent;
import com.atlassian.jira.event.project.ProjectAvatarUpdateEvent;
import com.atlassian.jira.event.project.ProjectCategoryUpdateEvent;
import com.atlassian.jira.event.project.VersionArchiveEvent;
import com.atlassian.jira.event.project.VersionCreateEvent;
import com.atlassian.jira.event.project.VersionDeleteEvent;
import com.atlassian.jira.event.project.VersionMergeEvent;
import com.atlassian.jira.event.project.VersionReleaseEvent;
import com.atlassian.jira.event.project.VersionUnarchiveEvent;
import com.atlassian.jira.event.project.VersionUnreleaseEvent;
import com.atlassian.jira.event.project.ProjectCategoryChangeEvent;
import com.atlassian.jira.event.project.VersionUpdatedEvent;
import com.atlassian.jira.event.role.ProjectRoleDeletedEvent;
import com.atlassian.jira.event.role.ProjectRoleUpdatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeAddedToProjectEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCopiedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCreatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeDeletedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeUpdatedEvent;

/**
 *
 * @since v6.2
 */
@SuppressWarnings("unused")
public class AuditingEventListener
{
    private final AuditingManager auditingManager;
    private final PermissionChangeHandler permissionChangeHandler;
    private final GroupEventHandler groupEventHandler;
    private final SchemeEventHandler schemeEventHandler;
    private final UserEventHandler userEventHandler;
    private final WorkflowEventHandler workflowEventHandler;
    private final NotificationChangeHandler notificationChangeHandler;
    private final FieldLayoutSchemeChangeHandler fieldLayoutSchemeChangeHandler;
    private final ProjectEventHandler projectEventHandler;
    private final ProjectComponentEventHandler projectComponentEventHandler;
    private final VersionEventHandler versionEventHandler;

    public AuditingEventListener(final AuditingManager auditingManager,
            final PermissionChangeHandler permissionChangeHandler,
            final GroupEventHandler groupEventHandler,
            final SchemeEventHandler schemeEventHandler,
            final UserEventHandler userEventHandler,
            final WorkflowEventHandler workflowEventHandler,
            final NotificationChangeHandler notificationChangeHandler,
            final FieldLayoutSchemeChangeHandler fieldLayoutSchemeChangeHandler,
            final ProjectEventHandler projectEventHandler,
            final ProjectComponentEventHandler projectComponentEventHandler,
            final VersionEventHandler versionEventHandler)
    {
        this.auditingManager = auditingManager;
        this.permissionChangeHandler = permissionChangeHandler;
        this.groupEventHandler = groupEventHandler;
        this.schemeEventHandler = schemeEventHandler;
        this.userEventHandler = userEventHandler;
        this.workflowEventHandler = workflowEventHandler;
        this.notificationChangeHandler = notificationChangeHandler;
        this.fieldLayoutSchemeChangeHandler = fieldLayoutSchemeChangeHandler;
        this.projectEventHandler = projectEventHandler;
        this.projectComponentEventHandler = projectComponentEventHandler;
        this.versionEventHandler = versionEventHandler;
    }

    @EventListener
    public void onUserCreation(UserCreatedEvent event)
    {
        auditingManager.store(userEventHandler.handleUserCreatedEvent(event));
    }

    @EventListener
    public void onUserDeleted(UserDeletedEvent event)
    {
        auditingManager.store(userEventHandler.handleUserDeletedEvent(event));
    }

    @EventListener
    public void onUserUpdatedEvent(UserUpdatedEvent event)
    {
        userEventHandler.handleUserUpdatedEvent(event).foreach(store());
    }

    @EventListener
    public void onUserCredentialUpdatedEvent(UserCredentialUpdatedEvent event)
    {
        auditingManager.store(userEventHandler.handleUserCredentialUpdatedEvent(event));
    }

    @EventListener
    public void onGroupCreatedEvent(GroupCreatedEvent event)
    {
        groupEventHandler.onGroupCreatedEvent(event).foreach(store());
    }

    @EventListener
    public void onGroupDeletedEvent(GroupDeletedEvent event)
    {
        groupEventHandler.onGroupDeletedEvent(event).foreach(store());
    }

    @EventListener
    public void onGroupMembershipCreatedEvent(GroupMembershipCreatedEvent event) {
        groupEventHandler.onGroupMembershipCreatedEvent(event).foreach(store());
    }

    @EventListener
    public void onGroupMembershipDeletedEvent(GroupMembershipDeletedEvent event) {
        groupEventHandler.onGroupMembershipDeletedEvent(event).foreach(store());
    }

    @EventListener
    public void onPermissionSchemeCreated(PermissionSchemeCreatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCreatedEvent(event));
    }

    @EventListener
    public void onPermissionSchemeCopied(PermissionSchemeCopiedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCopiedEvent(event));
    }

    @EventListener
    public void onPermissionSchemeDeleted(PermissionSchemeDeletedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeDeletedEvent(event));
    }

    @EventListener
    public void onPermissionSchemeUpdated(PermissionSchemeUpdatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeUpdatedEvent(event));
    }

    @EventListener
    public void onPermissionAddedEvent(PermissionAddedEvent event)
    {
        auditingManager.store(permissionChangeHandler.onPermissionAddedEvent(event));
    }

    @EventListener
    public void onPermissionDeletedEvent(PermissionDeletedEvent event)
    {
        auditingManager.store(permissionChangeHandler.onPermissionDeletedEvent(event));
    }

    @EventListener
    public void onGlobalPermissionAdded(GlobalPermissionAddedEvent event)
    {
        auditingManager.store(permissionChangeHandler.onGlobalPermissionAddedEvent(event));
    }

    @EventListener
    public void onGlobalPermissionRemoved(GlobalPermissionDeletedEvent event)
    {
        auditingManager.store(permissionChangeHandler.onGlobalPermissionDeletedEvent(event));
    }

    @EventListener
    public void onWorkflowSchemeCreatedEvent(WorkflowSchemeCreatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCreatedEvent(event));
    }

    @EventListener
    public void onWorkflowSchemeCopiedEvent(WorkflowSchemeCopiedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCopiedEvent(event));
    }

    @EventListener
    public void onWorkflowSchemeDeletedEvent(WorkflowSchemeDeletedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeDeletedEvent(event));
    }

    @EventListener
    public void onWorkflowSchemeUpdatedEvent(WorkflowSchemeUpdatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeUpdatedEvent(event));
    }

    @EventListener
    public void onWorkflowSchemeAddedToProjectEvent(final WorkflowSchemeAddedToProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeAddedToProject(event));
    }

    @EventListener
    public void onWorkflowSchemeRemovedFromProjectEvent(final WorkflowSchemeRemovedFromProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeRemovedFromProject(event));
    }

    @EventListener
    public void onWorkflowCreatedEvent(WorkflowCreatedEvent event)
    {
        auditingManager.store(workflowEventHandler.onWorkflowCreatedEvent(event));
    }

    @EventListener
    public void onWorkflowCopiedEvent(WorkflowCopiedEvent event)
    {
        auditingManager.store(workflowEventHandler.onWorkflowCopiedEvent(event));
    }

    @EventListener
    public void onWorkflowDeletedEvent(WorkflowDeletedEvent event)
    {
        auditingManager.store(workflowEventHandler.onWorkflowDeletedEvent(event));
    }

    @EventListener
    public void onWorkflowDeletedEvent(WorkflowUpdatedEvent event)
    {
        workflowEventHandler.onWorkflowUpdatedEvent(event).foreach(store());
    }

    @EventListener
    public void onDraftWorkflowPublishedEvent(DraftWorkflowPublishedEvent event)
    {
        auditingManager.store(workflowEventHandler.onDraftWorkflowPublishedEvent(event));
    }

    @EventListener
    public void onWorkflowRenamedEvent(WorkflowRenamedEvent event)
    {
        auditingManager.store(workflowEventHandler.onWorkflowRenamedEvent(event));
    }

    @EventListener
    public void onNotificationSchemeCreatedEvent(NotificationSchemeCreatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCreatedEvent(event));
    }

    @EventListener
    public void onNotificationSchemeDeletedEvent(NotificationSchemeDeletedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeDeletedEvent(event));
    }

    @EventListener
    public void onNotificationSchemeCopiedEvent(NotificationSchemeCopiedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCopiedEvent(event));
    }

    @EventListener
    public void onNotificationSchemeUpdatedEvent(NotificationSchemeUpdatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeUpdatedEvent(event));
    }

    @EventListener
    public void onNotificationAddedEvent(NotificationAddedEvent event)
    {
        auditingManager.store(notificationChangeHandler.onNotificationAddedEvent(event));
    }

    @EventListener
    public void onNotificationDeletedEvent(NotificationDeletedEvent event)
    {
        auditingManager.store(notificationChangeHandler.onNotificationDeletedEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeCreatedEvent(FieldLayoutSchemeCreatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCreatedEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeCopiedEvent(FieldLayoutSchemeCopiedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeCopiedEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeUpdatedEvent(FieldLayoutSchemeUpdatedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeUpdatedEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeDeletedEvent(FieldLayoutSchemeDeletedEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeDeletedEvent(event));
    }

    @EventListener
    public void onCustomFieldCreatedEvent(CustomFieldCreatedEvent event)
    {
        auditingManager.store(CustomFieldHandler.onCustomFieldCreatedEvent(event));
    }

    @EventListener
    public void onCustomFieldUpdatedEvent(CustomFieldUpdatedEvent event)
    {
        auditingManager.store(CustomFieldHandler.onCustomFieldUpdatedEvent(event));
    }


    @EventListener
    public void onCustomFieldDeletedEvent(CustomFieldDeletedEvent event)
    {
        auditingManager.store(CustomFieldHandler.onCustomFieldDeletedEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeEntityCreatedEvent(FieldLayoutSchemeEntityCreatedEvent event)
    {
        auditingManager.store(fieldLayoutSchemeChangeHandler.onFieldLayoutSchemeEntityEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeEntityRemovedEvent(FieldLayoutSchemeEntityRemovedEvent event)
    {
        auditingManager.store(fieldLayoutSchemeChangeHandler.onFieldLayoutSchemeEntityEvent(event));
    }

    @EventListener
    public void onFieldLayoutSchemeEntityUpdatedEvent(FieldLayoutSchemeEntityUpdatedEvent event)
    {
        auditingManager.store(fieldLayoutSchemeChangeHandler.onFieldLayoutSchemeEntityUpdatedEvent(event));
    }

    private Effect<RecordRequest> store()
    {
        return new Effect<RecordRequest>()
        {
            @Override
            public void apply(final RecordRequest recordRequest)
            {
                auditingManager.store(recordRequest);
            }
        };
    }

    @EventListener
    public void onNotificationSchemeAddedToProjectEvent(final NotificationSchemeAddedToProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeAddedToProject(event));
    }

    @EventListener
    public void onNotificationSchemeRemovedFromProjectEvent(final NotificationSchemeRemovedFromProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeRemovedFromProject(event));
    }

    @EventListener
    public void onIssueSecuritySchemeAddedToProjectEvent(final IssueSecuritySchemeAddedToProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeAddedToProject(event));
    }

    @EventListener
    public void onIssueSecuritySchemeRemovedFromProjectEvent(final IssueSecuritySchemeRemovedFromProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeRemovedFromProject(event));
    }

    @EventListener
    public void onPermissionSchemeAddedToProjectEvent(final PermissionSchemeAddedToProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeAddedToProject(event));
    }

    @EventListener
    public void onPermissionSchemeRemovedFromProjectEvent(final PermissionSchemeRemovedFromProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeRemovedFromProject(event));
    }

    @EventListener
    public void onFieldLayoutSchemeAddedToProjectEvent(final FieldLayoutSchemeAddedToProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeAddedToProject(event));
    }

    @EventListener
    public void onFieldLayoutSchemeRemovedFromProjectEvent(final FieldLayoutSchemeRemovedFromProjectEvent event)
    {
        auditingManager.store(schemeEventHandler.onSchemeRemovedFromProject(event));
    }

    @EventListener
    public void onProjectCreatedEvent(final ProjectCreatedEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectCreatedEvent(event));
    }

    @EventListener
    public void onProjectUpdatedEvent(final ProjectUpdatedEvent event)
    {
        projectEventHandler.onProjectUpdatedEvent(event).foreach(store());
    }

    @EventListener
    public void onProjectDeletedEvent(final ProjectDeletedEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectDeletedEvent(event));
    }

    @EventListener
    public void onProjectComponentCreatedEvent(final ProjectComponentCreatedEvent event)
    {
        auditingManager.store(projectComponentEventHandler.onProjectComponentCreatedEvent(event));
    }

    @EventListener
    public void onProjectComponentUpdatedEvent(final ProjectComponentUpdatedEvent event)
    {
        projectComponentEventHandler.onProjectComponentUpdatedEvent(event).foreach(store());
    }

    @EventListener
    public void onProjectComponentMergedEvent(final ProjectComponentMergedEvent event)
    {
        auditingManager.store(projectComponentEventHandler.onProjectComponentMergedEvent(event));
    }

    @EventListener
    public void onProjectComponentDeletedEvent(final ProjectComponentDeletedEvent event)
    {
        auditingManager.store(projectComponentEventHandler.onProjectComponentDeletedEvent(event));
    }

    @EventListener
    public void onVersionArchiveEvent(final VersionArchiveEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionArchiveEvent(event));
    }

    @EventListener
    public void onVersionCreateEvent(final VersionCreateEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionCreateEvent(event));
    }

    @EventListener
    public void onVersionDeleteEvent(final VersionDeleteEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionDeleteEvent(event));
    }

    @EventListener
    public void onVersionMergeEvent(final VersionMergeEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionMergeEvent(event));
    }

    @EventListener
    public void onVersionReleaseEvent(final VersionReleaseEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionReleaseEvent(event));
    }

    @EventListener
    public void onVersionUnarchiveEvent(final VersionUnarchiveEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionUnarchiveEvent(event));
    }

    @EventListener
    public void onVersionUnreleaseEvent(final VersionUnreleaseEvent event)
    {
        auditingManager.store(versionEventHandler.onVersionUnreleaseEvent(event));
    }

    @EventListener
    public void onVersionUpdatedEvent(final VersionUpdatedEvent event)
    {
        versionEventHandler.onVersionUpdatedEvent(event).foreach(store());
    }

    @EventListener
    public void onProjectCategoryChangeEvent(final ProjectCategoryChangeEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectCategoryChangeEvent(event));
    }

    @EventListener
    public void onProjectAvatarChangeEvent(final ProjectAvatarUpdateEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectAvatarUpdateEvent(event));
    }

    @EventListener
    public void onProjectCategoryUpdateEvent(final ProjectCategoryUpdateEvent event)
    {
        projectEventHandler.onProjectCategoryUpdateEvent(event).foreach(store());
    }

    @EventListener
    public void onProjectRoleUpdatedEvent(final ProjectRoleUpdatedEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectRoleUpdatedEvent(event));
    }

    @EventListener
    public void onProjectRoleDeletedEvent(final ProjectRoleDeletedEvent event)
    {
        auditingManager.store(projectEventHandler.onProjectRoleDeletedEvent(event));
    }
}
