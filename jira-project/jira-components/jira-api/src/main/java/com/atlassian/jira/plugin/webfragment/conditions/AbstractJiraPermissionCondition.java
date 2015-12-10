package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugin.PluginParseException;

import java.util.Map;

/**
 * Convenient abstraction to initialise conditions that require the {@link PermissionManager} and accept "permission"
 * param.
 * <p/>
 * The permission param is converted using {@link Permissions#getType(String)} and its value is set in {@link
 * #permission}
 * <p/>
 * @deprecated Use {@link AbstractPermissionCondition} instead. Since v6.0.  This class was previously in jira-core
 *      but has been moved into the API (and deprecated) to protect third-party plugins that were using it.  See
 *      JRA-30983 and JRA-32058.
 */
@Deprecated
@PublicSpi
public abstract class AbstractJiraPermissionCondition extends AbstractJiraCondition
{
    protected PermissionManager permissionManager;
    protected int permission;

    public AbstractJiraPermissionCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public void init(Map params) throws PluginParseException
    {
        permission = Permissions.getType((String) params.get("permission"));
        if (permission == -1)
        {
            throw new PluginParseException("Could not determine permission type for: " + params.get("permission"));
        }

        super.init(params);
    }

    public abstract boolean shouldDisplay(User user, JiraHelper jiraHelper);
}
