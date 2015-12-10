package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.ExcelColumnLayoutItem;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.web.component.subtask.ColumnLayoutItemFactory;
import com.atlassian.query.Query;
import com.atlassian.query.order.SearchSort;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A class for creating {@link IssueTableLayoutBean} objects.
 */
public class TableLayoutFactory
{
    private final FieldManager fieldManager;
    private final TableLayoutUtils tableLayoutUtils;
    private final ColumnLayoutItemFactory columnLayoutItemFactory;
    private final UserPreferencesManager userPreferencesManager;
    private final SearchService searchService;
    private final SearchSortUtil searchSortUtil;

    public TableLayoutFactory(FieldManager fieldManager,
            TableLayoutUtils tableLayoutUtils, ColumnLayoutItemFactory columnLayoutItemFactory,
            UserPreferencesManager userPreferencesManager, SearchService searchService,
            SearchSortUtil searchSortUtil)
    {
        this.fieldManager = fieldManager;
        this.tableLayoutUtils = tableLayoutUtils;
        this.columnLayoutItemFactory = columnLayoutItemFactory;
        this.userPreferencesManager = userPreferencesManager;
        this.searchService = searchService;
        this.searchSortUtil = searchSortUtil;
    }

    public IssueTableLayoutBean getStandardLayout(SearchRequest searchRequest, User user)
    {
        final IssueTableLayoutBean layoutBean = new IssueTableLayoutBean(getUserColumns(searchRequest, user), getSearchSorts(searchRequest));
        if (user != null)
        {
            layoutBean.setShowActionColumn(true);
        }
        return layoutBean;
    }

    /**
     * Get the columns relevant for the dashboard, but limiting to a specific list of column names.  The column names have to match {@link com.atlassian.jira.issue.IssueFieldConstants}.  If you pass null
     * you will get the default columns.
     * 
     * @param columnNames   The columns to display.  If no columns are specified, this will default to the columns specified in the jira-application.properties
     * file with the key represented by {@link com.atlassian.jira.config.properties.APKeys#ISSUE_TABLE_COLS_DASHBOARD}
     * @return A layout using the columns specified or default, with the correct display properties for the Dashboard.
     */

    public IssueTableLayoutBean getDashboardLayout(User user, List<String> columnNames) throws FieldException
    {
        IssueTableLayoutBean layout = new IssueTableLayoutBean(getDashboardColumns(user, columnNames));
        layout.setSortingEnabled(false);
        layout.setDisplayHeader(false);
        layout.setShowExteriorTable(false);
        layout.setTableCssClass("grid issuetable-db maxWidth");
        
        // note that the uniqueId here _should_ be used to set unique ids on the table, as there may be multiple
        // tables on one page (as is the case with AbstractSearchResultsPortlet and all portlets that extend it).
        //
        // However, a lot of the CSS for the issue table was based on the css 'id', not based on the css 'class' as it
        // should have been.  To change to id would have been a large change, and I'm afraid I didn't get to it (SF - 15/Nov/08)
        //
        // As a result of this, there will be two tables with the same 'id' on the dashboard.  This doesn't seem to cause
        // any problems that I could see.
        //
        // layout.setTableHtmlId(uniqueId);
        return layout;
    }

    private List<ColumnLayoutItem> getDashboardColumns(final User user, List<String> columnNames) throws FieldException
    {
        if (columnNames == null || columnNames.isEmpty())
            columnNames = tableLayoutUtils.getDefaultColumnNames(APKeys.ISSUE_TABLE_COLS_DASHBOARD);

        return tableLayoutUtils.getColumns(user, columnNames);
    }

    public IssueTableLayoutBean getPrintableLayout(SearchRequest searchRequest, User user)
    {
        IssueTableLayoutBean layout = new IssueTableLayoutBean(getUserColumns(searchRequest, user), getSearchSorts(searchRequest));
        layout.setSortingEnabled(false); // printable doesn't have sorting
        layout.addCellDisplayParam(FieldRenderingContext.PRINT_VIEW, Boolean.TRUE);
        return layout;
    }

    public IssueTableLayoutBean getStandardExcelLayout(SearchRequest searchRequest, User user)
    {
        IssueTableLayoutBean standardLayout = new IssueTableLayoutBean(getExcelUserColumns(searchRequest, user), getSearchSorts(searchRequest));
        setExcelLayout(standardLayout);
        return standardLayout;
    }

    public IssueTableLayoutBean getAllColumnsExcelLayout(SearchRequest searchRequest, User user)
    {
        IssueTableLayoutBean standardLayout = new IssueTableLayoutBean(getAllUserExcelColumns(searchRequest, user), getSearchSorts(searchRequest));
        setExcelLayout(standardLayout);
        return standardLayout;
    }

