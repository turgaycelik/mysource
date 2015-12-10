package com.atlassian.jira.plugin.userformat.descriptors;

import java.util.Collections;

import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Holds unit tests for {@link UserFormatTypes}
 *
 * @since v4.4
 */
public class TestDefaultUserFormatTypes
{
    private UserFormatModuleDescriptors mockUserFormatModuleDescriptors;

    @Before
    public void setUpMocks()
    {
        mockUserFormatModuleDescriptors = createMock(UserFormatModuleDescriptors.class);
    }

    @Test
    public void shouldReturnASingleTypeWhenThereIsOneEnabledUserFormatModuleDescriptor()
    {
        final UserFormatModuleDescriptor mockUserFormatModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        EasyMock.expect(mockUserFormatModuleDescriptor.getType()).andStubReturn("profileLink");

        EasyMock.expect(mockUserFormatModuleDescriptors.get()).
                andStubReturn(ImmutableList.of(mockUserFormatModuleDescriptor));

        DefaultUserFormatTypes defaultUserFormatTypes = new DefaultUserFormatTypes(mockUserFormatModuleDescriptors);

        replay(mockUserFormatModuleDescriptors, mockUserFormatModuleDescriptor);

        final Iterable<String> types = defaultUserFormatTypes.get();
        assertEquals(1, Iterables.size(types));
        assertTrue(Iterables.contains(types, "profileLink"));

        EasyMock.verify(mockUserFormatModuleDescriptors, mockUserFormatModuleDescriptor);
    }

    @Test
    public void shouldReturnASingleTypeWhenThereIsMoreThanOneEnabledUserFormatModuleDescriptorForThatType()
    {
        final UserFormatModuleDescriptor userFormatModuleDescriptorForTypeProfileLink1 = createMock(UserFormatModuleDescriptor.class);
        expect(userFormatModuleDescriptorForTypeProfileLink1.getType()).andStubReturn("profileLink");

        final UserFormatModuleDescriptor userFormatModuleDescriptorForTypeProfileLink2 = createMock(UserFormatModuleDescriptor.class);
        expect(userFormatModuleDescriptorForTypeProfileLink2.getType()).andStubReturn("profileLink");

        expect(mockUserFormatModuleDescriptors.get()).andStubReturn
                (
                        ImmutableList.of
                                (
                                        userFormatModuleDescriptorForTypeProfileLink1,
                                        userFormatModuleDescriptorForTypeProfileLink2
                                )
                );

        DefaultUserFormatTypes defaultUserFormatTypes = new DefaultUserFormatTypes(mockUserFormatModuleDescriptors);

        replay(mockUserFormatModuleDescriptors, userFormatModuleDescriptorForTypeProfileLink1, userFormatModuleDescriptorForTypeProfileLink2);

        final Iterable<String> types = defaultUserFormatTypes.get();
        assertEquals(1, Iterables.size(types));
        assertTrue(Iterables.contains(types, "profileLink"));

        EasyMock.verify(mockUserFormatModuleDescriptors, userFormatModuleDescriptorForTypeProfileLink1);
    }

    @Test
    public void shouldReturnAnEmptyIterableWhenThereAreNoModuleDescriptorsForAnyType()
    {
        expect(mockUserFormatModuleDescriptors.get()).andStubReturn(Collections.<UserFormatModuleDescriptor>emptyList());

        DefaultUserFormatTypes defaultUserFormatTypes = new DefaultUserFormatTypes(mockUserFormatModuleDescriptors);

        replay(mockUserFormatModuleDescriptors);

        final Iterable<String> types = defaultUserFormatTypes.get();
        assertNotNull(types);
        assertEquals(0, Iterables.size(types));
    }
}
