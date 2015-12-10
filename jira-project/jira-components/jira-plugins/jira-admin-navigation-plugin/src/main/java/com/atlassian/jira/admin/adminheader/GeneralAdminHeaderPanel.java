package com.atlassian.jira.admin.adminheader;

import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a simple soy generated web-panel containing the admin header including a new admin search link
 *
 *
 * @since v6.0
 */
public class GeneralAdminHeaderPanel implements WebPanel
{
    private final SoyTemplateRenderer soyTemplateRenderer;
    public static final String SYSTEM_ADMIN_HEADER_PAGEACTIONS = "system.admin.header.pageactions";
    private final SimpleLinkManager simpleLinkManager;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GeneralAdminHeaderPanel.class);
    private final JiraAuthenticationContext authenticationContext;

    public GeneralAdminHeaderPanel(final SoyTemplateRenderer soyTemplateRenderer, final SimpleLinkManager simpleLinkManager, final JiraAuthenticationContext authenticationContext)
    {
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.simpleLinkManager = simpleLinkManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String getHtml(final Map<String, Object> stringObjectMap)
    {
        try
        {
            final JiraHelper jiraHelper = new JiraHelper(ExecutingHttpRequest.get());
            final List<SimpleLink> items = simpleLinkManager.getLinksForSection(SYSTEM_ADMIN_HEADER_PAGEACTIONS, authenticationContext.getLoggedInUser(), jiraHelper);
            final List<String> actions = new ArrayList<String>(items.size());
            for (SimpleLink link : items)
            {
                actions.add(new PageAction(link, soyTemplateRenderer).getHtml());
            }
            stringObjectMap.put("pageActions", actions);

            return soyTemplateRenderer.render("com.atlassian.jira.jira-admin-navigation-plugin:admin-header-new-nav-soy","JIRA.Templates.header.admin.adminheading", stringObjectMap);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for admin header");
            log.debug("Exception: ",e);
        }
        return null;
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> stringObjectMap) throws IOException
    {
        try
        {
            final JiraHelper jiraHelper = new JiraHelper(ExecutingHttpRequest.get());
            final List<SimpleLink> items = simpleLinkManager.getLinksForSection(SYSTEM_ADMIN_HEADER_PAGEACTIONS, authenticationContext.getLoggedInUser(), jiraHelper);
            final List<String> actions = new ArrayList<String>(items.size());
            for (SimpleLink link : items)
            {
                actions.add(new PageAction(link, soyTemplateRenderer).getHtml());
            }
            stringObjectMap.put("pageActions", actions);

            soyTemplateRenderer.render(writer,"com.atlassian.jira.jira-admin-navigation-plugin:admin-header-new-nav-soy","JIRA.Templates.header.admin.adminheading", stringObjectMap);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for admin header");
            log.debug("Exception: ",e);
        }
    }

    public static class PageAction
    {
        private final SimpleLink link;
        private final SoyTemplateRenderer soyTemplateRenderer;

        private PageAction(SimpleLink link, SoyTemplateRenderer soyTemplateRenderer)
        {
            this.link = link;
            this.soyTemplateRenderer = soyTemplateRenderer;
        }

        public String getHtml()
        {
            try
            {
                final Map<String,Object> data = new HashMap<String,Object>();
                data.put("id", link.getId());
                data.put("url", link.getUrl());
                data.put("text", link.getLabel());
                data.put("extraClasses", link.getStyleClass());

                final String iconClass = (null != link.getParams()) ? link.getParams().get("iconClass") : "";
                if (!StringUtils.isEmpty(iconClass))
                {
                    data.put("iconType", "custom");
                    data.put("iconClass", "icon " + iconClass);
                    data.put("iconText", "");
                }

                return soyTemplateRenderer.render("jira.webresources:soy-templates", "JIRA.Templates.Links.button", data);
            }
            catch (SoyException e)
            {
                throw new RuntimeException("Failed to render button for '"+SYSTEM_ADMIN_HEADER_PAGEACTIONS+"' web-section.", e);
            }
        }
    }

}
