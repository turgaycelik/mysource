package com.atlassian.jira.web.bean;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.web.action.admin.workflow.AbstractWorkflowAction;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WorkflowDescriptorFormatBean
{
    private static final Logger log = Logger.getLogger(WorkflowDescriptorFormatBean.class);

    private Collection descriptorCollection;
    private String deleteAction;
    private String editAction;
    private boolean delete;
    private boolean orderable;
    private boolean edit;
    private final PluginAccessor pluginAccessor;
    private String pluginType;
    private String operatorTextKey;

    public WorkflowDescriptorFormatBean(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
        this.descriptorCollection = Collections.EMPTY_LIST;
        this.delete = false;
        this.edit = false;
    }

    public WorkflowDescriptorFormatBean()
    {
        this(ComponentAccessor.getPluginAccessor());
    }

    public WorkflowDescriptorInfo formatDescriptor(AbstractDescriptor descriptor)
    {
        final String type;
        final Map args;
        if (descriptor instanceof FunctionDescriptor)
        {
            type = ((FunctionDescriptor) descriptor).getType();
            args = ((FunctionDescriptor) descriptor).getArgs();
        }
        else if (descriptor instanceof ConditionDescriptor)
        {
            type = ((ConditionDescriptor) descriptor).getType();
            args = ((ConditionDescriptor) descriptor).getArgs();
        }
        else if (descriptor instanceof ValidatorDescriptor)
        {
            type = ((ValidatorDescriptor) descriptor).getType();
            args = ((ValidatorDescriptor) descriptor).getArgs();
        }
        else
        {
            throw new IllegalArgumentException("Invalid descriptor type");
        }

        if ("class".equalsIgnoreCase(type) && args.containsKey("class.name"))
        {
            final String className = (String) args.get("class.name");
            final String moduleKey = (String) args.get("full.module.key");
            try
            {
                final AbstractWorkflowModuleDescriptor abstractWorkflowModuleDescriptor  = getWorkflowModuleDescriptor(className, moduleKey, pluginType);

                if (abstractWorkflowModuleDescriptor != null)
                {
                    // We have a plugin module that can generate description HTML for us - so use it
                    String description = abstractWorkflowModuleDescriptor.getHtml(JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW, descriptor);
                    return new WorkflowDescriptorInfo(description, orderable && abstractWorkflowModuleDescriptor.isOrderable(), delete && abstractWorkflowModuleDescriptor.isDeletable(), edit && abstractWorkflowModuleDescriptor.isEditable());
                }
                else
                {
                    // If we do not have a descriptor for the module we cannot edit it
                    return new WorkflowDescriptorInfo(null, orderable, delete, false);
                }

            }
            catch (PluginParseException e)
            {
                final String message = "Cannot find module descriptors.";
                log.error(message, e);
                throw new RuntimeException(message, e);
            }

        }

        // If we do not have a descriptor for the module we cannot edit it
        return new WorkflowDescriptorInfo("Type: " + type + " with arguments " + args, orderable, delete, false);
    }

    protected AbstractWorkflowModuleDescriptor getWorkflowModuleDescriptor(final String className, final String moduleKey, final String pluginType)
            throws PluginParseException
    {
        final Collection moduleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByType(pluginType);

        for (final Object moduleDescriptor : moduleDescriptors)
        {
            AbstractWorkflowModuleDescriptor abstractWorkflowModuleDescriptor = (AbstractWorkflowModuleDescriptor) moduleDescriptor;

            if (moduleKey == null)
            {
                if (abstractWorkflowModuleDescriptor.getImplementationClass().getName().equals(className))
                {
                    return abstractWorkflowModuleDescriptor;
                }
            }
            else
            {
                if (AbstractWorkflowAction.getFullModuleKey(abstractWorkflowModuleDescriptor.getPluginKey(), abstractWorkflowModuleDescriptor.getKey()).equals(moduleKey) &&
                        abstractWorkflowModuleDescriptor.getImplementationClass().getName().equals(className))
                {
                    return abstractWorkflowModuleDescriptor;
                }
            }
        }

        return null;
    }

    public Collection getDescriptorCollection()
    {
        return descriptorCollection;
    }

    public void setDescriptorCollection(Collection descriptorCollection)
    {
        this.descriptorCollection = descriptorCollection;
    }

    public boolean isDelete()
    {
        return delete;
    }

    public void setDelete(boolean delete)
    {
        this.delete = delete;
    }

    public boolean isOrderable()
    {
        return orderable;
    }

    public void setOrderable(boolean orderable)
    {
        this.orderable = orderable;
    }

    public boolean isEdit()
    {
        return edit;
    }

    public void setEdit(boolean edit)
    {
        this.edit = edit;
    }

    public String getDeleteAction()
    {
        return deleteAction;
    }

    public void setDeleteAction(String deleteAction)
    {
        this.deleteAction = deleteAction;
    }

    public String getEditAction()
    {
        return editAction;
    }

    public void setEditAction(String editAction)
    {
        this.editAction = editAction;
    }

    public String getPluginType()
    {
        return pluginType;
    }

    public void setPluginType(String pluginType)
    {
        this.pluginType = pluginType;
    }

    public boolean isHasRelevantArgs(Map args)
    {
        // Is args is null, empty or only contains 'class.name' parameter, we should not display
        // anything
        return !(args == null || args.isEmpty() || (args.size() == 1 && args.containsKey("class.name")));

    }

    public String getOperatorTextKey()
    {
       return operatorTextKey;
    }

    public void setOperatorTextKey(String operatorTextKey)
    {
        this.operatorTextKey = operatorTextKey;
    }

    public String getParentPrefix()
    {
        return "";

    }

    public void setParentPrefix(String parentPrefix)
    {
        // Do nothing;
    }

    public boolean isAllowNested()
    {
        return false;
    }

    /**
     * Determines if the condition is the 'current' one, and should be highlighted. For example, when a condition is
     * added to a Conditions block it is highlighted.
     * <p>
     * This method is used from JSPs.
     * </p>
     */
    public boolean isHighlighted(int count, String current)
    {
        // Ensure that we take the plugin type into account as
        return (getPluginType() + getParentPrefix() + count).equals(current);
    }

    /**
     * Determines if the {@link #descriptorCollection} has only one element in it.
     * <p>
     * This method is used from JSPs.
     * </p>
     */
    public boolean isMultipleDescriptors()
    {
        return descriptorCollection != null && (descriptorCollection.size() > 1);
    }

    /**
     * Determines if the {@link #descriptorCollection} has more than one element in it.
     * <p>
     * This method is used from JSPs.
     * </p>
     */
    public boolean isSingleDescriptor()
    {
        return descriptorCollection != null && (descriptorCollection.size() == 1);
    }
}
