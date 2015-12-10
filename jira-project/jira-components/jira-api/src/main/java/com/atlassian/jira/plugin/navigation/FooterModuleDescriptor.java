package com.atlassian.jira.plugin.navigation;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.OrderableModuleDescriptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Module descriptor for footer modules.
 *
 * @since v3.12
 */
public interface FooterModuleDescriptor extends JiraResourcedModuleDescriptor<PluggableFooter>, OrderableModuleDescriptor
{
    /**
     * This method will setup the params related to the license information and render the html for the footer.
     *
     * @param request the servlet request
     * @param startingParams any parameters that you want to have available in the context when rendering the footer.
     * @return html representing the footer.
     */
    public String getFooterHtml(HttpServletRequest request, Map startingParams);
}
