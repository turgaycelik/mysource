package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.security.groups.GroupManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collection;
import java.util.Map;

public class WorkflowIsUserInGroupConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory
{
    private final GroupManager groupManager;

    public WorkflowIsUserInGroupConditionFactoryImpl(GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        Map groupMap = new ListOrderedMap();

        Collection<Group> crowdGroups = groupManager.getAllGroups();
        for (Group group : crowdGroups)
        {
            groupMap.put(group.getName(), group.getName());
        }

        velocityParams.put("groups", groupMap);
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

        velocityParams.put("group", conditionDescriptor.getArgs().get("group"));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        // Process The map
        String value = extractSingleParam(conditionParams, "group");
        return EasyMap.build("group", value);
    }
}
