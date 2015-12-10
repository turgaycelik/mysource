package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIssueLinkTypeImpl
{
    private IssueLinkTypeImpl issueLinkType;
    private GenericValue issueLinkTypeGV;
    private IssueLinkTypeImpl secondIssueLinkType;
    private GenericValue secondIssueLinkTypeGV;


    MockOfBizDelegator ofBizDelegator;

    @Rule
    public MockComponentContainer mockComponentContainer = new MockComponentContainer(this);


    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = new MockOfBizDelegator();
        mockComponentContainer.addMock(OfBizDelegator.class, ofBizDelegator);

        setupIssueLinkType("jira_test style");
        setupSecondIssueLinkType("custom style");
    }

    @Test
    public void testConstructor()
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("summary", "test summary"));

        try
        {
            new IssueLinkTypeImpl(issue);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Entity must be an 'IssueLinkType', not '" + issue.getEntityName() + "'.", e.getMessage());
        }
    }

    @Test
    public void testGetters()
    {
        assertEquals(issueLinkTypeGV.getLong("id"), issueLinkType.getId());
        assertEquals(issueLinkTypeGV.getString("linkname"), issueLinkType.getName());
        assertEquals(issueLinkTypeGV.getString("outward"), issueLinkType.getOutward());
        assertEquals(issueLinkTypeGV.getString("inward"), issueLinkType.getInward());
        assertEquals(issueLinkTypeGV.getString("style"), issueLinkType.getStyle());
    }

    @Test
    public void testIsSystemLinkType()
    {
        assertTrue(issueLinkType.isSystemLinkType());
        setupIssueLinkType("custom");
        assertFalse(issueLinkType.isSystemLinkType());
    }

    @Test
    public void testIsSubTaskLinkType()
    {
        assertFalse(issueLinkType.isSubTaskLinkType());
        setupIssueLinkType("jira_subtask");
        assertTrue(issueLinkType.isSubTaskLinkType());
    }

    @Test
    public void testCompareToNull()
    {
        assertEquals(1, issueLinkType.compareTo(null));
    }

    @Test
    public void testCompareToOtherNull()
    {
        secondIssueLinkTypeGV.set("linkname", null);
        assertEquals(1, issueLinkType.compareTo(secondIssueLinkType));
    }

    @Test
    public void testCompareToNameNull()
    {
        issueLinkTypeGV.set("linkname", null);
        assertEquals(-1, issueLinkType.compareTo(secondIssueLinkType));
    }

    @Test
    public void testCompareToAllNamesNull()
    {
        issueLinkTypeGV.set("linkname", null);
        secondIssueLinkTypeGV.set("linkname", null);
        assertEquals(0, issueLinkType.compareTo(secondIssueLinkType));
    }

    @Test
    public void testCompareToNoNulls()
    {
        assertEquals(issueLinkTypeGV.getString("linkname").compareTo(secondIssueLinkTypeGV.getString("linkname")), issueLinkType.compareTo(secondIssueLinkType));
    }

    private void setupIssueLinkType(String style)
    {
        issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("linkname", "test link name", "outward", "test outward", "inward", "test inward", "style", style));
        issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
    }

    private void setupSecondIssueLinkType(String style)
    {
        secondIssueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", ImmutableMap.of("linkname", "test link name", "outward", "test outward", "inward", "test inward", "style", style));
        secondIssueLinkType = new IssueLinkTypeImpl(secondIssueLinkTypeGV);
    }

}
