package com.atlassian.jira.security.type;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;

public abstract class AbstractSecurityType implements SecurityType
{
    public String getArgumentDisplay(String argument)
    {
        return argument;
    }

    /**
     * This abstract class will always return true for this method.
     * This is the required behaviour for almost all subclasses.
     * Only weird subclasses like CurrentReporterHasCreatePermission will override this.
     *
     * @param permissionKey the id of the permission.
     * @return true always for this abstract class.
     * see SecurityType#isValidForPermission
     * see CurrentReporterHasCreatePermission#isValidForPermission
     */
    public boolean isValidForPermission(ProjectPermissionKey permissionKey)
    {
        // by default you can be used for all permission types - only a few SecurityTypes will need to override this.
        return true;
    }
}
