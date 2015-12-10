package com.atlassian.jira.bc.project.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssocationType;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.event.bc.project.component.ProjectComponentCreatedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentDeletedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.comparator.ComponentComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.Sets;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Validates project component values and provides methods for accessing and persisting project components.
 */
public class DefaultProjectComponentManager implements ProjectComponentManager
{

    /**
     * store
     */
    private final ProjectComponentStore store;
    private final IssueManager issueManager;
    private final NodeAssociationStore nodeAssociationStore;

    /**
     * project ID field name
     */
    protected static final String FIELD_PROJECT_ID = "projectId";

    /**
     * name field name
     */
    protected static final String FIELD_NAME = "name";

    /**
     * lead field name
     */
    protected static final String FIELD_LEAD = "lead";
    protected final EventPublisher eventPublisher;
    private UserManager userManager;

    /**
     * Creates a new instance of this class and sets the store that will be used for persistence.
     *
     * @param store persistent store
     * @param issueManager
     * @param eventPublisher
     */
    public DefaultProjectComponentManager(ProjectComponentStore store, final IssueManager issueManager, EventPublisher eventPublisher,
            UserManager userManager, NodeAssociationStore nodeAssociationStore)
    {
        this.store = store;
        this.issueManager = issueManager;
        this.eventPublisher = eventPublisher;
        this.userManager = userManager;
        this.nodeAssociationStore = nodeAssociationStore;
    }

    /**
     * Create a new ProjectComponent object associated with the project with the ID specified and with the values given.
     *
     * @param name        name of component
     * @param description description of component
     * @param lead        user name associated with component
     * @param projectId   ID of project that component is associated with
     * @return new instance of ProjectComponent with the values specified
     */
    public ProjectComponent create(String name, String description, String lead, long assigneeType, Long projectId)
            throws IllegalArgumentException
    {
        try
        {
            MutableProjectComponent component = new MutableProjectComponent(null, name, description, lead, assigneeType, projectId);
            ProjectComponent projectComponent = convertToProjectComponent(store.store(component));
            eventPublisher.publish(new ProjectComponentCreatedEvent(projectComponent));
            return projectComponent;
        }
        catch (EntityNotFoundException e)
        {
            // Insert should never cause this exception - so this return statement should never be reached
            return null;
        }
    }

    /**
     * Find the component with the specified ID
     *
     * @param id component ID to search for
     * @return ProjectComponent with the specified ID
     * @throws EntityNotFoundException if the component is not found
     */
    public ProjectComponent find(Long id) throws EntityNotFoundException
    {
        return convertToProjectComponent(store.find(id));
    }

    /**
     * Find all components associated with the project with the ID specified
     *
     * @param projectId ID of project to search for
     * @return collection of ProjectComponent objects associated with the project with the ID specified
     */
    public Collection<ProjectComponent> findAllForProject(Long projectId)
    {
        return convertToProjectComponents(store.findAllForProject(projectId));
    }

    @Override
    public Collection<String> findAllUniqueNamesForProjects(Collection<Long> projectIds)
    {
        Set<String> uniqueNames = Sets.newTreeSet(ProjectComponentComparator.COMPONENT_NAME_COMPARATOR);

        for (Long projectId : projectIds)
        {
            Collection<MutableProjectComponent> projectComponents = store.findAllForProject(projectId);
            if (projectComponents != null)
            {
                for (MutableProjectComponent component : projectComponents)
                {
                    uniqueNames.add(component.getName());
                }
            }
        }

        return uniqueNames;
    }

