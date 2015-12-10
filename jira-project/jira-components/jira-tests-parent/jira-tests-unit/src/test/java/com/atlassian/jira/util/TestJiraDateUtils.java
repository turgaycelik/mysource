package com.atlassian.jira.util;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

public class TestJiraDateUtils
{

    @Test
    public void testCopyDateNullsafe() {

        // test that null parameter returns null
        assertNull(JiraDateUtils.copyDateNullsafe(null));

        // test that passing date returns a copy od that date
        Date originalDate = new Date();
        Date copyDate = JiraDateUtils.copyDateNullsafe(originalDate);
        assertNotNull(copyDate);
        assertEquals(originalDate, copyDate);
        assertNotSame(originalDate, copyDate);
    }

    @Test
    public void testCopyOrCreateDateNullsafe() {

        // test that null parameter returns null
        assertNotNull(JiraDateUtils.copyOrCreateDateNullsafe(null));

        // test that passing date returns a copy od that date
        Date originalDate = new Date();
        Date copyDate = JiraDateUtils.copyOrCreateDateNullsafe(originalDate);
        assertNotNull(copyDate);
        assertEquals(originalDate, copyDate);
        assertNotSame(originalDate, copyDate);
    }

}
