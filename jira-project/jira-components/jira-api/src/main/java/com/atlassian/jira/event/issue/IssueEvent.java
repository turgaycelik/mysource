package com.atlassian.jira.event.issue;

import java.util.Map;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.project.Project;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Basic representation of something that happens to an Issue, such as a modification or comment. Event listeners
 * register to receive these.
 * <p/>
 * The <code>IssueEvent</code> object thrown as a result of an edit operation, may now return <tt>null</tt> from a
 * <code>getChangeLog()</code> call. This can occur when a user chooses to edit an issue but only leaves a comment and
 * makes no other changes to the issue.
 * <p/>
 * Prior to 3.5.2 no event was fired in this case and this was identified as a bug (JRA-9415) and has since been fixed.
 * Check any calls to <code>getChangeLog()</code> for <tt>null</tt>.
 */
@PublicApi
public final class IssueEvent extends AbstractEvent implements IssueRelatedEvent
{

    public static final String SEND_MAIL = "sendMail";

    /**
     * Key of event parameter holding the application's base URL.
     *
     */
    public static final String BASE_URL_PARAM_NAME = asString("baseurl");

    // TODO we may consider having methods for the guys below. However, this will probably mean that we have to make all
    // issue event initializers put this data into event (A.K.A lots of work)

    /**
     * <p/>
     * In case of issue delete events, a param with this key may store the custom field values of the deleted issue.
     *
     * <p/>
     * <b>NOTE:</b> this parameter is not guaranteed to exist so explicit check in the event parameters map must
     * be performed by the clients. If not found, the custom field values may be retrieved by means of field manager
     * from the issue object associated with the event.
     *
     */
    public static final String CUSTOM_FIELDS_PARAM_NAME = asString("com.atlassian.jira.event.issue.CUSTOM_FIELDS");

    /**
     * <p/>
     * In case of issue delete events, a param with this key may store list of this issue's watchers as a list of
     * {@link com.atlassian.crowd.embedded.api.User} objects.
     *
     * <p/>
     * <b>NOTE:</b> this parameter is not guaranteed to exist so explicit check in the event parameters map must
     * be performed by the clients. If not found, the value may be retrieved via
     * {@link com.atlassian.jira.issue.IssueManager#getWatchers(com.atlassian.jira.issue.Issue)}.
     */
    public static final String WATCHERS_PARAM_NAME = asString("com.atlassian.jira.event.issue.WATCHERS");

    /**
     * In case of issue delete events, a param with this key will store list of this issue's change history as a list of
     * {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem} objects.
     */
    public static final String CHANGE_HISTORY_PARAM_NAME = asString("com.atlassian.jira.event.issue.CHANGE_HISTORY");

    /**
     * In case of issue delete events, a param with this key will store list of this issue's comments as a list of
     * {@link Comment} objects.
     */
    public static final String COMMENTS_PARAM_NAME = asString("com.atlassian.jira.event.issue.COMMENTS");

    private Issue issue;
    private User user;
    private Worklog worklog;
    private GenericValue changeGroup;
    private Comment comment;
    private Long eventTypeId;
    private boolean subtasksUpdated;

    /**
     * Option to send mail notification of the event - true by default
     */
    private boolean sendMail = true;

    /**
     * If true, indicates that this issue event can be ignored by listeners, and that some other issue event representing the same thing has been published.
     */
    private boolean redundant = false;

    /**
     * Create a new IssueEvent with a given list of parameters.
     *
     * @param issue       the issue this event refers to
     * @param params      parameters that can be retrieved by the Listener
     * @param user        the user who has initiated this event
     * @param eventTypeId the type ID of this event
     */
    public IssueEvent(Issue issue, Map params, User user, Long eventTypeId)
    {
        this(issue, user, null, null, null, params, eventTypeId, true, false);
    }

    /**
     * Allows configuration of whether the mail notification should be sent
     *
     * @param issue       the issue this event refers to
     * @param params      parameters that can be retrieved by the Listener
     * @param user        the user who has initiated this event
     * @param eventTypeId the type ID of this event
     * @param sendMail    configure whether mail notifications should be sent
     */
    public IssueEvent(Issue issue, Map params, User user, Long eventTypeId, boolean sendMail)
    {
        this(issue, user, null, null, null, params, eventTypeId, sendMail, false);
    }

    /**
     * Create a new IssueEvent with a given list of parameters.
     * <p/>
     * This event also has an attached changeGroup, comment and worklog (any of which may be null).
     *
     * @param issue       The issue this event refers to
     * @param user        the user who has initiated this event
     * @param comment     A comment for this event
     * @param worklog     A worklog for this event
     * @param changeGroup An attached changeGroup for this event
     * @param params      Parameters that can be retrieved by the Listener
     * @param eventTypeId the type ID of this event
     */
    public IssueEvent(Issue issue, User user, Comment comment, Worklog worklog, GenericValue changeGroup, Map params, Long eventTypeId)
    {
        this(issue, user, comment, worklog, changeGroup, params, eventTypeId, true, false);
    }

