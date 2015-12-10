/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.OnDemand;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class ViewProjects extends JiraWebActionSupport
{
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    /*
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     * CHANGING THIS CONSTRUCTOR WILL BREAK ON DEMAND.
     *
     * Please consider if you really have to do that (unless you're trying to improve Studio integration).
     *
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     *
     */
    @OnDemand ("ON DEMAND extends this action and thus changing this constructor will cause compilation errors")
    public ViewProjects(final UserUtil userUtil, final UserManager userManager,
            VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    protected String doExecute() throws Exception
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT);
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB);
        session.removeAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_RETURN_URL);

        return super.doExecute();
    }

    public boolean isAdmin() throws GenericEntityException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, getLoggedInUser());
    }

    /**
     * @deprecated
     */
    public boolean isProjectAdmin(GenericValue project) throws GenericEntityException
    {
        return isProjectAdmin(new ProjectImpl(project));
    }

    public boolean isProjectAdmin(Project project)
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, project, getLoggedInApplicationUser());
    }

    /**
     * @deprecated
     */
    public boolean hasAdminPermission(GenericValue project) throws GenericEntityException
    {
        return hasAdminPermission(new ProjectImpl(project));
    }

    public boolean hasAdminPermission(Project project)
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, project, getLoggedInApplicationUser());
    }

    public List<Project> getProjectObjects()
    {
        final ApplicationUser user = getLoggedInApplicationUser();
        final Collection<Project> projects = ComponentAccessor.getProjectManager().getProjectObjects();
        return Lists.newArrayList(Collections2.filter(projects, new Predicate<Project>()
        {
            @Override
            public boolean apply(@Nullable final Project input)
            {
                return ComponentAccessor.getPermissionManager().hasPermission(Permissions.ADMINISTER, user)
                        || ComponentAccessor.getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, input, user);
            }
        }));
    }

    /**
     * @deprecated use {@link #getProjectObjects()} instead.
     */
    public List<GenericValue> getProjects()
    {
        return Lists.transform(getProjectObjects(), new Function<Project, GenericValue>()
        {
            @Override
            public GenericValue apply(@Nullable final Project input)
            {
                return (null != input) ? input.getGenericValue() : null;
            }
        });
    }

    /**
     * @deprecated
     */
    public boolean isDefaultAssigneeAssignable(GenericValue project) throws GenericEntityException
    {
        return isDefaultAssigneeAssignable(new ProjectImpl(project));
    }

    public boolean isDefaultAssigneeAssignable(Project project)
    {
        Long assigneeType = project.getAssigneeType();
        if (assigneeType == null)
        {
            return true;
        }

        if (ProjectAssigneeTypes.PROJECT_LEAD == assigneeType.longValue())
        {
            return ComponentUtils.isProjectLeadAssignable(project.getGenericValue());
        }
        else
        {
            return true;
        }
    }

    public String abbreviateString(String str, int length)
    {
        return StringUtils.abbreviate(str, length);
    }

    /**
     * @deprecated call {@link com.atlassian.jira.project.Project#getProjectLead()} instead.
     */
    public User getUser(GenericValue project)
    {
        return ApplicationUsers.toDirectoryUser(userUtil.getUserByKey(project.getString("lead")));
    }

    public boolean getStringSet(GenericValue gv, String value)
    {
        return TextUtils.stringSet(gv.getString(value));
    }

    /**
     * @deprecated
     */
    public String getPrettyAssigneeType(Long assigneeType)
    {
        return ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType);
    }

    public String getPrettyAssigneeType(Project project)
    {
        return getPrettyAssigneeType(project.getAssigneeType());
    }

    public boolean isAllowSignUp()
    {
        return userManager.hasPasswordWritableDirectory() && JiraUtils.isPublicMode();
    }
}
