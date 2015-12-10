package com.atlassian.jira.action.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class ComponentUtils
{
    private static final Logger log = Logger.getLogger(ComponentUtils.class);

    public static boolean isProjectLeadAssignable(GenericValue entity)
    {
        String projectLeadUserkey;
        GenericValue project;
        try
        {
            if ("Component".equals(entity.getEntityName()))
            {
                project = getProject(entity);
            }
            else if ("Project".equals(entity.getEntityName()))
            {
                project = entity;
            }
            else
            {
                throw new IllegalArgumentException("Entity passed must be \"Component\" or \"Project\" and NOT " + entity);
            }
            projectLeadUserkey = project.getString("lead");
            return isUserAssignable(project, projectLeadUserkey);
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
            return false;
        }
    }

    public static boolean isComponentLeadAssignable(GenericValue component)
    {
        Project project = getProjectFor(component);
        String componentLeadUserkey = component.getString("lead");
        return isUserAssignable(project, componentLeadUserkey);
    }

    public static long getComponentAssigneeType(GenericValue component)
    {
        if (component == null)
        {
            throw new IllegalArgumentException("Component passed can not be null.");
        }

        Long assigneeType = component.getLong("assigneetype");
        return getAssigneeType(component, assigneeType);
    }

    public static long getAssigneeType(GenericValue component, Long assigneeType)
    {
        if (assigneeType == null)
        {
            return ComponentAssigneeTypes.PROJECT_DEFAULT;
        }
        else
        {
            if (ComponentAssigneeTypes.isProjectLead(assigneeType))
            {
                // Check is project lead is current assignable, if not return Project Default
                if (ComponentUtils.isProjectLeadAssignable(component))
                {
                    return ComponentAssigneeTypes.PROJECT_LEAD;
                }
                else
                {
                    return ComponentAssigneeTypes.PROJECT_DEFAULT;
                }
            }
            else if (ComponentAssigneeTypes.isComponentLead(assigneeType))
            {
                if (ComponentUtils.isComponentLeadAssignable(component))
                {
                    return ComponentAssigneeTypes.COMPONENT_LEAD;
                }
                else
                {
                    return ComponentAssigneeTypes.PROJECT_DEFAULT;
                }
            }
            else if (ComponentAssigneeTypes.isUnassigned(assigneeType))
            {
                if (ComponentUtils.isIssueUnassignable(component))
                {
                    return ComponentAssigneeTypes.UNASSIGNED;
                }
                else
                {
                    return ComponentAssigneeTypes.PROJECT_DEFAULT;
                }
            }
        }

        return ComponentAssigneeTypes.PROJECT_DEFAULT;
    }

    /**
     * The component is passed incase in the FUTURE the unassigned option is done by project and not globally
     * (Scotty are you happy?)
     * @param component
     */
    public static boolean isIssueUnassignable(GenericValue component)
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    /**
     * Checks if the user with given username is assignable to given project.
     *
     * @param project project to assign the user to
     * @param userkey userkey
     * @return true if the user is assignable to the project
     */
    private static boolean isUserAssignable(GenericValue project, String userkey)
    {
        if (userkey != null)
        {
            ApplicationUser applicationUser = ComponentAccessor.getUserManager().getUserByKey(userkey);
            if (applicationUser == null)
            {
                return false;
            }
            User user = applicationUser.getDirectoryUser();
            if (user != null)
            {
                return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user);
            }
            else
            {
                log.debug("User with userkey '" + userkey + "' is not assignable for project '"
                        + project.getString("name") + "'! User does not exist!");
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if the user with given username is assignable to given project.
     *
     * @param project project to assign the user to
     * @param userkey userkey
     * @return true if the user is assignable to the project
     */
    private static boolean isUserAssignable(Project project, String userkey)
    {
        if (userkey != null)
        {
            ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userkey);
            if (user != null)
            {
                return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, project, user);
            }
            else
            {
                log.debug("User with userkey '" + userkey + "' is not assignable for project '"
                        + project.getName() + "'! User does not exist!");
                return false;
            }
        }
        return false;
    }

    private static GenericValue getProject(GenericValue component) throws GenericEntityException
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        return projectManager.getProject(component.getLong("project"));
    }

    private static Project getProjectFor(GenericValue component)
    {
        return ComponentAccessor.getProjectManager().getProjectObj(component.getLong("project"));
    }
}
