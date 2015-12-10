package com.atlassian.jira.web.action.admin.scheme.mapper;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.scheme.mapper.SchemeGroupsToRoleTransformerService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.mapper.GroupToRoleMapping;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This action is the second step in the GroupToRoleMapping tool, it collects the information about the group to
 * role mappings.
 */
@WebSudoRequired
public class SchemeGroupToRoleMapperAction extends AbstractGroupToRoleAction
{
    private ProjectRoleService projectRoleService;
    private SchemeGroupsToRoleTransformerService schemeGroupsToRoleTransformerService;
    private List groupToRoleMappings;
    private static final String GROUP_TO_ROLE_SUFFIX = "_project_role";
    private Collection groupsWithoutGlobalUsePermission;
    private Collection groupsWithGlobalUsePermission;
    private Set groups;

    public SchemeGroupToRoleMapperAction(SchemeManagerFactory schemeManagerFactory, SchemeFactory schemeFactory, ProjectRoleService projectRoleService, ApplicationProperties applicationProperties, SchemeGroupsToRoleTransformerService groupsToRoleTransformerService)
    {
        super(schemeManagerFactory, schemeFactory, applicationProperties);
        this.projectRoleService = projectRoleService;
        this.schemeGroupsToRoleTransformerService = groupsToRoleTransformerService;
    }

    public String doDefault() throws Exception
    {
        return INPUT;
    }

    protected void doValidation()
    {
        if (!isHasSelectedSchemeIds())
        {
            addErrorMessage(getText("admin.scheme.group.role.mapper.no.selected.schemes"));
        }
        else if (getGroupToRoleMappings().isEmpty())
        {
            addErrorMessage(getText("admin.scheme.group.role.mapper.no.selected.mappings"));
        }
    }

    protected String doExecute() throws Exception
    {
        StringBuilder redirectString = new StringBuilder("SchemeGroupToRoleTransformer!default.jspa");
        redirectString.append("?selectedSchemeType=");
        redirectString.append(getSelectedSchemeType());

        storeGroupToRoleMappingInSession();

        return forceRedirect(redirectString.toString());
    }

    //get available roles
    public Collection getAvailableRoles()
    {
        return projectRoleService.getProjectRoles(this);
    }

    public Collection getGroupsWithoutGlobalUsePermission()
    {
        if (groupsWithoutGlobalUsePermission == null)
        {
            groupsWithoutGlobalUsePermission = schemeGroupsToRoleTransformerService.getGroupsWithoutGlobalUsePermission(getGroups());
        }
        return groupsWithoutGlobalUsePermission;
    }

    public Collection getGroupsWithGlobalUsePermission()
    {
        if (groupsWithGlobalUsePermission == null)
        {
            groupsWithGlobalUsePermission = schemeGroupsToRoleTransformerService.getGroupsWithGlobalUsePermission(getGroups());
        }
        return groupsWithGlobalUsePermission;
    }

    public boolean isExistsGroupsWithGlobalUsePermission()
    {
        return !getGroupsWithGlobalUsePermission().isEmpty();
    }

    private List getGroupToRoleMappings()
    {
        if (groupToRoleMappings == null)
        {
            groupToRoleMappings = new ArrayList();
            Map params = ActionContext.getParameters();
            for (Object key : params.keySet())
            {
                if (key.toString().endsWith(GROUP_TO_ROLE_SUFFIX))
                {
                    String[] values = (String[]) params.get(key);
                    for (String value : values)
                    {
                        if (!SchemeGroupToRoleTransformerAction.UNMAPPED_PROJECT_ROLE_VALUE.equals(value))
                        {
                            ProjectRole mappedProjectRole = getProjectRole(value);
                            // Create a GroupToRoleMapping
                            groupToRoleMappings.add(new GroupToRoleMapping(mappedProjectRole, extractGroupName(key.toString())));
                        }
                    }
                }
            }
            // Sort the GroupToRoleMappings
            Collections.sort(groupToRoleMappings, new GroupToRoleMappingComparator());
        }

        return groupToRoleMappings;
    }

    private ProjectRole getProjectRole(String idStr)
    {
        Long id = new Long(idStr);
        return projectRoleService.getProjectRole(id, this);
    }

    private Set getGroups()
    {
        if (groups == null)
        {
            groups = getUniqueGroupsForSelectedSchemes();
        }
        return groups;
    }

    private String extractGroupName(String groupToRoleKey)
    {
        return groupToRoleKey.substring(0, groupToRoleKey.indexOf(GROUP_TO_ROLE_SUFFIX));
    }

    private void storeGroupToRoleMappingInSession()
    {
        ActionContext.getSession().put(GROUP_TO_ROLE_MAP_SESSION_KEY, getGroupToRoleMappings());
    }

    private static class GroupToRoleMappingComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            if (o1 != null && o2 != null)
            {
                GroupToRoleMapping groupToRoleMapping1 = (GroupToRoleMapping) o1;
                GroupToRoleMapping groupToRoleMapping2 = (GroupToRoleMapping) o2;

                String projectRoleName2 = groupToRoleMapping2.getProjectRole().getName();
                String projectRoleName1 = groupToRoleMapping1.getProjectRole().getName();

                int projectRoleNameComparison = projectRoleName1.compareTo(projectRoleName2);

                if (projectRoleNameComparison != 0)
                {
                    return projectRoleNameComparison;
                }

                String groupName2 = groupToRoleMapping2.getGroupName();
                String groupName1 = groupToRoleMapping1.getGroupName();
                return groupName1.compareTo(groupName2);
            }
            return 0;
        }
    }
}
