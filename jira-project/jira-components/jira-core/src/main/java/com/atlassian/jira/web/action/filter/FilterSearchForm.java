package com.atlassian.jira.web.action.filter;

/**
 * Represents the form state for a filter search including the multiple tabs, known as filter views.
 */
public interface FilterSearchForm
{
    /**
     * Whether the desired sorting of results is ascending or descending.
     * @return true only if the sort order is ascending.
     */
    public boolean isSortAscending();

    /**
     * The column to sort by.
     * @return the name of the column to sort by.
     */
    public String getSortColumn();

    /**
     * The name of the view or tab.
     * @return
     */
    public String getFilterView();

    public String getSearchName();

    public String getSearchOwnerUserName();

    public String getSearchShareType();

    public String getProjectShare();

    public String getRoleShare();

    public String getGroupShare();

    public Long getPagingOffset();

    public String getShowProjects();

}
