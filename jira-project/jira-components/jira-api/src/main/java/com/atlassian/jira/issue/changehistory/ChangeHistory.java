package com.atlassian.jira.issue.changehistory;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.base.Function;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * Represents an issue change history.<br>
 * ChangeHistory is essentially a GenericValue wrapper with getters
 * @see com.atlassian.jira.issue.changehistory.ChangeHistoryManager#getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, User)
 */
@PublicApi
public class ChangeHistory implements WithId
{
    private GenericValue changeHistory;
    private IssueManager issueManager;
    private List<GenericValue> changeItems;
    private ApplicationUser user;
    private final UserManager userManager;

    /**
     * @deprecated Use {@link #ChangeHistory(org.ofbiz.core.entity.GenericValue, com.atlassian.jira.issue.IssueManager, com.atlassian.jira.user.util.UserManager)} ()} instead. Since v6.0.
     */
    public ChangeHistory(GenericValue changeHistoryGV, IssueManager issueManager)
    {
        this(changeHistoryGV, issueManager, ComponentAccessor.getUserManager());
    }

    public ChangeHistory(GenericValue changeHistoryGV, IssueManager issueManager, UserManager userManager)
    {
        this.changeHistory = changeHistoryGV;
        this.issueManager = issueManager;
        this.userManager = userManager;
    }

    public Long getId()
    {
        return changeHistory.getLong("id");
    }

    /**
     * Returns the author of this Change
     * @return the author of this Change
     * @deprecated Use {@link #getAuthorObject()} instead. Since v5.0.
     */
    public String getUsername()
    {
        User authorUser = getAuthorUser();
        if (authorUser == null)
        {
            return null;
        }
        return authorUser.getName();
    }

    /**
     * Returns the userkey of the author of this Change
     * @return the userkey of the author of this Change
     *
     * @deprecated Use {@link #getAuthorKey()} instead. Since v6.0.
     */
    public String getAuthor()
    {
        return getAuthorKey();
    }

    /**
     * Returns the userkey of the author of this Change
     * @return the userkey of the author of this Change
     */
    public String getAuthorKey()
    {
        return changeHistory.getString("author");
    }

    /**
     * Returns the author of this Change
     * @return the author of this Change
     */
    public ApplicationUser getAuthorObject()
    {
        final String authorKey = getAuthorKey();
        if (authorKey == null)
        {
            return null;
        }
        // lazy load the user object
        if (user == null)
        {
            user = userManager.getUserByKeyEvenWhenUnknown(authorKey);
        }
        return user;
    }

    /**
     * @deprecated Use {@link #getAuthorObject()} instead. Since v6.0.
     *
     * Returns the author of this Change
     * @return the author of this Change
     */
    public User getAuthorUser()
    {
        return ApplicationUsers.toDirectoryUser(getAuthorObject());
    }

    /**
     * Returns the display name of the author of this Change
     * @return the display name of the author of this Change
     */
    public String getAuthorDisplayName()
    {
        final ApplicationUser author = getAuthorObject();
        return (author != null) ? author.getDisplayName() : null;
    }

    /**
     * Returns the display name of the author of this Change
     * @return the display name of the author of this Change
     * @deprecated Use {@link #getAuthorDisplayName()} instead. Since v5.0.
     */
    public String getFullName()
    {
        return getAuthorDisplayName();
    }

    public Timestamp getTimePerformed()
    {
        return changeHistory.getTimestamp("created");
    }

    public String getLevel()
    {
        return changeHistory.getString("level");
    }

    public String getComment()
    {
        return changeHistory.getString("body");
    }

    public List<ChangeItemBean> getChangeItemBeans()
    {
        List<GenericValue> items = getChangeItems();
        return transform(items, new Function<GenericValue, ChangeItemBean>() {
            @Override
            public ChangeItemBean apply(GenericValue changeItemGV)
            {
                return new ChangeItemBean(changeItemGV.getString("fieldtype"),
                    changeItemGV.getString("field"), changeItemGV.getString("oldvalue"),
                    changeItemGV.getString("oldstring"), changeItemGV.getString("newvalue"),
                    changeItemGV.getString("newstring"), getTimePerformed());
            }
        });
    }

    public List<GenericValue> getChangeItems()
    {
        if (changeItems == null)
        {
            try
            {
                changeItems = changeHistory.getRelated("ChildChangeItem");
            }
            catch (GenericEntityException e)
            {
                // ignore and return empty list
            }

            if (changeItems == null)
            {
                // JRA-29298: the above call can potentially return null
                changeItems = Collections.emptyList();
            }
        }
        return changeItems;
    }

    public Issue getIssue()
    {
        return issueManager.getIssueObject(getIssueId());
    }

    public Long getIssueId()
    {
        return changeHistory.getLong("issue");
    }
}
