/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestKeyComparator
{
    @Test
    public void testKeys()
    {
        Comparator comp = KeyComparator.COMPARATOR;
        assertTrue(0 ==  comp.compare("JRA-123", "JRA-123"));
        assertTrue(0 > comp.compare("JRA-123", "JRA-124"));
        assertTrue(0 < comp.compare("JRA-124", "JRA-123"));
        assertTrue(0 > comp.compare("JRA-123", "JRA-1234567"));
        assertTrue(0 < comp.compare("JRA-1234567", "JRA-123"));
        assertTrue(0 > comp.compare("JR-123", "JRA-123"));
        assertTrue(0 < comp.compare("JRA-123", "JR-123"));
        assertTrue(0 > comp.compare("ABC-123", "JRA-123"));
        assertTrue(0 < comp.compare("JRA-123", "ABC-123"));
        assertTrue(0 > comp.compare("ABCDEF-123", "JRA-123"));
        assertTrue(0 < comp.compare("JRA-123", "ABCDEF-123"));
        assertTrue(0 == comp.compare(null, null));
        assertTrue(0 < comp.compare(null, "ABCDEF-123"));
        assertTrue(0 > comp.compare("JRA-123", null));
        assertTrue(0 == comp.compare("12345", "12345"));
        assertTrue(0 == comp.compare("12345", "123456"));
        assertTrue(0 < comp.compare("12345", "ABCDEF-123"));
        assertTrue(0 > comp.compare("JRA-123", "12345"));

    }

}
