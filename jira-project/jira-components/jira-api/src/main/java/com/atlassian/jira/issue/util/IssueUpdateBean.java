package com.atlassian.jira.issue.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.google.common.base.Objects;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@PublicApi
public class IssueUpdateBean
{
    //required fields for an update
    private final GenericValue changedIssue;
    private final GenericValue originalIssue;
    private final Long eventTypeId;
    private final ApplicationUser user;
    private final boolean sendMail;

    //optional fields for an update
    private Worklog worklog;
    private boolean dispatchEvent = true;
    private Comment comment;

    private Map params = new HashMap(); //this cannot be collections.EMPTY_MAP as consumers of this object add items to it
    Collection<ChangeItemBean> changeItems;
    private boolean subtasksUpdated;
    private HistoryMetadata historyMetadata;

    public Worklog getWorklog()
    {
        return worklog;
    }

    public void setWorklog(Worklog worklog)
    {
        this.worklog = worklog;
    }

    /**
     * @deprecated since v6.1. Use {@link #IssueUpdateBean(org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, Long, com.atlassian.jira.user.ApplicationUser)} instead.
     */
    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, User user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, ApplicationUser user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    /**
     * @deprecated since v6.1. Use {@link #IssueUpdateBean(org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, Long, com.atlassian.jira.user.ApplicationUser)} instead.
     */
    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, User user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, ApplicationUser user)
    {
        this(changedIssue, originalIssue, eventTypeId, user, true, false);
    }

    /**
     * @deprecated since v6.1. Use {@link #IssueUpdateBean(org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, Long, com.atlassian.jira.user.ApplicationUser, boolean, boolean)} instead.
     */
    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, User user, boolean sendMail, boolean subtasksUpdated)
    {
        this(changedIssue, originalIssue, eventTypeId, ApplicationUsers.from(user), sendMail, subtasksUpdated);
    }

    public IssueUpdateBean(GenericValue changedIssue, GenericValue originalIssue, Long eventTypeId, ApplicationUser user, boolean sendMail, boolean subtasksUpdated)
    {
        this.changedIssue = changedIssue;
        this.originalIssue = originalIssue;
        this.eventTypeId = eventTypeId;
        this.user = user;
        this.sendMail = sendMail;
        this.subtasksUpdated = subtasksUpdated;
    }

    /**
     * @deprecated since v6.1. Use {@link #IssueUpdateBean(org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, Long, com.atlassian.jira.user.ApplicationUser, boolean, boolean)} instead.
     */
    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, User user, boolean sendMail,
            boolean subtasksUpdated)
    {
        this(changedIssue.getGenericValue(), originalIssue.getGenericValue(), eventTypeId, user, sendMail, subtasksUpdated);
    }

    public IssueUpdateBean(Issue changedIssue, Issue originalIssue, Long eventTypeId, ApplicationUser user, boolean sendMail,
            boolean subtasksUpdated)
    {
        this(changedIssue.getGenericValue(), originalIssue.getGenericValue(), eventTypeId, user, sendMail, subtasksUpdated);
    }


    public boolean isDispatchEvent()
    {
        return dispatchEvent;
    }

    public void setDispatchEvent(boolean dispatchEvent)
    {
        this.dispatchEvent = dispatchEvent;
    }

    public Comment getComment()
    {
        return comment;
    }

    public void setComment(Comment comment)
    {
        this.comment = comment;
    }

    public Map getParams()
    {
        return params;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }

    public Collection<ChangeItemBean> getChangeItems()
    {
        return changeItems;
    }

    public void setChangeItems(Collection<ChangeItemBean> changeItems)
    {
        this.changeItems = changeItems;
    }

    public GenericValue getChangedIssue()
    {
        return changedIssue;
    }

    /**
     * @deprecated Use {@link #getApplicationUser()} instead. Since v6.3.
     */
    @Deprecated
    public User getUser()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    /**
     * @since JIRA 6.3
     */
    public ApplicationUser getApplicationUser()
    {
        return user;
    }

    public GenericValue getOriginalIssue()
    {
        return originalIssue;
    }

    public Long getEventTypeId()
    {
        return eventTypeId;
    }

    public boolean isSendMail()
    {
        return sendMail;
    }

    public boolean isSubtasksUpdated()
    {
        return subtasksUpdated;
    }

    /**
     * @since JIRA 6.3
     */
    public HistoryMetadata getHistoryMetadata()
    {
        return historyMetadata;
    }

    /**
     * @since JIRA 6.3
     */
    public void setHistoryMetadata(final HistoryMetadata historyMetadata)
    {
        this.historyMetadata = historyMetadata;
    }

    @SuppressWarnings ( { "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IssueUpdateBean)) return false;

        final IssueUpdateBean issueUpdateBean = (IssueUpdateBean) o;

        if (dispatchEvent != issueUpdateBean.dispatchEvent) return false;
        if ((eventTypeId == null && issueUpdateBean.getEventTypeId() != null) || (eventTypeId != null && !eventTypeId.equals(issueUpdateBean.getEventTypeId()))) return false;
        if (changeItems != null ? !changeItems.equals(issueUpdateBean.changeItems) : issueUpdateBean.changeItems != null) return false;
        if (changedIssue != null ? !changedIssue.equals(issueUpdateBean.changedIssue) : issueUpdateBean.changedIssue != null) return false;
        if (comment != null ? !comment.equals(issueUpdateBean.comment) : issueUpdateBean.comment != null) return false;
        if (originalIssue != null ? !originalIssue.equals(issueUpdateBean.originalIssue) : issueUpdateBean.originalIssue != null) return false;
        if (params != null ? !params.equals(issueUpdateBean.params) : issueUpdateBean.params != null) return false;
        if (user != null ? !user.equals(issueUpdateBean.user) : issueUpdateBean.user != null) return false;
        if (eventTypeId != null ? !eventTypeId.equals(issueUpdateBean.eventTypeId) : issueUpdateBean.eventTypeId != null) return false;
        if (sendMail != issueUpdateBean.sendMail) return false;
        if (subtasksUpdated != issueUpdateBean.subtasksUpdated) return false;
        if (!Objects.equal(historyMetadata, issueUpdateBean.historyMetadata)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (changedIssue != null ? changedIssue.hashCode() : 0);
        result = 29 * result + (originalIssue != null ? originalIssue.hashCode() : 0);
        result = 29 * result + (eventTypeId != null ? eventTypeId.hashCode() : 0);
        result = 29 * result + (user != null ? user.hashCode() : 0);
        result = 29 * result + (dispatchEvent ? 1 : 0);
        result = 29 * result + (sendMail ? 1 : 0);
        result = 29 * result + (comment != null ? comment.hashCode() : 0);
        result = 29 * result + (params != null ? params.hashCode() : 0);
        result = 29 * result + (changeItems != null ? changeItems.hashCode() : 0);
        result = 29 * result + (subtasksUpdated  ? 1 : 0);
        result = 29 * result + Objects.hashCode(historyMetadata);
        return result;
    }
}