    public Collection<ProjectComponent> findAll()
    {
        return convertToProjectComponents(store.findAll());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> findAllUniqueNamesForProjectObjects(Collection<Project> projects)
    {
        Set<String> uniqueNames = Sets.newTreeSet(ProjectComponentComparator.COMPONENT_NAME_COMPARATOR);

        for (Project project : projects)
        {
            Collection<MutableProjectComponent> projectComponents = store.findAllForProject(project.getId());
            if (projectComponents != null)
            {
                for (MutableProjectComponent component : projectComponents)
                {
                    uniqueNames.add(component.getName());
                }
            }
        }

        return uniqueNames;
    }

    public List<ProjectComponent> getComponents(List<Long> ids) throws EntityNotFoundException
    {
        final List<ProjectComponent> components = new ArrayList<ProjectComponent>(ids.size());

        for (final Long id : ids)
        {
            components.add(find(id));
        }
        return components;
    }

    /**
     * Update the component specified with the values given.
     *
     * @param component   component to be updated
     * @return ProjectComponent with updated values as specified
     * @throws EntityNotFoundException if component is not found
     */
    public ProjectComponent update(MutableProjectComponent component)
            throws EntityNotFoundException
    {
        ProjectComponent oldProjectComponent = find(component.getId());

        store.store(component);
        ProjectComponent projectComponent = convertToProjectComponent(component);

        eventPublisher.publish(new ProjectComponentUpdatedEvent(projectComponent, oldProjectComponent));

        return projectComponent;
    }

    /**
     * Delete the component.
     *
     * @param componentId component id
     * @throws EntityNotFoundException if the component is not found
     */
    public void delete(Long componentId) throws EntityNotFoundException
    {
        ProjectComponent projectComponent = null;

        if (componentId != null)
        {
            projectComponent = find(componentId);
        }

        store.delete(componentId);

        if (componentId != null)
        {
            eventPublisher.publish(new ProjectComponentDeletedEvent(projectComponent));
        }
    }

    /**
     * Check whether component with specified name is stored.
     *
     * @param name      component name, null will cause IllegalArgumentException
     * @param projectId project ID
     * @return true if new name is stored
     */
    public boolean containsName(String name, Long projectId)
    {
        return store.containsName(name, projectId);
    }

    public Long findProjectIdForComponent(Long id) throws EntityNotFoundException
    {
        return store.findProjectIdForComponent(id);
    }

    /**
     * Convert the specified MutableProjectComponent to a ProjectComponent object.
     *
     * @param value MutableProjectComponent to be converted into a ProjectComponent.
     * @return new instance of ProjectComponent with same values as given in the parameter object
     */
    protected ProjectComponent convertToProjectComponent(MutableProjectComponent value)
    {
        return getComponentConverter().convertToProjectComponent(value);
    }

    /**
     * Convert the specified ProjectComponent to a MutableProjectComponent object and sets its project ID with the given
     * value.
     *
     * @param value     ProjectComponent to be converted into a MutableProjectComponent.
     * @return new instance of MutableProjectComponent with same values as given in the parameter object and with
     *         project ID set
     */
    protected MutableProjectComponent convertToMutableProjectComponent(ProjectComponent value)
    {
        return new MutableProjectComponent(value.getId(), value.getName(), value.getDescription(), value.getLead(), value.getAssigneeType(), value.getProjectId());
    }

    /**
     * Convert a collection of MutableProjectComponent objects to a collection of ProjectComponent objects.
     *
     * @param mutables collection of MutableProjectComponent objects to convert
     * @return collection of new ProjectComponent objects that represent objects in the given MutableProjectComponent
     *         collection
     */
    private Collection<ProjectComponent> convertToProjectComponents(Collection<MutableProjectComponent> mutables)
    {
        return getComponentConverter().convertToProjectComponents(mutables);
    }

    public GenericValue convertToGenericValue(ProjectComponent projectComponent)
    {
        if (projectComponent == null)
        {
            return null;
        }
        return projectComponent.getGenericValue();
    }

    /**
     * Temporary method to allow conversion of a collection of ProjectComponent objects
     * to a collection of GenericValues representing a project component.
     *
     * @param projectComponents a collection of project components
     * @return Collection of GenericValues representing the collection of ProjectComponent objects passed in
     */
    public Collection<GenericValue> convertToGenericValues(Collection<ProjectComponent> projectComponents)
    {
        final Collection<GenericValue> projectComponentGVs = new ArrayList<GenericValue>(projectComponents.size());
        for (ProjectComponent projectComponent : projectComponents)
        {
            projectComponentGVs.add(projectComponent.getGenericValue());
        }
        return projectComponentGVs;
    }

    public ProjectComponent findByComponentName(Long projectId, String componentName)
    {
        MutableProjectComponent pc;
        try
        {
            pc = store.findByComponentName(projectId, componentName);
            return pc == null ? null : convertToProjectComponent(pc);
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }

    public Collection<ProjectComponent> findByComponentNameCaseInSensitive(String componentName)
    {
        return convertToProjectComponents(store.findByComponentNameCaseInSensitive(componentName));
    }

    /**
     * Retrieve a collection of components - where the lead of each component is
     * the specified user.
     *
     * @param userName the lead user name
     * @return collection of components - where the lead of each component is the specified user
     */
    public Collection<ProjectComponent> findComponentsByLead(String userName)
    {
        ApplicationUser appUser = userManager.getUserByName(userName);
        if (appUser == null)
        {
            return Collections.emptyList();
        }
        return store.findComponentsBylead(appUser.getKey());
    }

    public Collection<ProjectComponent> findComponentsByIssue(final Issue issue)
    {
        final Collection<GenericValue> components = issue.getComponents();
        final Collection<MutableProjectComponent> mutableComponents = getComponentConverter().convertToComponents(components);
        return convertToProjectComponents(mutableComponents);
    }

    @Nonnull
    @Override
    public Collection<Long> getIssueIdsWithComponent(@Nonnull final ProjectComponent component)
    {
        return nodeAssociationStore.getSourceIdsFromSink(NodeAssocationType.ISSUE_TO_COMPONENT, component.getId());
    }

    public Collection<GenericValue> findComponentsByIssueGV(final Issue issue)
    {
        Collection<GenericValue> components;
        final GenericValue genericValue = issue.getGenericValue();
        if (genericValue != null)
        {
            try
            {
                final List<GenericValue> newValue = new ArrayList<GenericValue>(issueManager.getEntitiesByIssue(IssueRelationConstants.COMPONENT, genericValue));
                Collections.sort(newValue, ComponentComparator.COMPARATOR);
                components = newValue;
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Error occurred while retrieving components for issue with id " + issue.getId() + "'.", e);
            }
        }
        else
        {
            components = Collections.emptyList();
        }

        return components;
    }

    protected ComponentConverter getComponentConverter()
    {
        return new ComponentConverter();
    }
}
