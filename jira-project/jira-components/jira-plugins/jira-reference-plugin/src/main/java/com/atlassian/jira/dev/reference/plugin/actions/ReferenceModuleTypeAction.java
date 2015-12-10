package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.dev.reference.plugin.module.ReferenceModuleTypeModuleDescriptor;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simple web action that displays the result of calling a method on
 * {@link com.atlassian.jira.dev.reference.plugin.components.ReferenceComponent}.
 *
 * @since v4.3
 */
public class ReferenceModuleTypeAction extends JiraWebActionSupport
{
    private final PluginAccessor accessor;

    public ReferenceModuleTypeAction(PluginAccessor pluginAccessor)
    {
        this.accessor = notNull("pluginAccessor", pluginAccessor);
    }

    public List<String> getReferenceModules()
    {
        List<String> answer = new ArrayList<String>(CollectionUtil.transform(accessor.getEnabledModuleDescriptorsByClass(ReferenceModuleTypeModuleDescriptor.class),
                new Function<ReferenceModuleTypeModuleDescriptor, String>()
                {
                    public String get(ReferenceModuleTypeModuleDescriptor input)
                    {
                        return input.getModule();
                    }
                }));
        Collections.sort(answer);
        return answer;
    }
}
