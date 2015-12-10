package com.atlassian.jira.jql.operand.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.concurrent.ThreadSafe;

import com.atlassian.fugue.Option;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.util.concurrent.ResettableLazyReference;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Keeps a cache of function names against the operand handlers for those functions.
 *
 * @since v4.0
 */
@ThreadSafe
class LazyResettableJqlFunctionHandlerRegistry implements JqlFunctionHandlerRegistry
{
    private static final Logger log = Logger.getLogger(LazyResettableJqlFunctionHandlerRegistry.class);

    private final PluginAccessor pluginAccesor;

    private final ModuleDescriptors.Orderings moduleDescriptorOrderings;

    private final I18nHelper i18nHelper;

    @ClusterSafe
    private final ResettableLazyReference<Map<String, FunctionOperandHandler>> registryRef =
            new ResettableLazyReference<Map<String, FunctionOperandHandler>>()
    {
        @Override
        protected Map<String, FunctionOperandHandler> create() throws Exception
        {
            return loadFromJqlFunctionModuleDescriptors();
        }
    };

    LazyResettableJqlFunctionHandlerRegistry(final PluginAccessor pluginAccesor,
            final ModuleDescriptors.Orderings moduleDescriptorOrderings,
            final I18nHelper i18nHelper)
    {
        this.pluginAccesor = pluginAccesor;
        this.moduleDescriptorOrderings = moduleDescriptorOrderings;
        this.i18nHelper = i18nHelper;
    }

    Map<String, FunctionOperandHandler> loadFromJqlFunctionModuleDescriptors()
    {
        final List<JqlFunctionModuleDescriptor> jqlFunctionModuleDescriptors =
                pluginAccesor.getEnabledModuleDescriptorsByClass(JqlFunctionModuleDescriptor.class);

        final List<JqlFunctionModuleDescriptor> sortedJqlModuleDescriptors =
                moduleDescriptorOrderings.byOrigin().compound(moduleDescriptorOrderings.natural()).
                        sortedCopy(jqlFunctionModuleDescriptors);

        final ConcurrentMap<String, FunctionOperandHandler> registry = new ConcurrentHashMap<String, FunctionOperandHandler>();
        for (JqlFunctionModuleDescriptor descriptor : sortedJqlModuleDescriptors)
        {
            final JqlFunction jqlFunction = descriptor.getModule();

            final Option<String> functionNameFromPlugin = SafePluginPointAccess.call(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return jqlFunction.getFunctionName();
                }
            });
            if (functionNameFromPlugin.isEmpty()) // error in plugin code, pretend the jqlFunction was not found
            {
                continue;
            }
            final String functionName = CaseFolding.foldString(functionNameFromPlugin.get());

            final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction, i18nHelper);

            final OperandHandler handler = registry.putIfAbsent(functionName, operandHandler);
            if (handler != null) // The function has already been registered.
            {
                log.error(
                        String.format
                                (
                                        "Plugin '%s' defined a function with the name: '%s' but one with that"
                                                + " name already exists.", descriptor.getPlugin().getName(), functionName
                                )
                );
            }
        }
        return registry;
    }

    /**
     * Resets the cache. It will be populated again when
     * {@link #getOperandHandler(com.atlassian.query.operand.FunctionOperand)} or {@link #getAllFunctionNames()}
     * gets called.
     */
    void reset()
    {
        registryRef.reset();
    }

    public FunctionOperandHandler getOperandHandler(final FunctionOperand operand)
    {
        notNull("operand", operand);
        final Map<String, FunctionOperandHandler> registry = registryRef.get();
        final FunctionOperandHandler handler = registry.get(CaseFolding.foldString(operand.getName()));
        if (handler == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Unable to find handler for function '" + operand.getName() + "'.");
            }
        }
        return handler;
    }

    public List<String> getAllFunctionNames()
    {
        final Collection<FunctionOperandHandler> functionOperandHandlers = registryRef.get().values();
        final List<String> functionNames = new ArrayList<String>(functionOperandHandlers.size());
        for (final FunctionOperandHandler functionOperandHandler : functionOperandHandlers)
        {
            CollectionUtils.addIgnoreNull(functionNames, SafePluginPointAccess.call(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return functionOperandHandler.getJqlFunction().getFunctionName();
                }
            }).getOrNull());
        }
        Collections.sort(functionNames);
        return functionNames;
    }
}
