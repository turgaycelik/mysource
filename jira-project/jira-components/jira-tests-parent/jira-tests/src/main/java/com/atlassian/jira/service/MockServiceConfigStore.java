package com.atlassian.jira.service;

import java.util.Collection;
import java.util.Map;

public class MockServiceConfigStore implements ServiceConfigStore
{
    public JiraServiceContainer addServiceConfig(final String name, final Class<? extends JiraService> clazz, final long delay)
    {
        throw new UnsupportedOperationException();
    }

    public void editServiceConfig(final JiraServiceContainer config, final long delay, final Map<String, String[]> params)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<JiraServiceContainer> getAllServiceConfigs()
    {
        throw new UnsupportedOperationException();
    }

    public JiraServiceContainer getServiceConfigForId(final Long id)
    {
        throw new UnsupportedOperationException();
    }

    public JiraServiceContainer getServiceConfigForName(final String name)
    {
        throw new UnsupportedOperationException();
    }

    public void removeServiceConfig(final JiraServiceContainer config)
    {
        throw new UnsupportedOperationException();
    }
}
