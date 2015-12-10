package com.atlassian.jira.web.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.issueview.IssueViewURLHandler;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IssueViewServlet extends HttpServlet
{
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        //For most of the IssueViews (XML, RSS, Word) all URLs should use an absolute path.  We therefore clear the requestcontext cache
        //before rendering anything.
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext(JiraUrl.constructBaseUrl(request), request);
        final IssueViewURLHandler urlHandler = ComponentAccessor.getComponentOfType(IssueViewURLHandler.class);
        urlHandler.handleRequest(request, response);
    }
}
