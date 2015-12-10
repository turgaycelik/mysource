package com.atlassian.jira.issue.fields.option;

import com.atlassian.annotations.Internal;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;

@Internal
public abstract class AbstractOption implements Option, Comparable
{
    public String getImagePath()
    {
        return null;
    }

    public String getImagePathHtml()
    {
        return StringEscapeUtils.escapeHtml(getImagePath());
    }

    public String getCssClass()
    {
        return null;
    }

    public List getChildOptions()
    {
        return Collections.EMPTY_LIST;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Option))
        {
            return false;
        }
        Option rhs = (Option) o;
        return new EqualsBuilder()
                .append(getId(), rhs.getId())
                .isEquals();
    }

    public int compareTo(Object obj)
    {
        Option o = (Option) obj;
        return new CompareToBuilder()
                .append(getId(), o.getId())
                .toComparison();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(41, 5)
                .append(getId())
                .toHashCode();
    }
}
