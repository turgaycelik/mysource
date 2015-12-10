package com.atlassian.jira.issue.comparator;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestCustomFieldComparator
{
    private CustomFieldComparator comparator;
    private GenericValue customField1;
    private GenericValue customField2;
    private GenericValue customField3;

    @Before
    public void setUp() throws Exception
    {
        comparator = new CustomFieldComparator();
        customField1 = new MockGenericValue("CustomField", EasyMap.build("name", "ABC"));
        customField2 = new MockGenericValue("CustomField", EasyMap.build("name", "BBC"));
        customField3 = new MockGenericValue("CustomField", EasyMap.build("name", "CBC"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void otherGenericValueShouldNotBeComparable()
    {
        comparator.compare(customField1, new MockGenericValue("OtherType"));
    }

    @Test
    public void nullValuesShouldBeEqual()
    {
        assertEquals(0, comparator.compare(null, null));
    }

    @Test
    public void nullValueShouldBeLessThanNonNull()
    {
        assertComparison(null, customField1);
    }

    @Test
    public void globalCustomFieldShouldBeLessThanProjectCustomField()
    {
        assertComparison(customField1, customField2);
    }

    @Test
    public void globalCustomFieldShouldBeLessThanIssueTypeCustomField()
    {
        assertComparison(customField1, customField3);
    }

    @Test
    public void projectCustomFieldShouldBeLessThanIssueTypeCustomField()
    {
        assertComparison(customField2, customField3);
    }

    private void assertComparison(final GenericValue lesser, final GenericValue greater)
    {
        assertLessThan(lesser, greater);
        assertGreaterThan(greater, lesser);
    }

    private void assertGreaterThan(final GenericValue greater, final GenericValue lesser)
    {
        final int result = comparator.compare(greater, lesser);
        assertTrue("Actual result = " + result, result > 0);
    }

    private void assertLessThan(final GenericValue lesser, final GenericValue greater)
    {
        final int result = comparator.compare(lesser, greater);
        assertTrue("Actual result = " + result, result < 0);
    }
}
