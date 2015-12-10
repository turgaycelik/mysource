package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.IssueConstant;
import com.google.common.collect.Lists;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves index info with a lucene field using the id of the domain object T to get the
 * indexed values from a NameResolver&lt;T&gt; .
 *
 * @since v4.0
 */
public class IssueConstantInfoResolver<T extends IssueConstant> implements IndexInfoResolver<T>
{
    private final NameResolver<T> resolver;

    /**
     * @param resolver         the name resolver to look up the id if necessary.
     */
    public IssueConstantInfoResolver(NameResolver<T> resolver)
    {
        this.resolver = notNull("resolver", resolver);
    }

    public List<String> getIndexedValues(final String singleValueOperand)
    {
        notNull("singleValueOperand", singleValueOperand);
        // our id is our index value

        List<String> ids = Lists.newArrayList();

        List<String> idsForName = resolver.getIdsFromName(singleValueOperand);
        ids.addAll(idsForName);

        // search by id
        Long valueAsLong = getValueAsLong(singleValueOperand);
        if (valueAsLong != null && resolver.idExists(valueAsLong))
        {
            ids.add(singleValueOperand);
        }

        return ids;
    }

    public List<String> getIndexedValues(final Long singleValueOperand)
    {
        notNull("singleValueOperand", singleValueOperand);
        if (resolver.idExists(singleValueOperand))
        {
            return Lists.newArrayList(singleValueOperand.toString());
        }
        else
        {
            return resolver.getIdsFromName(singleValueOperand.toString());
        }
    }

    public String getIndexedValue(final T indexedObject)
    {
        notNull("indexedObject", indexedObject);
        return indexedObject.getId();
    }

    private Long getValueAsLong(final String singleValueOperand)
    {
        try
        {
            return new Long(singleValueOperand);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
