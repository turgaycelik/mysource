package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ofbiz non-caching implementation of {@link ProjectComponentStore}. {@see CachingProjectComponentStore}
 */
public class OfBizProjectComponentStore implements ProjectComponentStore
{
    public static final Logger log = Logger.getLogger(ProjectComponentStore.class);

    private final OfBizDelegator delegator;
    static final String FIELD_ID = "id";
    static final String FIELD_NAME = "name";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_LEAD = "lead";
    static final String FIELD_PROJECT = "project";
    static final String FIELD_ASSIGNEE_TYPE = "assigneetype";

    /**
     * Create a new instance of the class backed by the delegator specified.
     *
     * @param delegator persistent store manager
     */
    public OfBizProjectComponentStore(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    /**
     * Retrieve the MutableProjectComponent with the specified ID. An EntityNotFoundException is thrown if the component
     * is not found.
     *
     * @param id component ID to search for
     * @return MutableProjectComponent component with the specified ID
     * @throws EntityNotFoundException if the component is not found
     */
    public MutableProjectComponent find(Long id) throws EntityNotFoundException
    {
        validateId(id);
        GenericValue componentGV = findComponentGV(FIELD_ID, id);
        if (componentGV == null)
        {
            throw new EntityNotFoundException("Component with ID = '" + id + "' does not exist.");
        }
        else
        {
            return getComponentConverter().convertToComponent(componentGV);
        }
    }

    /**
     * Retrieve all components that are related to the project with given ID and sort by name.
     *
     * @param projectId project ID
     * @return a collection of ProjectComponent objects that are related to the project with given ID
     */
    public Collection<MutableProjectComponent> findAllForProject(Long projectId)
    {
        return getComponentConverter().convertToComponents(findComponentGVs(FIELD_PROJECT, projectId,FIELD_NAME));
    }

    protected ComponentConverter getComponentConverter()
    {
        return new ComponentConverter();
    }

    public MutableProjectComponent findByComponentName(Long projectId, String componentName)
            throws IllegalArgumentException
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("Project ID is required, was null.");
        }
        if (componentName == null)
        {
            throw new IllegalArgumentException("Component Name is required, was null.");
        }

