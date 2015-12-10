package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.bc.EntityNotFoundException;

import java.util.Collection;

public interface ProjectComponentStore
{

    /**
     * Implement this method to look up the project component by the given ID and return it. If not found, throw the
     * EntityNotFoundException. Do not return null!
     *
     * @param id project component ID
     * @return project component found by a given ID
     * @throws com.atlassian.jira.bc.EntityNotFoundException
     *          if the component not found
     */
    public MutableProjectComponent find(Long id) throws EntityNotFoundException;

    /**
     * Implement this method to look up all components that are related to the project with given ID.
     *
     * @param projectId project ID
     * @return a collection of ProjectComponent objects that are related to the project with given ID
     */
    public Collection findAllForProject(Long projectId);

    /**
     * Implement this method to look up the project ID for the given component ID. If project is not found throw
     * EntityNotFoundException, never return null!
     *
     * @param componentId component ID
     * @return project ID
     * @throws com.atlassian.jira.bc.EntityNotFoundException
     *          if component not found for the given component ID
     */
    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException;

    /**
     * Implement this method to persist the component. If component has no ID (null), insert it to the store, otherwise
     * perform an update operation on the store.
     *
     * @param component component to persist
     * @throws EntityNotFoundException in case of update if the component does not exist (maybe was deleted :-)
     */
    public MutableProjectComponent store(MutableProjectComponent component) throws EntityNotFoundException;

    /**
     * Remove the given component from the persistent storage.
     *
     * @param componentId ID of the component.
     * @throws EntityNotFoundException if component does not exist (maybe was removed previously).
     */
    public void delete(Long componentId) throws EntityNotFoundException;

    /**
     * Implement this method to check whether component with specified name is stored.
     *
     * @param name component name, null will cause IllegalArgumentException
     * @return true if new name is stored
     */
    public boolean containsName(String name, Long projectId);

    /**
     * Finds the ProjectComponent with the given name (case sensitive) in the project with the
     * given Id.
     * @param projectId the id of the component's project.
     * @param componentName the name of the component to find.
     * @return a MutableProjectComponent or null if none found.
     */
    public MutableProjectComponent findByComponentName(Long projectId, String componentName) throws EntityNotFoundException;

    /**
     * Finds the ProjectComponents with the given name (case insensitive) in all projects.
     *
     * @param componentName the name of the component to find.
     * @return All MutableProjectComponents with the given name or an empty list if none found.
     */
    public Collection<MutableProjectComponent> findByComponentNameCaseInSensitive(String componentName);

    /**
     * Retrieve a collection of ProjectComponents - where the lead of each component is
     * the specified user.
     *
     * @param userKey the lead user's key
     * @return collection of components - where the lead of each component is the specified user
     */
    public Collection findComponentsBylead(String userKey);

    /**
     * Retrieve all ProjectComponent objects stored.
     *
     * @return all ProjectComponent objects stored
     */
    public Collection findAll();
}
