package com.atlassian.jira.crowd.embedded.ofbiz;

import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestOfBizUser
{
    @Test
    public void usersShouldHaveABlankEmailAddressWhenBuiltFromAGenericValueWithANullAddress()
    {
        final GenericValue userAsGenericValue = mock(GenericValue.class);
        when(userAsGenericValue.getString(UserEntity.EMAIL_ADDRESS)).thenReturn(null);

        final OfBizUser userObject = OfBizUser.from(userAsGenericValue);

        assertTrue
                (
                        "The email address on the user object should be set to the empty string if the email address "
                                + "value stored in the generic value is 'null'.", isEmpty(userObject.getEmailAddress())
                );
    }

    @Test
    public void usersEmailAddressShouldBePopulatedFromTheAddressInTheGenericValueIfItIsNotNull()
    {
        final GenericValue userAsGenericValue = mock(GenericValue.class);
        when(userAsGenericValue.getString(UserEntity.EMAIL_ADDRESS)).thenReturn("test-user@example.com");

        final OfBizUser userObject = OfBizUser.from(userAsGenericValue);

        assertTrue
                (
                        "The email address on the user object should be set to the email address value stored in the "
                                + "generic value when it is not 'null'.",
                        userObject.getEmailAddress().equals("test-user@example.com")
                );
    }

    @Test
    public void instancesShouldBeSerializableInOrderToBeReplicatedAsCacheValues()
    {
        // Set up
        final GenericValue userAsGenericValue = mock(GenericValue.class);
        when(userAsGenericValue.getString(UserEntity.USER_NAME)).thenReturn("test");
        final OfBizUser user = OfBizUser.from(userAsGenericValue);

        // Invoke
        final Object deserializedUser = deserialize(serialize(user));

        // Check
        assertEquals(user, deserializedUser);
        assertNotSame(user, deserializedUser);
    }
}
