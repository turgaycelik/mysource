package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.search.ClauseNames;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simple helper class that generates navigator param and form names given a date field id.
 *
 * @since v4.0
 */
public final class DateSearcherConfig
{
    public static final String AFTER_SUFFIX = ":after";
    public static final String BEFORE_SUFFIX = ":before";
    public static final String NEXT_SUFFIX = ":next";
    public static final String PREVIOUS_SUFFIX = ":previous";
    public static final String EQUALS_SUFFIX = ":equals";

    private final String id;
    private final String after;
    private final String before;
    private final String next;
    private final String previous;
    private final String equals;
    private final ClauseNames clauseNames;
    private final String fieldName;

    public DateSearcherConfig(final String id, final ClauseNames clauseNames, final String fieldName)
    {
        this.id = notBlank("id", id);
        this.fieldName = notBlank("fieldName", fieldName);
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.after = this.id + AFTER_SUFFIX;
        this.before = this.id + BEFORE_SUFFIX;
        this.next = this.id + NEXT_SUFFIX;
        this.previous = this.id + PREVIOUS_SUFFIX;
        this.equals = this.id + EQUALS_SUFFIX;
    }

    public ClauseNames getClauseNames()
    {
        return clauseNames;
    }

    /**
     * @return the name of this date field e.g. <code>created</code> or <code>My Custom Date Field</code>
     */
    public String getFieldName()
    {
        return fieldName;
    }

    public String getId()
    {
        return id;
    }

    public String[] getAbsoluteFields()
    {
        return new String[] { getAfterField(), getBeforeField(), getEqualsField() };
    }

    public String getAfterField()
    {
        return after;
    }

    public String getBeforeField()
    {
        return before;
    }

    public String[] getRelativeFields()
    {
        return new String[] { getPreviousField(), getNextField() };
    }

    public String getNextField()
    {
        return next;
    }

    public String getPreviousField()
    {
        return previous;
    }

    public String getEqualsField()
    {
        return equals;
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

        final DateSearcherConfig that = (DateSearcherConfig) o;

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
