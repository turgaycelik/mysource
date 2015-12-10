package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.user.UserFilterUtils;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;

/**
 * Webwork action for editing user filter for user pickers.
 *
 * @since v6.2
 */
@WebSudoRequired
@SuppressWarnings ("UnusedDeclaration")
public class EditCustomFieldUserPickerFilter extends AbstractEditConfigurationItemAction
{
    static final String REDIRECT_URL_PREFIX = "ConfigureCustomField!default.jspa?customFieldId=";

    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;
    private final UserFilterManager userFilterManager;
    private final WebResourceManager webResourceManager;

    private String filterJson = null;

    public EditCustomFieldUserPickerFilter(GroupManager groupManager,
            ManagedConfigurationItemService managedConfigurationItemService, final ProjectRoleManager projectRoleManager,
            UserFilterManager userFilterManager, final WebResourceManager webResourceManager)
    {
        super(managedConfigurationItemService);
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
        this.userFilterManager = userFilterManager;
        this.webResourceManager = webResourceManager;
    }

    @RequiresXsrfCheck
    public String doSave() throws JSONException
    {
        try
        {
            final UserFilter filter = UserFilterUtils.fromJsonString(filterJson);

            userFilterManager.updateFilter(getFieldConfig(), filter);
        }
        catch (final JSONException e)
        {
            log.error("Unable to parse the returned user filter", e);
        }

        return redirectToView();
    }

    @Override
    protected String doExecute() throws Exception
    {
        webResourceManager.requireResource("jira.webresources:user-picker-filter-configuration-resources");
        return INPUT;
    }

    public void setUserFilterJson(final String userFilterJson)
    {
        filterJson = userFilterJson;
    }

    @ActionViewDataMappings ({"input", "error"})
    public Map<String, Object> getDataMap()
    {
        final Collection<ProjectRole> allProjectRoles = getAllProjectRoles();
        final Collection<Group> allGroups = getAllGroups();
        final FieldConfig fieldConfig = getFieldConfig();
        final UserFilter filter = getFilter(fieldConfig, allGroups);
        final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("configcustomfield");
        return ImmutableMap.<String, Object>builder()
                .put("atl_token", getXsrfToken())
                .put("customField", getCustomField())
                .put("fieldConfig", fieldConfig)
                .put("groupsJson", getGroupsAsJsonString(allGroups))
                .put("helpPath", helpPath)
                .put("projectRolesJson", getProjectRolesAsJsonString(allProjectRoles))
                .put("userFilter", filter)
                .put("userFilterJson", getFilterAsJsonString(filter, allProjectRoles)).build();
    }

    // ------------------------------------------------------------------------------------------ Private Helper Methods
    @VisibleForTesting
    UserFilter getFilter(final FieldConfig fieldConfig, final Collection<Group> allGroups)
    {
        return UserFilterUtils.getFilterWithoutRemovedGroupsAndRoles(userFilterManager.getFilter(fieldConfig), allGroups, projectRoleManager);
    }

    private Collection<Group> getAllGroups()
    {
        return groupManager.getAllGroups();
    }

    private String redirectToView()
    {
        return getRedirect(REDIRECT_URL_PREFIX + getCustomField().getIdAsLong());
    }


    private Collection<ProjectRole> getAllProjectRoles()
    {
        return projectRoleManager.getProjectRoles();
    }

    private String getFilterAsJsonString(final UserFilter filter, final Collection<ProjectRole> allProjectRoles)
    {
        try
        {
            return UserFilterUtils.toJson(filter, projectRoleManager).toString();
        }
        catch (final JSONException e)
        {
            log.error("Unable to create JSON representation of user filter: " + e.getMessage(), e);

            return "";
        }
    }

    /**
     * Returns a json string of [ {name: groupName}, ... ]
     */
    private String getGroupsAsJsonString(final Collection<Group> groups)
    {
        JSONArray root = new JSONArray();
        for (Group group : groups)
        {
            JSONObject groupJson = new JSONObject();
            try
            {
                groupJson.put("name", group.getName());
            }
            catch (JSONException e)
            {
                log.warn("skipping project role object that could not converted to json: " + group.getName() + " - " + e.getMessage());
            }
            root.put(groupJson);
        }
        return root.toString();
    }

    /**
     * Returns a json string of [ {id: 1, name: groupName, description: desc}, ... ]
     */
    private String getProjectRolesAsJsonString(final Collection<ProjectRole> projectRoles)
    {
        JSONArray root = new JSONArray();
        for (ProjectRole projectRole : projectRoles)
        {
            JSONObject role = new JSONObject();
            try
            {
                role.put("id", projectRole.getId());
                role.put("name", projectRole.getName());
                role.put("description", projectRole.getDescription());
            }
            catch (JSONException e)
            {
                log.warn("skipping project role object that could not converted to json: " + projectRole + " - " + e.getMessage());
            }
            root.put(role);
        }
        return root.toString();
    }
}
