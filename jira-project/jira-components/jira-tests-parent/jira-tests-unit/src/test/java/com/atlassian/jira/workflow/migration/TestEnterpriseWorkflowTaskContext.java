package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestEnterpriseWorkflowTaskContext
{
    private static final Project PROJECT1 = new MockProject(5);
    private static final Long SCHEME_ID1 = new Long(5000);
    private static final Project PROJECT2 = new MockProject(6);
    private static final Project PROJECT3 = new MockProject(7);
    private static final Project PROJECT4 = new MockProject(8);

    @Test
    public void testBuildProgressConstruction()
    {
        try
        {
            new EnterpriseWorkflowTaskContext(null, SCHEME_ID1, false);
            fail("Object should not accept a null workflow.");
        }
        catch (RuntimeException e)
        {
            //expected.
        }

        EnterpriseWorkflowTaskContext ctx = new EnterpriseWorkflowTaskContext(PROJECT1, null, false);
        assertEquals(PROJECT1.getId(), ctx.getTriggerProjectId());
        assertNull(ctx.getSchemeId());

        ctx = new EnterpriseWorkflowTaskContext(PROJECT1, SCHEME_ID1, false);
        assertEquals(PROJECT1.getId(), ctx.getTriggerProjectId());
        assertEquals(SCHEME_ID1, ctx.getSchemeId());
    }

    @Test
    public void testBuildProgressURL()
    {
        EnterpriseWorkflowTaskContext ctx = new EnterpriseWorkflowTaskContext(PROJECT1, SCHEME_ID1, false);
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(-1)).startsWith("/"));

        ctx = new EnterpriseWorkflowTaskContext(PROJECT1, null, false);
        assertNotNull(ctx.buildProgressURL(new Long(6)));
        assertTrue(ctx.buildProgressURL(new Long(-1)).startsWith("/"));
    }

    @Test
    public void testEquals()
    {
        EnterpriseWorkflowTaskContext project1Scheme1 = new EnterpriseWorkflowTaskContext(PROJECT1, SCHEME_ID1, false);
        EnterpriseWorkflowTaskContext project1SchemeNull = new EnterpriseWorkflowTaskContext(PROJECT1, null, false);
        EnterpriseWorkflowTaskContext project2Scheme1 = new EnterpriseWorkflowTaskContext(PROJECT2, SCHEME_ID1, false);
        EnterpriseWorkflowTaskContext project2SchemeNull = new EnterpriseWorkflowTaskContext(PROJECT2, null, false);
        EnterpriseWorkflowTaskContext project1Project2Scheme1 = new EnterpriseWorkflowTaskContext(PROJECT1, asList(PROJECT1, PROJECT2), SCHEME_ID1, false);
        EnterpriseWorkflowTaskContext project2Project3Scheme1 = new EnterpriseWorkflowTaskContext(PROJECT2, asList(PROJECT2, PROJECT3), SCHEME_ID1, false);
        EnterpriseWorkflowTaskContext project3Project4Scheme1 = new EnterpriseWorkflowTaskContext(PROJECT2, asList(PROJECT3, PROJECT4), SCHEME_ID1, false);

        assertFalse(project1Scheme1.equals(null));
        assertFalse(project1SchemeNull.equals(null));
        assertFalse(project1Project2Scheme1.equals(null));

        assertTrue(project1Scheme1.equals(project1Scheme1));
        assertTrue(project1SchemeNull.equals(project1SchemeNull));
        assertTrue(project1Project2Scheme1.equals(project1Project2Scheme1));

        assertTrue(project1Scheme1.equals(project1SchemeNull));
        assertTrue(project1Scheme1.equals(project1Project2Scheme1));
        assertFalse(project1Scheme1.equals(project2Scheme1));
        assertFalse(project1Scheme1.equals(project2SchemeNull));
        assertFalse(project1Scheme1.equals(project2Project3Scheme1));
        assertFalse(project1Scheme1.equals(project3Project4Scheme1));

        assertTrue(project1SchemeNull.equals(project1Scheme1));
        assertTrue(project1SchemeNull.equals(project1Project2Scheme1));
        assertFalse(project1SchemeNull.equals(project2Scheme1));
        assertFalse(project1SchemeNull.equals(project2SchemeNull));
        assertFalse(project1SchemeNull.equals(project2Project3Scheme1));
        assertFalse(project1SchemeNull.equals(project3Project4Scheme1));

        assertTrue(project2Scheme1.equals(project2SchemeNull));
        assertTrue(project2Scheme1.equals(project1Project2Scheme1));
        assertFalse(project2Scheme1.equals(project1Scheme1));
        assertFalse(project2Scheme1.equals(project1SchemeNull));
        assertTrue(project2Scheme1.equals(project2Project3Scheme1));
        assertFalse(project2Scheme1.equals(project3Project4Scheme1));

        assertTrue(project2SchemeNull.equals(project2Scheme1));
        assertTrue(project2SchemeNull.equals(project1Project2Scheme1));
        assertFalse(project2SchemeNull.equals(project1Scheme1));
        assertFalse(project2SchemeNull.equals(project1SchemeNull));
        assertTrue(project2SchemeNull.equals(project2Project3Scheme1));
        assertFalse(project2SchemeNull.equals(project3Project4Scheme1));

        assertTrue(project1Project2Scheme1.equals(project1Scheme1));
        assertTrue(project1Project2Scheme1.equals(project1SchemeNull));
        assertTrue(project1Project2Scheme1.equals(project2Scheme1));
        assertTrue(project1Project2Scheme1.equals(project2SchemeNull));
        assertTrue(project1Project2Scheme1.equals(project2Project3Scheme1));
        assertFalse(project1Project2Scheme1.equals(project3Project4Scheme1));

        assertEquals(project1Scheme1.hashCode(), project1SchemeNull.hashCode());
    }

    @Test
    public void instancesShouldBeSerializable()
    {
        rountrip(new EnterpriseWorkflowTaskContext(PROJECT1, null, false));
        rountrip(new EnterpriseWorkflowTaskContext(PROJECT1, SCHEME_ID1, false));
        rountrip(new EnterpriseWorkflowTaskContext(PROJECT1, asList(PROJECT1, PROJECT2), SCHEME_ID1, false));
    }

    private void rountrip(final EnterpriseWorkflowTaskContext context)
    {// Invoke
        final EnterpriseWorkflowTaskContext roundTrippedEvent = (EnterpriseWorkflowTaskContext) deserialize(serialize(context));
        // Equals is not implemented so we just assert we got something new.
        assertNotSame(context, roundTrippedEvent);
    }

}
