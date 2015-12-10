package com.atlassian.jira.event.issue;

import java.util.Map;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;

import org.ofbiz.core.entity.GenericValue;

/**
 * Component responsible for dispatching issue events.
 *
 * @since v4.4
 */
@PublicApi
public interface IssueEventManager
{
    /**
     * Dispatch event of given type, configuring whether or not a mail notification should be sent (useful e.g. for bulk
     * edit).
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail);

    /**
     * Dispatch a redundant event of given type with custom parameters.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId type of event
     * @param issue  affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser);

    /**
     * Dispatch event of given type.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    An attached changeGroup for this event
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog);


    /**
     * Dispatch a redundant event of given type.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    An attached changeGroup for this event
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog);

    /**
     * Dispatch event of given type.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    attached changeGroup for this event
     * @param sendMail     whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    attached changeGroup for this event
     * @param params       custom event parameters
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params);

    /**
     * Dispatch a redundant event of given type with custom parameters.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    attached changeGroup for this event
     * @param params       custom event parameters
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    attached changeGroup for this event
     * @param params       custom event parameters
     * @param sendMail     whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail);

    /**
     * Dispatch a redundant event of given type with custom parameters.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param comment      comment for this event
     * @param worklog      worklog for this event
     * @param changelog    attached changeGroup for this event
     * @param params       custom event parameters
     * @param sendMail     whether or not a mail notification should be sent
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail);

    /**
     * Dispatch event of given type.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param changelog    attached changeGroup for this event
     * @param sendMail     whether or not a mail notification should be sent
     * @param subtasksUpdated  if subtask have been modified.
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog, boolean sendMail, boolean subtasksUpdated);

    /**
     * Dispatch a redundant event of given type.
     *
     * @param eventTypeId  type of event
     * @param issue        affected issue
     * @param remoteUser   remoteUser user initiating the event
     * @param changelog    attached changeGroup for this event
     * @param sendMail     whether or not a mail notification should be sent
     * @param subtasksUpdated  if subtask have been modified.
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog, boolean sendMail, boolean subtasksUpdated);

    /**
     * Dispatch event of given type with custom parameters.
     *
     * @param eventTypeId   type of event
     * @param issue         affected issue
     * @param remoteUser    remoteUser user initiating the event
     * @param comment       comment for this event
     * @param worklog       worklog for this event
     * @param changelog     attached changeGroup for this event
     * @param params        custom event parameters
     * @param sendMail      whether or not a mail notification should be sent
     * @param subtasksUpdated if subtask have been modified.
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated);

    /**
     * Dispatch a redundant event of given type with custom parameters.
     *
     * @param eventTypeId   type of event
     * @param issue         affected issue
     * @param remoteUser    remoteUser user initiating the event
     * @param comment       comment for this event
     * @param worklog       worklog for this event
     * @param changelog     attached changeGroup for this event
     * @param params        custom event parameters
     * @param sendMail      whether or not a mail notification should be sent
     * @param subtasksUpdated if subtask have been modified.
     * @deprecated since v6.4.10, please use {@link #dispatchEvent(IssueEventBundle)}.
     */
    @Deprecated
    void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated);

    /**
     * Dispatches a bundle of issue events.
     * This method will dispatch one event for the bundle itself, and one per event contained in the bundle.
     * @param issueEventBundle The bundle with all the issue events.
     * @see com.atlassian.jira.event.issue.IssueEventBundle
     * @since 6.3.8
     */
    void dispatchEvent(IssueEventBundle issueEventBundle);
}
