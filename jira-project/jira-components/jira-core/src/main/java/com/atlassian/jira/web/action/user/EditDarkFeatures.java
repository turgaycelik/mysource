package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Updates a User's DarkFeatures by adding or removing them.
 *
 * @since v5.0
 */
public class EditDarkFeatures extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(EditDarkFeatures.class);

    private FeatureManager featureManager;
    private String featureKey;
    private String action;

    public EditDarkFeatures(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        return forceRedirect("ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-darkfeatures-panel");
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        if (featureManager.isEnabled("jira.user.darkfeature.admin"))
        {
            String feature = featureKey.trim();
            if (StringUtils.isNotEmpty(feature))
            {
                User user = getLoggedInUser();
                try
                {
                    if ("remove".equals(action))
                    {
                        featureManager.disableUserDarkFeature(user, feature);
                        log.debug("User '" + user.getName() + "' disabled Dark Feature '" + feature + "'");
                    }
                    else
                    {
                        featureManager.enableUserDarkFeature(user, feature);
                        log.debug("User '" + user.getName() + "' enabled Dark Feature '" + feature + "'");
                    }
                }
                catch (IllegalStateException e)
                {
                    log.warn("User '" + user.getName() + "' attempted to change Core Feature '" + feature + "'. This feature may only be changed site-wide.");
                }
            }
        }
        return forceRedirect("ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-darkfeatures-panel");
    }

    public void setFeatureKey(String featureKey)
    {
        this.featureKey = featureKey;
    }

    public void setAction(String action)
    {
        this.action = action;
    }
}
