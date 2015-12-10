package com.atlassian.jira.ofbiz;

import com.google.common.collect.ForwardingIterator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;

import static com.atlassian.jira.ofbiz.IssueGenericValueFactory.wrap;

/**
 * @since v6.1
 */
class WrappingIterator extends ForwardingIterator<GenericValue>
{
    private Iterator<GenericValue> delegate;

    WrappingIterator(final Iterator<GenericValue> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    protected Iterator<GenericValue> delegate()
    {
        return delegate;
    }

    @Override
    public GenericValue next()
    {
        return wrap(super.next());
    }

}
