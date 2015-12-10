package com.atlassian.jira.web.bean;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.plugin.PluginAccessor;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import org.apache.log4j.Logger;

public class WorkflowConditionFormatBean extends WorkflowDescriptorFormatBean
{
    private static final Logger log = Logger.getLogger(WorkflowConditionFormatBean.class);

    private ConditionsDescriptor conditionsDescriptor;
    private String parentPrefix;

    public WorkflowConditionFormatBean(PluginAccessor pluginAccessor)
    {
        super(pluginAccessor);
    }

    public WorkflowConditionFormatBean()
    {
        this(ComponentAccessor.getPluginAccessor());
    }

    public void setDescriptor(ConditionsDescriptor conditionsDescriptor)
    {
        this.conditionsDescriptor = conditionsDescriptor;
        setDescriptorCollection(conditionsDescriptor.getConditions());
    }

    public boolean isNestedDescriptor(AbstractDescriptor descriptor)
    {
        return (descriptor instanceof ConditionsDescriptor);
    }

    public String getOperatorType()
    {
        return conditionsDescriptor.getType();
    }

    public String getOperatorTextKey()
    {
        if (WorkflowEditorTransitionConditionUtil.OPERATOR_AND.equalsIgnoreCase(conditionsDescriptor.getType()))
            return "admin.workflowtransition.operator.and";
        else
            return "admin.workflowtransition.operator.or";
    }

    public String getOtherOperatorTextKey()
    {
        if (WorkflowEditorTransitionConditionUtil.OPERATOR_AND.equalsIgnoreCase(conditionsDescriptor.getType()))
            return "admin.workflowtransition.operator.or";
        else
            return "admin.workflowtransition.operator.and";
    }

    public boolean isAllowNested()
    {
        return isMultipleDescriptors();
    }

    public String getParentPrefix()
    {
        if (TextUtils.stringSet(parentPrefix))
        {
            return parentPrefix + WorkflowEditorTransitionConditionUtil.SEPARATOR;
        }
        else
        {
            return "";
        }
    }

    public void setParentPrefix(String parentPrefix)
    {
        this.parentPrefix = parentPrefix;
    }
}
