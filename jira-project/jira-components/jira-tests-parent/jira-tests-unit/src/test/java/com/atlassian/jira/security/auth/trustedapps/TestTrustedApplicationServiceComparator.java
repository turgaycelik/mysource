package com.atlassian.jira.security.auth.trustedapps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTrustedApplicationServiceComparator
{
    MockTrustedApplicationInfo info1 = new MockTrustedApplicationInfo(1, "first", "First Application", 1000);
    MockTrustedApplicationInfo info2 = new MockTrustedApplicationInfo(1, "first", "Second Application", 1000);

    @Test
    public void testFirstBeforeSecond() throws Exception
    {
        assertTrue(TrustedApplicationService.NAME_COMPARATOR.compare(info1, info2) < 0);
    }

    @Test
    public void testFirstAfterSecond() throws Exception
    {
        assertTrue(TrustedApplicationService.NAME_COMPARATOR.compare(info2, info1) > 0);
    }

    @Test
    public void testFirstEqualsFirst() throws Exception
    {
        assertEquals(0, TrustedApplicationService.NAME_COMPARATOR.compare(info1, info1));
    }

    @Test
    public void testSecondEqualsSecond() throws Exception
    {
        assertEquals(0, TrustedApplicationService.NAME_COMPARATOR.compare(info2, info2));
    }

    @Test
    public void testNameEquivalence() throws Exception
    {
        MockTrustedApplicationInfo info = new MockTrustedApplicationInfo(2, "second", "First Application", 5000);
        assertEquals(0, TrustedApplicationService.NAME_COMPARATOR.compare(info1, info));
    }
}
