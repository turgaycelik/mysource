package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause validator that can be used for component clause types and considers permissions
 *
 */
class ComponentValuesExistValidator extends ValuesExistValidator
{
    private final ComponentIndexInfoResolver componentIndexInfoResolver;
    private final PermissionManager permissionManager;
    private final ProjectComponentManager projectComponentManager;
    private final ProjectManager projectManager;

    ComponentValuesExistValidator(final JqlOperandResolver operandResolver, ComponentIndexInfoResolver componentIndexInfoResolver,
            PermissionManager permissionManager, ProjectComponentManager projectComponentManager, ProjectManager projectManager, I18nHelper.BeanFactory beanFactory)
    {
        super(operandResolver, beanFactory);
        this.componentIndexInfoResolver = notNull("componentIndexInfoResolver", componentIndexInfoResolver);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.projectComponentManager = notNull("projectComponentManager", projectComponentManager);
        this.projectManager = notNull("projectManager", projectManager);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        final List<String> ids = componentIndexInfoResolver.getIndexedValues(value);
        return componentExists(searcher, ids);
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        final List<String> ids = componentIndexInfoResolver.getIndexedValues(value);
        return componentExists(searcher, ids);
    }

    boolean componentExists(final User searcher, final List<String> ids)
    {
        for (String sid : ids)
        {
            Long id = convertToLong(sid);
            if (id != null)
            {
                Project project = getComponentsProject(id);
                if (project != null && permissionManager.hasPermission(Permissions.BROWSE, project, searcher))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Project getComponentsProject(Long componentId)
    {
        final ProjectComponent component;
        try
        {
            component = projectComponentManager.find(componentId);
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
        return projectManager.getProjectObj(component.getProjectId());
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