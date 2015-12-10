package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test of AttributeKey.
 *
 * @since 6.2
 */
public class TestAttributeKey
{
    @Test
    public void instancesShouldBeSerializable()
    {
        // Set up
        final Serializable key = new AttributeKey(1, 2);

        // Invoke
        final Object roundTrippedKey = deserialize(serialize(key));

        // Check
        assertEquals(key, roundTrippedKey);
        assertNotSame(key, roundTrippedKey);
    }
}
