package com.atlassian.jira.issue.comparator;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIssueKeyComparator extends AbstractComparatorTestCase
{
    @Test
    public void testCompareGenericValues()
    {
        MockGenericValue k1 = new MockGenericValue("issue");
        k1.set("key", "TST-1");
        MockGenericValue k2 = new MockGenericValue("issue");
        k2.set("key", "TST-2");
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1.set("key", "TST-10");
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2.set("key", "TST-10");
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testCompareIssues()
    {
        MockIssue k1 = new MockIssue();
        k1.setKey("TST-1");
        MockIssue k2 = new MockIssue();
        k2.setKey("TST-2");
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1.setKey("TST-10");
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2.setKey("TST-10");
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testCompareIssueToGenericValue()
    {
        MockIssue k1 = new MockIssue();
        k1.setKey("TST-1");
        MockGenericValue k2 = new MockGenericValue("issue");
        k2.set("key", "TST-2");
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1.setKey("TST-10");
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2.set("key", "TST-10");
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testCompareIssueToString()
    {
        MockIssue k1 = new MockIssue();
        k1.setKey("TST-1");
        String k2 = "TST-2";
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1.setKey("TST-10");
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2 = "TST-10";
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testCompareStringToIssue()
    {
        String k1 = "TST-1";
        MockIssue k2 = new MockIssue();
        k2.setKey("TST-2");
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1 = "TST-10";
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2.setKey("TST-10");
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testCompareStringToString()
    {
        String k1 = "TST-1";
        String k2 = "TST-2";
        assertLessThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k1 = "TST-10";
        assertGreaterThan(IssueKeyComparator.COMPARATOR, k1, k2);
        k2 = "TST-10";
        assertEqualTo(IssueKeyComparator.COMPARATOR, k1, k2);
    }

    @Test
    public void testLHSWrongType()
    {
        assertEqualTo(IssueKeyComparator.COMPARATOR, "", "");
        try
        {
            assertEqualTo(IssueKeyComparator.COMPARATOR, new Integer(1), "");
            fail("Exception expected");
        }
        catch (ClassCastException expected)
        {
            // this is expected
        }
    }

    @Test
    public void testRHSWrongType()
    {
        assertEqualTo(IssueKeyComparator.COMPARATOR, "", "");
        try
        {
            assertEqualTo(IssueKeyComparator.COMPARATOR, "", new Integer(1));
            fail("Exception expected");
        }
        catch (ClassCastException expected)
        {
            // this is expected
        }
    }
    @Test
    public void testEquals()
    {
        IssueKeyComparator comp = new IssueKeyComparator();

        GenericValue gv = new MockGenericValue("Issue", EasyMap.build("key", "JRA-52"));
        GenericValue gv2 = new MockGenericValue("Issue", EasyMap.build("key", "JRA-53"));
        assertTrue(comp.compare(gv, gv2) < 0);
        assertTrue(comp.compare(gv2, gv) > 0);

        gv2 = new MockGenericValue("Issue", EasyMap.build("key", "ABC-53"));
        assertTrue(comp.compare(gv, gv2) > 0);
        assertTrue(comp.compare(gv2, gv) < 0);

        gv2 = new MockGenericValue("Issue", EasyMap.build("key", "JRA-52"));
        assertTrue(0 == comp.compare(gv, gv2));
        assertTrue(0 == comp.compare(gv2, gv));

        gv2 = new MockGenericValue("Issue", EasyMap.build("key", null));
        assertTrue(comp.compare(gv, gv2) < 0);
        assertTrue(comp.compare(gv2, gv) > 0);

        gv = new MockGenericValue("Issue", EasyMap.build("key", "OM-406"));
        gv2 = new MockGenericValue("Issue", EasyMap.build("key", "OM-443"));
        assertTrue(comp.compare(gv, gv2) < 0);
        assertTrue(comp.compare(gv2, gv) > 0);

    }
    
}
