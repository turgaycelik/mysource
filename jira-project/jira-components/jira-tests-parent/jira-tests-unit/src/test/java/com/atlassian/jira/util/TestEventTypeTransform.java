package com.atlassian.jira.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.event.type.EventType;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the JiraKeyUtils class
 */
public class TestEventTypeTransform
{
    private final Transformer transformer = new EventTypeOrderTransformer();
    private final Long createdOrder = new Long(1);
    private final Long commentEditedOrder = new Long(7);
    private final Long customEventId = new Long(9876);

    private final EventType createdEvent = new EventType(EventType.ISSUE_CREATED_ID, null, null, null);
    private final EventType assignedEvent = new EventType(EventType.ISSUE_ASSIGNED_ID, null, null, null);
    private final EventType deletedEvent = new EventType(EventType.ISSUE_DELETED_ID, null, null, null);
    private final EventType commentedEvent = new EventType(EventType.ISSUE_COMMENTED_ID, null, null, null);
    private final EventType commentEditEvent = new EventType(EventType.ISSUE_COMMENT_EDITED_ID, null, null, null);
    private final EventType closedEvent = new EventType(EventType.ISSUE_CLOSED_ID, null, null, null);
    private final EventType customEvent = new EventType(customEventId, null, null, null);


    @Test
    public void testSingleTransform()
    {
        assertEquals("Created is First", transformer.transform(createdEvent), createdOrder);
        assertEquals("Comment Edited transformed", transformer.transform(commentEditEvent), commentEditedOrder);
        assertEquals("Non existant mapping", transformer.transform(customEvent), customEventId);
    }

    @Test
    public void testTransformSort()
    {
        List<EventType> eventList = new ArrayList<EventType>();
        eventList.add(commentedEvent);
        eventList.add(closedEvent);
        eventList.add(deletedEvent);
        eventList.add(customEvent); // Non Existant key
        eventList.add(commentEditEvent);
        eventList.add(assignedEvent);
        eventList.add(createdEvent);

        Collections.sort(eventList, new TransformingComparator(transformer));

        int i = 0;
        assertEquals(eventList.get(i++), createdEvent);
        assertEquals(eventList.get(i++), assignedEvent);
        assertEquals(eventList.get(i++), closedEvent);
        assertEquals(eventList.get(i++), commentedEvent);
        assertEquals(eventList.get(i++), commentEditEvent);
        assertEquals(eventList.get(i++), deletedEvent);
        assertEquals(eventList.get(i++), customEvent);
    }

}
