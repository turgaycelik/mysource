package com.atlassian.jira.issue.search.searchers.transformer;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SearchInput
{
    static class InputType
    {
        private final String name;

        InputType(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    final InputType type;
    private final String value;

    SearchInput(InputType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }

        if (that == null || getClass() != that.getClass())
        {
            return false;
        }

        SearchInput input = (SearchInput) that;

        return type.equals(input.type) && ObjectUtils.equals(value, input.getValue());
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append(type).append(value).toString();
    }
}
