package com.atlassian.jira.util.map;

import java.io.Serializable;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for CacheObject.
 *
 * @since 6.2
 */
public class TestCacheObject
{
    private static void assertSerializable(final Serializable object)
    {
        // Invoke
        final Object roundTrippedObject = deserialize(serialize(object));

        // Check
        assertEquals(object, roundTrippedObject);
    }

    @Test
    public void wrappedNullShouldBeSerializableForReplicatingBetweenClusteredCaches()
    {
        assertSerializable(CacheObject.NULL());
    }

    @Test
    public void wrappedObjectShouldBeSerializableForReplicatingBetweenClusteredCaches()
    {
        assertSerializable(CacheObject.wrap("Foo"));
    }
}
