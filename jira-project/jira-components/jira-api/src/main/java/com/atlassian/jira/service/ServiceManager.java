/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraManager;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

@PublicApi
public interface ServiceManager extends JiraManager
{
    String SERVICE_ID_KEY = ServiceManager.class.getName() + ":serviceId";
    /**
     * This gets all currently registered services with JIRA. This is an unmodifiable Collections that is returned, modifications
     * to the services map will be made as a side-effect of calling the edit/refresh/add/remove methods of this manager.
     *
     * @return an unmodifiable Collection with JiraServiceContainers as the value
     */
    Collection<JiraServiceContainer> getServices();

    /**
     * This will add a service configuration to the JIRA DB and the service to the list of services which are running.
     *
     * <p>It is preferred to pass the actual Class rather than the class name when adding services to JIRA from plugins,
     * because it avoids possible ClassLoader issues.
     * See http://jira.atlassian.com/browse/JRA-18578.
     *
     * @param name  the key this service is to be known by
     * @param className the class defining the service
     * @param delay how often the service should run in milliseconds
     * @return the JiraServiceContainer that was just created
     *
     * @throws ServiceException If there is an error creating the Service.
     * @throws ClassNotFoundException If the className could not be resolved.
     *
     * @see #addService(String, Class, long)
     * @see #addService(String, String, long, java.util.Map)
     */
    JiraServiceContainer addService(String name, String className, long delay)
            throws ServiceException, ClassNotFoundException;

