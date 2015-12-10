package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.jql.operand.QueryLiteral;

/**
 * Converts a query literal into the votes index representation. Must be
 * positive number, otherwise null is returned.
 *
 * @since v4.0
 */
public class GroupCustomFieldIndexValueConverter implements IndexValueConverter
{
    private final GroupConverter groupConverter;

    public GroupCustomFieldIndexValueConverter(GroupConverter groupConverter)
    {
        this.groupConverter = groupConverter;
    }

    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        if (rawValue.isEmpty())
        {
            return null;
        }
        
        final Group group;
        try
        {
            group = groupConverter.getGroup(rawValue.asString());
        }
        catch (FieldValidationException e)
        {
            return null;
        }
        if (group != null)
        {
            return groupConverter.getString(group);
        }
        else
        {
            return null;
        }
    }
}
