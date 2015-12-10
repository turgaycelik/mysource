package com.atlassian.jira.util;

import com.atlassian.jira.event.type.EventType;
import org.apache.commons.collections.Transformer;

import java.util.HashMap;
import java.util.Map;

public class EventTypeOrderTransformer implements Transformer
{
    private Map<Long,Long> mappings;


    public EventTypeOrderTransformer()
    {
        long i = 1L;
        mappings = new HashMap<Long,Long>();

        // The order they appear below is the order tehy appear on screen
        mappings.put(EventType.ISSUE_CREATED_ID, i++);
        mappings.put(EventType.ISSUE_UPDATED_ID, i++);
        mappings.put(EventType.ISSUE_ASSIGNED_ID, i++);
        mappings.put(EventType.ISSUE_RESOLVED_ID, i++);
        mappings.put(EventType.ISSUE_CLOSED_ID, i++);
        mappings.put(EventType.ISSUE_COMMENTED_ID, i++);
        mappings.put(EventType.ISSUE_COMMENT_EDITED_ID, i++);
        mappings.put(EventType.ISSUE_COMMENT_DELETED_ID, i++);
        mappings.put(EventType.ISSUE_REOPENED_ID, i++);
        mappings.put(EventType.ISSUE_DELETED_ID, i++);
        mappings.put(EventType.ISSUE_MOVED_ID, i++);
        mappings.put(EventType.ISSUE_WORKLOGGED_ID, i++);
        mappings.put(EventType.ISSUE_WORKSTARTED_ID, i++);
        mappings.put(EventType.ISSUE_WORKSTOPPED_ID, i++);
        mappings.put(EventType.ISSUE_WORKLOG_UPDATED_ID, i++);
        mappings.put(EventType.ISSUE_WORKLOG_DELETED_ID, i++);
        mappings.put(EventType.ISSUE_GENERICEVENT_ID, i++);

    }


    public Object transform(Object object) {
        if (object instanceof EventType)
        {
            EventType eventType = (EventType)object;
            Long order;

            if (mappings.containsKey(eventType.getId()))
            {
                order = mappings.get(eventType.getId());
            }
            else
            {
                order = eventType.getId();
            }

            return order;

        }
        return object;
    }
}
