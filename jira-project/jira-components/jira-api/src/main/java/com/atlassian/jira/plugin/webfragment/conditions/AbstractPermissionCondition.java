package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.annotations.PublicSpi;
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
 *
 * @since v6.0
 */
@PublicSpi
public abstract class AbstractPermissionCondition extends AbstractWebCondition
{
    protected final PermissionManager permissionManager;
    protected int permission;

    public AbstractPermissionCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public void init(Map<String,String> params) throws PluginParseException
    {
        permission = Permissions.getType(params.get("permission"));
        if (permission == -1)
        {
            throw new PluginParseException("Could not determine permission type for: " + params.get("permission"));
        }
        super.init(params);
    }
}
