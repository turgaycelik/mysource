package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A generator for Column Names - used when selecting columns to display in Issue Navigator.
 * <p/>
 * This generator will return all available columns for the currently logged in user, and will
 * put them in an order that resembles the current ordering.  It will also add a 'Default' column
 * whose value is {@link Property#DEFAULT_COLUMNS}.
 *
 * @since v4.0
 */
public class ColumnNamesValuesGenerator implements ValuesGenerator
{
    private final JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();

    public static final class Property
    {
        public static final String DEFAULT_COLUMNS = IssueTableLayoutBean.DEFAULT_COLUMNS;
    }

    public Map getValues(Map params)
    {
//        User u = (User) params.get("User");  //  some value generators get users like this?  I don't think I'll use it though.
        try
        {
            final ListOrderedMap values = new ListOrderedMap();
            populateAllAvailableFields(values);
            putDefaultColumnsFirstInList(values);
            insertValueForSelectingDefaults(values);

            return values;
        }
        catch (FieldException e)
        {
            throw new RuntimeException(e);
        }
        catch (ColumnLayoutStorageException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void insertValueForSelectingDefaults(ListOrderedMap values)
    {
        values.put(0, Property.DEFAULT_COLUMNS, authenticationContext.getI18nHelper().getText("portlet.abstractsearchresults.defaultcols"));
    }

    private void putDefaultColumnsFirstInList(ListOrderedMap values) throws ColumnLayoutStorageException
    {
        List<ColumnLayoutItem> columnLayoutItems = ComponentAccessor.getColumnLayoutManager().getDefaultColumnLayout(authenticationContext.getLoggedInUser()).getColumnLayoutItems();

        int i = 0;
        for (ColumnLayoutItem layoutItem : columnLayoutItems)
        {
            String fieldId = layoutItem.getId();
            if (values.containsKey(fieldId))
            {
                String fieldName = (String) values.remove(fieldId);
                values.put(i++, fieldId, fieldName);
            }
        }
    }

    private void populateAllAvailableFields(ListOrderedMap values) throws FieldException
    {
        Set fields = ComponentAccessor.getFieldManager().getAvailableNavigableFields(authenticationContext.getLoggedInUser());
        for (Object field1 : fields)
        {
            Field field = (Field) field1;
            values.put(field.getId(), field.getName());
        }
    }


}
