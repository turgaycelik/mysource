package com.atlassian.jira.util;

import java.util.Locale;

import com.atlassian.jira.issue.fields.FieldManager;

import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertEquals;


public class TestJiraVelocityUtils
{
    private static final String lSep = System.getProperty("line.separator");
    private FieldManager mockFieldManager;
    private MockControl ctrlFieldManager;

    JiraVelocityHelper helper;

    @Before
    public void setUp() throws Exception
    {
        ctrlFieldManager = MockControl.createControl(FieldManager.class);
        mockFieldManager = (FieldManager) ctrlFieldManager.getMock();
        ctrlFieldManager.replay();

        helper = new JiraVelocityHelper(mockFieldManager); // stateless
    }

    @Test
    public void testQuote()
    {
        assertEquals("", helper.quote(""));
        assertEquals("> foo", helper.quote("foo"));
        assertEquals("> foo"+lSep+"> bar", helper.quote("foo\nbar"));
        assertEquals("> foo"+lSep+"> bar"+lSep+"> baz", helper.quote("foo\nbar\nbaz"));
        assertEquals("> foo bar", helper.quote("foo bar"));
    }

    @Test
    public void testPrintChangelog() throws GenericEntityException
    {
        // Write me!
    }

    @Test
    public void testFirstDayOfWeekFrench()
    {
        assertEquals(2, new JiraVelocityUtils.LazyCalendar(Locale.FRANCE, null).getFirstDayOfWeek());
    }

    @Test
    public void testFirstDayOfWeekYank()
    {
        assertEquals(1, new JiraVelocityUtils.LazyCalendar(Locale.US, null).getFirstDayOfWeek());
    }

    @Test
    public void testFirstDayOfWeekOz()
    {
        assertEquals(1, new JiraVelocityUtils.LazyCalendar(new Locale("en", "AU"), null).getFirstDayOfWeek());
    }
}
