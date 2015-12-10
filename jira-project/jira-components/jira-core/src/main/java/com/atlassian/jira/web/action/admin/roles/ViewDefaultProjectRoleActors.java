package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 *
 */
@WebSudoRequired
public class ViewDefaultProjectRoleActors extends AbstractRoleActors
{
    public ViewDefaultProjectRoleActors(ProjectRoleService projectRoleService, PluginAccessor pluginAccessor)
    {
        super(projectRoleService, pluginAccessor);
    }

    public Collection getRoleActorTypes(ProjectRole projectRole, String type, int length)
    {
        DefaultRoleActors defaultRoleActors = projectRoleService.getDefaultRoleActors(projectRole, this);
        Set<RoleActor> roleActorsByType = defaultRoleActors.getRoleActorsByType(type);
        if (length == -1 || roleActorsByType.size() <= length)
        {
            return defaultRoleActors.getRoleActorsByType(type);
        }
        else
        {
            // Sublist the return set via an ArrayList (Sorry!)
            return new ArrayList<RoleActor>(defaultRoleActors.getRoleActorsByType(type)).subList(0,length);
        }
    }


}
