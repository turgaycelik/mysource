package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;

import java.util.Collection;

/**
 * Interface that should be implemented by actions which wish to display search requests with multiple views (both
 * system defined and pluginised ones).
 *
 * @since v4.0
 */
public interface SearchRequestViewsAction
{
    public SearchRequestViewModuleDescriptor getPrintable();

    public SearchRequestViewModuleDescriptor getFullContent();

    public SearchRequestViewModuleDescriptor getXml();

    public SearchRequestViewModuleDescriptor getRssIssues();

    public SearchRequestViewModuleDescriptor getRssComments();

    public SearchRequestViewModuleDescriptor getWord();

    public SearchRequestViewModuleDescriptor getAllExcelFields();

    public SearchRequestViewModuleDescriptor getCurrentExcelFields();

    public SearchRequestViewModuleDescriptor getChart();

    /**
     * @return all the SearchRequestViewModuleDescriptors loaded via plugins (no system defined ones).
     */
    public Collection<SearchRequestViewModuleDescriptor> getNonSystemSearchRequestViews();
}
