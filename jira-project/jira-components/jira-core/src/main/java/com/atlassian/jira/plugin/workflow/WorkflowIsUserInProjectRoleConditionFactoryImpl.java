package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.workflow.condition.InProjectRoleCondition;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collection;
import java.util.Map;

public class WorkflowIsUserInProjectRoleConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory
{
    protected void getVelocityParamsForInput(Map velocityParams)
    {
        Map projectRoleMap = new ListOrderedMap();

        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
        final Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles();
        for (final ProjectRole projectRole : projectRoles)
        {
            projectRoleMap.put(projectRole.getId().toString(), projectRole.getName());
        }
        velocityParams.put("key", InProjectRoleCondition.KEY_PROJECT_ROLE_ID);
        velocityParams.put("projectroles", projectRoleMap);
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
        String id = (String) conditionDescriptor.getArgs().get(InProjectRoleCondition.KEY_PROJECT_ROLE_ID);
        ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(id));

        //Put the project role id into the velocity context
        velocityParams.put("projectrole", id);
        //If the project role exists, put it in the velocity context - if its missing the display logic should handle it
        if (projectRole != null)
        {
            velocityParams.put("projectrolename", projectRole.getName());
        }
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        // Process The map
        String value = extractSingleParam(conditionParams, InProjectRoleCondition.KEY_PROJECT_ROLE_ID);
        return EasyMap.build(InProjectRoleCondition.KEY_PROJECT_ROLE_ID, value);
    }
}
