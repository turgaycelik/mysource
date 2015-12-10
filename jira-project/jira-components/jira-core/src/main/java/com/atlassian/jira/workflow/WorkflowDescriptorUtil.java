package com.atlassian.jira.workflow;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.atlassian.jira.permission.ProjectPermissions.systemProjectPermissionKeyByShortName;

public class WorkflowDescriptorUtil
{
    /**
     * Resolves the project permission key from the given workflow descriptor arguments.
     *
     * @param descriptorArgs workflow descriptor arguments to resolve from
     * @return resolved permission key
     */
    public static ProjectPermissionKey resolvePermissionKey(@Nonnull Map<?, ?> descriptorArgs)
    {
        // First check if we have the 'permissionKey' argument (JIRA version >= 6.3.3).
        String permissionKey = (String) descriptorArgs.get("permissionKey");
        if (permissionKey != null)
        {
            return new ProjectPermissionKey(permissionKey);
        }

        // Check legacy argument (JIRA version < 6.3.3)
        String permission = (String) descriptorArgs.get("permission");
        if (permission != null)
        {
            // Try to match the short name to the system project key.
            ProjectPermissionKey key = systemProjectPermissionKeyByShortName(permission);
            if (key != null)
            {
                return key;
            }

            // Could not resolve the short name, let's treat the value as the key.
            return new ProjectPermissionKey(permission);
        }

        return null;
    }
}
