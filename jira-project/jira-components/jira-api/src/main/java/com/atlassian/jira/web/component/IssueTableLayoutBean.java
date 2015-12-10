package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldRenderingContext;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.query.order.SearchSort;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This bean is used to control the display properties of the IssueTable, and works with
 * {@link IssueTableWebComponent} and issuetable.vm to achieve this.
 * <p>
 * It contains sensible defaults.
 */
public class IssueTableLayoutBean
{
    private static final Logger log = Logger.getLogger(IssueTableLayoutBean.class);

    public static final String CELL_NO_LINK = "nolink";
    public static final String CELL_TEXT_ONLY = "textOnly";
    public static final String FULL_LINK = "full_link";

    public static final String DEFAULT_COLUMNS = "--default--";

    /**
     * @deprecated use {@link FieldRenderingContext#EXCEL_VIEW} instead
     */
    public static final String EXCEL_VIEW = FieldRenderingContext.EXCEL_VIEW;

    /**
     * @deprecated use {@link FieldRenderingContext#NAVIGATOR_VIEW} instead
     */
    public static final String NAVIGATOR_VIEW = FieldRenderingContext.NAVIGATOR_VIEW;

    private final List<ColumnLayoutItem> columns;
    private final Collection<SearchSort> searchSorts;

    // Note to developers.  Only parameters which apply to the table itself should be added here
    // All other parameters should be passed as 'cellDisplayParams' - SF
    private boolean sortingEnabled = true;
    private boolean showHeaderDescription = false;
    private boolean alternateRowColors = true;
    private boolean showTableEditHeader = false;
    private boolean displayHeader = true;
    private boolean displayHeaderPager = true;
    private boolean showExteriorTable = true;
    private String tableCssClass = "grid";
    private Map<String, Object> cellDisplayParams = new HashMap<String, Object>();
    private Long filterId;
    private String actionUrl;

    private boolean showActionColumn = false;

    public IssueTableLayoutBean(List<ColumnLayoutItem> columns)
    {
        this(columns, null);
    }

    public IssueTableLayoutBean(List<ColumnLayoutItem> columns, Collection<SearchSort> searchSorts)
    {
        this.columns = columns;
        this.searchSorts = searchSorts;
    }

    public IssueTableLayoutBean(User user, Collection<SearchSort> searchSorts)
    {
        this(getDefaultColumns(user), searchSorts);
    }

    private static List<ColumnLayoutItem> getDefaultColumns(User user)
    {
        List<ColumnLayoutItem> columns = Collections.emptyList();
        try
        {
            ColumnLayout columnLayout = ComponentAccessor.getColumnLayoutManager().getColumnLayout(user);
            columns = columnLayout.getAllVisibleColumnLayoutItems(user);
        }
        catch (Exception e)
        {
            log.error("Exception whilst getting a users default columns " + e.getMessage(), e);
        }
        return columns;
    }


    public List<ColumnLayoutItem> getColumns()
    {
        return columns;
    }

    public SearchSort getFirstSorter()
    {
        if (searchSorts != null && !searchSorts.isEmpty())
        {
            return searchSorts.iterator().next();
        }

        // Return the default key sort
        return null;
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public String getActionUrl()
    {
        return actionUrl;
    }

    /**
     * You can use this method to pass parameters through to individual cells / issues
     * for formatting.
     * @see #CELL_NO_LINK
     * @see #CELL_TEXT_ONLY
     */
    public void addCellDisplayParam(String key, Object value)
    {
        cellDisplayParams.put(key, value);
    }

    public Map getCellDisplayParams()
    {
        return cellDisplayParams;
    }

    public boolean isSortingEnabled()
    {
        return sortingEnabled;
    }

    public void setSortingEnabled(boolean sortingEnabled)
    {
        this.sortingEnabled = sortingEnabled;
    }

    public boolean isShowHeaderDescription()
    {
        return showHeaderDescription;
    }

    public void setShowHeaderDescription(boolean showHeaderDescription)
    {
        this.showHeaderDescription = showHeaderDescription;
    }

    public boolean isAlternateRowColors()
    {
        return alternateRowColors;
    }

    public void setAlternateRowColors(boolean alternateRowColors)
    {
        this.alternateRowColors = alternateRowColors;
    }

    public boolean isShowTableEditHeader()
    {
        return showTableEditHeader;
    }

    public void setShowTableEditHeader(boolean showTableEditHeader)
    {
        this.showTableEditHeader = showTableEditHeader;
    }

    public void setFilterId(Long filterId)
    {
        this.filterId = filterId;
    }

    public void setActionUrl(String actionUrl)
    {
        this.actionUrl = actionUrl;
    }

    public void setDisplayHeader(boolean displayHeader)
    {
        this.displayHeader = displayHeader;
    }

    public boolean isDisplayHeader()
    {
        return displayHeader;
    }

    public boolean isDisplayHeaderPager()
    {
        return displayHeaderPager;
    }

    public void setDisplayHeaderPager(final boolean displayHeaderPager)
    {
        this.displayHeaderPager = displayHeaderPager;
    }

    public boolean isShowExteriorTable()
    {
        return showExteriorTable;
    }

    public void setShowExteriorTable(boolean showExteriorTable)
    {
        this.showExteriorTable = showExteriorTable;
    }

    public String getTableCssClass()
    {
        return tableCssClass;
    }

    /**
     * Set the CSS class for the table element in the issue table.  Defaults to 'grid'
     */
    public void setTableCssClass(String tableCssClass)
    {
        this.tableCssClass = tableCssClass;
    }

    public boolean isShowActionColumn()
    {
        return showActionColumn;
    }

    public void setShowActionColumn(boolean showActionColumn)
    {
        this.showActionColumn = showActionColumn;
    }
}
