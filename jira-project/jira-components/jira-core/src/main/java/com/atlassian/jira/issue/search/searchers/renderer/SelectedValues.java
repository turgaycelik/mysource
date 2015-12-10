package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collection;

public class SelectedValues
{
    private final Collection<String> selectedValuesInLowerCase;

    SelectedValues(Collection<String> selectedValues)
    {
        this.selectedValuesInLowerCase = CollectionUtil.transform(selectedValues, new Function<String, String>()
        {
            @Override
            public String get(String value)
            {
                return value.toLowerCase();
            }
        });
    }

    public boolean contains(String value)
    {
        return value != null && selectedValuesInLowerCase != null && selectedValuesInLowerCase.contains(value.toLowerCase());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SelectedValues that = (SelectedValues) o;

        return ObjectUtils.equals(selectedValuesInLowerCase, that.selectedValuesInLowerCase);
    }

    @Override
    public int hashCode()
    {
        return selectedValuesInLowerCase != null ? selectedValuesInLowerCase.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append(selectedValuesInLowerCase).toString();
    }
}
