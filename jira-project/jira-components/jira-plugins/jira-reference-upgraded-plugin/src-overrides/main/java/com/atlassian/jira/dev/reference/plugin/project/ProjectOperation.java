package com.atlassian.jira.dev.reference.plugin.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.projectoperation.AbstractPluggableProjectOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Simple tests class for project operation.
 *
 * @since v4.4
 */
public class ProjectOperation extends AbstractPluggableProjectOperation
{
    private static final String PERMISSION_PARAM = "permissions";

    private final PermissionManager permissionManager;

    public ProjectOperation(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public String getHtml(Project project, User user)
    {
        return descriptor.getHtml("view", descriptor.getParams()).replace("Operation", "Upgraded Operation");
    }

    public boolean showOperation(Project project, User user)
    {
        Map<String, String> params = descriptor.getParams();
        if (params.containsKey(PERMISSION_PARAM))
        {
            String [] permissions = params.get(PERMISSION_PARAM).split(",");
            for (String permission : permissions)
            {
                int type = Permissions.getType(StringUtils.stripToNull(permission));
                if (type >= 0)
                {
                    if (Permissions.isGlobalPermission(type))
                    {
                        if (!permissionManager.hasPermission(type, user))
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if (!permissionManager.hasPermission(type, project, user))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }
}