    /**
     * Create a new IssueEvent with a given list of parameters.
     * <p/>
     * This event also has an attached changeGroup, comment and worklog (any of which may be null).
     *
     * @param issue       the issue this event refers to
     * @param user        the user who has initiated this event
     * @param comment     comment for this event
     * @param worklog     A worklog for this event
     * @param changeGroup an attached changeGroup for this event
     * @param params      parameters that can be retrieved by the Listener
     * @param eventTypeId the type ID of this event
     * @param sendMail    configure whether mail notifications should be sent
     */
    public IssueEvent(Issue issue, User user, Comment comment, Worklog worklog, GenericValue changeGroup, Map params, Long eventTypeId, boolean sendMail)
    {
        this(issue, user, comment, worklog, changeGroup, params, eventTypeId, sendMail, false);
    }

    public IssueEvent(Issue issue, User user, Comment comment, Worklog worklog, GenericValue changeGroup, Map params, Long eventTypeId, boolean sendMail, boolean subtasksUpdated)
    {
        super(params);
        this.issue = issue;
        this.user = user;
        this.eventTypeId = eventTypeId;
        this.sendMail = sendMail;
        this.worklog = worklog;
        this.changeGroup = changeGroup;
        this.comment = comment;
        this.subtasksUpdated = subtasksUpdated;
    }

    public Issue getIssue()
    {
        return issue;
    }

    public Project getProject()
    {
        return issue.getProjectObject();
    }

    /**
     * Returns the user who initiated this event.
     *
     * @return the user who initiated this event.
     */
    public User getUser()
    {
        return user;
    }

    public GenericValue getChangeLog()
    {
        return changeGroup;
    }

    public Comment getComment()
    {
        return comment;
    }

    public Worklog getWorklog()
    {
        return worklog;
    }

    public void setWorklog(Worklog worklog)
    {
        this.worklog = worklog;
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
     * Marks this issue event as being redundant. If this flag is set to true, it means the same event is dispatched by
     * an {@link com.atlassian.jira.event.issue.IssueEventBundle} separately and this event will be ignored by mail handler.
     */
    @Internal
    void makeRedundant()
    {
        redundant = true;
    }

    /**
     * If true, indicates that this issue event can be ignored by listeners, and that some other issue
     * event representing the same thing has been published.
     * @return a boolean indicating whether this event is redundant.
     */
    @Internal
    public boolean isRedundant()
    {
        return redundant;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("issue", getIssue()).
                append("comment", getComment()).
                append("worklog", getWorklog()).
                append("changelog", getChangeLog()).
                append("eventTypeId", getEventTypeId()).
                append("sendMail", isSendMail() ? "true" : "false").
                append("params", getParams()).
                append("subtasksUpdated", isSubtasksUpdated()).
                toString();
    }

    /**
     * Note: this will not compare the time stamps of two events - only everything else.
     */
    @SuppressWarnings ( { "RedundantIfStatement" })
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IssueEvent))
        {
            return false;
        }

        final IssueEvent event = (IssueEvent) o;

        if (getParams() != null ? !getParams().equals(event.getParams()) : event.getParams() != null)
        {
            return false;
        }
        if (changeGroup != null ? !changeGroup.equals(event.changeGroup) : event.changeGroup != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(event.comment) : event.comment != null)
        {
            return false;
        }
        if (worklog != null ? !worklog.equals(event.worklog) : event.worklog != null)
        {
            return false;
        }
        if (issue != null ? !issue.equals(event.issue) : event.issue != null)
        {
            return false;
        }
        if (eventTypeId != null ? !eventTypeId.equals(event.eventTypeId) : event.eventTypeId != null)
        {
            return false;
        }
        if (sendMail != event.isSendMail())
        {
            return false;
        }
        if (subtasksUpdated != event.isSubtasksUpdated())
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (issue != null ? issue.hashCode() : 0);
        result = 29 * result + (changeGroup != null ? changeGroup.hashCode() : 0);
        result = 29 * result + (comment != null ? comment.hashCode() : 0);
        result = 29 * result + (worklog != null ? worklog.hashCode() : 0);
        result = 29 * result + (eventTypeId != null ? eventTypeId.hashCode() : 0);
        result = 29 * result + (sendMail ? 1 : 0);
        result = 29 * result + (subtasksUpdated ? 1 : 0);
        return result;
    }
}
