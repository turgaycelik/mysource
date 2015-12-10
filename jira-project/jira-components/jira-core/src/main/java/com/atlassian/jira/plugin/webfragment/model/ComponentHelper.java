package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * A {@link com.atlassian.jira.bc.project.component.ProjectComponent} specific implementation
 * of the Jira Helper
 */
public class ComponentHelper extends JiraHelper
{
    private final BrowseComponentContext context;

    public ComponentHelper(HttpServletRequest request, final BrowseComponentContext context)
    {
        super(request, context.getProject());
        this.context = context;
    }

    public ProjectComponent getComponent()
    {
        return context.getComponent();
    }

    public String getQueryString()
    {
        // note: we don't use the code from super#getQueryString() because BrowseComponentContext query string already
        // contains a project parameter
        StringBuilder sb = new StringBuilder();
        if (context != null)
        {
            sb.append(context.getQueryString());
        }
        return sb.toString();
    }

    @Override
    public Map<String, Object> getContextParams()
    {
        final Map<String, Object> params = super.getContextParams();

        params.put("component", context.getComponent());
        params.put("componentContext", context);

        return params;
    }
}
