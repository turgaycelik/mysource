package com.atlassian.jira.event.issue;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.annotations.VisibleForTesting;

import org.ofbiz.core.entity.GenericValue;

/**
 * Default implementation of {@link com.atlassian.jira.event.issue.IssueEventManager}.
 *
 * @since v4.4
 */
public class DefaultIssueEventManager implements IssueEventManager
{
    private final IssueEventParamsTransformer paramsTransformer;
    private final EventPublisher eventPublisher;

    public DefaultIssueEventManager(IssueEventParamsTransformer paramsTransformer, EventPublisher eventPublisher)
    {
        this.paramsTransformer = paramsTransformer;
        this.eventPublisher = Assertions.notNull(eventPublisher);
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail)
    {
        dispatchEvent(eventTypeId, issue, Collections.<String, Object>emptyMap(), remoteUser, sendMail);
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail)
    {
        publishEvent(new IssueEvent(issue, transformParams(params), remoteUser, eventTypeId, sendMail));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, Map<String,Object> params, User remoteUser, boolean sendMail)
    {
        publishAsRedundant(new IssueEvent(issue, transformParams(params), remoteUser, eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser)
    {
        publishEvent(new IssueEvent(issue, params, remoteUser, eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(null), eventTypeId));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        publishAsRedundant(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(null), eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail)
    {
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(null), eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        publishAsRedundant(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId, sendMail));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        publishAsRedundant(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId, sendMail));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog, boolean sendMail, boolean subtasksUpdated)
    {
        publishEvent(new IssueEvent(issue, remoteUser, null, null, changelog, transformParams(null), eventTypeId, sendMail, subtasksUpdated));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog, boolean sendMail, boolean subtasksUpdated)
    {
        publishAsRedundant(new IssueEvent(issue, remoteUser, null, null, changelog, transformParams(null), eventTypeId, sendMail, subtasksUpdated));
    }

    @Override
    public void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        publishEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId, sendMail, subtasksUpdated));
    }

    @Override
    public void dispatchRedundantEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        publishAsRedundant(new IssueEvent(issue, remoteUser, comment, worklog, changelog, transformParams(params), eventTypeId, sendMail, subtasksUpdated));
    }

    @Override
    public void dispatchEvent(final IssueEventBundle issueEventBundle)
    {
        for (Object event : issueEventBundle.getEvents())
        {
            publishEvent(event);
        }
        publishEvent(issueEventBundle);
    }

    private Map<String, Object> transformParams(Map<String, Object> params)
    {
        return paramsTransformer.transformParams(params);
    }

    protected void publishEvent(Object event)
    {
        if (areNotificationsEnabled())
        {
            eventPublisher.publish(event);
        }
    }

    @VisibleForTesting
    boolean areNotificationsEnabled()
    {
        return ImportUtils.isEnableNotifications();
    }

    @VisibleForTesting
    void publishAsRedundant(@Nonnull final IssueEvent issueEvent)
    {
        issueEvent.makeRedundant();
        publishEvent(issueEvent);
    }
}
