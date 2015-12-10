/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestBuildNumComparator
{
    private BuildNumComparator versionComparator = new BuildNumComparator();

    @Test
    public void testComparator()
    {
        assertTrue(compare("59", "6") > 0);
        assertTrue(compare("1.1", "1.1beta2") == 0);
        assertTrue(compare("1.0", "1.1") < 0);
        assertTrue(compare("1.1", "1.0") > 0);
        assertTrue(compare("1.2", "1.1beta2") > 0);
        assertTrue(compare("1.1.1.1", "1.2.0.1") < 0);
        assertTrue(compare("1", "0.9") > 0);
        assertTrue(compare("1", "0.1") > 0);
        assertTrue(compare("1", "1.0.0.0beta3") == 0);
        assertTrue(compare("59", "5.9") > 0);
        assertTrue(compare("10", "9.9") > 0);
        assertTrue(compare("10", "10.0") == 0);
        assertTrue(compare("1.0.1", "1.0.0.1") > 0);
        assertTrue(compare("1.1.1.0", "1.1.0.1") > 0);
    }

    private int compare(String o, String o2)
    {
        return versionComparator.compare(o, o2);
    }
}
