package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BulkEditOperation implements ProgressAwareBulkOperation
{
    public static final String NAME_KEY = "bulk.edit.operation.name";
    public static final String NAME = "BulkEdit";
    private static final String DESCRIPTION_KEY = "bulk.edit.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.edit.cannotperform";
    private static final List<String> ALL_SYSTEM_FIELDS = ImmutableList.of(
            IssueFieldConstants.ISSUE_TYPE, IssueFieldConstants.SECURITY, IssueFieldConstants.PRIORITY,
            IssueFieldConstants.FIX_FOR_VERSIONS, IssueFieldConstants.AFFECTED_VERSIONS,
            IssueFieldConstants.COMPONENTS, IssueFieldConstants.ASSIGNEE, IssueFieldConstants.REPORTER,
            IssueFieldConstants.ENVIRONMENT, IssueFieldConstants.DUE_DATE, IssueFieldConstants.COMMENT,
            IssueFieldConstants.LABELS);
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;

    public BulkEditOperation(final IssueManager issueManager, final PermissionManager permissionManager,
                             final ProjectManager projectManager, final FieldManager fieldManager,
                             final JiraAuthenticationContext authenticationContext)
    {
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser)
    {
        // Ensure that all selected issues can be edited
        for (final Issue issue : bulkEditBean.getSelectedIssues())
        {
            if (!issue.isEditable())
            {
                return false;
            }
        }

        // Check if any of the actions are available
        final Collection actions = getActions(bulkEditBean, remoteUser).values();
        for (final Object action : actions)
        {
            final BulkEditAction bulkEditAction = (BulkEditAction) action;
            if (bulkEditAction.isAvailable(bulkEditBean))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Initialises all the bulk edit actions and returns them.
     *
     * @param bulkEditBean    bean used for actions retrieval
     * @param applicationUser remote user
     * @return bulk edit actions
     */
    public Map getActions(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser)
    {
        final Map actions = new ListOrderedMap();
        // Go through all system fields
        for (final String actionName : ALL_SYSTEM_FIELDS)
        {
            actions.put(actionName, buildBulkEditAction(actionName));
        }

        // Add all custom field actions
        actions.putAll(getCustomFieldActions(bulkEditBean, applicationUser));

        return actions;
    }

    private BulkEditAction buildBulkEditAction(final String fieldId)
    {
        return new BulkEditActionImpl(fieldId, fieldManager, authenticationContext);
    }

    public Map getCustomFieldActions(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser)
    {
        Long projectId;
        if (!bulkEditBean.isMultipleProjects())
        {
            projectId = (Long) bulkEditBean.getProjectIds().iterator().next();
        }

        final SearchContext searchContext = new SearchContextImpl(null, new ArrayList(bulkEditBean.getProjectIds()),
                new ArrayList(bulkEditBean.getIssueTypes()));

        final List<CustomField> customFields =
                ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(searchContext);
        final List<CustomField> availableCustomFields = new ArrayList<CustomField>();
        for (final CustomField customField : customFields)
        {
            // Need to check if the field is NOT hidden in ALL field layouts of selected projects
            for (final FieldLayout fieldLayout : bulkEditBean.getFieldLayouts())
            {
                if (!fieldLayout.isFieldHidden(customField.getId()))
                {
                    availableCustomFields.add(customField);
                }
            }
        }

        if (!availableCustomFields.isEmpty())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission in all projects of the selected issues
            for (final Long aLong : bulkEditBean.getProjectIds())
            {
                projectId = aLong;
                // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
                // just the ASSIGNEE permission, so the permissions to check depend on the field
                if (!hasPermission(Permissions.EDIT_ISSUE, projectManager.getProjectObj(projectId), applicationUser))
                {
                    return EasyMap.build(null, new UnavailableBulkEditAction("common.concepts.customfields",
                            "bulk.edit.unavailable.customfields", authenticationContext));
                }
            }

            final Map bulkEditActions = new ListOrderedMap();
            // Create BulkEditActions to represent each bulk-editable custom field
            for (final CustomField customField : availableCustomFields)
            {
                bulkEditActions.put(customField.getId(), buildBulkEditAction(customField.getId()));
            }

            return bulkEditActions;
        }
        else
        {
            return EasyMap.build(null,
                    new UnavailableBulkEditAction("common.concepts.customfields", "bulk.edit.unavailable.customfields",
                            authenticationContext));
        }
    }

    private boolean hasPermission(final int permission, final Project project, final ApplicationUser remoteUser)
    {
        return permissionManager.hasPermission(permission, project, remoteUser);
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser, Context taskContext)
            throws BulkOperationException
    {
        final boolean sendMail = bulkEditBean.isSendBulkNotification();
        for (final Issue issue1 : bulkEditBean.getSelectedIssues())
        {
            Context.Task task = taskContext.start(issue1);
            final MutableIssue issue = (MutableIssue) issue1;
            for (final BulkEditAction bulkEditAction : bulkEditBean.getActions().values())
            {
                final OrderableField field = bulkEditAction.getField();
                final FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager()
                        .getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId())
                        .getFieldLayoutItem(field);
                field.updateIssue(fieldLayoutItem, issue, bulkEditBean.getFieldValuesHolder());
            }

            issueManager.updateIssue(applicationUser.getDirectoryUser(), issue, EventDispatchOption.ISSUE_UPDATED,
                    sendMail);
            task.complete();
        }
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getSelectedIssues().size();
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public boolean equals(final Object o)
    {
        return this == o || o instanceof BulkEditOperation;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }
}
