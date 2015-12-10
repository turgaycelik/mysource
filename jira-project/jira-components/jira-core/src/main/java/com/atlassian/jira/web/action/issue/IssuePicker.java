package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Responsible for controlling the rendering of the Issue Picker control. This control is shown as a pop-up window that
 * allows an user to select one or more issues using a list of the most recently viewed issues, the current search or a
 * filter.
 */
public class IssuePicker extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(IssuePicker.class);
    private static final String RECENT = "recent"; //show the recently viewed issues
    private static final String SEARCH = "search"; //show issues from a specific search
    private static final int MAX_ISSUES_RETURNED = 50;
    private static final PagerFilter PAGER_FILTER = new PagerFilter(MAX_ISSUES_RETURNED); // show 50 issues

    private static final String SELECT_SINGLE = "single";
    private static final String SELECT_MULTIPLE = "multiple";

    private final BugAssociatorPrefs bugAssociatorPrefs;
    private final IssueManager issueManager;
    private final SearchProvider searchProvider;
    private final SearchRequestService searchRequestService;
    private final UserIssueHistoryManager userHistoryManager;

    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    //---- Getters & Setters (parameters) ---//
    private String mode = null;
    private String selectMode = null;
    private long searchRequestId = -1;
    private String currentIssue;
    private boolean singleSelectOnly = false;
    private boolean showSubTasks = true;
    private boolean showSubTasksParent;

    private Long selectedProjectId = null;

    //derived
    private SearchRequest searchRequest;

    public IssuePicker(BugAssociatorPrefs bugAssociatorPrefs, IssueManager issueManager, SearchProvider searchProvider,
            final SearchRequestService searchRequestService, final UserIssueHistoryManager userHistoryManager, final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory)
    {
        this.bugAssociatorPrefs = bugAssociatorPrefs;
        this.issueManager = issueManager;
        this.searchProvider = searchProvider;
        this.searchRequestService = searchRequestService;
        this.userHistoryManager = userHistoryManager;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
    }

    //--- Methods for All Modes ---//
    public Collection<SearchRequest> getAvailableFilters()
    {
        return searchRequestService.getFavouriteFilters(getLoggedInApplicationUser());
    }

    //--- Methods for Mode 'Search' ---//

    /**
     * @return A collection of {@link Issue}s.
     */
    public Collection<Issue> getSearchRequestIssues()
    {
        if (getSearchRequestId() == -1)
        {
            log.warn("Trying to search for issues with no search request Id.");
            return Collections.emptyList();
        }

        searchRequest = searchRequestService.getFilter(getJiraServiceContext(), getSearchRequestId());
        if (searchRequest == null)
        {
            log.warn("Search request Id not valid for this user.");
            return Collections.emptyList();
        }

        return executeSearch(searchRequest);
    }

    //--- Methods for Mode 'Recent' ---//

    /**
     * @return A collection of {@link Issue}s.
     */
    public Collection<Issue> getUserHistoryIssues()
    {
        return filterIssues(userHistoryManager.getShortIssueHistory(getLoggedInUser()));

    }

    public Collection<Issue> getBrowsableIssues()
    {
        final SearchRequest currentSearch = getSearchRequestFromSession();
        return executeSearch(currentSearch);
    }

    private SearchRequest getSearchRequestFromSession()
    {
        final SessionSearchRequestManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createSearchRequestManager();
        return sessionSearchRequestManager.getCurrentObject();
    }

    //--- Helper Methods ---//
    private Collection<Issue> executeSearch(SearchRequest searchRequest)
    {
        try
        {
            SearchResults searchResults = searchProvider.search((searchRequest == null) ? null : searchRequest.getQuery(), getLoggedInUser(), PAGER_FILTER);
            return filterIssues(searchResults.getIssues());
        }
        catch (SearchException e)
        {
            log.error("Unable to run search in issue picker.", e);
            return Collections.emptyList();
        }
    }

    // Checks additional options to decide whether to show the issue or not
    private boolean showIssue(Issue issue)
    {
        //if a project Id is specified, only show issues with the same project id
        if (selectedProjectId != null && !selectedProjectId.equals(issue.getProjectObject().getId()))
        {
            return false;
        }

        //if showSubTasks() is disabled don't show subtasks
        if (!showSubTasks && issue.isSubTask())
        {
            return false;
        }
        if (currentIssue != null)
        {
            //Don't show the current issue.
            if (this.currentIssue.equals(issue.getKey()))
            {
                return false;
            }

            //if showSubTasksParent is disabled don't show the current issues parent
            final Issue currentIssue = getIssue();
            if (currentIssue != null && !showSubTasksParent && currentIssue.isSubTask() && currentIssue.getParentId().equals(issue.getId()))
            {
                return false;
            }
        }
        return true;
    }

    private Collection<Issue> filterIssues(Collection<? extends Issue> issues)
    {
        if (issues == null || issues.isEmpty())
        {
            return Collections.emptyList();
        }

        List<Issue> result = new ArrayList<Issue>(issues.size());
        for (Issue issue : issues)
        {
            if (showIssue(issue))
            {
                result.add(issue);
            }
        }
        return result;
    }

    private Issue getIssue()
    {
        return issueManager.getIssueObject(currentIssue);
    }

    private String getDefaultMode()
    {
        int mode = bugAssociatorPrefs.getDefaultMode(getIssue());
        if (BugAssociatorPrefs.RECENT == mode)
        {
            return RECENT;
        }
        else
        {
            return SEARCH;
        }
    }

    private String getDefaultSelectMode()
    {
        int searchMode = bugAssociatorPrefs.getDefaultSearchMode(getIssue());
        if (BugAssociatorPrefs.SINGLE == searchMode)
        {
            return SELECT_SINGLE;
        }
        else if (BugAssociatorPrefs.MULTIPLE == searchMode)
        {
            return SELECT_MULTIPLE;
        }
        else
        {
            return SELECT_SINGLE;
        }
    }

    public long getSearchRequestId()
    {
        if (searchRequestId == -1)
        {
            return bugAssociatorPrefs.getDefaultSearchRequestId(getIssue());
        }
        else
        {
            return searchRequestId;
        }
    }

    public void setSearchRequestId(long searchRequestId)
    {
        this.searchRequestId = searchRequestId;
    }

    public String getSearchRequestName()
    {
        return (searchRequest != null) ? searchRequest.getName() : "";
    }

    public String getCurrentIssue()
    {
        return currentIssue;
    }

    public void setCurrentIssue(String currentIssue)
    {
        this.currentIssue = currentIssue;
    }

    public String getMode()
    {
        if (mode == null || (SEARCH.equals(mode) && searchRequestId < 0))
        {
            mode = getDefaultMode();
        }
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }

    public void setSelectMode(String selectMode)
    {
        this.selectMode = selectMode;
    }

    public String getSelectMode()
    {
        if (selectMode == null)
        {
            selectMode = getDefaultSelectMode();
        }
        return selectMode;
    }

    public boolean isSingleSelectOnly()
    {
        return singleSelectOnly;
    }

    public void setSingleSelectOnly(boolean singleSelectOnly)
    {
        this.singleSelectOnly = singleSelectOnly;
    }

    public Long getSelectedProjectId()
    {
        return selectedProjectId;
    }

    public void setSelectedProjectId(Long selectedProjectId)
    {
        this.selectedProjectId = selectedProjectId;
    }

    /**
     * Returns whether sub-tasks should be displayed in the list of issues.
     * @return true, if sub-tasks are set to be shown in the list of issues; otherwise, false.
     */
    public boolean isShowSubTasks()
    {
        return showSubTasks;
    }

    /**
     * Sets whether sub-tasks should be shown in the list of issues.
     * @param showSubTasks a boolean that indicates whether sub-tasks should be shown in the list of issues.
     */
    public void setShowSubTasks(boolean showSubTasks)
    {
        this.showSubTasks = showSubTasks;
    }

    /**
     * Returns whether the parent of the current issue (the one in the view issue page or the one that's selected in the
     * issue navigator) should be displayed in the list of issues.
     * @return true if the parent of the current issue should be displayed in the list of issues; otherwise, false.
     */
    public boolean isShowSubTasksParent()
    {
        return showSubTasksParent;
    }

    /**
     * Sets whether the parent of the current issue (the one in the view issue page or the one that's selected in the
     * issue navigator) should be displayed in the list of issues.
     * @param showSubTasksParent a boolean that indicates whether the parent of the current issue should be displayed
     * in the list of issues.
     */
    public void setShowSubTasksParent(boolean showSubTasksParent)
    {
        this.showSubTasksParent = showSubTasksParent;
    }
}
