package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * @since v6.0
 */
public abstract class AbstractFilterAction extends SearchDescriptionEnabledAction
{
    private final SearchRequestService searchRequestService;

    private Long filterId;
    private SearchRequest filter;

    public AbstractFilterAction(final IssueSearcherManager issueSearcherManager, final SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        searchRequestService = ComponentAccessor.getComponent(SearchRequestService.class);
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(final Long filterId)
    {
        this.filterId = filterId;
    }

    protected SearchRequest getFilter()
    {
        if (filter == null && getFilterId() != null)
        {
            filter = searchRequestService.getFilter(getJiraServiceContext(), getFilterId());
        }
        return filter;
    }

    public String getFilterName() throws GenericEntityException
    {
        final SearchRequest filter = getFilter();
        return filter != null ? filter.getName() : null;
    }
}
