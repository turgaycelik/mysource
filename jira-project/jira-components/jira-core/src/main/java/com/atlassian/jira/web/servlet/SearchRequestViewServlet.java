package com.atlassian.jira.web.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestURLHandler;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet is responsible for setting up the VelocityContext used in the searchrequest views
 * (i.e.: Printable, XML, etc).  Currently this involves, storing the baseURL in a threadlocal velocity request
 * cache.
 */
public class SearchRequestViewServlet extends HttpServlet
{
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        //For most of the IssueViews (XML, RSS, Word) all URLs should use an absolute path.  We therefore set the baseUrl to what
        //we can gather from the request.
        DefaultVelocityRequestContextFactory.cacheVelocityRequestContext(JiraUrl.constructBaseUrl(request), request);
        final SearchRequestURLHandler searchRequestURLHandler = ComponentAccessor.getComponentOfType(SearchRequestURLHandler.class);
        searchRequestURLHandler.handleRequest(request, response);
    }
}
