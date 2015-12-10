/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.util.ImportUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Static utility to dispatch issue events.
 *
 * @deprecated Since v5.0 use  {@link com.atlassian.jira.event.issue.IssueEventManager} instead.
 */
public class IssueEventDispatcher
{

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), remoteUser);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     *
     * @deprecated use {@link com.atlassian.jira.event.issue.IssueEventManager#dispatchEvent(Long,
     * com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User, boolean)} instead.
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), remoteUser, sendMail);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, remoteUser, eventTypeId));
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     *
     * @deprecated use {@link com.atlassian.jira.event.issue.IssueEventManager#dispatchEvent(Long,
     * com.atlassian.jira.issue.Issue, java.util.Map, com.atlassian.crowd.embedded.api.User, boolean)} instead.
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, remoteUser, eventTypeId, sendMail));
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, remoteUser, comment, worklog, changelog, copyOfParams);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, remoteUser, comment, worklog, changelog, copyOfParams, sendMail);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId));
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail));
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog,
                                        boolean sendMail, boolean subtasksUpdated)
    {
        dispatchEvent(eventTypeId, issue, remoteUser, null, null, changelog, null, sendMail, subtasksUpdated);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @deprecated Since v5.0
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog,
                                          GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail, subtasksUpdated));
    }

    private static Map<String, Object> copyParams(final Map params)
    {
        Map<String,Object> copyOfParams = new HashMap<String,Object>();
        if (params != null)
        {
            copyOfParams.putAll(params);
        }
        return copyOfParams;
    }

    private static void putBaseUrlIntoPlay(final Map<String, Object> params)
    {
        params.put("baseurl", ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
    }

    /**
     * Notifies registered IssueEventListeners of the given event.
     * @param event the event.
     */
    public static void dispatchEvent(IssueEvent event)
    {
        if (ImportUtils.isEnableNotifications())
        {
            ComponentAccessor.getComponentOfType(EventPublisher.class).publish(event);
        }
    }
}
