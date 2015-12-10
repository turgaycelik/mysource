package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.priority.Priority;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Priotity Sequences are high-priority to lowest-priority
 *
 * @since v4.0
 */
public class TestPriorityObjectComparator
{
    @Test
    public void testCompare() {
        Comparator<Priority> comparator = PriorityObjectComparator.PRIORITY_OBJECT_COMPARATOR;

        MockPriority mock1 = new MockPriority("1", "one");
        MockPriority mock2 = new MockPriority("2", "two");
        MockPriority mock3 = new MockPriority("3", "three");
        MockPriority mock4 = new MockPriority("4", "four");

        mock1.setSequence(new Long(4));
        mock2.setSequence(new Long(3));
        mock3.setSequence(new Long(2));
        mock4.setSequence(new Long(1));

        assertEquals(comparator.compare(mock1, mock2), -1);
        assertEquals(comparator.compare(mock2, mock1), 1);
        assertEquals(comparator.compare(mock1, mock1), 0);
        assertEquals(comparator.compare(mock1, mock3), -1);
        assertEquals(comparator.compare(mock3, mock1), 1);
    }
}
