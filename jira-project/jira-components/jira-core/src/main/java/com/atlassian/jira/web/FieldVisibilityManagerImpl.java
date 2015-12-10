package com.atlassian.jira.web;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @since v4.0
 */
public class FieldVisibilityManagerImpl implements FieldVisibilityManager
{
    private static final Logger log = Logger.getLogger(FieldVisibilityManagerImpl.class);

    private final FieldManager fieldManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final SubTaskManager subTaskManager;

    @Deprecated
    public FieldVisibilityManagerImpl(final FieldManager fieldManager, final ProjectManager projectManager)
    {
        this(fieldManager, projectManager, ComponentAccessor.getConstantsManager(),
                ComponentAccessor.getFieldLayoutManager(), ComponentAccessor.getSubTaskManager());
    }

    public FieldVisibilityManagerImpl(final FieldManager fieldManager, final ProjectManager projectManager,
            final ConstantsManager constantsManager, final FieldLayoutManager fieldLayoutManager,
            final SubTaskManager subTaskManager)
    {
        this.fieldManager = fieldManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.subTaskManager = subTaskManager;
    }

    @Override
    public boolean isFieldHidden(final User remoteUser, final String id)
    {
        return fieldManager.isFieldHidden(remoteUser, id);
    }

    @Override
    public boolean isFieldVisible(final User remoteUser, final String id)
    {
        return !fieldManager.isFieldHidden(remoteUser, id);
    }

    @Override
    public boolean isFieldHidden(final String fieldId, final Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        final Long projectId = issue.getProjectId();
        final String issueTypeId = issue.getIssueTypeId();
        if (issueTypeId == null)
        {
            log.warn("Issue with id '" + issue.getId() + "' and key '" + issue.getKey() + "' has a null issue type, returning true for isFieldHidden check.");
            return true;
        }
        if (projectId == null)
        {
            log.warn("Issue with id '" + issue.getId() + "' and key '" + issue.getKey() + "' has a null project, returning true for isFieldHidden check.");
            return true;
        }
        return isFieldHidden(projectId, fieldId, issueTypeId);
    }

    @Override
    public boolean isFieldVisible(final String fieldId, final Issue issue)
    {
        return !isFieldHidden(fieldId, issue);
    }

    @Override
    public boolean isCustomFieldHidden(final Long projectId, final Long customFieldId, final String issueTypeId)
    {
        return isFieldHidden(projectId, FieldManager.CUSTOM_FIELD_PREFIX + customFieldId, issueTypeId);
    }

    @Override
    public boolean isCustomFieldVisible(final Long projectId, final Long customFieldId, final String issueTypeId)
    {
        return isFieldVisible(projectId, FieldManager.CUSTOM_FIELD_PREFIX + customFieldId, issueTypeId);
    }

    @Override
    public boolean isFieldHidden(final Long projectId, final String fieldId, final Long issueTypeId)
    {
        return isFieldHidden(projectId, fieldId, issueTypeId.toString());
    }

    @Override
    public boolean isFieldVisible(final Long projectId, final String fieldId, final Long issueTypeId)
    {
        return isFieldVisible(projectId, fieldId, issueTypeId.toString());
    }

    @Override
    public boolean isFieldHidden(final Long projectId, final String fieldId, final String issueTypeId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("projectId cannot be null.");
        }

        final Project project = projectManager.getProjectObj(projectId);

        if (TextUtils.stringSet(issueTypeId))
        {
            if (ALL_ISSUE_TYPES.equals(issueTypeId))
            {
                final List<String> issueTypes;
                if (subTaskManager.isSubTasksEnabled())
                {
                    issueTypes = constantsManager.expandIssueTypeIds(Lists.newArrayList(ConstantsManager.ALL_ISSUE_TYPES));
                }
                else
                {
                    issueTypes = constantsManager.expandIssueTypeIds(Lists.newArrayList(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
                }

                for (final String issueType : issueTypes)
                {
                    final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueType);

                    if (fieldLayout == null || fieldLayout.isFieldHidden(fieldId))
                    {
                        return true;
                    }
                }
                // Field is not hidden in any field layout scheme associated with this project
                return false;
            }
            // Check if field is hidden in layout associated with specific project and issue type
            else
            {
                // Retrieve the field layout associated with specified project and issue type
                final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(project, issueTypeId);

                // Check if the field is present in the list
                return fieldLayout == null || fieldLayout.isFieldHidden(fieldId);
            }
        }
        log.warn("Unable to determine field visibility with project with id '" + projectId + "', issue type with id '" + issueTypeId + "' and field with id '" + fieldId + "'.");
        return true;
    }

    @Override
    public boolean isFieldVisible(final Long projectId, final String fieldId, final String issueTypeId)
    {
        return !isFieldHidden(projectId, fieldId, issueTypeId);
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId, List<String> issueTypes)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("projectId cannot be null");
        }

        final Project project = projectManager.getProjectObj(projectId);

        if (issueTypes == null || issueTypes.isEmpty())
        {
            // Project specified only - gather all Issue Types
            if (subTaskManager.isSubTasksEnabled())
            {
                issueTypes = constantsManager.expandIssueTypeIds(Lists.newArrayList(ConstantsManager.ALL_ISSUE_TYPES));
            }
            else
            {
                issueTypes = constantsManager.expandIssueTypeIds(Lists.newArrayList(ConstantsManager.ALL_STANDARD_ISSUE_TYPES));
            }
        }

        final FieldConfigurationScheme fieldConfigurationScheme = fieldLayoutManager.getFieldConfigurationScheme(project);

        // For efficiency we want to gather distinct fieldLayouts to minimise the number of checks we do.
        final HashSet<Long> fieldLayoutIds = new HashSet<Long>();
        if (fieldConfigurationScheme == null)
        {
            // Default scheme
            fieldLayoutIds.add(null);
        }
        else
        {
            for (final String issueType : issueTypes)
            {
                fieldLayoutIds.add(fieldConfigurationScheme.getFieldLayoutId(issueType));
            }
        }
        // Now check the unique FieldLayouts
        for (final Long fieldLayoutId : fieldLayoutIds)
        {
            final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(fieldLayoutId);
            if (fieldLayout != null && !fieldLayout.isFieldHidden(fieldId))
            {
                // Field is visible in at least one scheme association
                return false;
            }
        }


        // Field is hidden in all associated Field Configs
        return true;
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(final Long projectId, final String fieldId)
    {
        return isFieldHiddenInAllSchemes(projectId, fieldId, Collections.<String>emptyList());
    }

    @Override
    public boolean isFieldHiddenInAllSchemes(final String fieldId, final SearchContext context, final User user)
    {
        if (context.isForAnyProjects())
        {
            // Sees if it's hidden in all of the browseable schemes for the user
            return isFieldHidden(user, fieldId);
        }
        else
        {
            // Loop through the projects
            final List<Long> projectIds = context.getProjectIds();
            for (final Long projectId : projectIds)
            {

                // Checks if the project exists & is visible to the user
                if (projectManager.getProjectObj(projectId) != null)
                {
                    final boolean hidden = isFieldHiddenInAllSchemes(projectId, fieldId, context.getIssueTypeIds());
                    if (!hidden)
                    {
                        return false;
                    }
                }
                else
                {
                    log.warn("Unable to find project with id " + projectId);
                }
            }
            return true;
        }
    }
}
