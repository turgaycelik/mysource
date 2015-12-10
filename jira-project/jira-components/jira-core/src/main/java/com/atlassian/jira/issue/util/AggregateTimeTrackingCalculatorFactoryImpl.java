package com.atlassian.jira.issue.util;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.DocumentIssueImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

/**
 * Implementation of factory
 *
 * @since v4.4
 */
public class AggregateTimeTrackingCalculatorFactoryImpl implements AggregateTimeTrackingCalculatorFactory
{
    private final JiraAuthenticationContext context;
    private final PermissionManager permissionManager;
    private final SearchProviderFactory searchProviderFactory;

    private SearchProvider searchProvider = null;
    private FieldManager fieldManager = null;

    /**
     * Note. Constructor can not take {@link com.atlassian.jira.issue.fields.FieldManager} or
     * {@link com.atlassian.jira.issue.search.SearchProvider} due to cyclic dependencies
     *
     * @param context               JIRA authentication context
     * @param searchProviderFactory serach provider factory
     * @param permissionManager     permission manager
     */
    public AggregateTimeTrackingCalculatorFactoryImpl(JiraAuthenticationContext context, SearchProviderFactory searchProviderFactory, PermissionManager permissionManager)
    {
        this.context = context;
        this.searchProviderFactory = searchProviderFactory;
        this.permissionManager = permissionManager;
    }

    @Override
    public AggregateTimeTrackingCalculator getCalculator(Issue issue)
    {
        if (issue instanceof DocumentIssueImpl)
        {
            return new DocumentIssueAggregateTimeTrackingCalculator(context, searchProviderFactory, getSearchProvider(), getFieldManager());
        }
        else
        {
            return new IssueImplAggregateTimeTrackingCalculator(context, permissionManager);
        }
    }

    private FieldManager getFieldManager()
    {
        if (fieldManager == null)
        {
            fieldManager = ComponentAccessor.getFieldManager();
        }
        return fieldManager;
    }

    public void setFieldManager(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    private SearchProvider getSearchProvider()
    {
        if (searchProvider == null)
        {
            searchProvider = ComponentAccessor.getComponent(SearchProvider.class);
        }
        return searchProvider;
    }

    public void setSearchProvider(SearchProvider searchProvider)
    {
        this.searchProvider = searchProvider;
    }
}
