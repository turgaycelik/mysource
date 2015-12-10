package com.atlassian.jira.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestJiraUserPreferences
{
    ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = new MockApplicationProperties();
    }

    /** An osuser User Object that returns null for getPropertySet() will cause NPE. */
    @Test
    public void test_JRA_13778()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        // Should act as though it has a null user:
        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        try
        {
            jiraUserPreferences.setString("Fruit", "Banana");
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetString() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
    }

    @Test
    public void testGetAndSetStringNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        assertNull(jiraUserPreferences.getString("Fruit"));
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        try
        {
            jiraUserPreferences.setString("Fruit", "Banana");
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetLong() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        try
        {
            jiraUserPreferences.getLong("Size");
            fail("getLong() on  non-existant value should throw NumberFormatException.");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }
        applicationProperties.setString("Size", "12");
        assertEquals(12, jiraUserPreferences.getLong("Size"));
        jiraUserPreferences.setLong("Size", 20);
        assertEquals(20, jiraUserPreferences.getLong("Size"));
    }

    @Test
    public void testModifyPropertyViaUserPropertySetAndSeeChangeInPrefs() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        propertySet.setString("Test", "Stuff");
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);
        assertEquals("Stuff", jiraUserPreferences.getString("Test"));

        // Now change it on the users property
        propertySet.setString("Test", "Other Stuff");

        // We should see the change
        assertEquals("Other Stuff", jiraUserPreferences.getString("Test"));
    }

    @Test
    public void testGetAndSetLongNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        try
        {
            jiraUserPreferences.getLong("Size");
            fail("getLong() on  non-existant value should throw NumberFormatException.");
        }
        catch (NumberFormatException ex)
        {
            // expected
        }
        applicationProperties.setString("Size", "10");
        assertEquals(10, jiraUserPreferences.getLong("Size"));

        // Test the setter
        try
        {
            jiraUserPreferences.setLong("Size", 20);
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testGetAndSetBoolean() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
        applicationProperties.setOption("GoFastFlag", true);
        assertTrue(jiraUserPreferences.getBoolean("GoFastFlag"));
        jiraUserPreferences.setBoolean("GoFastFlag", false);
        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
    }

    @Test
    public void testGetAndSetBooleanNullUser()
    {
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(null);

        assertFalse(jiraUserPreferences.getBoolean("GoFastFlag"));
        applicationProperties.setOption("GoFastFlag", true);
        assertTrue(jiraUserPreferences.getBoolean("GoFastFlag"));

        // Test the setter
        try
        {
            jiraUserPreferences.setBoolean("Size", true);
            fail("Should not be allowed to set a Value on JiraUserPreferences with null User.");
        }
        catch (AtlassianCoreException e)
        {
            // Expected behaviour
        }
    }

    @Test
    public void testRemove() throws AtlassianCoreException
    {
        // Now create a JiraUserPreferences object for this User
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);
        JiraUserPreferences jiraUserPreferences = new MyJiraUserPreferences(propertySet);

        assertNull(jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.remove("Fruit");
        assertNull(jiraUserPreferences.getString("Fruit"));

        // Now test with underlying default property
        applicationProperties.setString("Fruit", "Apple");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.setString("Fruit", "Banana");
        assertEquals("Banana", jiraUserPreferences.getString("Fruit"));
        jiraUserPreferences.remove("Fruit");
        assertEquals("Apple", jiraUserPreferences.getString("Fruit"));
    }

    @Test
    public void testEquals() throws AtlassianCoreException
    {
        final PropertySet ps1 = new MemoryPropertySet();
        ps1.init(null, null);
        ps1.setString("colour", "blue");

        final PropertySet ps2 = new MemoryPropertySet();
        ps2.init(null, null);
        ps2.setString("fruit", "bat");

        // Equality is determined by the userkey and nothing else
        assertThat(new JiraUserPreferences(null, null), equalTo(new JiraUserPreferences(null, null)));
        assertThat(new JiraUserPreferences(null, ps1), equalTo(new JiraUserPreferences(null, ps1)));
        assertThat(new JiraUserPreferences(null, ps1), equalTo(new JiraUserPreferences(null, ps2)));
        assertThat(new JiraUserPreferences("fred", null), equalTo(new JiraUserPreferences("fred", null)));
        assertThat(new JiraUserPreferences("fred", null), equalTo(new JiraUserPreferences("fred", ps1)));
        assertThat(new JiraUserPreferences("fred", ps1), equalTo(new JiraUserPreferences("fred", ps1)));
        assertThat(new JiraUserPreferences("fred", ps1), equalTo(new JiraUserPreferences("fred", ps2)));
        assertThat(new JiraUserPreferences("fred", null), not(equalTo(new JiraUserPreferences("george", null))));
        assertThat(new JiraUserPreferences("fred", null), not(equalTo(new JiraUserPreferences("george", ps1))));
        assertThat(new JiraUserPreferences("fred", ps1), not(equalTo(new JiraUserPreferences("george", ps1))));
        assertThat(new JiraUserPreferences("fred", ps1), not(equalTo(new JiraUserPreferences("george", ps2))));
    }

    @Test
    public void testHashCode() throws AtlassianCoreException
    {
        final PropertySet ps = new MemoryPropertySet();
        ps.init(null, null);
        ps.setString("colour", "blue");

        final JiraUserPreferences prefsForFred = new JiraUserPreferences("fred", ps);
        final JiraUserPreferences prefsForGeorge = new JiraUserPreferences("george", ps);

        assertThat("fred".hashCode(), equalTo(prefsForFred.hashCode()));
        assertThat("george".hashCode(), equalTo(prefsForGeorge.hashCode()));

        // "fred" and "george" should deterministically have different hash codes.  If they were
        // the same then the other two tests would be inconclusive.
        assertThat(prefsForFred.hashCode(), not(equalTo(prefsForGeorge.hashCode())));
    }

    /**
     * In order to avoid DB operations to get ApplicationProperties, we extend JiraUserPreferences and override the
     * getApplicationProperties() method.
     */
    private class MyJiraUserPreferences extends JiraUserPreferences
    {
        MyJiraUserPreferences(PropertySet propertySet)
        {
            super(propertySet);
        }

        ApplicationProperties getApplicationProperties()
        {
            return applicationProperties;
        }
    }
}

