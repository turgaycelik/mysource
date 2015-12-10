package com.atlassian.jira.jql.resolver;

import com.google.common.collect.Lists;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A do-nothing IndexInfoResolver useful for numeric values that should not be even so much as padded to match
 * index values.
 *
 * @since v4.0
 */
public class IdentityIndexInfoResolver implements IndexInfoResolver<Object>
{
    public List<String> getIndexedValues(final String singleValueOperand)
    {
        return Lists.newArrayList(singleValueOperand);
    }

    public List<String> getIndexedValues(final Long singleValueOperand)
    {
        return Lists.newArrayList(singleValueOperand.toString());
    }

    /**
     * Delegates to toString().
     *
     * @param indexedObject
     * @return
     */
    public String getIndexedValue(final Object indexedObject)
    {
        notNull("indexedObject", indexedObject);
        return indexedObject.toString();
    }
}
