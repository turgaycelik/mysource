package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause validator that can be used for project clause types and considers permissions
 *
 */
class ProjectValuesExistValidator extends ValuesExistValidator
{
    private final ProjectIndexInfoResolver projectIndexInfoResolver;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;

    ProjectValuesExistValidator(final JqlOperandResolver operandResolver, ProjectIndexInfoResolver projectIndexInfoResolver, PermissionManager permissionManager, ProjectManager projectManager, I18nHelper.BeanFactory beanFactory)
    {
        super(operandResolver, beanFactory);
        this.projectIndexInfoResolver = notNull("projectIndexInfoResolver", projectIndexInfoResolver);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectManager = notNull("projectManager", projectManager);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        final List<String> ids = projectIndexInfoResolver.getIndexedValues(value);
        return projectExists(searcher, ids);
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        final List<String> ids = projectIndexInfoResolver.getIndexedValues(value);
        return projectExists(searcher, ids);
    }

    boolean projectExists(final User searcher, final List<String> ids)
    {
        for (String sid : ids)
        {
            Long id = convertToLong(sid);
            if (id != null)
            {
                final Project project = projectManager.getProjectObj(id);
                if (project != null && permissionManager.hasPermission(Permissions.BROWSE, project, searcher))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Long convertToLong(String str)
    {
        try
        {
            return Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}