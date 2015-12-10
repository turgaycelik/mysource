package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.model.WebIcon;
import com.atlassian.plugin.web.model.WebLink;

/**
 * A jira specific wrapper for the {@link com.atlassian.plugin.web.model.DefaultWebIcon}
 */
public class JiraWebIcon implements WebIcon
{
    private WebIcon webIcon;
    private final JiraAuthenticationContext authenticationContext;

    public JiraWebIcon(WebIcon webIcon, JiraAuthenticationContext authenticationContext)
    {
        this.webIcon = webIcon;
        this.authenticationContext = authenticationContext;
    }

    public WebLink getUrl()
    {
        return new JiraWebLink(webIcon.getUrl(), authenticationContext);
    }

    public int getWidth()
    {
        return webIcon.getWidth();
    }

    public int getHeight()
    {
        return webIcon.getHeight();
    }
}
