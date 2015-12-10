package com.atlassian.jira.plugin;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;

import java.util.Map;

/**
 * ModuleDescriptors that can be configured using an {@link ObjectConfiguration}.
 *
 * @since v5.0
 */
public interface ConfigurableModuleDescriptor
{

    ObjectConfiguration getObjectConfiguration(Map params) throws ObjectConfigurationException;
}
