package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestDashboardUtil
{
    @Test
    public void testToLong()
    {
        assertNull(DashboardUtil.toLong((DashboardId) null));
        assertNull(DashboardUtil.toLong((GadgetId) null));

        assertEquals(Long.valueOf(100), DashboardUtil.toLong(DashboardId.valueOf("100")));
        assertEquals(Long.valueOf(10220), DashboardUtil.toLong(GadgetId.valueOf("10220")));

        try
        {
            DashboardUtil.toLong(DashboardId.valueOf("abc"));
            fail("Should have barfed");
        }
        catch (Exception e)
        {
            //yay
        }

        try
        {
            DashboardUtil.toLong(GadgetId.valueOf("abc"));
            fail("Should have barfed");
        }
        catch (Exception e)
        {
            //yay
        }
    }

    @Test
    public void testGetMaxGadgets()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        expect(mockApplicationProperties.getDefaultBackedString("jira.dashboard.max.gadgets")).andReturn("25");

        replay(mockApplicationProperties);
        final int max = DashboardUtil.getMaxGadgets(mockApplicationProperties);
        assertEquals(25, max);
        verify(mockApplicationProperties);
    }

    @Test
    public void testGetMaxGadgetsInvalid()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        expect(mockApplicationProperties.getDefaultBackedString("jira.dashboard.max.gadgets")).andReturn("asdfasdf");

        replay(mockApplicationProperties);
        final int max = DashboardUtil.getMaxGadgets(mockApplicationProperties);
        assertEquals(20, max);
        verify(mockApplicationProperties);
    }

    @Test
    public void testGetMaxGadgetsNotSet()
    {
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        expect(mockApplicationProperties.getDefaultBackedString("jira.dashboard.max.gadgets")).andReturn(null);

        replay(mockApplicationProperties);
        final int max = DashboardUtil.getMaxGadgets(mockApplicationProperties);
        assertEquals(20, max);
        verify(mockApplicationProperties);
    }
}
