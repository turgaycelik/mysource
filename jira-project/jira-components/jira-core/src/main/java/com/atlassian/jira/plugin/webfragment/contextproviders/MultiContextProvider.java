package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper to enable you use multiple {@link ContextProvider}s to provide context to a {@link
 * com.atlassian.plugin.web.model.WebPanel} or {@link com.atlassian.plugin.web.model.WebLabel} or simalar web fragment.
 * Usage: <context-provider class="com.atlassian.jira.plugin.webfragment.contextproviders.MultiContextProvider"> <param
 * name="pluginKey">com.yourorg.jira.myplugin</param> <param name="ctxProvider-1">com.yourorg.jira.web.contextproviders.yourfirstcontextprovider</param>
 * <param name="ctxProvider-1:firstKey">A value for the 1st ContextProvider</param> <param
 * name="ctxProvider-2">com.yourorg.jira.web.contextproviders.yourfirstcontextprovider</param> <param
 * name="ctxProvider-2:anotherKey">A value for the 2nd ContextProvider</param> </context-provider>
 * <p/>
 * Component plugin providers are loaded within the context of the plugin having the key supplied in the "pluginKey"
 * parameter.
 *
 * @since v4.4
 */
public class MultiContextProvider implements ContextProvider
{
    private final List<ContextProvider> ctxProviders = new ArrayList<ContextProvider>();

    private final PluginAccessor pluginAccessor;

    private final WebFragmentHelper webFragmentHelper;

    public MultiContextProvider(final PluginAccessor pluginAccessor, final WebFragmentHelper webFragmentHelper)
    {
        this.pluginAccessor = pluginAccessor;
        this.webFragmentHelper = webFragmentHelper;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        final String pluginKey = params.get("pluginKey");
        if (pluginKey == null)
        {
            throw new PluginParseException("The pluginKey parameter must be specified.");
        }
        final Plugin plugin = pluginAccessor.getPlugin(pluginKey);
        if (plugin == null)
        {
            throw new PluginParseException("Could not locate plugin with key \"" + pluginKey + "\".");
        }
        int classIndex = 1;
        while (params.containsKey("ctxProvider-" + classIndex))
        {
            final String classKey = "ctxProvider-" + classIndex;
            final String className = params.get(classKey);
            final ContextProvider contextProvider;
            try
            {
                contextProvider = webFragmentHelper.loadContextProvider(className, plugin);
            }
            catch (final ConditionLoadingException exception)
            {
                throw new PluginParseException("Could not locate ContextProvider implementation named \"" + className + "\".", exception);
            }

            final MapBuilder<String, String> subParamBuilder = MapBuilder.newBuilder();

            final Set<Map.Entry<String, String>> allParams = params.entrySet();

            for (Map.Entry<String, String> paramEntry : allParams)
            {
                final String key = paramEntry.getKey();
                if (key.startsWith(classKey + ":"))
                {
                    subParamBuilder.add(key.substring(key.indexOf(":") + 1), paramEntry.getValue());
                }
            }
            contextProvider.init(subParamBuilder.toMap());
            ctxProviders.add(contextProvider);
            classIndex++;

        }
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        Map<String, Object> returnContext = MapBuilder.newBuilder(context).toMutableMap();

        for (ContextProvider provider : ctxProviders)
        {
            returnContext = CompositeMap.of(returnContext, provider.getContextMap(context));
        }
        return returnContext;
    }

    public List<ContextProvider> getCtxProviders()
    {
        return ctxProviders;
    }
}
