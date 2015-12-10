package com.atlassian.jira.plugin.workflow;

import java.util.Map;
import java.util.concurrent.Callable;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.module.ModuleFactory;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import static org.apache.commons.lang3.Validate.notNull;

public class WorkflowConditionModuleDescriptor extends AbstractWorkflowModuleDescriptor<WorkflowPluginConditionFactory>
{

    public WorkflowConditionModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            OSWorkflowConfigurator workflowConfigurator, final ComponentClassManager componentClassManager, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Override
    protected String getParameterName()
    {
        return "condition-class";
    }

    @Override
    public String getHtml(final String resourceName, final AbstractDescriptor descriptor)
    {
        if ((descriptor != null) && !(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor");
        }

        final ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;

        final WorkflowPluginConditionFactory workflowConditionFactory = getModule();
        return super.getHtml(resourceName, workflowConditionFactory.getVelocityParams(resourceName, conditionDescriptor));
    }

    @Override
    public boolean isOrderable()
    {
        return false;
    }

    @Override
    public boolean isUnique()
    {
        return false;
    }

    @Override
    public boolean isDeletable()
    {
        return true;
    }

    @Override
    public boolean isAddable(final String actionType)
    {
        return true;
    }

    @Override
    public PluginTypeResolver createPluginTypeResolver()
    {
        return new SafeConditionPluginTypeResolver();
    }

    static class SafeConditionPlugin implements Condition
    {
        private Condition condition;

        SafeConditionPlugin(Condition condition)
        {
            notNull(condition);
            this.condition = condition;
        }

        @Override
        public boolean passesCondition(final Map transientVars, final Map args, final PropertySet ps) throws WorkflowException
        {
            try
            {
                return condition.passesCondition(transientVars, args, ps);
            }
            catch(Throwable throwable)
            {
                SafePluginPointAccess.handleException(throwable);
                return false;
            }
        }
    }

    class SafeConditionPluginTypeResolver extends PluginTypeResolver
    {
        private final PluginTypeResolver pluginTypeResolver = new PluginTypeResolver();

        @Override
        protected Object loadObject(final String clazz)
        {
            return SafePluginPointAccess.call(new Callable<Condition>()
            {
                @Override
                public Condition call() throws Exception
                {
                    final Condition condition = (Condition) pluginTypeResolver.loadObject(clazz);
                    return new SafeConditionPlugin(condition);
                }
            }).getOrNull();
        }
    }
}
