package com.atlassian.jira.dev.reference.plugin.services;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.services.DebugService;

/**
 * A reference debugging service.
 *
 * @since v4.3
 */
public class ReferenceDebugService extends DebugService
{

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("REFDEBUGSERVICE", "com/atlassian/jira/dev/reference/plugin/services/refdebugservice.xml", null);
    }
}
