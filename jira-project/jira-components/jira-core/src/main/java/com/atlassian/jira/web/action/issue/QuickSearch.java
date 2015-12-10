package com.atlassian.jira.web.action.issue;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.QuickBrowseEvent;
import com.atlassian.jira.event.issue.QuickSearchEvent;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.util.QueryCreator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.opensymphony.util.TextUtils;
import webwork.action.ServletActionContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class QuickSearch extends ProjectActionSupport
{
    public String searchString;
    private final QueryCreator queryCreator;
    private final EventPublisher eventPublisher;

    public QuickSearch(ProjectManager projectManager, QueryCreator queryCreator, PermissionManager permissionManager, EventPublisher eventPublisher)
    {
        super(projectManager, permissionManager);
        this.queryCreator = queryCreator;
        this.eventPublisher = eventPublisher;
    }

    public QuickSearch(QueryCreator queryCreator, EventPublisher eventPublisher)
    {
        this.queryCreator = queryCreator;
        this.eventPublisher = eventPublisher;
    }

    protected String doExecute() throws Exception
    {
        if (TextUtils.stringSet(getKey()))
        {
            eventPublisher.publish(new QuickBrowseEvent(getKey()));
            return getRedirect("/browse/" + getKey());
        }
        else
        {
            eventPublisher.publish(new QuickSearchEvent(searchString));
            // JRA-3309 - This enables i18n characters in quick search (form in bodytop.jsp is also changed to a post)
            sendInternalRedirect(createQuery(searchString));
            return NONE;
        }
    }

    protected void sendInternalRedirect(String redirectLocation) throws ServletException, IOException {
        final HttpServletRequest request = ServletActionContext.getRequest();
        request.getRequestDispatcher(redirectLocation).forward(request, ServletActionContext.getResponse());
    }

    protected String createQuery(String searchString)
    {
        //        return "IssueNavigator.jspa?reset=true&mode=hide&summary=true&description=true&query=" + URLEncoder.encode(searchString);
        return queryCreator.createQuery(searchString);
    }

    /**
     * Get the key using various lookup mechanisms.
     * <ul>
     * <li> If the key is a valid project (irrespective of the number), then return it
     * <li> If the key is a number - return the current project key + number, or if
     * no project is found - return the normal number
     * <li> Else return null.
     * </ul>
     *
     * @throws Exception
     */
    public String getKey() throws Exception
    {
        if (searchString == null)
            return null;

        String potentialKey = searchString.toUpperCase().trim(); //issue keys are always upper case

        if (JiraKeyUtils.validIssueKey(potentialKey))
        {
            return potentialKey;
        }
        else
        {
            try
            {
                //if the search string is a number, then look it up.
                Long.parseLong(potentialKey);

                Project searchProject = getSearchProject();

                //if we find a project - return a key
                if (searchProject != null)
                {
                    return searchProject.getKey() + "-" + (potentialKey.startsWith("-") ? potentialKey.substring(1, potentialKey.length()) : potentialKey);
                }

                //else return the normal number.  The user will get an 'issue not found' page
                else
                    return potentialKey;
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    private Project getSearchProject() throws PermissionException
    {
        // try the issue with the current project key on the front (maybe they just put in a number)
        final Project project = getSelectedProjectObject();
        if (project != null)
        {
            return project;
        }
        else if (getBrowsableProjects().size() == 1)
        {
            // otherwise if there is only one project - try that one
            return getBrowsableProjects().iterator().next();
        }
        else
        {
            //there is no selected project, and there is more than one project to choose from
            return null;
        }
    }

    public void setSearchString(String searchString)
    {
        this.searchString = searchString;
    }
}
