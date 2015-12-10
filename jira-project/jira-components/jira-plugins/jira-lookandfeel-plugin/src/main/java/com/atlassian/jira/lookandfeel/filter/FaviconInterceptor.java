package com.atlassian.jira.lookandfeel.filter;


import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.lookandfeel.LogoChoice;
import com.atlassian.jira.lookandfeel.LookAndFeelConstants;
import com.atlassian.jira.lookandfeel.LookAndFeelProperties;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The entire purpose of this filter is to redirect all Favicons to the one that JIRA  provides.
 */
public class FaviconInterceptor implements Filter
{
    private FilterConfig config;
    private LookAndFeelProperties properties;
    private final JiraHome jiraHome;
    private final PluginSettings globalSettings;
    private final WebResourceUrlProvider webResourceUrlProvider;

    private static final String JIRA_FAVICON_HIRES = "jira-favicon-hires.png";
    private static final String JIRA_FAVICON = "jira-favicon-scaled.png";
    private static final String JIRA_FAVICON_IE = "jira-favicon-scaled.ico";


    public FaviconInterceptor(final LookAndFeelProperties properties,
                              final JiraHome jiraHome,
                              final PluginSettingsFactory pluginSettingsFactory,
                              final WebResourceUrlProvider webResourceUrlProvider)
    {
        this.properties = properties;
        this.jiraHome = jiraHome;
        this.globalSettings = pluginSettingsFactory.createGlobalSettings();
        this.webResourceUrlProvider = webResourceUrlProvider;

    }

    public void init(final FilterConfig filterConfig) throws ServletException
    {
        this.config = filterConfig;
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        if(request instanceof HttpServletRequest) {
            final HttpServletResponse res = (HttpServletResponse )response;
            final HttpServletRequest req = (HttpServletRequest)request;
            String requestURL = req.getRequestURL().toString();
            //JST-5361 ignore request for attachments
            if (requestURL.contains("/attachment/"))
            {
                chain.doFilter(request, response);
                return;
            }
            if(requestURL.contains("favicon")) {

                //check that the favicon is not default
                if (properties.getFaviconChoice().equals(LogoChoice.JIRA))
                {
                    if ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON)))
                    {
                        String faviconUrl = (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
                        if (!faviconUrl.startsWith("http") && !faviconUrl.startsWith("."))
                        {
                            faviconUrl =  webResourceUrlProvider.getStaticResourcePrefix(UrlMode.AUTO) + faviconUrl;
                        }
                        res.sendRedirect(faviconUrl);
                    }
                    else
                    {
                        chain.doFilter(request, response);
                    }
                    return;
                }

                // The null check is here because if you are in jira and ask for Application.JIRA it returns null
                //String correctURL = (jiraInstance == null ? studioInfo.getStudioBaseUrl() : jiraInstance.getUrl()) + FAVICON_SERVLET;
                //res.sendRedirect(res.encodeRedirectURL(correctURL));
                ImageDownloader downloader = new ImageDownloader();
                if (requestURL.contains(".ico"))
                {
                    downloader.doDownload(req, res, config.getServletContext(), jiraHome.getHomePath()+"/logos/"+ JIRA_FAVICON_IE, true);
                }
                else if (requestURL.contains("hires"))
                {
                    downloader.doDownload(req, res, config.getServletContext(), jiraHome.getHomePath()+"/logos/"+ JIRA_FAVICON_HIRES, true);
                }
                else
                {
                    downloader.doDownload(req, res, config.getServletContext(), jiraHome.getHomePath()+"/logos/"+ JIRA_FAVICON, true);
                }
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    private void addCacheHeaders(HttpServletResponse res)
    {

    }

    public void destroy() { }
}
