package com.atlassian.jira.event.issue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class TestIssueEventBundle
{
    @Test
    public void theListOfEventsIsImmutable()
    {
        List<JiraIssueEvent> events = new ArrayList<JiraIssueEvent>();
        events.add(newEvent());
        events.add(newEvent());

        IssueEventBundle eventBundle = DefaultIssueEventBundle.create(events);

        events.add(newEvent());

        assertThat(eventBundle.getEvents().size(), is(2));
    }

    private JiraIssueEvent newEvent()
    {
        return mock(JiraIssueEvent.class);
    }
}
