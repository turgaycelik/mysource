package com.atlassian.jira.web.action.util;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.MetalResourcesManager;
import com.atlassian.plugin.PluginAccessor;

/**
 * @since v6.1
 */
public class Error404 extends JiraWebActionSupport
{

    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;

    public Error404(final ApplicationProperties applicationProperties, final PluginAccessor pluginAccessor)
    {
        this.applicationProperties = applicationProperties;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    protected String doExecute() throws Exception
    {

        return SUCCESS;
    }

    /**
     * This method intentionally does not delegates fetching {@link HttpServletRequest} to any components, as they
     * might not be defined when JIRA is not fully operational (for example during setup)
     * @return current request
     */
    @Override
    public HttpServletRequest getHttpRequest()
    {
        return ExecutingHttpRequest.get();
    }

    @ActionViewData
    public String getOriginalURL()
    {
        String attribute = (String) getHttpRequest().getAttribute("javax.servlet.forward.servlet_path");
        if (attribute == null)
        {
            //no servlet_path - 404 page was hit directly
            attribute = getHttpRequest().getRequestURI().substring(getHttpRequest().getContextPath().length());
        }
        String baseUrl = applicationProperties.getDefaultBackedString(APKeys.JIRA_BASEURL);

        //when base URL is not defined (e.g. during setup) try to guess like setup does
        if(baseUrl == null){
            baseUrl = JiraUrl.constructBaseUrl(getHttpRequest());
        }
        return baseUrl + attribute;
    }

    @ActionViewData
    public String getJiraTitle()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE);
    }

    @ActionViewData
    public String getFooterContent()
    {
        FooterModuleDescriptor footer = (FooterModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.footer:standard-footer");
        if (footer != null)
        {
            return footer.getModule().getFullFooterHtml(getHttpRequest());
        }
        return "";
    }

    @ActionViewData
    public String getResourcesContent()
    {
        return MetalResourcesManager.getMetalResources(getHttpRequest().getContextPath());
    }
}
