package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.util.NonInjectableComponent;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

/**
 * Simple helper class that generates navigator param and form names given a date field id.
 *
 * @since v4.0
 */
@NonInjectableComponent
public final class WorkRatioSearcherConfig
{
    public static final String MIN_SUFFIX = ":min";
    public static final String MAX_SUFFIX = ":max";

    private final String id;
    private final String min;
    private final String max;

    public WorkRatioSearcherConfig(final String id)
    {
        this.id = notBlank("id", id);

        this.min = this.id + MIN_SUFFIX;
        this.max = this.id + MAX_SUFFIX;
    }

    public String getId()
    {
        return id;
    }

    public String getMinField()
    {
        return min;
    }

    public String getMaxField()
    {
        return max;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final WorkRatioSearcherConfig that = (WorkRatioSearcherConfig) o;

        if (!id.equals(that.id))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}