package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel for viewing and editing Dark Features. Best pretend you never saw this.
 *
 * @since v5.0
 */
public class DarkFeaturesUserProfilePanel implements ViewProfilePanel, OptionalUserProfilePanel
{
    private final JiraAuthenticationContext authenticationContext;
    private ViewProfilePanelModuleDescriptor moduleDescriptor;
    private PermissionManager permissionManager;
    private FeatureManager featureManager;

    public DarkFeaturesUserProfilePanel(JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager, FeatureManager featureManager)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.featureManager = featureManager;
    }


    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor)
    {
        this.moduleDescriptor = moduleDescriptor;
    }

    public boolean showPanel(User profileUser, User currentUser)
    {
        return featureManager.isEnabled("jira.user.darkfeature.admin") && profileUser.equals(currentUser);
    }

    public String getHtml(User profileUser)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Map<String, Object> params = new HashMap<String, Object>();

        DarkFeatures darkFeatures = featureManager.getDarkFeatures();
        List<String> globalEnabledFeatures = Lists.newArrayList(darkFeatures.getGlobalEnabledFeatureKeys());
        List<String> userEnabledFeatures = Lists.newArrayList(darkFeatures.getUserEnabledFeatures());
        Collections.sort(globalEnabledFeatures);
        Collections.sort(userEnabledFeatures);

        params.put("textUtils", new TextUtils());
        params.put("isAdmin", permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user));
        params.put("globalEnabledFeatures", globalEnabledFeatures);
        params.put("userEnabledFeatures", userEnabledFeatures);

        return moduleDescriptor.getHtml(VIEW_TEMPLATE, params);
    }
}