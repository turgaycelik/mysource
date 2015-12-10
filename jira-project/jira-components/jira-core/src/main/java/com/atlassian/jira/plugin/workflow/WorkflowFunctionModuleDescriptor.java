package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Collection;
import java.util.HashSet;

public class WorkflowFunctionModuleDescriptor extends AbstractWorkflowModuleDescriptor<WorkflowPluginFunctionFactory>
{
    private boolean orderable = true;
    private boolean unique = false;
    private boolean deletable = true;
    private Collection<String> addableActionTypes = JiraWorkflow.ACTION_TYPE_ALL;
    private Integer weight = null;
    private boolean isDefault = false;

    public static final String ACTION_TYPE_NON_INITIAL = "non-initial";

    public WorkflowFunctionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager,
            final ModuleFactory moduleFactory)
    {
        super(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        String paramValue = getParamValue(element, "orderable");
        if (paramValue != null)
        {
            orderable = Boolean.valueOf(paramValue);
        }

        paramValue = getParamValue(element, "unique");
        if (paramValue != null)
        {
            unique = Boolean.valueOf(paramValue);
        }

        paramValue = getParamValue(element, "deletable");
        if (paramValue != null)
        {
            deletable = Boolean.valueOf(paramValue);
        }

        paramValue = getParamValue(element, "addable");
        if (paramValue != null)
        {
            final String[] addableParams = StringUtils.split(paramValue, ",");
            addableActionTypes = new HashSet<String>();
            for (final String addableParam : addableParams)
            {
                if (ACTION_TYPE_NON_INITIAL.equals(addableParam))
                {
                    addableActionTypes.add(JiraWorkflow.ACTION_TYPE_GLOBAL);
                    addableActionTypes.add(JiraWorkflow.ACTION_TYPE_COMMON);
                    addableActionTypes.add(JiraWorkflow.ACTION_TYPE_ORDINARY);
                }
                else if (JiraWorkflow.ACTION_TYPE_ALL.contains(addableParam))
                {
                    addableActionTypes.add(addableParam);
                }
                else
                {
                    throw new PluginParseException("Invalid addable parameter '" + addableParam + "'.");
                }
            }
        }

        paramValue = getParamValue(element, "weight");
        if (paramValue != null)
        {
            if (unique)
            {
                weight = Integer.valueOf(paramValue);
            }
            else
            {
                throw new PluginParseException("Function descriptors with 'weight' must be 'unique'.");
            }
        }

        paramValue = getParamValue(element, "default");
        if (paramValue != null)
        {
            isDefault = Boolean.valueOf(paramValue);
        }
    }

    @Override
    protected String getParameterName()
    {
        return "function-class";
    }

    @Override
    public String getHtml(final String resourceName, final AbstractDescriptor descriptor)
    {
        if ((descriptor != null) && !(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor");
        }

        final FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        final WorkflowPluginFunctionFactory workflowPluginFunctionFactory = getModule();
        return super.getHtml(resourceName, workflowPluginFunctionFactory.getVelocityParams(resourceName, functionDescriptor));
    }

    @Override
    public boolean isOrderable()
    {
        return orderable;
    }

    @Override
    public boolean isUnique()
    {
        return unique;
    }

    @Override
    public boolean isDeletable()
    {
        return deletable;
    }

    @Override
    public boolean isAddable(final String actionType)
    {
        return addableActionTypes.contains(actionType);
    }

    public Integer getWeight()
    {
        return weight;
    }

    public boolean isDefault()
    {
        return isDefault;
    }
}
