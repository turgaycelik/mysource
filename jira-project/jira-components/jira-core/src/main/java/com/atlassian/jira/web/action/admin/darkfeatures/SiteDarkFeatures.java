package com.atlassian.jira.web.action.admin.darkfeatures;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Manage site wide dark features.  This is copied from Confluence
 *
 * @since v5.0
 */
@WebSudoRequired
public class SiteDarkFeatures extends JiraWebActionSupport
{
    private String featureKey;
    private final FeatureManager featureManager;
    private DarkFeatures darkFeatures;
    private final PermissionManager permissionManager;

    public SiteDarkFeatures(FeatureManager featureManager, PermissionManager permissionManager)
    {
        this.featureManager = featureManager;
        this.permissionManager = permissionManager;
    }

    public boolean isPermitted()
    {
        return featureManager.hasSiteEditPermission();
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!featureManager.hasSiteEditPermission())
        {
            return "securitybreach";
        }
        return SUCCESS;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!featureManager.hasSiteEditPermission())
        {
            return "securitybreach";
        }

        // Enable a dark feature
        if (StringUtils.isNotBlank(featureKey))
        {
            featureManager.enableSiteDarkFeature(featureKey.trim());
        }

        featureKey = "";
        return forceRedirect("SiteDarkFeatures!default.jspa");
    }

    @RequiresXsrfCheck
    public String doRemove()
    {
        // Disable a dark feature
        if (StringUtils.isNotBlank(featureKey))
        {
            featureManager.disableSiteDarkFeature(featureKey.trim());
        }

        featureKey = "";
        return forceRedirect("SiteDarkFeatures!default.jspa");
    }

    public List<String> getSystemEnabledFeatures()
    {
        List<String> enabledFeatures = Lists.newArrayList(getDarkFeatures().getSystemEnabledFeatures());
        Collections.sort(enabledFeatures);
        return enabledFeatures;
    }

    public List<String> getSiteEnabledFeatures()
    {
        List<String> enabledFeatures = Lists.newArrayList(getDarkFeatures().getSiteEnabledFeatures());
        Collections.sort(enabledFeatures);
        return enabledFeatures;
    }

    private DarkFeatures getDarkFeatures()
    {
        if (darkFeatures == null)
        {
            darkFeatures = featureManager.getDarkFeatures();
        }
        return darkFeatures;
    }

    public void setFeatureKey(String featureKey)
    {
        this.featureKey = featureKey;
    }

    public boolean isEnabled(String featureKey)
    {
        return featureManager.isEnabled(featureKey);
    }

    public String getFeatureKey()
    {
        return featureKey;
    }
}
