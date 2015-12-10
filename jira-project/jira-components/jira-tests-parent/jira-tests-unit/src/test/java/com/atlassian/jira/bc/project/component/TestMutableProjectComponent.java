package com.atlassian.jira.bc.project.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestMutableProjectComponent
{
    private static final Long PROJECT_ID = new Long(9);
    private MutableProjectComponent pc1;
    private MutableProjectComponent pc2;
    private MutableProjectComponent pc3;

    @Before
    public void setUp() throws Exception
    {
        pc1 = new MutableProjectComponent(new Long(123), "name", "desc", "lead", 0, PROJECT_ID);
        pc2 = new MutableProjectComponent(new Long(222), "name", "desc", "lead", 0, PROJECT_ID);
        pc3 = new MutableProjectComponent(new Long(123), "long name", null, null, 0, PROJECT_ID);
    }

    @Test
    public void testEquals()
    {

        assertFalse(pc1.equals(null));
        assertFalse(pc1.equals("text"));

        assertTrue(pc1.equals(pc1));
        assertFalse(pc1.equals(pc2));
        assertTrue(pc1.equals(pc3));

        assertFalse(pc2.equals(pc1));
        assertTrue(pc2.equals(pc2));
        assertFalse(pc2.equals(pc3));

        assertTrue(pc3.equals(pc1));
        assertFalse(pc3.equals(pc2));
        assertTrue(pc3.equals(pc3));
    }

    @Test
    public void testHashCode()
    {
        assertTrue(pc1.hashCode() == pc1.hashCode());
        assertTrue(pc1.hashCode() != pc2.hashCode());
        assertTrue(pc1.hashCode() == pc3.hashCode());
        assertTrue(pc2.hashCode() == pc2.hashCode());
        assertTrue(pc2.hashCode() != pc3.hashCode());
        assertTrue(pc3.hashCode() == pc3.hashCode());
    }

    @Test
    public void testGetters()
    {
        assertEquals(new Long(123), pc1.getId());
        assertEquals("name", pc1.getName());
        assertEquals("desc", pc1.getDescription());
        assertEquals("lead", pc1.getLead());

        assertEquals(new Long(222), pc2.getId());
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

    @Test
    public void testCopySingle()
    {
        MutableProjectComponent copy1 = MutableProjectComponent.copy(pc1);
        MutableProjectComponent copy2 = MutableProjectComponent.copy(pc2);
        MutableProjectComponent copy3 = MutableProjectComponent.copy(pc3);
        assertCopyComponentEquals(copy1, pc1);
        assertCopyComponentEquals(copy2, pc2);
        assertCopyComponentEquals(copy3, pc3);
    }

    private void assertCopyComponentEquals(MutableProjectComponent c1, MutableProjectComponent c2)
    {
        assertNotNull(c1);
        assertEquals(c1, c2);
        assertEquals(c1.getId(), c2.getId());
        assertEquals(c1.getName(), c2.getName());
        assertEquals(c1.getDescription(), c2.getDescription());
        assertEquals(c1.getLead(), c2.getLead());
        assertEquals(c1.getProjectId(), c2.getProjectId());
    }

    @Test
    public void testCopyCollection()
    {
        Collection col = new ArrayList(3);
        col.add(pc1);
        col.add(pc2);
        col.add(pc3);

        Collection copy = MutableProjectComponent.copy(col);
        assertNotNull(copy);
        assertEquals(col.size(), copy.size());
        for (Iterator i = col.iterator(); i.hasNext();)
        {
            MutableProjectComponent component = (MutableProjectComponent) i.next();
            assertTrue(copy.contains(component));
        }
        for (Iterator i = copy.iterator(); i.hasNext();)
        {
            MutableProjectComponent component = (MutableProjectComponent) i.next();
            assertTrue(col.contains(component));
        }

    }

}
