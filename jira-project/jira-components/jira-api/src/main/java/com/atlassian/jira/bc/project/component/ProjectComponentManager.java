package com.atlassian.jira.bc.project.component;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Methods for accessing and persisting project components.
 */
@PublicApi
public interface ProjectComponentManager
{
    /**
     * Used to represent empty component fields.
     */
    public static final String NO_COMPONENTS = "-1";

    /**
     * Implement this method to create a new ProjectComponent object associated with the project with the ID specified
     * and with the values given. It should also validate the values - store and return the ProjectComponent if
     * validation succeeds. Otherwise, throw {@link com.atlassian.jira.bc.ValidationErrorsException}.
     *
     * @param name name of component
     * @param description description of component
     * @param lead user name associated with component
     * @param assigneeType assignee type
     * @param projectId ID of project that component is associated with
     * @return new instance of ProjectComponent with the values specified
     * @throws IllegalArgumentException if one or more arguments have invalid values
     */
    public ProjectComponent create(String name, String description, String lead, long assigneeType, Long projectId)
            throws IllegalArgumentException;

    /**
     * Implement this method to find the component with the specified ID.
     *
     * @param id component ID to search for
     * @return ProjectComponent with the specified ID
     * @throws EntityNotFoundException if the component is not found
     */
    public ProjectComponent find(Long id) throws EntityNotFoundException;

    /**
     * Implement this method to find all components associated with the project with the ID specified.
     *
     * @param projectId ID of project to search for
     * @return collection of ProjectComponent objects associated with the project with the ID specified
     */
    public Collection<ProjectComponent> findAllForProject(Long projectId);

    /**
     * Returns all unique names of components associated with all the projects with the ID-s specified.
     *
     * @param projectIds ID-s of project to search for
     * @return collection of unique names of components associated with all the projects with the ID-s specified.
     */
    Collection<String> findAllUniqueNamesForProjects(Collection<Long> projectIds);

    /**
     * Implement this method to find all components.
     *
     * @return collection of all ProjectComponent objects
     */
    public Collection<ProjectComponent> findAll();

    /**
     * Returns all unique names of the components that belong to the passed projects.
     *
     * @param projects projects to search in
     * @return collection of unique names of all components.
     */
    Collection<String> findAllUniqueNamesForProjectObjects(Collection<Project> projects);

    /**
     * Creates a list of ProjectComponent objects from the given list of IDs.
     *
     * @param ids The List of ProjectComponent IDs.
     * @return a list of ProjectComponent objects from the given list of IDs.
     * @throws EntityNotFoundException if no ProjectComponent exists for any of the given IDs.
     */
    public List<ProjectComponent> getComponents(List<Long> ids) throws EntityNotFoundException;

    /**
     * Implement this method to update the component specified with the values given.
     *
     * @param component component to be updated
     * @return ProjectComponent with updated values as specified
     * @throws EntityNotFoundException if component is not found
     */
    public ProjectComponent update(MutableProjectComponent component)
            throws EntityNotFoundException;

    /**
     * Implement this method to delete the component.
     *
     * @param componentId component id
     * @throws EntityNotFoundException if the component is not found
     */
    public void delete(Long componentId) throws EntityNotFoundException;

    /**
     * Implement this method in order to check whether component with specified name is stored.
     *
     * @param name component name, null will cause IllegalArgumentException
     * @param projectId project ID
     * @return true if new name is stored
     */
    public boolean containsName(String name, Long projectId);

    /**
     * Implement this method to look up the project ID for the given component ID. If project is not found, throws
     * EntityNotFoundException.
     *
     * @param componentId component ID
     * @return project ID
     * @throws EntityNotFoundException if component with the specified id cannot be found
     */
    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException;

    /**
     * Converts the ProjectComponent to GenericValue form, provided as a transitional measure until GenericValue is
     * eradicated from the front end.
     *
     * @param projectComponent project component
     * @return the ProjectComponent as a GenericValue.
     * @deprecated don't use GenericValue use the ProjectComponent instead.
     */
    public GenericValue convertToGenericValue(ProjectComponent projectComponent);

    /**
     * Temporary method to allow conversion of a collection of ProjectComponent objects to a collection of GenericValues
     * representing a project component.
     *
     * @param projectComponents a collection of project components
     * @return Collection of GenericValues representing the collection of ProjectComponent objects passed in
     * @deprecated don't use GenericValue use the ProjectComponent instead.
     */
    public Collection<GenericValue> convertToGenericValues(Collection<ProjectComponent> projectComponents);

    /**
     * Finds the ProjectComponent with the given name in the project with the given id.
     *
     * @param projectId id of the project.
     * @param componentName name of the component.
     * @return the ProjectComponent or null if there is no such ProjectComponent.
     */
    public ProjectComponent findByComponentName(Long projectId, String componentName);

    /**
     * Finds all ProjectComponents with the given name with comparisons case insenstive.
     *
     * @param componentName name of the component.
     * @return all ProjectComponents with the given name or an empty collection if there is no such ProjectComponent.
     */
    public Collection<ProjectComponent> findByComponentNameCaseInSensitive(String componentName);

    /**
     * Retrieve a collection of ProjectComponents - where the lead of each component is the specified user.
     *
     * @param userName the lead user name
     * @return collection of components - where the lead of each component is the specified user
     */
    public Collection<ProjectComponent> findComponentsByLead(String userName);

    /**
     * @since 4.2
     * @param issue find components on this issue
     * @return collection of project components associated with this issue
     */
    public Collection<ProjectComponent> findComponentsByIssue(final Issue issue);

    /**
     * Returns a list of all Issue IDs with the given component.
     * @param component the component
     * @return a not null list of all Issue IDs with the given component.
     */
    @Nonnull
    public Collection<Long> getIssueIdsWithComponent(@Nonnull ProjectComponent component);

    /**
     * This code used to live directly in the IssueImpl but it has been refactored into
     * the ProjectComponentManager to make things a little cleaner. That's why it is "new in 4.2"
     * but immediately marked as deprecated
     * @since 4.2
     * @param issue find components for this issue
     * @return collection of generic values representing the components assigned to the issue
     * @deprecated use findComponentsByIssue that returns a Collection<ProjectComponent> instead
     */
    public Collection<GenericValue> findComponentsByIssueGV(final Issue issue);
}
