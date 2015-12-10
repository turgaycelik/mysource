package com.atlassian.jira.plugin;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ClassPrefixModuleFactory;
import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
import com.atlassian.plugin.module.PrefixModuleFactory;
import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;

/**
 * atlassian-plugins 2.5 requires us to implement a module factory.
 * @see https://extranet.atlassian.com/display/DEV/Atlassian+Plugins+2.5+Migration+Guide#AtlassianPlugins2.5MigrationGuide-Step3DeclarenewHostComponent'ModuleFactory'
 * @since v4.2
 */
public class JiraModuleFactory extends PrefixDelegatingModuleFactory
{
    public JiraModuleFactory(final HostContainer jiraHostContainer)
    {
        super(CollectionBuilder.<PrefixModuleFactory>newBuilder()
                .add(new ClassPrefixModuleFactory(jiraHostContainer))
                .add(new BeanPrefixModuleFactory())
                .asSet());
    }
}
