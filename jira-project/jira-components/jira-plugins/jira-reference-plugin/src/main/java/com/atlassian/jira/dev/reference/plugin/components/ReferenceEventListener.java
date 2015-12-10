package com.atlassian.jira.dev.reference.plugin.components;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import org.apache.log4j.Logger;

public class ReferenceEventListener
{
    private static final Logger log = Logger.getLogger(ReferenceEventListener.class);
    
    @EventListener
    public void listenForIssueEvent(IssueEvent issueEvent)
    {
        if (issueEvent.getEventTypeId().equals(EventType.ISSUE_CREATED_ID))
        {
            log.info("Reference Event listener got Issue Created Event");
        }
    }
}
