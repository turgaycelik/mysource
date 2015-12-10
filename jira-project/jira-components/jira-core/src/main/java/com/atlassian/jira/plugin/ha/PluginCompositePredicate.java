package com.atlassian.jira.plugin.ha;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Plugin events such as PluginEnable wil also cause lots of PluginModule events - to prevent raising these twice this
 * predicate allows us to exclude these module events
 *
 * @since v6.1
 */
public class PluginCompositePredicate implements Predicate<PluginOperation>
{
    private Multimap<String, PluginEventType> exclusions = ArrayListMultimap.create();

    private PluginCompositePredicate() {}

    ;

    @Override
    public boolean apply(@Nullable final PluginOperation input)
    {
        boolean excluded = exclusions.containsEntry(input.getPluginKey(), input.getPluginEventType());
        return !excluded;
    }

    private void addExclusions(final Map<String, List<PluginEventType>> excludedTypes)
    {
        for (String pluginKey : excludedTypes.keySet())
        {
            exclusions.putAll(pluginKey, excludedTypes.get(pluginKey));
        }
    }

    public static class Builder
    {
        private PluginCompositePredicate pluginCompositePredicate;

        public Builder()
        {
            pluginCompositePredicate = new PluginCompositePredicate();
        }

        public Builder addExclusions(final Map<String, List<PluginEventType>> exclusions)
        {
            pluginCompositePredicate.addExclusions(exclusions);
            return this;
        }

        public PluginCompositePredicate build()
        {
            return pluginCompositePredicate;
        }
    }
}
