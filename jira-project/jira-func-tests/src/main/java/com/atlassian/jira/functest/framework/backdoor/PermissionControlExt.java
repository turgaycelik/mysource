package com.atlassian.jira.functest.framework.backdoor;

import java.util.List;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;

import com.sun.jersey.api.client.GenericType;

public class PermissionControlExt extends BackdoorControl<PermissionControlExt>
{
    private static final GenericType<List<String>> LIST_GENERIC_TYPE = new GenericType<List<String>>()
    {
    };

    public PermissionControlExt(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void addGlobalPermissionByKey(final String permissionType, final String group)
    {
        get(createResource().path("permissions/global/add/key")
                .queryParam("type", permissionType)
                .queryParam("group", group));
    }

    public String getPermissionsByKey(String permissionType)
    {
        return get(createResource().path("permissions/global/key").queryParam("type", permissionType));
    }

    public void addAnyoneGlobalPermissionByKey(final String permissionType)
    {
        get(createResource().path("permissions/global/add/key").queryParam("type", permissionType));
    }

    public void removeGlobalPermissionByKey(final String permissionType, final String group)
    {
        get(createResource().path("permissions/global/remove/key")
                .queryParam("type", permissionType)
                .queryParam("group", group));
    }

    public void removeAnyoneGlobalPermissionByKey(final String permissionType)
    {
        get(createResource().path("permissions/global/remove/key")
                .queryParam("type", permissionType));
    }

    public List<String> getGlobalPermissionGroupsByKey(final String permissionType)
    {
        return createResource().path("permissions/global/getgroups/key")
                .queryParam("type", permissionType).get(LIST_GENERIC_TYPE);
    }
}
