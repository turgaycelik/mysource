package com.atlassian.jira.plugin.issueview;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.searchrequestview.HttpRequestHeaders;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class takes care of handling the creation of URLs for the issue view plugin, as well as handling the incoming
 * requests
 */
public class DefaultIssueViewURLHandler implements IssueViewURLHandler
{
    private final PluginAccessor pluginAccessor;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final SearchProvider searchProvider;
    private final IssueViewRequestParamsHelper issueViewRequestParamsHelper;
    private final CrowdService crowdService;

    public DefaultIssueViewURLHandler(PluginAccessor pluginAccessor, IssueManager issueManager, PermissionManager permissionManager, SearchProvider searchProvider, IssueViewRequestParamsHelper issueViewRequestParamsHelper, final CrowdService crowdService)
    {
        this.pluginAccessor = pluginAccessor;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.searchProvider = searchProvider;
        this.issueViewRequestParamsHelper = issueViewRequestParamsHelper;
        this.crowdService = crowdService;
    }

    public String getURLWithoutContextPath(IssueViewModuleDescriptor moduleDescriptor, String issueKey)
    {
        return "/si/" + moduleDescriptor.getCompleteKey() + "/" + issueKey + "/" + issueKey + "." + moduleDescriptor.getFileExtension();
    }

    /**
     * @return A sample URL to be used for used for error messages
     */
    private static String getSampleURL()
    {
        return "/si/jira.issueviews:xml/JRA-10/JRA-10.xml";
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings (value = "HRS_REQUEST_PARAMETER_TO_HTTP_HEADER", justification = "JIRA has a HeaderSanitisingFilter that protects against this")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String pathInfo = request.getPathInfo();
        // JRA-15847: check for null path
        if (StringUtils.isBlank(pathInfo))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }

        //trim any leading slash
        if (pathInfo.startsWith("/"))
        {
            pathInfo = pathInfo.substring(1);
        }

        int firstSlashLocation = pathInfo.indexOf("/");
        if (firstSlashLocation == -1)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }
        String pluginKey = pathInfo.substring(0, firstSlashLocation);

        int secondSlashLocation = pathInfo.indexOf("/", firstSlashLocation + 1);
        if (secondSlashLocation == -1)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path format. Path should be of format " + getSampleURL());
            return;
        }
        String issueKey = pathInfo.substring(firstSlashLocation + 1, secondSlashLocation);

        User user = null;
        if (request.getRemoteUser() != null)
        {
            user = crowdService.getUser(request.getRemoteUser());
            if (user == null)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find a user with the username " + StringEscapeUtils.escapeHtml(request.getRemoteUser()));
                return;
            }
        }

        IssueViewModuleDescriptor moduleDescriptor = getPluginModule(pluginKey);
        if (moduleDescriptor == null)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find any enabled plugin with key " + StringEscapeUtils.escapeHtml(pluginKey));
            return;
        }

        Issue issue;

        if ("index".equalsIgnoreCase(request.getParameter("jira.issue.searchlocation")))
        {
            issue = getIssueFromIndex(issueKey, user);  // for functional tests, we want to do testing with the index
            // we don't need permission checks here, as they are done by the index
        }
        else
        {
            issue = getIssueFromDatabase(issueKey); // by default get things from the database

            if (issue != null && !issue.getKey().equals(issueKey))
            {
                //found a moved issue! Redirect to it...
                String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
                String queryString = request.getQueryString() != null ? '?' + request.getQueryString() : "";
                response.sendRedirect(contextPath + getURLWithoutContextPath(moduleDescriptor, issue.getKey()) + queryString);
                return; //return to avoid double send errors
            }

            if (issue != null && !permissionManager.hasPermission(Permissions.BROWSE, issue, user))
            {
                if (user == null)
                {
                    response.sendRedirect(RedirectUtils.getLoginUrl(request));
                }
                else
                {
                    runJSP(request, response, "/secure/views/permissionviolation.jsp");
                }
                return;
            }
        }

        if (issue == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not find issue with issue key " + StringEscapeUtils.escapeHtml(issueKey));
            return;
        }

        IssueView view = moduleDescriptor.getIssueView();

        IssueViewFieldParams issueViewFieldParams = issueViewRequestParamsHelper.getIssueViewFieldParams(request.getParameterMap());
        if (issueViewFieldParams.isCustomViewRequested() && !issueViewFieldParams.isAnyFieldDefined())
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No valid field defined for issue custom view");
            return;
        }

        IssueViewRequestParams issueViewRequestParams = new IssueViewRequestParamsImpl(issueViewFieldParams);

        String content = view.getContent(issue, issueViewRequestParams);

        if (!"true".equalsIgnoreCase(request.getParameter(NO_HEADERS_PARAMETER)))
        {
            response.setContentType(moduleDescriptor.getContentType() + ";charset=" + ComponentAccessor.getApplicationProperties().getEncoding());
            view.writeHeaders(issue, new HttpRequestHeaders(response), issueViewRequestParams);
        }

        response.getWriter().write(content);
    }

    private IssueViewModuleDescriptor getPluginModule(String pluginKey)
    {
        try
        {
            return (IssueViewModuleDescriptor) pluginAccessor.getEnabledPluginModule(pluginKey);
        }
        catch (ClassCastException e)
        {
            return null;
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private void runJSP(HttpServletRequest request, HttpServletResponse response, final String jspPath) throws IOException
    {
        try
        {
            request.getRequestDispatcher(jspPath).forward(request, response);
        }
        catch (ServletException e)
        {
            throw new RuntimeException("Could not load java server page", e);
        }
    }

    private Issue getIssueFromDatabase(String issueKey)
    {
        return issueManager.getIssueObject(issueKey);
    }

    private Issue getIssueFromIndex(String issueKey, User user)
    {
        final Query query;
        if (StringUtils.isNotBlank(issueKey))
        {
            query = JqlQueryBuilder.newBuilder().where().issue(issueKey).buildQuery();
        }
        else
        {
            query = JqlQueryBuilder.newBuilder().buildQuery();
        }
        try
        {
            SearchResults searchResults = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());
            if (searchResults.getTotal() > 1)
            {
                throw new IllegalStateException("More than one issue returned when searching index for issue key " + issueKey);
            }

            if (searchResults.getTotal() == 0)
            {
                return null;
            }

            return searchResults.getIssues().iterator().next();
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

}
