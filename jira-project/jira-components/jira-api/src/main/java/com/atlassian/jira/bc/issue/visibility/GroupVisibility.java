package com.atlassian.jira.bc.issue.visibility;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class restricts a visibility of a comment or a worklog to a specified group.
 *
 * @since v6.4
 */
@Immutable
public final class GroupVisibility implements Visibility
{
    final private String groupLevel;

    GroupVisibility(final String groupLevel)
    {
        this.groupLevel = groupLevel;
    }

    public String getGroupLevel()
    {
        return groupLevel;
    }

    @Override
    public <T> T accept(final VisibilityVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof GroupVisibility))
        {
            return false;
        }
        GroupVisibility rhs = (GroupVisibility) obj;
        return new EqualsBuilder()
                .append(getGroupLevel(), rhs.getGroupLevel())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getGroupLevel())
                .toHashCode();
    }
}
