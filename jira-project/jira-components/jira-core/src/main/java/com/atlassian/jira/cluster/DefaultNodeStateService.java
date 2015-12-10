package com.atlassian.jira.cluster;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.jsoup.helper.Validate;

import static com.atlassian.jira.cluster.Node.NodeState.ACTIVE;
import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR;

/**
 * The NodeStateService implementation.
 *
 * @since 6.1
 */
public class DefaultNodeStateService implements NodeStateService
{
    // Constants
    static final String FORBIDDEN_MESSAGE_CODE = "jira.admin.cluster.service.forbidden";

    // Fields
    private final GlobalPermissionManager globalPermissionManager;
    private final I18nHelper i18nHelper;
    private final NodeStateManager nodeStateManager;

    public DefaultNodeStateService(final GlobalPermissionManager globalPermissionManager, final I18nHelper i18nHelper,
            final NodeStateManager nodeStateManager)
    {
        Validate.noNullElements(new Object[] { globalPermissionManager, i18nHelper, nodeStateManager });
        this.globalPermissionManager = globalPermissionManager;
        this.i18nHelper = i18nHelper;
        this.nodeStateManager = nodeStateManager;
    }

    @Override
    public ServiceResult activate(final ApplicationUser user)
    {
        return doPrivileged(user, new NodeManagerCallback()
        {
            @Override
            public void execute(final NodeStateManager nodeStateManager) throws ClusterStateException
            {
                nodeStateManager.activate();
            }
        });
    }

    @Override
    public ServiceResult deactivate(final ApplicationUser user)
    {
        return doPrivileged(user, new NodeManagerCallback()
        {
            @Override
            public void execute(final NodeStateManager nodeStateManager) throws NotClusteredException
            {
                nodeStateManager.deactivate();
            }
        });
    }

    @Override
    public boolean isActive()
    {
        return nodeStateManager.getNode().getState() == ACTIVE;
    }

    /**
     * Executes the given callback as the given user, checking that they have the required privileges.
     *
     * @param user the user performing the action (required)
     * @param callback the callback to execute (required)
     * @return a non-null result
     */
    private ServiceResult doPrivileged(final ApplicationUser user, final NodeManagerCallback callback)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            if (hasManageNodePermission(user))
            {
                callback.execute(nodeStateManager);
            }
            else
            {
                rejectAsForbidden(errorCollection);
            }
        }
        catch (NotClusteredException e)
        {
            errorCollection.addErrorMessage(e.getMessage(), FORBIDDEN);
        }
        catch (ClusterStateException e)
        {
            errorCollection.addErrorMessage(e.getMessage(), SERVER_ERROR);
        }
        return new ServiceResultImpl(errorCollection);
    }

    private boolean hasManageNodePermission(final ApplicationUser user)
    {
        return globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private void rejectAsForbidden(final ErrorCollection errorCollection)
    {
        final String errorMessage = i18nHelper.getText(FORBIDDEN_MESSAGE_CODE);
        errorCollection.addErrorMessage(errorMessage, FORBIDDEN);
    }

    /**
     * A callback that allows arbitrary invocation of a NodeStateManager.
     */
    private interface NodeManagerCallback
    {
        /**
         * Executes this callback against the given NodeStateManager.
         *
         * @param nodeStateManager the manager to invoke (never null)
         * @throws ClusterStateException when the NodeStateManager fails to do as it is told.
         */
        void execute(NodeStateManager nodeStateManager) throws ClusterStateException;
    }
}
