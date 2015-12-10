package com.atlassian.jira.bc.project.index;

import com.atlassian.jira.project.MockProject;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/** @since v3.13 */
public class TestProjectIndexTaskContext
{
    @Test
    public void testBuildProgressURL()
    {
        ProjectIndexTaskContext ctx = new ProjectIndexTaskContext(new MockProject(1L));
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(7)).startsWith("/"));
    }

    @Test
    public void testEquals()
    {
        ProjectIndexTaskContext ctx1 = new ProjectIndexTaskContext(new MockProject(1L));
        ProjectIndexTaskContext ctx2 = new ProjectIndexTaskContext(new MockProject(1L));

        assertFalse(ctx1.equals(null));
        assertTrue(ctx1.equals(ctx2));
        assertTrue(ctx1.equals(ctx1));

        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }

    @Test
    public void instancesShouldBeSerializable()
    {
        final ProjectIndexTaskContext context = new ProjectIndexTaskContext(new MockProject(1L));
        // Invoke
        final ProjectIndexTaskContext roundTrippedEvent = (ProjectIndexTaskContext) deserialize(serialize(context));

        assertEquals(context, roundTrippedEvent);
        assertNotSame(context, roundTrippedEvent);
    }

}