    /**
     * This will add a service configuration to the JIRA DB and the service to the list of services which are running.
     *
     * <p>It is preferred to pass the actual Class rather than the class name when adding services to JIRA from plugins,
     * because it avoids possible ClassLoader issues.
     * See http://jira.atlassian.com/browse/JRA-18578.
     *
     * @param name  the key this service is to be known by
     * @param serviceClass the class defining the service
     * @param delay how often the service should run in milliseconds
     * @return the JiraServiceContainer that was just created
     *
     * @throws ServiceException If there is an error creating the Service.
     *
     * @see #addService(String, String, long)
     * @see #addService(String, Class, long, java.util.Map)
     */
    JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay)
            throws ServiceException;

    /**
     * This will add a service configuration to the JIRA DB and the service to the list of services which are running with
     * an optional list of params.
     *
     * <p>It is preferred to pass the actual Class rather than the class name when adding services to JIRA from plugins,
     * because it avoids possible ClassLoader issues.
     * See http://jira.atlassian.com/browse/JRA-18578.
     *
     * @param name   the key this service is to be known by
     * @param className  the class defining the service
     * @param delay  how often the service should run in milliseconds
     * @param params Additional params to specify for the service
     * @return the JiraServiceContainer that was just created
     *
     * @throws ServiceException If there is an error creating the Service.
     * @throws ClassNotFoundException If the className could not be resolved.
     *
     * @see #addService(String, Class, long, java.util.Map)
     * @see #addService(String, String, long)
     */
    JiraServiceContainer addService(String name, String className, long delay, Map<String, String[]> params)
            throws ServiceException, ClassNotFoundException;

    /**
     * This will add a service configuration to the JIRA DB and the service to the list of services which are running with
     * an optional list of params.
     *
     * <p>It is preferred to pass the actual Class rather than the class name when adding services to JIRA from plugins,
     * because it avoids possible ClassLoader issues.
     * See http://jira.atlassian.com/browse/JRA-18578.
     *
     * @param name   the key this service is to be known by
     * @param serviceClass  the class defining the service
     * @param delay  how often the service should run in milliseconds
     * @param params Additional params to specify for the service
     * @return the JiraServiceContainer that was just created
     *
     * @throws ServiceException If there is an error creating the Service.
     *
     * @see #addService(String, String, long, java.util.Map) 
     * @see #addService(String, Class, long)
     */
    JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay, Map<String, String[]> params)
            throws ServiceException;

    /**
     * This will return a JiraServiceContainer for the provided id if one is registered otherwise it will return null.
     *
     * @param id service id
     * @return a JiraServiceContainer that is represented by the unique id.
     * @throws Exception if there is a problem looking up the value in the db.
     */
    @Nullable
    JiraServiceContainer getServiceWithId(Long id) throws Exception;

    /**
     * This will return a JiraServiceContainer for the provided name if one is registered, otherwise this will return
     * null.
     *
     * @param name the name of the JiraServiceContainer
     * @return a JiraServiceContainer if it is registered otherwise null
     * @throws Exception if there is a problem looking up the value in the db.
     */
    @Nullable
    JiraServiceContainer getServiceWithName(String name) throws Exception;

    /**
     * Quick way of telling whether a service with a specific id is registered or not.
     *
     * @param id service id
     * @return true if the service with the id is registered
     */
    boolean containsServiceWithId(Long id);

    /**
     * This will update the service if a service by this name can be found. The delay and the params will be
     * updated.
     *
     * @param name   the name of the service to find.
     * @param delay  the delay to set on the service in milliseconds
     * @param params the params to set on the service
     * @throws IllegalArgumentException if the name can not be resolved
     */
    void editServiceByName(String name, long delay, Map<String, String[]> params) throws Exception;

    /**
     * This will update the service if a service with this id can be found. The delay and the params will be
     * updated.
     *
     * @param id     the id of the service to find.
     * @param delay  the delay to set on the service in milliseconds
     * @param params the params to set on the service
     * @throws Exception if there is a problem updating the value
     */
    void editService(Long id, long delay, Map<String, String[]> params) throws Exception;

    /**
     * This will update the in-memory cache with the values from the db for the named service if it can be resolved.
     *
     * @param name the name of the service to find.
     * @throws IllegalArgumentException if the name can not be resolved
     */
    void refreshServiceByName(String name) throws Exception;

    /**
     * This will update the in-memory cache with the values from the db for the service with the id if it can be resolved.
     *
     * @param id the id of the service to find.
     * @throws Exception if the value can not be refreshed
     */
    void refreshService(Long id) throws Exception;

    /**
     * This will remove a service from the db and cache and it will try to resolve the service by name.
     *
     * @param name the name of the service to find.
     * @throws IllegalArgumentException if the name can not be resolved
     */
    void removeServiceByName(String name) throws Exception;

    /**
     * This will remove a service from the db and cache and it will try to resolve the service by id.
     *
     * @param id the id of the service to find.
     * @throws Exception if there is a problem removing the service
     */
    void removeService(Long id) throws Exception;

    /**
     * This will force a complete re-sync of the service cache with the values stored in the db.
     */
    void refreshAll();

    /**
     * Returns the service schedule skipper.
     *
     * @return service schedule skipper
     * @since v3.10
     * @deprecated v6.2 This is no longer used. Just call {@link #runNow}
     */
    ServiceScheduleSkipper getScheduleSkipper();

    /**
     * Gets the services that are ready for execution at a particular time.
     *
     * @param currentTime the time to check for due services.
     * @return an immutable collection of services.
     * @since v4.0
     * @deprecated since v6.2 This is going away, it was always only meant for internal use.
     */
    Iterable<JiraServiceContainer> getServicesForExecution(long currentTime);

    /**
     * Gets the services that can be managed by an specific user.
     * @param user The user in play.
     * @return A collection of services.
     */
    Iterable<JiraServiceContainer> getServicesManageableBy(User user);

    /**
     * Runs the service immediately. This call returns once the job is submitted. It does not wait
     * for the service to complete.
     * In a clustered environment the service may run on any node in the cluster.
     * @param serviceId
     */
    void runNow(long serviceId) throws Exception;

    /**
     * This interface provides capability for other processes to request a service run.
     *
     * @since v3.10
     * @deprecated v6.2 This is no longer used.
     */
    @PublicApi
    interface ServiceScheduleSkipper
    {
        /**
         * Adds a service with a given id to this skipper
         *
         * @param serviceId service id
         * @return true if service was not in this skipper already, false otherwise
         */
        boolean addService(Long serviceId);

        /**
         * Await this service being run.
         * @param serviceId Service ID
         * @throws InterruptedException if the current thread was interrupted while waiting
         */
        void awaitServiceRun(Long serviceId) throws InterruptedException;
    }

}
