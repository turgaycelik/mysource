package com.atlassian.jira.plugin.customfield;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

public class CustomFieldSearcherModuleDescriptorsImpl implements CustomFieldSearcherModuleDescriptors
{
    private static final Logger log = Logger.getLogger(CustomFieldSearcherModuleDescriptorsImpl.class);

    private PluginAccessor pluginAccessor;

    public CustomFieldSearcherModuleDescriptorsImpl(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Option<CustomFieldSearcher> getCustomFieldSearcher(final String completeModuleKey)
    {
        if (!ObjectUtils.isValueSelected(completeModuleKey))
        {
            return Option.none();
        }

        final ModuleDescriptor module = pluginAccessor.getEnabledPluginModule(completeModuleKey);

        if (module instanceof CustomFieldSearcherModuleDescriptor)
        {
            CustomFieldSearcher searcher = ((CustomFieldSearcherModuleDescriptor) module).getModule();
            return Option.some(searcher);
        }
        else
        {
            log.warn("Custom field searcher module: " + completeModuleKey + " is invalid. Null being returned.");
            return Option.none();
        }
    }
}
