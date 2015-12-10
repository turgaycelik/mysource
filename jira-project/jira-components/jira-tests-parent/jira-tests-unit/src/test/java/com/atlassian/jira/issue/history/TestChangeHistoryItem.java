package com.atlassian.jira.issue.history;

import java.sql.Timestamp;

import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @since v4.4.2
 */
public class TestChangeHistoryItem
{
    @Test
    public void testChangeHistoryItem()
    {
        final Timestamp created = new Timestamp(1);
        ChangeHistoryItem item = new ChangeHistoryItem(1L, 2L, 3L, 4L, "Issue4", "Field1", new Timestamp(1), "from", "to", "fromValue" , "toValue", "user");

        assertSingleValue(item, created);

        ChangeHistoryItem.Builder builder = new ChangeHistoryItem.Builder().fromChangeItem(item).changedFrom("from2", "from2Value").to("to2", "to2Value");
        item = builder.build();

        assertTrue(item.containsFromValue("from2Value"));
        assertTrue(item.containsToValue("to2Value"));
        assertTrue(item.getFroms().size() == 2);
        assertTrue(item.getTos().size() == 2);
    }

    private void assertSingleValue(ChangeHistoryItem item, Timestamp created)
    {
        assertEquals("Field1", item.getField());
        assertEquals("from", item.getFrom());
        assertEquals("fromValue", item.getFromValue());
        assertEquals("to", item.getTo());
        assertEquals("toValue", item.getToValue());
        assertEquals(created , item.getCreated());
        assertTrue(item.containsFromValue("fromValue"));
        assertTrue(item.containsToValue("toValue"));
    }
}
