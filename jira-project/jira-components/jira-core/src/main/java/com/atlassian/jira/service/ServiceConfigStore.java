package com.atlassian.jira.service;

import java.util.Collection;
import java.util.Map;

/**
 * A store interface for the ServiceConfigs
 */
public interface ServiceConfigStore
{
    /**
     * Adds a new Service of the given class with the the given configuration.
     *
     * @param serviceName The service name.
     * @param serviceClass The JiraService class that we wish to add as a service.
     * @param serviceDelay the service delay.
     *
     * @return JiraServiceContainer for this service.
     *
     * @throws ServiceException If there is any errors trying to add this Service.
     */
    public JiraServiceContainer addServiceConfig(String serviceName, Class<? extends JiraService> serviceClass, long serviceDelay)
            throws ServiceException;

    public void editServiceConfig(JiraServiceContainer config, long delay, Map<String, String[]> params) 
            throws ServiceException;

    public void removeServiceConfig(JiraServiceContainer config);

    public JiraServiceContainer getServiceConfigForId(Long id);

    public JiraServiceContainer getServiceConfigForName(String name);

    public Collection<JiraServiceContainer> getAllServiceConfigs();
}
