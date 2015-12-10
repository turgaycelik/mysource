package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItemImpl;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TableLayoutUtilsImpl implements TableLayoutUtils
{
    private final ApplicationProperties applicationProperties;
    private final FieldManager fieldManager;
    private static final List<String> defaultCols = new ArrayList<String>();

    static
    {
        defaultCols.add(IssueFieldConstants.ISSUE_TYPE);
        defaultCols.add(IssueFieldConstants.ISSUE_KEY);
        defaultCols.add(IssueFieldConstants.SUMMARY);
        defaultCols.add(IssueFieldConstants.PRIORITY);
    }

    public TableLayoutUtilsImpl(ApplicationProperties applicationProperties, final FieldManager fieldManager)
    {
        this.applicationProperties = applicationProperties;
        this.fieldManager = fieldManager;
    }

    public List<String> getDefaultColumnNames(String applicationPropertyName)
    {
        if (StringUtils.isNotBlank(applicationPropertyName))
        {
            String property = applicationProperties.getString(applicationPropertyName);
            if (null == property)
            {
                property = applicationProperties.getDefaultString(applicationPropertyName);
            }
            if (StringUtils.isNotBlank(property))
            {
                final String[] strings = StringUtils.split(property, ", ");
                if (strings != null && strings.length > 0)
                {
                    return Arrays.asList(strings);
                }
            }
        }

        return Collections.unmodifiableList(defaultCols);

    }

    public List<ColumnLayoutItem> getColumns(final User user, String applicationPropertyName) throws FieldException
    {
        final List<String> cols = getDefaultColumnNames(applicationPropertyName);
        return getColumns(user, cols);
    }

    public List<ColumnLayoutItem> getColumns(final User user, List<String> fields) throws FieldException
    {
        final Set<NavigableField> availableFields = fieldManager.getAvailableNavigableFields(user);
        final List<ColumnLayoutItem> columnLayoutItems = new ArrayList<ColumnLayoutItem>();

        if (fields != null)
        {
            for (String fieldName : fields)
            {
                for (NavigableField field : availableFields)
                {
                    if (fieldName.equals(field.getId()))
                    {
                        columnLayoutItems.add(new ColumnLayoutItemImpl(field, columnLayoutItems.size()));
                        break;
                    }
                }
            }
        }
        return columnLayoutItems;
    }

    public List<ColumnLayoutItem> getColumns(User user, final String context, final List<String> columnNames, final boolean addDefaults)
            throws FieldException
    {
        LinkedHashSet<String> columnNamesToUse = new LinkedHashSet<String>();
        final List<String> defaultColumnNames = getDefaultColumnNames(context);
        if (columnNames == null || columnNames.isEmpty())
        {
            columnNamesToUse.addAll(defaultColumnNames);
        }
        else
        {
            if (addDefaults)
            {
                columnNamesToUse.addAll(defaultColumnNames);
            }
            columnNamesToUse.addAll(columnNames);
        }
        return getUserColumns(user, new ArrayList<String>(columnNamesToUse), defaultColumnNames);
    }

    private List<ColumnLayoutItem> getUserColumns(User user, final List<String> requestedColumnNames, final List<String> defaultColumnNames)
            throws FieldException
    {
        List<ColumnLayoutItem> columns;
        columns = getColumns(user, requestedColumnNames);
        if (columns.isEmpty())
        {
            columns = getColumns(user, defaultColumnNames);
        }

        return columns;
    }
}
