package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.config.IndexTaskContext;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/** @since v3.13 */
public class TestIndexTaskContext
{
    @Test
    public void testBuildProgressURL()
    {
        IndexTaskContext ctx = new IndexTaskContext();
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(7)).startsWith("/"));
    }

    @Test
    public void testEquals()
    {
        IndexTaskContext ctx1 = new IndexTaskContext();
        IndexTaskContext ctx2 = new IndexTaskContext();

        assertFalse(ctx1.equals(null));
        assertTrue(ctx1.equals(ctx2));
        assertTrue(ctx1.equals(ctx1));

        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }

    @Test
    public void instancesShouldBeSerializable()
    {
        final IndexTaskContext context = new IndexTaskContext();
        // Invoke
        final IndexTaskContext roundTrippedEvent = (IndexTaskContext) deserialize(serialize(context));

        // Equals is not implemented so we just assert we got something new.
        assertNotSame(context, roundTrippedEvent);
    }

}
