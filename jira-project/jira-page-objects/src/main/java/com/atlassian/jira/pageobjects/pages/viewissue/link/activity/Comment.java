package com.atlassian.jira.pageobjects.pages.viewissue.link.activity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a comment in a JIRA issue.
 *
 * @since v5.0
 */
public class Comment
{
    private final String text;
    
    private final String restriction;

    public Comment(String text, String restriction)
    {
        super();
        this.text = text;
        this.restriction = restriction;
    }

    public Comment(String text)
    {
        this(text, null);
    }
    
    public String getText()
    {
        return text;
    }

    public String getRestriction()
    {
        return restriction;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
