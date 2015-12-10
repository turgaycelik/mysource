package com.atlassian.jira.issue.changehistory;

import java.sql.Timestamp;
import java.util.List;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.collect.Lists;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This groups {@link ChangeHistoryGroup} together to try and reduce {@link org.apache.lucene.document.Document} proliferation.
 *
 * @since v5.0
 */
@PublicApi
public class ChangeHistoryGroup implements Comparable<ChangeHistoryGroup>
{
    private final Long id;
    private final String userKey;
    private final Long projectId;
    private final Long issueId;
    private final String issueKey;
    private final List<ChangeHistoryItem> changeItems;
    private final Timestamp created;


    public ChangeHistoryGroup(Long id, Long projectId, Long issueId, String issueKey, String userKey, List<ChangeHistoryItem> changeItems, Timestamp created)
    {
        this.id = id;
        this.userKey = userKey;
        this.projectId = projectId;
        this.issueId = issueId;
        this.issueKey = issueKey;
        this.changeItems = changeItems;
        this.created = created;
    }



    public Long getId()
    {
        return id;
    }

    /**
     * Note: This is the user's key, which since 6.0 is not necessarily the same as the username.
     * @deprecated Use {@link #getUserKey()} instead (for clarity). Since v6.0.
     */
    public String getUser()
    {
        return userKey;
    }

    public String getUserKey()
    {
        return userKey;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public List<ChangeHistoryItem> getChangeItems()
    {
        return changeItems;
    }

    @Override
    public int compareTo(ChangeHistoryGroup other)
    {
        if (created.compareTo(other.getCreated()) == 0)
        {
            return id.compareTo(other.getId());
        }
        return created.compareTo(other.getCreated());
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof  ChangeHistoryGroup))
        {
            return false;
        }
        ChangeHistoryGroup rhs = (ChangeHistoryGroup)o;
        return new EqualsBuilder()
                .append(getId(), rhs.getId())
                .append(getChangeItems(), rhs.getChangeItems())
                .append(getUserKey(), rhs.getUserKey())
                .append(getProjectId(), rhs.getProjectId())
                .append(getIssueId(), rhs.getIssueId())
                .append(getIssueKey(), rhs.getIssueKey())
                .append(getCreated(), rhs.getCreated())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getId())
                .append(getChangeItems())
                .append(getUserKey())
                .append(getProjectId())
                .append(getIssueId())
                .append(getIssueKey())
                .append(getCreated())
                .toHashCode();
    }

    public static class Builder
    {
        private long id;
        private long projectId;
        private long issueId;
        private String issueKey;
        private String userKey;
        private Timestamp created;
        private List<ChangeHistoryItem> changeItems = Lists.newArrayList();

        public Builder withId(final long id)
        {
            Assertions.notNull(id);
            this.id = id;
            return this;
        }

        public Builder inProject(final long projectId)
        {
            Assertions.notNull(projectId);
            this.projectId = projectId;
            return this;
        }

        public Builder forIssue(final long issueId, final String issueKey)
        {
            Assertions.notNull(issueId);
            Assertions.notNull(issueKey);
            this.issueId = issueId;
            this.issueKey = issueKey;
            return this;
        }

        public Builder on(final Timestamp on)
        {
            Assertions.notNull(on);
            this.created = on;
            return this;
        }

        /**
         * Note: This accepts the user's key, which since 6.0 is not necessarily the same as the username.
         */
        public Builder byUser(final String userKey)
        {
            this.userKey = userKey;
            return this;
        }

        public Builder withChanges(List<ChangeHistoryItem> items)
        {
            Assertions.notNull(items);
            this.changeItems = Lists.newArrayList(items);
            return this;
        }

        public Builder addChangeItem(ChangeHistoryItem item)
        {
            Assertions.notNull(item);
            changeItems.add(item);
            if (issueId == 0)
            {
                id = item.getChangeGroupId();
                issueId = item.getIssueId();
                issueKey = item.getIssueKey();
                projectId = item.getProjectId();
                userKey = item.getUserKey();
                created = item.getCreated();
            }
            return this;
        }

        public ChangeHistoryGroup build()
        {
            return new ChangeHistoryGroup(id, projectId, issueId, issueKey, userKey, changeItems, created);
        }

    }
}
