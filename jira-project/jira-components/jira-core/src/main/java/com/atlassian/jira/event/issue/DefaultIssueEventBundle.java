package com.atlassian.jira.event.issue;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;

import com.google.common.collect.ImmutableList;

/**
 * This is the default implementation of {@link com.atlassian.jira.event.issue.IssueEventBundle}.
 * @since 6.3.10
 */
@ExperimentalApi
public class DefaultIssueEventBundle implements IssueEventBundle
{
    private final Collection<JiraIssueEvent> events;
    private final boolean sendEmailNotification;

    private DefaultIssueEventBundle(final Collection<? extends JiraIssueEvent> events,
            final boolean sendEmailNotification)
    {
        this.events = ImmutableList.copyOf(events);
        this.sendEmailNotification = sendEmailNotification;
    }

    /**
     * Creates an instance of {@link com.atlassian.jira.event.issue.DefaultIssueEventBundle} with the given events.
     * The returned instance is configured so if it is published, email notifications will be generated for the events.
     * @param events The list of events.
     * @return An instance of {@link com.atlassian.jira.event.issue.DefaultIssueEventBundle} containing the events.
     * @see {@link com.atlassian.jira.event.issue.JiraIssueEvent}
     */
    @Nonnull
    public static DefaultIssueEventBundle create(@Nonnull final Collection<? extends JiraIssueEvent> events)
    {
        return new DefaultIssueEventBundle(events, true);
    }

    /**
     * Creates an instance of {@link com.atlassian.jira.event.issue.DefaultIssueEventBundle} with the given events.
     * The returned instance is configured so if it is published, no email notifications will be generated for the events.
     * @param events The list of events.
     * @return An instance of {@link com.atlassian.jira.event.issue.DefaultIssueEventBundle} containing the events.
     */
    @Nonnull
    public static DefaultIssueEventBundle createDisallowingEmailNotifications(
            @Nonnull final Collection<JiraIssueEvent> events)
    {
        return new DefaultIssueEventBundle(events, false);
    }

    @Override
    @Nonnull
    public Collection<JiraIssueEvent> getEvents()
    {
        return events;
    }

    @Override
    public boolean doesSendEmailNotification()
    {
        return sendEmailNotification;
    }
}
