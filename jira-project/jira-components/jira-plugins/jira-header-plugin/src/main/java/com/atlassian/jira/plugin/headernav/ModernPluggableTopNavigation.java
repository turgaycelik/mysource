package com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.plugin.navigation.PluggableTopNavigation;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModernPluggableTopNavigation implements PluggableTopNavigation
{
    private TopNavigationModuleDescriptor descriptor;
    private final WebInterfaceManager webInterfaceManager;
    private final SoyTemplateRenderer soy;
    private final ApplicationProperties applicationProperties;
    private FeatureManager featureManager;
    private final LookAndFeelBean lookAndFeelBean;

    public ModernPluggableTopNavigation(final WebInterfaceManager webInterfaceManager, ApplicationProperties applicationProperties,
            FeatureManager featureManager, SoyTemplateRendererProvider soy)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
        this.lookAndFeelBean = LookAndFeelBean.getInstance(this.applicationProperties);
        this.soy = soy.getRenderer();
    }

    public void init(final TopNavigationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    @Override
    public String getHtml(final HttpServletRequest request)
    {
        final Map<String, Object> context = getTopNavigationContext(request);

        // Build the primary navigation items -- app switcher, create button, etc.
        StringBuilder leftSide = new StringBuilder();
        for(WebPanelModuleDescriptor moduleDescriptor :
            webInterfaceManager.getDisplayableWebPanelDescriptors("com.atlassian.jira.plugin.headernav.left.context", context))
        {
            leftSide.append(moduleDescriptor.getModule().getHtml(context));
        }

        // Build the secondary nav items -- search, admin, profile, etc.
        StringBuilder rightSide = new StringBuilder();
        for (WebPanelModuleDescriptor moduleDescriptor :
            webInterfaceManager.getDisplayableWebPanelDescriptors("com.atlassian.jira.plugin.headernav.right.context", context))
        {
            rightSide.append(moduleDescriptor.getModule().getHtml(context));
        }

        // Call the soy template for the common header.
        try
        {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("primaryNavContent", "<ul class='aui-nav'>" + leftSide.toString() + "</ul>");
            params.put("secondaryNavContent", "<ul class='aui-nav'>" + rightSide.toString() + "</ul>");

            insertAppSwitcher(params);

            params.put("headerLink", applicationProperties.getString(APKeys.JIRA_BASEURL) + "/secure/MyJiraHome.jspa");
            params.put("headerText", getHeaderText(shouldShowTitleInHeader()));

            // TODO JRADEV-14328 - JIRA should know whether a custom logo is being used or not.
            // if (lookAndFeelBean.isCustomLogo())
            // {
                params.put("logo", "");
                params.put("headerLogoText", getHeaderText(!shouldShowTitleInHeader()));
                params.put("headerLogoImageUrl", lookAndFeelBean.getAbsoluteLogoUrl());
            // }
            // else
            // {
                params.put("logo", "jira");
                // TODO JRADEV-14328 - When JIRA knows whether a custom logo is being used or not, remove this
                params.put("logo", "");
                params.put("headerLogoText", getHeaderText(!shouldShowTitleInHeader()));
                params.put("headerLogoImageUrl", lookAndFeelBean.getAbsoluteLogoUrl());
                // END remove this
            // }
            return soy.render("com.atlassian.auiplugin:aui-experimental-soy-templates", "aui.page.header", params);
        }
        catch (SoyException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts the new or the old app switcher depending on whether the feature is enabled. In case of the old app
     * switcher we put a marker class in the header to give the JS/CSS a hint that we are displaying the old version.
     * This code should be removed once the new app switcher is live on all products.
     *
     * @param params
     * @throws SoyException
     */
    private void insertAppSwitcher(Map<String, Object> params) throws SoyException
    {
        if (featureManager.isEnabled("app-switcher.new"))
        {
            params.put("headerBeforeContent", soy.render("com.atlassian.plugins.atlassian-nav-links-plugin:rotp-menu",
                       "navlinks.templates.appswitcher.switcher", Collections.<String, Object>emptyMap()));
        }
        else
        {
            params.put("extraClasses", "app-switcher-old");
        }
    }

    private Map<String, Object> getTopNavigationContext(HttpServletRequest request)
    {
        Map<String,Object> params = descriptor.getTopNavigationContext(request, null);
        params.put("user", params.get("currentUser"));
        params.put("helper", params.get("jiraHelperWithProject"));
        return params;
    }

    private String getHeaderText(boolean shouldShow)
    {
        return shouldShow ? applicationProperties.getText(APKeys.JIRA_TITLE) : "";
    }

    private boolean shouldShowTitleInHeader()
    {
        return applicationProperties.getOption("jira.lf.logo.show.application.title");
    }
}