        List components = delegator.findByAnd(OfBizDelegator.PROJECT_COMPONENT, MapBuilder.<String, Object>newBuilder().add(FIELD_PROJECT, projectId).add(FIELD_NAME, componentName).toMap());
        // should only be one or none since component name is unique per project
        if (components.size() == 1)
        {
            return getComponentConverter().convertToComponent((GenericValue) components.get(0));
        }
        else if (components.size() == 0)
        {
            return null;
        }
        else
        {
            log.error("found " + components.size() + " components with name " + componentName + " in project with id " + projectId);
            return (MutableProjectComponent) components.get(0);
        }
    }

    public Collection<MutableProjectComponent> findByComponentNameCaseInSensitive(String componentName)
    {
        if (componentName == null)
        {
            throw new IllegalArgumentException("Component Name is required, was null.");
        }
        final Collection<MutableProjectComponent> components = getComponentConverter().convertToComponents(delegator.findAll(OfBizDelegator.PROJECT_COMPONENT));
        List<MutableProjectComponent> matched = new ArrayList<MutableProjectComponent>();
        for (MutableProjectComponent component : components)
        {
            if (componentName.equalsIgnoreCase(component.getName()))
            {
                matched.add(component);
            }
        }
        return matched;
    }


    /**
     * Retrieve the project ID for the given component ID. If project is not found, a EntityNotFoundException is
     * thrown.
     *
     * @param componentId component ID
     * @return project ID
     * @throws EntityNotFoundException if component not found for the given component ID
     */
    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException
    {
        GenericValue componentGV = findComponentGV(FIELD_ID, componentId);
        if (componentGV == null)
        {
            throw new EntityNotFoundException("Component with ID = '" + componentId + "' does not exist.");
        }
        else
        {
            return componentGV.getLong(FIELD_PROJECT);
        }
    }

    /**
     * Persists the component. If component has no ID (null), it is inserted in the store, otherwise an update operation
     * is performed on the store.
     *
     * @param component component to persist
     * @return persisted project component
     * @throws EntityNotFoundException in case of update if the component does not exist (maybe was deleted :-)
     * @throws DataAccessException if cannot persist the component
     */
    public MutableProjectComponent store(MutableProjectComponent component)
            throws EntityNotFoundException, DataAccessException
    {

        validateProjectId(component);
        validateName(component);

        Long id = component.getId();
        if (id == null)
        {
            // insert the new component
            String name = component.getName();

            // Define parameters for Component GenericValue
            Map<String, Object> componentParams = new HashMap<String, Object>();
            componentParams.put(FIELD_NAME, name);
            componentParams.put(FIELD_DESCRIPTION, component.getDescription());
            componentParams.put(FIELD_LEAD, component.getLead());
            componentParams.put(FIELD_PROJECT, component.getProjectId());
            componentParams.put(FIELD_ASSIGNEE_TYPE, component.getAssigneeType());

            // Create the Component GenericValue
            GenericValue componentGV = delegator.createValue(OfBizDelegator.PROJECT_COMPONENT, componentParams);

            component.setId(componentGV.getLong(FIELD_ID));
            component.setGenericValue(componentGV);
        }
        else
        {
            // update

            // Assert that the component exists before updating
            if (findComponentGV(FIELD_ID, component.getId()) == null)
            {
                throw new EntityNotFoundException("Component with ID = '" + id + "' does not exist.");
            }

            // At present, the GV is associated with the component object. In the future,
            // the GV should be retrieved by the component ID - e.g.:
            // GenericValue componentGV = findComponentGV(FIELD_ID, component.getId());

            GenericValue componentGV = component.getGenericValue();
            if (componentGV == null)
            {
                throw new EntityNotFoundException("Component with ID = '" + id + "' does not exist.");
            }

            // Update the GV associated with the component being updated
            componentGV.setString(FIELD_NAME, component.getName());
            componentGV.setString(FIELD_DESCRIPTION, component.getDescription());
            componentGV.setString(FIELD_LEAD, component.getLead());
            componentGV.set(FIELD_ASSIGNEE_TYPE, component.getAssigneeType());
            try
            {
                componentGV.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Unable to update the component with ID = '" + id + "'.", e);
            }
        }
        return MutableProjectComponent.copy(component);
    }

    /**
     * Validates that the name is not null
     *
     * @param component component to validate
     * @throws IllegalArgumentException if name is null
     */
    private void validateName(MutableProjectComponent component) throws IllegalArgumentException
    {
        if (component.getName() == null)
        {
            throw new IllegalArgumentException("Name is required, was null.");
        }
    }

    /**
     * Validates that the project ID is not null
     *
     * @param component component to validate
     * @throws IllegalArgumentException if name is null
     */
    private void validateProjectId(MutableProjectComponent component) throws IllegalArgumentException
    {
        if (component.getProjectId() == null)
        {
            throw new IllegalArgumentException("Project ID is required, was null.");
        }
    }

    /**
     * Validates that the ID is not null
     *
     * @param id project component ID to validate
     * @throws IllegalArgumentException if name is null
     */
    private void validateId(Long id) throws IllegalArgumentException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Project Component ID is required, was null.");
        }
    }

    public void delete(Long componentId) throws EntityNotFoundException
    {
        int deleted = delegator.removeByAnd(OfBizDelegator.PROJECT_COMPONENT, MapBuilder.build(FIELD_ID, componentId));
        if (deleted == 0)
        {
            throw new EntityNotFoundException("Unable to find the component with ID = '" + componentId + "' for deletion.");
        }
    }

    /**
     * Check whether component with specified name is stored.
     *
     * @param name component name, null will cause IllegalArgumentException
     * @param projectId project ID
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
        Collection<GenericValue> componentsForProject = findComponentGVs(FIELD_PROJECT, projectId);
        for (final GenericValue componentGV : componentsForProject)
        {
            if (name.equalsIgnoreCase(componentGV.getString("name")))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieve a collection of components - where the lead of each component is the specified user.
     *
     * @param userKey the lead user's key
     * @return collection of components - where the lead of each component is the specified user
     */
    public Collection findComponentsBylead(String userKey)
    {
        return getComponentConverter().convertToComponents(findComponentGVs(FIELD_LEAD, userKey));
    }

    /**
     * Retrieve all ProjectComponent objects stored and sort by name.
     *
     * @return all ProjectComponent objects stored
     */
    public Collection findAll()
    {
        return getComponentConverter().convertToComponents(delegator.findAll(OfBizDelegator.PROJECT_COMPONENT, CollectionBuilder.list(FIELD_NAME)));
    }

    /**
     * Retrieve the unique GenericValue object that has a field of specified name set to the specified value. The
     * GenericValue returned should be unique.
     *
     * @param fieldName field name to search by
     * @param fieldValue field value searched for
     * @return the unqiue GenericValue
     */
    private GenericValue findComponentGV(String fieldName, Object fieldValue)
    {
        return EntityUtil.getOnly(findComponentGVs(fieldName, fieldValue));
    }

    /**
     * Retrieve the collection of GenericValue objects that have the field of specified name set to the specified
     * value.
     *
     * @param fieldName field name to search by
     * @param fieldValue field value searched for
     * @return a collection of GenericValue objects that have the field of specified name set to the specified value
     */
    private List<GenericValue> findComponentGVs(String fieldName, Object fieldValue)
    {
        return delegator.findByAnd(OfBizDelegator.PROJECT_COMPONENT, MapBuilder.build(fieldName, fieldValue));
    }

    private List<GenericValue> findComponentGVs(String fieldName, Object fieldValue, String orderByField)
    {
        return delegator.findByAnd(OfBizDelegator.PROJECT_COMPONENT, MapBuilder.build(fieldName, fieldValue), CollectionBuilder.list(orderByField));
    }

}
