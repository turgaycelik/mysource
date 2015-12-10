package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.google.common.collect.Lists;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for select fields.
 *
 * @since v4.0
 */
public class SelectCustomFieldIndexInfoResolver implements IndexInfoResolver<CustomField>
{
    private final CustomFieldOptionResolver customFieldOptionResolver;

    public SelectCustomFieldIndexInfoResolver(CustomFieldOptionResolver customFieldOptionResolver)
    {
        this.customFieldOptionResolver = customFieldOptionResolver;
    }

    public List<String> getIndexedValues(final String singleValueOperand)
    {
        return customFieldOptionResolver.getIdsFromName(singleValueOperand);
    }

    public List<String> getIndexedValues(final Long singleValueOperand)
    {
        Option option = customFieldOptionResolver.get(singleValueOperand);
        // if there is no matching option try to get ids from the name and if there are some we return these
        if (option == null)
        {
            List<String> ids = customFieldOptionResolver.getIdsFromName(singleValueOperand.toString());
            if (!ids.isEmpty())
            {
                return ids;
            }
        }
        return Lists.newArrayList(singleValueOperand.toString());
    }

    @Override
    public String getIndexedValue(CustomField indexedObject)
    {
        notNull("indexedObject", indexedObject);
        return indexedObject.toString().toLowerCase();
    }

}
