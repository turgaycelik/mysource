package com.atlassian.jira.dashboard.permission;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * Class ensures users can see only the gadgets they have permission to.  This ensures that gadgets a user can't see
 * will be removed from the gadget directory and not rendered on a dashboard.
 *
 * @since v4.0
 */
public class JiraPluginGadgetSpecProviderPermission implements PluginGadgetSpecProviderPermission
{
    private final GadgetPermissionManager gadgetPermissionManager;
    private final JiraAuthenticationContext authenticationContext;


    public JiraPluginGadgetSpecProviderPermission(final GadgetPermissionManager gadgetPermissionManager,
            final JiraAuthenticationContext authenticationContext)
    {
        this.gadgetPermissionManager = gadgetPermissionManager;
        this.authenticationContext = authenticationContext;
    }

    public Vote voteOn(final PluginGadgetSpec pluginGadgetSpec)
    {
        return gadgetPermissionManager.voteOn(pluginGadgetSpec, authenticationContext.getUser());
    }
}
