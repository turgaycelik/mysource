package com.atlassian.jira.user.util;

import java.io.Serializable;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test of UserKeyStoreImpl.
 *
 * @since 6.2
 */
public class TestUserKeyStoreImpl
{
    @Test
    public void lazyCacheKeysShouldBeSerializable()
    {
        // Set up
        final Serializable key = new UserKeyStoreImpl.LazyCacheKey("foo", "bar");

        // Invoke
        final Object roundTrippedKey = deserialize(serialize(key));

        // Check
        assertEquals(key, roundTrippedKey);
        assertNotSame(key, roundTrippedKey);
    }
}
