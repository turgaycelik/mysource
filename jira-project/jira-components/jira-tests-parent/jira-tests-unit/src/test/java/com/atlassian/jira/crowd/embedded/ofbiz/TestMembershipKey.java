package com.atlassian.jira.crowd.embedded.ofbiz;

import java.io.Serializable;

import com.atlassian.crowd.model.membership.MembershipType;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Unit test of MembershipKey.
 *
 * @since 6.2
 */
public class TestMembershipKey
{
    @Test
    public void instancesShouldBeSerializable()
    {
        // Set up
        final Serializable key = MembershipKey.getKey(1, "Foo", MembershipType.values()[0]);

        // Invoke
        final Object roundTrippedKey = deserialize(serialize(key));

        // Check
        assertEquals(key, roundTrippedKey);
        assertNotSame(key, roundTrippedKey);
    }
}
