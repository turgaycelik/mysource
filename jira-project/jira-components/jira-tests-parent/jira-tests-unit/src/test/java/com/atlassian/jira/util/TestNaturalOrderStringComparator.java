package com.atlassian.jira.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link TestNaturalOrderStringComparator}.
 *
 * @since v5.0
 */
public class TestNaturalOrderStringComparator
{
    @Test
    public void testJiraIssueKeyOrder()
    {
        Comparator<String> comp = NaturalOrderStringComparator.CASE_SENSITIVE_ORDER;
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
    }

    @Test
    public void testAlphaNumOrder()
    {
        List<String> expected = Arrays.asList("", "0", "1-2", "1-02", "1-20", "10-20",
                                              "fred", "jane", "pic01", "pic2", "pic02", "pic02a",
                                              "pic3", "pic4", "pic 4 else", "pic 5",
                                              "pic 5 something", "pic 06", "pic   7",
                                              "pic100", "pic100a", "pic120", "pic121",
                                              "pic02000", "tom", "x2-g8", "x2-y7", "x2-y08",
                                              "x8-y8", "xmen 000", "xmen 001");
        List<String> actual = Arrays.asList("xmen 001", "pic02a", "1-02", "x2-g8",
                                            "pic2", "pic02", "jane", "pic   7",
                                            "pic120", "pic 5 something", "fred", "xmen 000",
                                            "x2-y08", "pic3", "pic4", "1-2", "x8-y8",
                                            "10-20", "pic121", "pic01", "pic 5", "pic 06",
                                            "pic 4 else", "pic100", "1-20", "pic02000", "x2-y7",
                                            "pic100a", "tom", "0", "");
        Collections.sort(actual, NaturalOrderStringComparator.CASE_SENSITIVE_ORDER);
        assertEquals(expected, actual);
    }

    @Test
    public void testUnicodeOrder()
    {
        String monkeys = new StringBuilder().appendCodePoint(0x1F648).appendCodePoint(0x1F649).appendCodePoint(0x1F64A).toString();
        String cats = new StringBuilder().appendCodePoint(0x1F638).appendCodePoint(0x1F63A).toString();
        List<String> expected = Arrays.asList(monkeys+"0", monkeys+"1", monkeys+"02", monkeys+"  03",
                                              monkeys+" 4", monkeys+"  4 "+cats, monkeys+" 9",
                                              monkeys+" 10");
        List<String> actual = Arrays.asList(monkeys+"1", monkeys+"  03",
                                            monkeys+"  4 "+cats, monkeys+"0", monkeys+" 9",
                                            monkeys+" 10", monkeys+" 4", monkeys+"02");
        Collections.sort(actual, NaturalOrderStringComparator.CASE_SENSITIVE_ORDER);
        assertEquals(expected, actual);
    }

    @Test
    public void testCaseSensitiveOrdering()
    {
        List<String> expected = Arrays.asList("12", "APPLE", "Zebra", "mouse");
        List<String> actual = Arrays.asList("mouse", "APPLE", "12", "Zebra");
        Collections.sort(actual, NaturalOrderStringComparator.CASE_SENSITIVE_ORDER);
        assertEquals(expected, actual);
    }

    @Test
    public void testCaseInsensitiveOrdering()
    {
        List<String> expected = Arrays.asList("12", "APPLE", "mouse", "Zebra");
        List<String> actual = Arrays.asList("mouse", "APPLE", "12", "Zebra");
        Collections.sort(actual, NaturalOrderStringComparator.CASE_INSENSITIVE_ORDER);
        assertEquals(expected, actual);
    }
}
