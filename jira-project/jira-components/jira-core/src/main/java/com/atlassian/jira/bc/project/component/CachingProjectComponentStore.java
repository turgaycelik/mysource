package com.atlassian.jira.bc.project.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;

import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;

/**
 * Decorates an implementation of the project component delegateStore with caching. The actual delegateStore
 * implementation is delegated so this class is a Composite and also a Decorator.
 */
@EventComponent
public class CachingProjectComponentStore implements ProjectComponentStore, Startable
{
    private static final Logger log = Logger.getLogger(CachingProjectComponentStore.class);
    private static final String UPDATE_LOCK_NAME = CachingProjectComponentStore.class.getName() + ".updateLock";

    private ClusterLock updateLock;

    /**
     * component ID -> component. (Map&lt;Long, MutableProjectComponent&gt;)
     */
    private final Cache<Long, CacheObject<ProjectComponent>> componentIdToComponentMap;

    /**
     * project ID -> list of components. (Map&lt;Long, List&lt;MutableProjectComponent&gt;&gt;)
     */
    private final Cache<Long, Collection<ProjectComponent>> projectIdToComponentsMap;

    /**
     * backing delegateStore
     */
    private final ProjectComponentStore delegateStore;
    private final ClusterLockService clusterLockService;

    /**
     * Creates a new instance of this class backed by given delegateStore.
     * Initialises the cache with the data in the persistence store.
     *
     * @param delegateStore underlying persistence store
     * @param clusterLockService
     *
     */
    public CachingProjectComponentStore(final ProjectComponentStore delegateStore, final CacheManager cacheManager, final ClusterLockService clusterLockService)
    {
        this.delegateStore = delegateStore;
        this.clusterLockService = clusterLockService;
        componentIdToComponentMap = cacheManager.getCache(CachingProjectComponentStore.class.getName() + ".componentIdToComponentMap",
                new ComponentByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        projectIdToComponentsMap = cacheManager.getCache(CachingProjectComponentStore.class.getName() + ".projectIdToComponentsMap",
                new ComponentByProjectCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        projectIdToComponentsMap.removeAll();
        componentIdToComponentMap.removeAll();
    }

    @Override
    public void start()
    {
        updateLock = clusterLockService.getLockForName(UPDATE_LOCK_NAME);
    }
    /**
     * Looks up the project component by the given ID and returns it. If not found, throws the EntityNotFoundException,
     * it never returns null.
     *
     * @param id project component ID
     * @return project component found by a given ID
     * @throws EntityNotFoundException if the component not found
     */
    public MutableProjectComponent find(Long id) throws EntityNotFoundException
    {
        Assertions.notNull("id", id);

        ProjectComponent component = componentIdToComponentMap.get(id).getValue();
        if (component == null)
        {
            throw new EntityNotFoundException("The component with id '" + id + "' does not exist.");
        }
        return MutableProjectComponent.copy(component);
    }

    /**
     * Looks up all components that are related to the project with given ID.
     *
     * @param projectId project ID
     * @return a collection of ProjectComponent objects that are related to the project with given ID
     */
    public Collection<MutableProjectComponent> findAllForProject(Long projectId)
    {
        Collection<ProjectComponent> components = projectIdToComponentsMap.get(projectId);
        return MutableProjectComponent.copy(components);
    }

    /**
     * Looks up the component with the given name in the project with the given id.
     *
     * @param projectId id of the project.
     * @param componentName name of the component.
     * @return the component.
     * @throws EntityNotFoundException if no such component can be found.
     */
    public MutableProjectComponent findByComponentName(Long projectId, String componentName)
            throws EntityNotFoundException
    {
        Collection<MutableProjectComponent> components = findAllForProject(projectId);
        for (MutableProjectComponent c : components)
        {
            if (c.getName().equals(componentName))
            {
                return c;
            }
        }
        throw new EntityNotFoundException("The project with id '" + projectId + "' is not associated with a component with the name '" + componentName + "'.");
    }

    /**
     * Finds one or more ProjectComponent with a given name.
     *
     * @param componentName the name of the component to find.
     * @return a Collection of Components with the given name.
     * @throws EntityNotFoundException
     */
    public Collection<MutableProjectComponent> findByComponentNameCaseInSensitive(String componentName)
    {
        Collection<MutableProjectComponent> components = findAll();
        Collection<MutableProjectComponent> matchingComponents = new ArrayList<MutableProjectComponent>();

        for (final MutableProjectComponent component : components)
        {
            if (component.getName().equalsIgnoreCase(componentName))
            {
                matchingComponents.add(component);
            }
        }
        return matchingComponents;
    }

    /**
     * Looks up the project ID for the given component ID. If project is not found, throws EntityNotFoundException.
     *
     * @param componentId component ID
     * @return project ID
     * @throws EntityNotFoundException if project not found for the given component ID
     */
    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException
    {
        ProjectComponent component = componentIdToComponentMap.get(componentId).getValue();
        if (component == null)
        {
            throw new EntityNotFoundException("The component with the id '" + componentId + "' does not exist.");
        }
        return component.getProjectId();
    }

    /**
     * Checks whether component with specified name is stored.
     *
     * @param name component name, null will cause IllegalArgumentException
     * @return true if new name is stored
     * @throws IllegalArgumentException if name or projectId is null
     */
    public boolean containsName(String name, Long projectId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("Component project ID can not be null!");
        }
        if (name == null)
        {
            throw new IllegalArgumentException("Component name can not be null!");
        }
        Collection<ProjectComponent> components = projectIdToComponentsMap.get(projectId);
        return containsNameIgnoreCase(components, name);
    }

    static boolean containsNameIgnoreCase(Collection<ProjectComponent> components, String name)
    {
        boolean containsName = false;
        if (components != null)
        {
            for (ProjectComponent component : components)
            {
                String componentName = component.getName();
                if (name.equalsIgnoreCase(componentName))
                {
                    containsName = true;
                }
            }
        }
        return containsName;
    }

    /**
     * Persist the component. If component has no ID (null) it is inserted to the database and added to the cache,
     * otherwise an update operation is performed on both cache and database.
     *
     * @param component component to persist
     * @throws EntityNotFoundException in case of update if the component does not exist (maybe was deleted :-)
     * @throws com.atlassian.jira.exception.DataAccessException if cannot persist the component
     */
    public MutableProjectComponent store(MutableProjectComponent component)
            throws EntityNotFoundException, DataAccessException
    {
        MutableProjectComponent copy = MutableProjectComponent.copy(component);
        MutableProjectComponent newComponent;
        if (copy.getId() == null)
        {
            newComponent = insert(copy);
        }
        else
        {
            newComponent = update(copy);
        }
        return newComponent;
    }

    /**
     * Removes the component from the persistent storage and a cache.
     *
     * @param componentId the id of the component to delete
     * @throws EntityNotFoundException if component does not exist (maybe was removed previously :-)
     */
    public void delete(Long componentId) throws EntityNotFoundException
    {
        ProjectComponent component = null;
        if (componentId != null)
        {
            component = delegateStore.find(componentId);
        }
        delegateStore.delete(componentId);
        if (componentId != null)
        {
            componentIdToComponentMap.remove(componentId);
        }
        if (component != null)
        {
            projectIdToComponentsMap.remove(component.getProjectId());
        }
    }

    /**
     * Retrieves all ProjectComponents that have the given user as their lead.
     * Not synchronised, because findAll() returns a private copy of all components.
     *
     * @param userKey key of the lead user
     * @return possibly empty Collection of ProjectComponents.
     */
    public Collection findComponentsBylead(String userKey)
    {
        Collection<ProjectComponent> leadComponents = new ArrayList<ProjectComponent>();
        Collection components = findAll();
        for (final Object component : components)
        {
            MutableProjectComponent projectComponent = (MutableProjectComponent) component;
            if (projectComponent != null && TextUtils.stringSet(projectComponent.getLead()) && projectComponent.getLead().equals(userKey))
            {
                leadComponents.add(projectComponent);
            }

        }
        return leadComponents;
    }

    /**
     * Retrieve all ProjectComponent objects stored.
     *
     * @return all ProjectComponent objects stored
     */
    public Collection findAll()
    {
        // Can't inject the component manager because of circular dependency.
        final ProjectManager projectManager = ComponentAccessor.getProjectManager();
        List<ProjectComponent> components = new ArrayList<ProjectComponent>();
        List<Project> allProjects = projectManager.getProjectObjects();
        for (Project project : allProjects)
        {
            components.addAll(projectIdToComponentsMap.get(project.getId()));
        }
        return components;
    }

    // helper methods

    /**
     * Add the specified component to the persistent storage and the cache. The returned component has ID set -
     * indicating that it has been persisted.
     *
     * @param component the component to stored
     * @return the updated component (with ID set)
     */
    private MutableProjectComponent insert(MutableProjectComponent component)
    {
        updateLock.lock();
        try
        {
            final String name = component.getName();
            if (containsName(name, component.getProjectId()))
            {
                throw createIllegalArgumentExceptionForName(name);
            }

            try
            {
                component = delegateStore.store(component);
                return component;
            }
            catch (EntityNotFoundException e)
            {
                // This exception should never be thrown - insertion should always complete successfully
                return null;
            }
            finally
            {
                componentIdToComponentMap.remove(component.getId());
                projectIdToComponentsMap.remove(component.getProjectId());
            }
        }
        finally
        {
            updateLock.unlock();
        }
    }

    /**
     * Retrieve the component with the ID of the component specified and update it with the new values of the given
     * component.
     *
     * @param component component with new values
     * @return updated component
     * @throws EntityNotFoundException if component with ID does not exist
     * @throws com.atlassian.jira.exception.DataAccessException if cannot persist the component
     * @throws IllegalArgumentException if duplicate name
     */
    private MutableProjectComponent update(MutableProjectComponent component)
            throws EntityNotFoundException, DataAccessException
    {
        updateLock.lock();
        try
        {
            MutableProjectComponent old = find(component.getId());
            if (!old.equalsName(component))
            {
                if (containsName(component.getName(), component.getProjectId()))
                {
                    throw new IllegalArgumentException("New component name '" + component.getName() + "' is not unique!");
                }
            }
            delegateStore.store(component);
            updateCache(component);
            return component;
        }
        finally
        {
            updateLock.unlock();
        }
    }

    /**
     * Invalidate the caches for the given component/project.
     *
     * @param component component to be updated in cache
     */
    private void updateCache(ProjectComponent component)
    {
        Long id = component.getId();
        Long projectId = component.getProjectId();
        componentIdToComponentMap.remove(id);
        projectIdToComponentsMap.remove(projectId);
    }

    private IllegalArgumentException createIllegalArgumentExceptionForName(String name)
    {
        return new IllegalArgumentException("Component name = '" + name + "' is not unique");
    }

    /**
     * Sorts the List of {@link MutableProjectComponent} in projectIdToComponentsMap using the
     * ProjectComponentComparator.
     * When returning components for a project the components have to be sorted, due to compatiblity with the
     * deprecated ProjectManager.
     *
     */
    private Collection<ProjectComponent> sortByComponentNames(Collection<ProjectComponent> components)
    {
        ArrayList<ProjectComponent> componentList = new ArrayList<ProjectComponent>(components);
        Collections.sort((List) componentList, ProjectComponentComparator.INSTANCE);
        return componentList;
    }

    private class ComponentByIdCacheLoader implements CacheLoader<Long, CacheObject<ProjectComponent>>
    {
        @Override
        public CacheObject<ProjectComponent> load(@Nonnull final Long id)
        {
            try
            {
                return new CacheObject<ProjectComponent>(CachingProjectComponentStore.this.delegateStore.find(id));
            }
            catch (EntityNotFoundException e)
            {
                return CacheObject.NULL();
            }
        }
    }

    private class ComponentByProjectCacheLoader implements CacheLoader<Long, Collection<ProjectComponent>>
    {
        @Override
        public Collection<ProjectComponent> load(@Nonnull final Long projectId)
        {
            return sortByComponentNames(CachingProjectComponentStore.this.delegateStore.findAllForProject(projectId));
        }
    }
}
