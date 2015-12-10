package com.atlassian.jira.bc.issue.visibility;

import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;

import org.apache.commons.lang.StringUtils;

/**
 * Static factory methods to create {@link com.atlassian.jira.bc.issue.visibility.Visibility} instances.
 * Methods which create a {@link Visibility} from a given groupLevel and roleLevelId or from a {@link VisibilityJsonBean}
 * could return an {@link InvalidVisibility} when passed arguments was incorrect.
 *
 * @since v6.4
 */
@PublicApi
public class Visibilities
{
    private Visibilities() {}

    public static Visibility groupVisibility(String groupLevel)
    {
        return new GroupVisibility(groupLevel);
    }

    public static Visibility roleVisibility(long roleId)
    {
        return new RoleVisibility(roleId);
    }

    public static Visibility publicVisibility()
    {
        return PublicVisibility.INSTANCE;
    }

    /**
     * Create a visibility level from {@link VisibilityJsonBean}
     *
     * @param visibilityBean is a base to create the Visibility.
     * @param projectRoleManager is used to retrieve an id of role from its name.
     * @return a visibility based on the visibility bean. An {@link InvalidVisibility} may be returned if a role from the visibilityBean don't exist.
     */
    public static Visibility fromVisibilityBean(final VisibilityJsonBean visibilityBean, final ProjectRoleManager projectRoleManager)
    {
        if (visibilityBean == null)
        {
            return PublicVisibility.INSTANCE;
        }
        else if (visibilityBean.getType() == VisibilityJsonBean.VisibilityType.group)
        {
            return new GroupVisibility(visibilityBean.getValue());
        }
        else if (visibilityBean.getType() == VisibilityJsonBean.VisibilityType.role)
        {
            return fromRoleLevel(visibilityBean.getValue(), projectRoleManager);
        }
        else
        {
            return PublicVisibility.INSTANCE;
        }
    }

    /**
     * Create a visibility level for a specified group or role.
     *
     * @param groupLevel contains a name of the group which members can view an element with this visibility.
     * @param roleLevelId contains an id of project role which members can view an element with this visibility.
     * @return a {@link GroupVisibility} if groupLevel is defined, a {@link RoleVisibility} if roleLevelId is defined,
     * a {@link PublicVisibility} if both parameters are nulls and an {@link InvalidVisibility} if both parameters are defined.
     */
    public static Visibility fromGroupAndRoleId(@Nullable final String groupLevel, @Nullable final Long roleLevelId)
    {
        final boolean roleLevelIdBlank = roleLevelId == null;
        final boolean groupLevelBlank = StringUtils.isBlank(groupLevel);

        if (groupLevelBlank && roleLevelIdBlank)
        {
            return PublicVisibility.INSTANCE;
        }
        else if (!groupLevelBlank && !roleLevelIdBlank)
        {
            return new InvalidVisibility("service.error.visibility");
        }
        else if (!groupLevelBlank)
        {
            return new GroupVisibility(groupLevel);
        }
        else
        {
            return new RoleVisibility(roleLevelId);
        }
    }

    /**
     * Create a visibility level for a specified group or role.
     *
     * @param groupLevel contains a name of the group which members can view an element with this visibility.
     * @param roleLevelId contains an string representation of id of project role which members can view an element with this visibility.
     * @return a {@link GroupVisibility} if groupLevel is defined, a {@link RoleVisibility} if roleLevelId is defined,
     * a {@link PublicVisibility} if both parameters are nulls and an {@link InvalidVisibility} if both parameters are defined
     * or if passed roleLevelId can't be parsed to long.
     */
    public static Visibility fromGroupAndStrRoleId(@Nullable final String groupLevel, @Nullable final String roleLevelId)
    {
        try
        {
            return fromGroupAndRoleId(groupLevel, StringUtils.isNotBlank(roleLevelId) ? new Long(roleLevelId) : null);
        }
        catch (NumberFormatException e)
        {
            return new InvalidVisibility("service.error.roleidnotnumber");
        }
    }

    private static Visibility fromRoleLevel(final String roleLevel, final ProjectRoleManager projectRoleManager)
    {
        final ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
        if (projectRole == null)
        {
            return new InvalidVisibility("service.error.roledoesnotexist", roleLevel);
        }
        else
        {
            return new RoleVisibility(projectRole.getId());
        }
    }
}
