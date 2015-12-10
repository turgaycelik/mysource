package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Factory for editing plugins that require a group selector custom field, eg.
 * {@link com.atlassian.jira.workflow.condition.InGroupCFCondition}
 */
public class WorkflowIsUserInGroupCFConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory
{
    private GroupSelectorUtils groupSelectorUtils;

    public WorkflowIsUserInGroupCFConditionFactoryImpl(GroupSelectorUtils groupSelectorUtils)
    {
        this.groupSelectorUtils = groupSelectorUtils;
    }
    protected void getVelocityParamsForInput(Map velocityParams)
    {
        List fields = groupSelectorUtils.getCustomFieldsSpecifyingGroups();
        Map groupcf = new HashMap(fields.size());
        Iterator iter = fields.iterator();
        while (iter.hasNext())
        {
            CustomField customField = (CustomField) iter.next();
            groupcf.put(customField.getId(), customField.getName());

        }
        velocityParams.put("groupcfs", groupcf);
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

        FieldManager fieldManager = ComponentAccessor.getFieldManager();
        String fieldId = (String) conditionDescriptor.getArgs().get("groupcf");
        Field field = fieldManager.getField(fieldId);
        velocityParams.put("groupcf", (field == null ? fieldId : field.getName()));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        // Process The map
        String value = extractSingleParam(conditionParams, "groupcf");
        return EasyMap.build("groupcf", value);
    }
}
