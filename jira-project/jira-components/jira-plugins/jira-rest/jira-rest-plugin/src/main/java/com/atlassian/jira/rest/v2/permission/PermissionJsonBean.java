package com.atlassian.jira.rest.v2.permission;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents one permission and whether the caller has it
 *
 * @since v5.0
 */
public class PermissionJsonBean extends com.atlassian.jira.issue.fields.rest.json.beans.PermissionJsonBean
{
    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    boolean havePermission;

    /**
     * Construct a bean using a Global Permission - rather than the Permission enum
     */
    public PermissionJsonBean(GlobalPermissionType globalPermissionType, boolean havePermission, JiraAuthenticationContext authenticationContext)
    {
        final Integer id = GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.inverse().get(globalPermissionType.getGlobalPermissionKey());
        if (id != null)
        {
            id(id.toString());
        }
        key(globalPermissionType.getKey());
        this.name = authenticationContext.getI18nHelper().getText(globalPermissionType.getNameI18nKey());
        this.description = authenticationContext.getI18nHelper().getText(globalPermissionType.getDescriptionI18nKey());
        this.havePermission = havePermission;
    }

    public PermissionJsonBean(Permissions.Permission permission, boolean havePermission, JiraAuthenticationContext authenticationContext)
    {
        id(String.valueOf(permission.getId()));
        key(permission.name());
        this.name = authenticationContext.getI18nHelper().getText(permission.getNameKey());
        this.description = authenticationContext.getI18nHelper().getText(permission.getDescriptionKey());
        this.havePermission = havePermission;
    }

    // please dont use this one...
    public PermissionJsonBean(Permissions.Permission permission, String name, String description, boolean havePermission)
    {
        id(String.valueOf(permission.getId()));
        key(permission.name());
        this.name = name;
        this.description = description;
        this.havePermission = havePermission;
    }
}
