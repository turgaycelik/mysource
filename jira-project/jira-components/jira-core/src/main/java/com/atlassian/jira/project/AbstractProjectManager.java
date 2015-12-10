/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractProjectManager implements ProjectManager
{
    private static final Logger log = Logger.getLogger(AbstractProjectManager.class);
    final UserManager userManager;
    private final ApplicationProperties applicationProperties;

    public AbstractProjectManager(final UserManager userManager, ApplicationProperties applicationProperties)
    {
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
    }

    // Business Methods ------------------------------------------------------------------------------------------------
    // Create Methods --------------------------------------------------------------------------------------------------

    // Get / Finder Methods --------------------------------------------------------------------------------------------
    public GenericValue getProject(final GenericValue issue)
    {
        return getProject(issue.getLong("project"));
    }

    @Override
    public Project getProjectByCurrentKey(final String projectKey)
    {
        return getProjectObjByKey(projectKey);
    }

    public GenericValue getProjectCategoryByName(final String projectCategoryName) throws DataAccessException
    {
        final GenericValue pc = (GenericValue) CollectionUtils.find(getProjectCategories(), new Predicate()
        {
            public boolean evaluate(final Object object)
            {
                final GenericValue category = (GenericValue) object;
                return projectCategoryName.equals(category.getString("name"));
            }
        });
        return pc;
    }

    @Override
    public ProjectCategory getProjectCategoryObjectByName(final String projectCategoryName) throws DataAccessException
    {
        if (projectCategoryName == null)
        {
            return null;
        }
        // The production version has all ProjectCategorys cached, so this is cool.
        for (ProjectCategory projectCategory : getAllProjectCategories())
        {
            if (projectCategoryName.equals(projectCategory.getName()))
            {
                return projectCategory;
            }
        }
        return null;
    }

    @Override
    public ProjectCategory getProjectCategoryObjectByNameIgnoreCase(final String projectCategoryName) throws DataAccessException
    {
        if (projectCategoryName == null)
        {
            return null;
        }
        // The production version has all ProjectCategorys cached, so this is cool.
        for (ProjectCategory projectCategory : getAllProjectCategories())
        {
            if (projectCategoryName.equalsIgnoreCase(projectCategory.getName()))
            {
                return projectCategory;
            }
        }
        return null;
    }

    public GenericValue getProjectCategoryByNameIgnoreCase(final String projectCategoryName) throws DataAccessException
    {
        // NOTE: This is cool to return one object since JIRA does not let you create two categories that differ
        // only in case.
        final GenericValue pc = (GenericValue) CollectionUtils.find(getProjectCategories(), new Predicate()
        {
            public boolean evaluate(final Object object)
            {
                final GenericValue category = (GenericValue) object;
                return projectCategoryName.equalsIgnoreCase(category.getString("name"));
            }
        });
        return pc;
    }

    /**
     * This function checks if there is a valid default assignee set in the system<br>
     * If this returns false then the {@link #getDefaultAssignee(GenericValue, GenericValue)} will throw an exception
     *
     * @param project
     * @param component
     */
    public boolean isDefaultAssignee(final GenericValue project, final GenericValue component)
    {
        // If component is not null check which assignee type it has if it is no project default then is must be valid
        if (component != null)
        {
            final long componentAssigneeType = ComponentUtils.getComponentAssigneeType(component);
            if (AssigneeTypes.PROJECT_DEFAULT != componentAssigneeType)
            {
                return true;
            }
        }

        return isDefaultAssignee(project);
    }

    /**
     * @param project
     * @return False if no assignee type is set for a project and unassigned issues are not allowed, and the projectlead is not assignable.
     *         Also false, if either the assigneetype is not unassigned or unassigned issues are not allowed and the projectleas is not assignable.
     *         Otherwise there is a default assigneed.
     */
    public boolean isDefaultAssignee(final GenericValue project)
    {
        final Long projectDefaultAssigneeType = project.getLong("assigneetype");
        if (projectDefaultAssigneeType == null)
        {
            if (!AssigneeTypes.isAllowUnassigned())
            {
                if (!ComponentUtils.isProjectLeadAssignable(project))
                {
                    return false;
                }
            }
        }
        else
        {
            if (!(ProjectAssigneeTypes.isUnassigned(projectDefaultAssigneeType) && AssigneeTypes.isAllowUnassigned()))
            {
                if (!ComponentUtils.isProjectLeadAssignable(project))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<GenericValue> convertToProjects(final Collection<Long> projectIds)
    {
        if ((projectIds == null) || projectIds.isEmpty())
        {
            return null;
        }
        final List<GenericValue> projects = new ArrayList<GenericValue>(projectIds.size());
        for (final Long id : projectIds)
        {
            final GenericValue project = getProject(id);
            if (project != null)
            {
                projects.add(getProject(id));
            }
        }
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    @Override
    public List<Project> convertToProjectObjects(final Collection<Long> projectIds)
    {
        if (projectIds == null)
        {
            return null;
        }
        final List<Project> projects = new ArrayList<Project>(projectIds.size());
        for (final Long id : projectIds)
        {
            final Project project = getProjectObj(id);
            if (project != null)
            {
                projects.add(project);
            }
        }
        return projects;
    }

    public User getDefaultAssignee(Project project, ProjectComponent component)
    {
        return getDefaultAssignee(project.getGenericValue(), component.getGenericValue());
    }

    public User getDefaultAssignee(Project project, Collection<ProjectComponent> components) throws DefaultAssigneeException
    {
        User defaultAssignee = getConfiguredDefaultAssignee(project, components);
        // now check if this is a valid default assignee for this project:
        if (defaultAssignee == null)
        {
            // are unassigned issues allowed?
            if (applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED))
            {
                return null;
            }
            else
            {
                throw new DefaultAssigneeException("Invalid default assignee for project '" + project.getKey() +
                        "'. Unassigned issues not allowed.");
            }
        }
        else
        {
            // User is assignable check is done in getConfiguredDefaultAssignee()
            return defaultAssignee;
        }
    }

    private User getConfiguredDefaultAssignee(Project project, Collection<ProjectComponent> components) throws DefaultAssigneeException
    {
        boolean useProjectLead = false;
        boolean useUnassigned = false;
        // Loop over the components and try to find the most specific default assignee setting:
        for (ProjectComponent component : components)
        {
            long assigneeType = component.getAssigneeType();
            if (assigneeType == AssigneeTypes.COMPONENT_LEAD)
            {
                // Component Lead is the most specific of all settings
                try
                {
                    return getDefaultAssignee(project, component.getLead());
                }
                catch (InvalidAssigneeException ex)
                {
                    // Log the error and try another default assignee - we really want to create the issue if we can.
                    log.warn("Unable to assign default assignee for " + project.getKey() + " component '" + component.getName() + "'. " + ex.getMessage());
                }
            }
            else if (assigneeType == AssigneeTypes.PROJECT_LEAD)
            {
                useProjectLead = true;
            }
            else if (assigneeType == AssigneeTypes.UNASSIGNED)
            {
                useUnassigned = true;
            }
        }
        // No components that default assignee to Component Lead were found - check for other default settings
        if (useProjectLead)
        {
            // At least one component specified to use "Project Lead"
            return useProjectLeadAsDefaultAssignee(project);
        }
        else if (useUnassigned)
        {
            // At least one component specified to use Unassigned
            return null;
        }
        else
        {
            // Use the Project Default
            Long projectAssigneeType = project.getAssigneeType();
            if (projectAssigneeType != null && projectAssigneeType.longValue() == AssigneeTypes.UNASSIGNED)
            {
                return null;
            }
            else
            {
                return useProjectLeadAsDefaultAssignee(project);
            }
        }
    }

    private User useProjectLeadAsDefaultAssignee(Project project)
    {
        try
        {
            return getDefaultAssignee(project, project.getLeadUserKey());
        }
        catch (InvalidAssigneeException ex)
        {
            // Project Lead is not found or not assignable
            log.warn("Unable to assign default assignee for project " + project.getKey() + ". " + ex.getMessage());
            throw new DefaultAssigneeException(ex.getMessage());
        }
    }

    /**
     * Finds the user for the given username or throws DefaultAssigneeException if not found.
     *
     * @param userkey userkey
     * @return the User for the given username
     * @throws DefaultAssigneeException if the user with the given username is not found in the system.
     */
    private User getDefaultAssignee(Project project, String userkey) throws InvalidAssigneeException
    {
        if (userkey == null)
        {
            throw new InvalidAssigneeException("Lead user not configured.");
        }
        ApplicationUser assignee = userManager.getUserByKey(userkey);
        if (assignee == null)
        {
            throw new InvalidAssigneeException("Cannot find user '" + userkey + "'.");
        }
        if (isUserAssignable(project, assignee.getDirectoryUser()))
        {
            return assignee.getDirectoryUser();
        }
        else
        {
            throw new InvalidAssigneeException("User '" + userkey + "' does not have assign permission.");
        }
    }

    private static boolean isUserAssignable(Project project, User user)
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user);
    }

    /**
     * This function returns the default assignee if the system has been setup incorrectly then it will throw an error
     * The {@link #isDefaultAssignee(GenericValue, GenericValue)} checks if there is a valid default assignee
     *
     * @param project
     * @param component
     */
    public User getDefaultAssignee(final GenericValue project, final GenericValue component)
    {
        if (isDefaultAssignee(project, component))
        {
            if (component != null)
            {
                final long componentAssigneeType = ComponentUtils.getComponentAssigneeType(component);

                if (AssigneeTypes.COMPONENT_LEAD == componentAssigneeType)
                {
                    return getUser(component);
                }
                else if (AssigneeTypes.PROJECT_LEAD == componentAssigneeType)
                {
                    return getUser(project);
                }
                else if (AssigneeTypes.UNASSIGNED == componentAssigneeType)
                {
                    return null;
                }
            }

            // Component type is project default return correct user
            final Long projectDefaultAssigneeType = project.getLong("assigneetype");

            // If the assignee type is Unassigned and this is allowed then return the null user
            if (projectDefaultAssigneeType != null)
            {
                if (ProjectAssigneeTypes.isUnassigned(projectDefaultAssigneeType) && AssigneeTypes.isAllowUnassigned())
                {
                    return null;
                }
            }
            else
            {
                // Otherwise check if allow unassigned is turned on
                if (AssigneeTypes.isAllowUnassigned())
                {
                    return null;
                }
            }

            if (ComponentUtils.isProjectLeadAssignable(project))
            {
                return getUser(project);
            }
        }

        throw new DefaultAssigneeException("The default assignee does NOT have ASSIGNABLE permission OR Unassigned issues are turned off.");
    }

    private User getUser(final GenericValue entity) throws DefaultAssigneeException
    {
        User user = userManager.getUserByKey(entity.getString("lead")).getDirectoryUser();
        if (user == null)
        {
            log.error("Could not retrieve a user.");
            throw new DefaultAssigneeException("Could not retrieve user " + entity.getString("lead"));
        }
        return user;
    }

    public Project createProject(final String name, final String key, final String description, final String lead,
            final String url, final Long assigneeType)
    {
        return createProject(name, key, description, lead, url, assigneeType, null);
    }

    public Project updateProject(Project updatedProject, String name, String description, String lead, String url, Long assigneeType)
    {
        return updateProject(updatedProject, name, description, lead, url, assigneeType, null);
    }

    public Project updateProject(Project updatedProject, String name, String description, String lead, String url, Long assigneeType, Long avatarId)
    {
        return updateProject(updatedProject, name, description, lead, url, assigneeType, avatarId, null);
    }

    private class InvalidAssigneeException extends Exception
    {
        public InvalidAssigneeException(String message)
        {
            super(message);
        }
    }
}
