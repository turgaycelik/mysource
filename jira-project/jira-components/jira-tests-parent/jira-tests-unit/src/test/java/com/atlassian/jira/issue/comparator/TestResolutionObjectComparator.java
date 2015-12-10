package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.resolution.Resolution;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestResolutionObjectComparator
{
    @Test
    public void testSimpleComparison() throws Exception
    {
        final MockControl mockResolutionControl1 = MockControl.createStrictControl(Resolution.class);
        final Resolution mockResolution1 = (Resolution) mockResolutionControl1.getMock();
        mockResolution1.getSequence();
        mockResolutionControl1.setReturnValue(new Long(1));
        mockResolutionControl1.replay();

        final MockControl mockResolutionControl2 = MockControl.createStrictControl(Resolution.class);
        final Resolution mockResolution2 = (Resolution) mockResolutionControl2.getMock();
        mockResolution2.getSequence();
        mockResolutionControl2.setReturnValue(new Long(3));
        mockResolutionControl2.replay();

        assertEquals(-1, ResolutionObjectComparator.RESOLUTION_OBJECT_COMPARATOR.compare(mockResolution1, mockResolution2));

        mockResolutionControl1.verify();
        mockResolutionControl2.verify();
    }
}
