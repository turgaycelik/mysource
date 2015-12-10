package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.project.browse.BrowseProjectContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * A {@link com.atlassian.jira.project.browse.BrowseProjectContext} specific implemention
 * of the JiraHelper.
 *
 * @since v4.0
 */
public class ProjectHelper extends JiraHelper
{
    private final BrowseProjectContext projectContext;

    public ProjectHelper(HttpServletRequest request, BrowseProjectContext projectContext)
    {
        super(request, projectContext.getProject());
        this.projectContext = projectContext;
    }

    public BrowseProjectContext getProjectContext()
    {
        return projectContext;
    }

    public String getQueryString()
    {
        StringBuilder sb = new StringBuilder();
        if (projectContext != null)
        {
            sb.append(projectContext.getQueryString());
        }
        return sb.toString();
    }

    @Override
    public Map<String, Object> getContextParams()
    {
        final Map<String, Object> params = super.getContextParams();

        params.put("projectContext", projectContext);

        return params;
    }
}
