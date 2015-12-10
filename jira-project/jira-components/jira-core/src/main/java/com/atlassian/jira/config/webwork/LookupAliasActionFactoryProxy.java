package com.atlassian.jira.config.webwork;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import webwork.action.Action;
import webwork.action.factory.ActionFactory;
import webwork.action.factory.ActionFactoryProxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>This will convert the given action alias name to an Action implementation name.<p/>
 * <p>This also ensures that only actions that you have explicitly exposed for invocation can be accessed.
 * The default alias for an action configuration entry is the action's name.</p>
 *
 * @since v5.0.7
 */
public class LookupAliasActionFactoryProxy extends ActionFactoryProxy
{
    @ClusterSafe
    private ConcurrentMap<String, ActionConfiguration.Entry> actionAliases = new ConcurrentHashMap<String, ActionConfiguration.Entry>();

    public LookupAliasActionFactoryProxy(ActionFactory aFactory)
    {
        super(aFactory);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Looks up the action name for the specified alias in the webwork configuration, and then delegates the
     * lookup of the Action instance to the action factory proxy chain.</p>
     *
     * @param alias The alias of the action to lookup.
     *
     * @return The action corresponding to the given alias.
     * @throws ActionNotFoundException if the action called <code>actionName</code> is not found
     * @throws Exception
     */
    @Override
    public Action getActionImpl(final String alias) throws Exception
    {
        // Check cache
        ActionConfiguration.Entry actionCommand = actionAliases.get(alias);

        // Find class
        if (actionCommand == null)
        {
            actionCommand = getActionConfiguration().getActionCommand(alias);
            if (actionCommand == null)
            {
                throw new ActionNotFoundException(alias);
            }
            final ActionConfiguration.Entry fromMap = actionAliases.putIfAbsent(alias, actionCommand);
            if (fromMap != null)
            {
                actionCommand = fromMap;
            }
        }
        authorise(actionCommand);
        return getNextFactory().getActionImpl(actionCommand.toActionFactoryString());
    }

    private void authorise(ActionConfiguration.Entry actionCommand)
    {
        for (Integer permission : actionCommand.getPermissionsRequired())
        {
            if (!getPermissionManager().hasPermission(permission, getJiraAuthenticationContext().getUser()))
            {
                throw new UnauthorisedActionException();
            }
        }
    }

    private PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getComponent(PermissionManager.class);
    }

    private JiraAuthenticationContext getJiraAuthenticationContext()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class);
    }

    private ActionConfiguration getActionConfiguration()
    {
        return ComponentAccessor.getComponent(ActionConfiguration.class);
    }

    @Override
    public void flushCaches()
    {
        getNextFactory().flushCaches();
        actionAliases.clear();
    }

    public class UnauthorisedActionException extends RuntimeException
    {
        UnauthorisedActionException() {}

        @Override
        public Throwable fillInStackTrace()
        {
            return this;
        }
    }
}
