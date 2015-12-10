package com.atlassian.jira.rest.v2.permission;

import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;

/**
 * Represents a list of Permissions
 *
 * @since v5.0
 */
public class PermissionsJsonBean
{
    @JsonProperty
    private HashMap<String, PermissionJsonBean> permissions;

    public PermissionsJsonBean(HashMap<String, PermissionJsonBean> permissions)
    {
        this.permissions = permissions;
    }

    public static final PermissionsJsonBean DOC_EXAMPLE;

    static
    {
        DOC_EXAMPLE = new PermissionsJsonBean(MapBuilder.<String, PermissionJsonBean>newBuilder()
                .add("EDIT_ISSUE", new PermissionJsonBean(Permissions.Permission.EDIT_ISSUE, "Edit Issues", "Ability to edit issues.", true))
                .toHashMap());
    }
}
