package com.atlassian.jira.web.component.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.web.util.AccessKeyHelperImpl;

import java.util.List;
import java.util.Map;

/**
 * This class displays sections of the web interface through plugins
 * <p/>
 * The layout of sections and its items are handled by the velocity template file
 */
public class WebFragmentWebComponent extends AbstractWebComponent
{
    private final JiraWebInterfaceManager webInterfaceManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SimpleLinkManager simpleLinkManager;

    public WebFragmentWebComponent(JiraWebInterfaceManager webInterfaceManager, VelocityTemplatingEngine templatingEngine,
                                   ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
                                   SimpleLinkManager simpleLinkManager)
    {
        super(templatingEngine, applicationProperties);
        this.webInterfaceManager = webInterfaceManager;
        this.authenticationContext = authenticationContext;
        this.simpleLinkManager = simpleLinkManager;
    }

    public String getHtml(String template, String location, JiraHelper jiraHelper)
    {
        return getHtml(template, location, jiraHelper, null);
    }

    public String getHtml(String template, String location, JiraHelper jiraHelper, ContextLayoutBean layoutBean)
    {
        return getHtml(template, getDefaultParams(makeContext(location, authenticationContext.getLoggedInUser(), jiraHelper, layoutBean, authenticationContext.getI18nHelper())));
    }

    private Map<String, Object> makeContext(String location, User remoteUser, JiraHelper jiraHelper, ContextLayoutBean layoutBean, I18nHelper i18n)
    {
        return MapBuilder.<String, Object>newBuilder("webInterfaceManager", webInterfaceManager)
                .add("linkManager", simpleLinkManager)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_LOCATION, location)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_USER, remoteUser)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper)
                .add(JiraWebInterfaceManager.CONTEXT_KEY_I18N, i18n)
                .add("accessKeyHelper", new AccessKeyHelperImpl())
                .add("layout", layoutBean).toMap();
    }

    private Map<String, Object> getDefaultParams(Map<String, Object> startingParams)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

    /*
     * Checks whether the location has any displayable sections and items for the user
     */
    public boolean hasDisplayableItems(String location, JiraHelper jiraHelper)
    {
        User remoteUser = authenticationContext.getLoggedInUser();

        if (!simpleLinkManager.getLinksForSection(location, remoteUser, jiraHelper).isEmpty())
        {
            return true;
        }

        final List<SimpleLinkSection> displayableSections = simpleLinkManager.getSectionsForLocation(location, remoteUser, jiraHelper);
        for (SimpleLinkSection section : displayableSections)
        {
            if (!simpleLinkManager.getLinksForSection(location + "/" + section.getId(), remoteUser, jiraHelper).isEmpty())
            {
                return true;
            }
        }
        return false;
    }
}
