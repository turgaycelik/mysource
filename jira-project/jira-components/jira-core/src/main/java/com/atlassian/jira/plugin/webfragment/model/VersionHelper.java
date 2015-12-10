package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * A {@link com.atlassian.jira.plugin.versionpanel.BrowseVersionContext} specific implemention
 * of the JiraHelper.
 */
public class VersionHelper extends JiraHelper
{
    private final BrowseVersionContext versionContext;

    public VersionHelper(HttpServletRequest request, BrowseVersionContext versionContext)
    {
        super(request, versionContext.getProject());
        this.versionContext = versionContext;
    }

    public BrowseVersionContext getVersionContext()
    {
        return versionContext;
    }

    public String getQueryString()
    {
        // note: we don't use the code from super#getQueryString() because VersionContext query string already contains
        // a project parameter
        StringBuilder sb = new StringBuilder();
        if (versionContext != null)
        {
            sb.append(versionContext.getQueryString());
        }
        return sb.toString();
    }

    @Override
    public Map<String, Object> getContextParams()
    {
        final Map<String, Object> params = super.getContextParams();
        params.put("versionContext", versionContext);

        return params;
    }
}
