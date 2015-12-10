package com.atlassian.jira.external.beans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestExternalUser
{
    @Test
    public void testSetExtraProperty()
    {
        ExternalUser externalUser = new ExternalUser();
        assertTrue(externalUser.getUserPropertyMap().isEmpty());
        // Add just one property
        externalUser.setUserProperty("banana", "yellow");
        assertEquals(1, externalUser.getUserPropertyMap().size());
        assertEquals("yellow", externalUser.getUserPropertyMap().get("banana"));

        // Add another property
        externalUser.setUserProperty("cherry", "red");
        assertEquals(2, externalUser.getUserPropertyMap().size());
        assertEquals("yellow", externalUser.getUserPropertyMap().get("banana"));
        assertEquals("red", externalUser.getUserPropertyMap().get("cherry"));

        // Change one property
        externalUser.setUserProperty("banana", "green");
        assertEquals(2, externalUser.getUserPropertyMap().size());
        assertEquals("green", externalUser.getUserPropertyMap().get("banana"));
        assertEquals("red", externalUser.getUserPropertyMap().get("cherry"));
    }

    @Test
    public void testEqualsForUserProperty()
    {
        ExternalUser externalUser1 = new ExternalUser("tom", "Thomas de Tankenjin", "tom@tank.com", "cat");
        ExternalUser externalUser2 = new ExternalUser("tom", "Thomas de Tankenjin", "tom@tank.com", "cat");
        // Users should be the same
        assertTrue(externalUser1.equals(externalUser2));

        // Add "extra" property
        externalUser1.setUserProperty("banana", "green");
        assertFalse(externalUser1.equals(externalUser2));
        externalUser2.setUserProperty("banana", "green");
        assertTrue(externalUser1.equals(externalUser2));

    }
}
