package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Date;

import com.atlassian.crowd.model.group.GroupType;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test of OfBizGroup.
 */
public class TestOfBizGroup
{
    @Test
    public void instancesShouldBeSerializableForReplicationBetweenClusteredCaches()
    {
        // Set up
        final OfBizGroup before =
                new OfBizGroup(1, 2, "foo", true, true, new Date(3), new Date(4), GroupType.values()[0], "bar");

        // Invoke
        final OfBizGroup after = (OfBizGroup) SerializationUtils.deserialize(SerializationUtils.serialize(before));

        // Check
        assertEquals(before, after);
        assertNotSame(before, after);
    }
}
