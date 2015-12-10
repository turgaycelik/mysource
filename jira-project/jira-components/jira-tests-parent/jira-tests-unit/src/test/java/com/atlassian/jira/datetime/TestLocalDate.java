package com.atlassian.jira.datetime;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 *
 * @since v4.4
 */
public class TestLocalDate
{
    @Test
    public void testEqual() throws Exception
    {
        LocalDate localDate1 = new LocalDate(1989, 11, 3);
        LocalDate localDate2 = new LocalDate(1989, 11, 3);
        assertTrue(localDate1.equals(localDate2));
    }

    @Test
    public void testNotEqual() throws Exception
    {
        LocalDate localDate1 = new LocalDate(1979, 11, 3);
        LocalDate localDate2 = new LocalDate(1989, 11, 4);
        assertFalse(localDate1.equals(localDate2));
    }

    @Test
    public void testCompareLess() throws Exception
    {
        LocalDate localDate1 = new LocalDate(1989, 11, 3);
        LocalDate localDate2 = new LocalDate(1989, 11, 4);
        assertEquals(-1, localDate1.compareTo(localDate2));
    }

    @Test
    public void testCompareGreater() throws Exception
    {
        LocalDate localDate1 = new LocalDate(1989, 11, 5);
        LocalDate localDate2 = new LocalDate(1989, 11, 4);
        assertEquals(1, localDate1.compareTo(localDate2));
    }

}
