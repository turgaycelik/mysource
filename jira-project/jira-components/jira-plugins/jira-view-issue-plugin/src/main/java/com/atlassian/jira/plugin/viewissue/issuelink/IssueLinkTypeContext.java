package com.atlassian.jira.plugin.viewissue.issuelink;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Represents an Issue Link Type context object. Used by the velocity template renderer.
 *
 * @since v5.0
 */
public final class IssueLinkTypeContext
{
    private final String relationship;
    private final List<IssueLinkContext> issueLinkContexts;

    public IssueLinkTypeContext(String relationship, List<IssueLinkContext> issueLinkContexts)
    {
        this.relationship = relationship;
        this.issueLinkContexts = issueLinkContexts;
    }

    public String getRelationship()
    {
        return relationship;
    }

    public List<IssueLinkContext> getIssueLinkContexts()
    {
        return issueLinkContexts;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("relationship", relationship)
                .append("issueLinkContexts", issueLinkContexts)
                .toString();
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

        IssueLinkTypeContext that = (IssueLinkTypeContext) o;

        if (issueLinkContexts != null ? !issueLinkContexts.equals(that.issueLinkContexts) : that.issueLinkContexts != null)
        {
            return false;
        }
        if (relationship != null ? !relationship.equals(that.relationship) : that.relationship != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = relationship != null ? relationship.hashCode() : 0;
        result = 31 * result + (issueLinkContexts != null ? issueLinkContexts.hashCode() : 0);
        return result;
    }
}