    /**
     * Get the layout for sub-tasks on the view issue page.
     * <p/>
     * Users can specify which columns to show in jira-application.properties.  The default columns that are always shown:
     * <ul>
     * <li>The sequence number (1, 2, etc)
     * <li>The summary of the sub-task
     * <li>The controls to re-order sub-tasks
     * <li>The links to update workflow on the subtasks
     * </ul>
     * <p/>
     * The standard columns are retrieved from {@link com.atlassian.jira.web.component.subtask.ColumnLayoutItemFactory}.
     *
     * @param user             To get the available columns from
     * @param parentIssue      The parent issue of all the subTasks
     * @param subTaskBean      The subTask bean that contains all the subtasks that will be displayed on this page
     * @param subTaskView      The 'view' which is passed to the subTaskBean to get the list of subtasks to display.  Usually either 'unresolved' or 'all'
     * @param timeTrackingData whether or not time tracking data should be shown
     * @return the IssueTableLayoutBean based on the application configuration.
     * @throws ColumnLayoutStorageException if there is a problem accessing the column layout backing data
     * @throws FieldException               if there is a problem accessing the field backing data
     */
    public IssueTableLayoutBean getSubTaskIssuesLayout(User user, final Issue parentIssue, final SubTaskBean subTaskBean, final String subTaskView, boolean timeTrackingData) throws ColumnLayoutStorageException, FieldException
    {
        List<String> userSpecifiedColumns = tableLayoutUtils.getDefaultColumnNames(APKeys.ISSUE_TABLE_COLS_SUBTASK);

        // please don't look at this code, your morals will be compromised...
        /// QUALITY: OFF
        if (!timeTrackingData && userSpecifiedColumns != null)
        {
            userSpecifiedColumns = new ArrayList<String>(userSpecifiedColumns);
            userSpecifiedColumns.remove("progress");
        }
        /// QUALITY: ON
        // ok you can look again now

        final ColumnLayoutItem displaySequence = columnLayoutItemFactory.getSubTaskDisplaySequenceColumn();
        final ColumnLayoutItem simpleSummary = columnLayoutItemFactory.getSubTaskSimpleSummaryColumn();
        final ColumnLayoutItem subTaskReorder = columnLayoutItemFactory.getSubTaskReorderColumn(user, parentIssue, subTaskBean, subTaskView);

        final List<ColumnLayoutItem> columns = new ArrayList<ColumnLayoutItem>();
        columns.add(displaySequence);
        columns.add(simpleSummary);
        columns.addAll(tableLayoutUtils.getColumns(user, userSpecifiedColumns));
        columns.add(subTaskReorder);

        final IssueTableLayoutBean layout = new IssueTableLayoutBean(columns, Collections.<SearchSort>emptyList());
        layout.setSortingEnabled(false);
        layout.setDisplayHeader(false);
        layout.setShowExteriorTable(false);
        layout.setTableCssClass(""); //override the grid CSS class
        layout.setShowActionColumn(true);
        return layout;
    }

    private void setExcelLayout(IssueTableLayoutBean layoutBean)
    {
        layoutBean.setSortingEnabled(false);
        layoutBean.setAlternateRowColors(false);
        layoutBean.addCellDisplayParam(IssueTableLayoutBean.CELL_NO_LINK, Boolean.TRUE);
        layoutBean.addCellDisplayParam(IssueTableLayoutBean.CELL_TEXT_ONLY, Boolean.TRUE);
        layoutBean.addCellDisplayParam(IssueTableLayoutBean.FULL_LINK, Boolean.TRUE);
        layoutBean.addCellDisplayParam(FieldRenderingContext.EXCEL_VIEW, Boolean.TRUE);
    }

    private List<SearchSort> getSearchSorts(SearchRequest searchRequest)
    {
        if (searchRequest != null)
        {
            return searchSortUtil.getSearchSorts(searchRequest.getQuery());
        }
        return null;
    }

    /**
     * Gets all the available navigable fields for the given user and search requests project, also removing any custom
     * fields with no view ({@link com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor#TEMPLATE_NAME_VIEW}
     * and {@link com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor#TEMPLATE_NAME_COLUMN}) defined.
     *
     * @param searchRequest used to determine the project for which to check field visibility
     * @param user          used to determine the user for which to check field visibility
     * @return {@link List} of {@link ExcelColumnLayoutItem}
     */
    private List<ColumnLayoutItem> getAllUserExcelColumns(SearchRequest searchRequest, User user)
    {
        try
        {
            final List<ColumnLayoutItem> columnItems = new ArrayList<ColumnLayoutItem>();
            final Set<NavigableField> availableFields;
            final Query query = searchRequest.getQuery();
            if (query.getWhereClause() == null)
            {
                availableFields = fieldManager.getAvailableNavigableFieldsWithScope(user);
            }
            else
            {
                final QueryContext queryContext = searchService.getQueryContext(user, query);
                availableFields = fieldManager.getAvailableNavigableFieldsWithScope(user, queryContext);
            }

            // Remove all custom fields that do not have view values - JRA-11514
            for (Iterator<NavigableField> iterator = availableFields.iterator(); iterator.hasNext();)
            {
                final NavigableField field = iterator.next();
                if (field instanceof CustomField)
                {
                    final CustomField customField = (CustomField) field;
                    if (!customField.getCustomFieldType().getDescriptor().isViewTemplateExists() &&
                            !customField.getCustomFieldType().getDescriptor().isColumnViewTemplateExists())
                    {
                        iterator.remove();
                    }
                    else
                    {
                        columnItems.add(new ExcelColumnLayoutItem(field, columnItems.size()));
                    }
                }
                else
                {
                    columnItems.add(new ExcelColumnLayoutItem(field, columnItems.size()));
                }
            }

            return columnItems;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    List<ColumnLayoutItem> getUserColumns(SearchRequest searchRequest, User user)
    {
        if (searchRequest == null)
        {
            throw new NullPointerException("searchRequest cannot be null");
        }
        try
        {
            return getColumnsProvider().getColumns(user, searchRequest);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    ColumnLayoutItemProvider getColumnsProvider()
    {
        return new ColumnLayoutItemProvider();
    }

    private List<ColumnLayoutItem> getExcelUserColumns(SearchRequest searchRequest, User user)
    {
        final List<ColumnLayoutItem> visibleColumns = new ArrayList<ColumnLayoutItem>(getUserColumns(searchRequest, user));

        CollectionUtils.transform(visibleColumns, new Transformer()
        {
            public Object transform(Object input)
            {
                return new ExcelColumnLayoutItem((ColumnLayoutItem) input);
            }
        });

        return visibleColumns;
    }
}
