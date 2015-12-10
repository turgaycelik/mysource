/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.util.system;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Eugene Kuleshov
 */
public class TestVersionNumber
{
    @Test
    public void testVersion() throws Exception
    {
        assertVersion(null, null, 0);
        assertVersion(null, "", 0);
        assertVersion(null, "3", -1);

        assertVersion("", null, 0);
        assertVersion("", "", 0);
        assertVersion("", "3", -1);

        assertVersion("3", null, 1);
        assertVersion("3", "", 1);
        assertVersion("3-dev", "", 1);
        assertVersion("2", "3", -1);
        assertVersion("2", "3-dev", -1);
        assertVersion("3", "3", 0);
        assertVersion("3", "3-dev", -3);
        assertVersion("4", "3", 1);
        assertVersion("10", "3", 1);
        assertVersion("10", "100", -1);
        assertVersion("3", "3.1", -1);
        assertVersion("3", "3.1-dev", -1);
        assertVersion("3-dev", "3.1", -1);
        assertVersion("3", "3.3.3", -1);

        assertVersion("3.1", null, 1);
        assertVersion("3.1", "", 1);
        assertVersion("2.1", "3", -1);
        assertVersion("3.1", "3", 1);
        assertVersion("4.1", "3", 1);
        assertVersion("3.1", "3.3", -1);
        assertVersion("3.1", "3.10", -1);
        assertVersion("3.1", "2.10", 1);
        assertVersion("3.1", "3.3.3", -1);

        assertVersion("3.1.1", null, 1);
        assertVersion("3.1.1", "", 1);
        assertVersion("2.1.1", "3", -1);
        assertVersion("3.1.1", "3", 1);
        assertVersion("4.1.1", "3", 1);
        assertVersion("3.1.1", "3.3", -1);
        assertVersion("3.1.1", "3.10", -1);
        assertVersion("3.1.1", "2.10", 1);
        assertVersion("3.1.1", "3.3.3", -1);
        assertVersion("3.3.1", "3.3.3", -1);
        assertVersion("3.3.4", "3.3.3", 1);
        assertVersion("3.3.10", "3.3.3", 1);
        assertVersion("3.3.10a", "3.3.3a", 1);
        assertVersion("3.3a.10", "3.3a.3", 1);
        assertVersion("3a.3.10", "3a.3.3", 1);
    }

    @Test
    public void testToString() throws Exception
    {
        assertEquals("3.0", new VersionNumber("3.0").toString());
        assertEquals("3.0-dev", new VersionNumber("3.0-dev").toString());
        assertEquals("3.6.5-#161", new VersionNumber("3.6.5-#161").toString());
        assertEquals("3.9-#233", new VersionNumber("3.9-#233").toString());
        assertEquals("3.10-DEV-190607-#251", new VersionNumber("3.10-DEV-190607-#251").toString());
    }
    
    @Test
    public void testGetOSGIVersion() throws Exception
    {
        assertEquals("3.0.0", new VersionNumber("3.0").getOSGIVersion());
        assertEquals("3.0.0.dev", new VersionNumber("3.0-dev").getOSGIVersion());
        assertEquals("3.6.5.#161", new VersionNumber("3.6.5-#161").getOSGIVersion());
        assertEquals("3.9.0.#233", new VersionNumber("3.9-#233").getOSGIVersion());
        assertEquals("3.10.0.DEV-190607-#251", new VersionNumber("3.10-DEV-190607-#251").getOSGIVersion());
        assertEquals("4.0.0.SNAPSHOT", new VersionNumber("4.0-SNAPSHOT").getOSGIVersion());
    }

    private void assertVersion(String s1, String s2, int expected)
    {
        VersionNumber v1 = v(s1);
        VersionNumber v2 = v(s2);
        final String msg = s1 + " / " + s2;
        assertEquals(msg, expected, v1.compareTo(v2));
        switch (expected) {
            case 1:
            {
                assertTrue(msg, v1.isGreaterThan(v2));
                assertFalse(msg, v2.isGreaterThan(v1));
                assertFalse(msg, v2.isGreaterThanOrEquals(v1));
                assertFalse(msg, v1.equals(v2));
                assertFalse(msg, v2.equals(v1));
                assertTrue(msg, v2.isLessThan(v1));
                assertTrue(msg, v2.isLessThanOrEquals(v1));
                assertFalse(msg, v1.isLessThan(v2));
                assertFalse(msg, v1.hashCode() == v2.hashCode());
                break;
            }
            case 0:
            {
                assertFalse(msg, v2.isGreaterThan(v1));
                assertFalse(msg, v1.isGreaterThan(v2));
                assertTrue(msg, v1.equals(v2));
                assertTrue(msg, v2.equals(v1));
                assertTrue(msg, v1.isGreaterThanOrEquals(v2));
                assertTrue(msg, v2.isGreaterThanOrEquals(v1));
                assertTrue(msg, v1.isLessThanOrEquals(v2));
                assertTrue(msg, v2.isLessThanOrEquals(v1));
                assertFalse(msg, v1.isLessThan(v2));
                assertFalse(msg, v1.isLessThan(v2));
                assertEquals(msg, v1, v2);
                assertEquals(msg, v1.hashCode(), v2.hashCode());
                break;
            }
            case -1:
            {
                assertTrue(msg, v2.isGreaterThan(v1));
                assertTrue(msg, v2.isGreaterThanOrEquals(v1));
                assertFalse(msg, v1.isGreaterThan(v2));
                assertFalse(msg, v1.equals(v2));
                assertFalse(msg, v2.equals(v1));
                assertTrue(msg, v1.isLessThan(v2));
                assertTrue(msg, v1.isLessThanOrEquals(v2));
                assertFalse(msg, v2.isLessThan(v1));
                assertFalse(msg, v1.hashCode() == v2.hashCode());
                break;
            }
        }
    }

    private VersionNumber v(String v)
    {
        return new VersionNumber(v);
    }
}
