package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.jql.context.QueryContext;

import java.util.List;

import javax.annotation.Nullable;

@PublicApi
public interface ColumnLayout
{
    /**
     * Represents the cause or source of columns in an issue table, e.g. whether they were requested explicitly, configured
     * as the columns of a filter or the user's configured defaults.
     *
     * @since v6.1
     */
    public enum ColumnConfig
    {
        /**
         * Columns come from the system defaults
         */
        SYSTEM,

        /**
         * Columns were explicitly listed in the issue table request.
         */
        EXPLICIT,

        /**
         * Columns come from the filter.
         */
        FILTER,

        /**
         * Columns come from the user's default column config.
         */
        USER,

        /**
         * No columns are used. The default value.
         */
        NONE;

        /**
         * Gets a ColumnConfig by string value (name).
         *
         * @param value the case insensitive name to get the value for.
         * @return the instance that corresponds to the given value or null if none exists.
         */
        public static ColumnConfig byValueIgnoreCase(@Nullable String value)
        {
            ColumnConfig config = null;
            if (value != null)
            {
                try
                {
                    config = ColumnConfig.valueOf(value.toUpperCase());
                }
                catch (IllegalArgumentException ignored)
                {
                }
            }
            return config;
        }
    }

    public List<ColumnLayoutItem> getColumnLayoutItems();

    /**
     * Get the {@link com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem ColumnLayoutItems} that can be displayed to the user.
     *
     * @param user       the user for whom the columns are to be displayed.
     * @param queryContext the context of the search the columns are being displayed for
     * @return All visible column layout items for the given query context
     * @throws ColumnLayoutException if exception thrown while retreiving column layout
     * @deprecated Current implementation of this method is not very good performance wise. Use {@link #getColumnLayoutItems()} instead. Since 6.3.3
     */
    @Deprecated
    public List<ColumnLayoutItem> getVisibleColumnLayoutItems(User user, QueryContext queryContext) throws ColumnLayoutException;


    /**
     * Get the {@link com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem ColumnLayoutItems} that can be displayed to the user.
     *
     * @param user       the user for whom the columns are to be displayed.
     * @return All visible column layout items
     * @throws ColumnLayoutException if exception thrown while retreiving column layout
     */
    public List<ColumnLayoutItem> getAllVisibleColumnLayoutItems(User user) throws ColumnLayoutException;

    public boolean contains(NavigableField navigableField);

    /**
     * @return the column layout items as a list of string
     */
    public List<String> asFieldNames();

    /**
     * @return the columns used when creating the ColumnLayout
     */
    public ColumnConfig getColumnConfig();
}
