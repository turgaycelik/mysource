package com.atlassian.jira.jql.operand.registry;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.query.operand.FunctionOperand;

import java.util.List;

/**
 * Represents a JqlFunctionHandlerRegistry that can detect when the plugins system enables / disables new jql function
 * modules and updates the registry of function operand handlers accordingly.
 *
 * The current implementation is backed by a {@link LazyResettableJqlFunctionHandlerRegistry}.
 *
 * @since v4.4
 */
public class PluginsAwareJqlFunctionHandlerRegistry implements JqlFunctionHandlerRegistry, Startable
{
    private final LazyResettableJqlFunctionHandlerRegistry delegateRegistry;

    private final EventPublisher eventPublisher;


    public PluginsAwareJqlFunctionHandlerRegistry(final PluginAccessor pluginAccessor, final EventPublisher eventPublisher,
            final ModuleDescriptors.Orderings moduleDescriptorOrderings, final I18nHelper i18nHelper)
    {
        this.delegateRegistry = new LazyResettableJqlFunctionHandlerRegistry(pluginAccessor, moduleDescriptorOrderings, i18nHelper);
        this.eventPublisher = eventPublisher;
    }

    @PluginEventListener
    public void onPluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        if (event.getModule() instanceof JqlFunctionModuleDescriptor)
        {
            delegateRegistry.reset();
        }
    }

    @PluginEventListener
    public void onPluginModuleDisabled(final PluginModuleDisabledEvent event)
    {
        if (event.getModule() instanceof JqlFunctionModuleDescriptor)
        {
            delegateRegistry.reset();
        }
    }

    @Override
    public FunctionOperandHandler getOperandHandler(FunctionOperand operand)
    {
        return delegateRegistry.getOperandHandler(operand);
    }

    @Override
    public List<String> getAllFunctionNames()
    {
        return delegateRegistry.getAllFunctionNames();
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
        delegateRegistry.reset();
    }
}
