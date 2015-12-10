package com.atlassian.jira.bc.issue.visibility;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class restricts a visibility of a comment or a worklog to specified project role.
 *
 * @since v6.4
 */
@Immutable
public final class RoleVisibility implements Visibility
{
    final long roleId;

    RoleVisibility(final long roleId) {this.roleId = roleId;}

    public Long getRoleLevelId()
    {
        return roleId;
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
        if (!(obj instanceof RoleVisibility))
        {
            return false;
        }
        RoleVisibility rhs = (RoleVisibility) obj;
        return new EqualsBuilder()
                .append(getRoleLevelId(), rhs.getRoleLevelId())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getRoleLevelId())
                .toHashCode();
    }
}
