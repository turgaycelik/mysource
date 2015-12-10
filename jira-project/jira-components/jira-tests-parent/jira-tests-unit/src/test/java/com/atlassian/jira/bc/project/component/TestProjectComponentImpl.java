package com.atlassian.jira.bc.project.component;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class TestProjectComponentImpl
{
    private ProjectComponent pc1;
    private ProjectComponent pc2;
    private ProjectComponent pc3;

    @Before
    public void setUp() throws Exception
    {
        final Long projectId = 4l;
        pc1 = new ProjectComponentImpl(123l, "name", "desc", "lead", 0, projectId, null);
        pc2 = new ProjectComponentImpl(222l, "name", "desc", "lead", 0, projectId, null);
        pc3 = new ProjectComponentImpl(123l, "long name", null, null, 0, projectId, null);
    }

    @Test
    public void testEquals()
    {

        assertThat(pc1, not(equalTo(null)));
        assertFalse("ProjectComponent and text should not equal",pc1.equals("text"));

        assertThat(pc1, equalTo(pc1));
        assertThat(pc1, not(equalTo(pc2)));
        assertThat(pc1, equalTo(pc3));

        assertThat(pc2, not(equalTo(pc1)));
        assertThat(pc2, equalTo(pc2));
        assertThat(pc2, not(equalTo(pc3)));

        assertThat(pc3, equalTo(pc1));
        assertThat(pc3, not(equalTo(pc2)));
        assertThat(pc3, equalTo(pc3));
    }

    @Test
    public void testHashCode()
    {
        assertThat(pc1.hashCode(), equalTo(pc1.hashCode()));
        assertThat(pc1.hashCode(), not(equalTo(pc2.hashCode())));
        assertThat(pc1.hashCode(), equalTo(pc3.hashCode()));
        assertThat(pc2.hashCode(), equalTo(pc2.hashCode()));
        assertThat(pc2.hashCode(), not(equalTo(pc3.hashCode())));
        assertThat(pc3.hashCode(), equalTo(pc3.hashCode()));
    }

    @Test
    public void testGetters()
    {
        assertEquals(new Long(123l), pc1.getId());
        assertEquals("name", pc1.getName());
        assertEquals("desc", pc1.getDescription());
        assertEquals("lead", pc1.getLead());

        assertEquals(new Long(222l), pc2.getId());
        assertEquals("name", pc2.getName());
        assertEquals("desc", pc2.getDescription());
        assertEquals("lead", pc2.getLead());

        assertEquals(new Long(123), pc3.getId());
        assertEquals("long name", pc3.getName());
        assertNull(pc3.getDescription());
        assertNull(pc3.getLead());
    }

    @Test
    public void testToString()
    {
        assertNotNull(pc1.toString());
        assertNotNull(pc2.toString());
        assertNotNull(pc3.toString());
        assertNotNull(new ProjectComponentImpl(null, null, null, null, 0, null, null).toString());
    }

}
