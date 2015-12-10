package com.atlassian.jira.ofbiz;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.entity.Entity.Name.ISSUE;
import static com.google.common.collect.Lists.transform;

/**
 * @since v6.1
 */
class IssueGenericValueFactory
{
    static List<GenericValue> wrap(final List<GenericValue> genericValues)
    {
        final List<GenericValue> wrapped = transform(genericValues, new Function<GenericValue, GenericValue>()
        {
            @Override
            public GenericValue apply(@Nullable final GenericValue gv)
            {
                return wrap(gv);
            }
        });
        return new ArrayList<GenericValue>(wrapped);
    }

    static GenericValue wrap(@Nullable final GenericValue gv)
    {
        if (gv != null && ISSUE.equals(gv.getEntityName()))
        {
            return new IssueGenericValue(gv);
        }
        return gv;
    }
}
