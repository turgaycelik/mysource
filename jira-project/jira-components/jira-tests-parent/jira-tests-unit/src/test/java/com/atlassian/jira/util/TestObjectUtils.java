package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestObjectUtils
{

    @Test
    public void testEqualsNullSafe()
    {
        // nulls are equal
        assertTrue(ObjectUtils.equalsNullSafe(null, null));

        // null and not-null are not equal
        assertFalse(ObjectUtils.equalsNullSafe("", null));
        assertFalse(ObjectUtils.equalsNullSafe("text", null));
        assertFalse(ObjectUtils.equalsNullSafe(null, ""));
        assertFalse(ObjectUtils.equalsNullSafe(null, "text"));

        // not equal object are not equal
        assertFalse(ObjectUtils.equalsNullSafe("", "text"));
        assertFalse(ObjectUtils.equalsNullSafe(new Integer(2), "text"));
        assertFalse(ObjectUtils.equalsNullSafe(new Integer(2), "2"));

        // equal objects are equal
        assertTrue(ObjectUtils.equalsNullSafe("text", "text"));
        assertTrue(ObjectUtils.equalsNullSafe(new Integer(2), new Integer(2)));
    }
}
