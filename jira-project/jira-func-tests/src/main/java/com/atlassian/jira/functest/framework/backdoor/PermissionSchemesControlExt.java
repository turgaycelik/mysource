package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.PermissionSchemesControl;

public class PermissionSchemesControlExt extends PermissionSchemesControl
{
    public PermissionSchemesControlExt(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void addEveryonePermission(Long schemeId, int permission)
    {
        addPermission(schemeId, permission, "group");
    }

    private void addPermission(long schemeId, int permission, String type)
    {
        get(createResource().path("permissionSchemes/entity/add")
                .queryParam("schemeId", "" + schemeId)
                .queryParam("permission", "" + permission)
                .queryParam("type", type)
        );
    }

}
