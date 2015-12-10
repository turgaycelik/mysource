package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;

import java.util.List;

/**
 * Util class for getting columns for issue tables.
 *
 * @since v4.0
 */
public interface TableLayoutUtils
{
    /**
     * Users can specify a list of columns to display in their application properties.  This is a method to retrieve
     * them.
     *
     * @param applicationPropertyName The name of the property in jira-application.properties
     * @return A list of String objects
     */
    List<String> getDefaultColumnNames(String applicationPropertyName);

    /**
     * Get the columns based off the a list of field names
     *
     * @param user The suer to retreive columns for.
     * @param fields The list of columns to retrieve.
     * @return a list containing the field equivs of the field names
     * @throws FieldException if there is an exception thrown while retieving the fields
     */
    List<ColumnLayoutItem> getColumns(final User user, List<String> fields) throws FieldException;

    /**
     * Get the columns based on a list of field names, and the default as defined in application properties
     * with the given property name.
     *
     * @param user The user to retreive columns for.
     * @param applicationPropertyName  The name of the property in jira-application.properties
     * @param fields The list of columns to retrieve.
     * @param addDefaults if true, will add the default columns.
     * @return a list containing the field equivs of the field names
     * @throws FieldException if there is an exception thrown while retieving the fields
     */
    List<ColumnLayoutItem> getColumns(User user, String applicationPropertyName, List<String> fields, boolean addDefaults)
            throws FieldException;

    /**
     * Get the columns based off an application property
     *
     * @param user The suer to retreive columns for.
     * @param applicationPropertyName The property to get the list of fields from in the application properties
     * @return a list containing the field equivs of the field names
     * @throws FieldException if there is an exception thrown while retieving the fields
     */
    List<ColumnLayoutItem> getColumns(final User user, String applicationPropertyName) throws FieldException;
}
