package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.user.UserKeyService;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @since v4.4
 */
public class TestDefaultUserFormats
{
    private UserFormatTypeConfiguration mockUserFormatTypeConfiguration;
    private UserFormatModuleDescriptors mockUserFormatModuleDescriptors;
    private UserKeyService userKeyService;

    @Before
    public void setUpMocks()
    {
        mockUserFormatTypeConfiguration = EasyMock.createMock(UserFormatTypeConfiguration.class);
        mockUserFormatModuleDescriptors = EasyMock.createMock(UserFormatModuleDescriptors.class);
    }
    @Test
    public void forTypeShouldReturnTheConfiguredUserFormatForThatTypeIfThereIsAnEntryForIt()
    {
        final UserFormatModuleDescriptor aConfiguredTypeModuleDescriptor = EasyMock.createMock(UserFormatModuleDescriptor.class);
        final UserFormat expectedUserFormatForAConfiguredType = EasyMock.createMock(UserFormat.class);
        expect(aConfiguredTypeModuleDescriptor.getModule()).andStubReturn(expectedUserFormatForAConfiguredType);

        expect(mockUserFormatTypeConfiguration.containsType("a-configured-type")).
                andStubReturn(true);
        expect(mockUserFormatTypeConfiguration.getUserFormatKeyForType("a-configured-type")).
                andStubReturn("a-configured-type-module-key");
        expect(mockUserFormatModuleDescriptors.withKey("a-configured-type-module-key")).
                andStubReturn(aConfiguredTypeModuleDescriptor);

        replay(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration, aConfiguredTypeModuleDescriptor,
                expectedUserFormatForAConfiguredType);

        final DefaultUserFormats defaultUserFormats =
                new DefaultUserFormats(mockUserFormatTypeConfiguration, mockUserFormatModuleDescriptors, userKeyService);

        final CachingUserFormat cachingUserFormat = (CachingUserFormat) defaultUserFormats.forType("a-configured-type");
        final UserFormat actualUserFormatForAConfiguredType = cachingUserFormat.getDelegate();

        assertTrue(actualUserFormatForAConfiguredType.equals(expectedUserFormatForAConfiguredType));

        verify(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration, aConfiguredTypeModuleDescriptor,
                expectedUserFormatForAConfiguredType);
    }

    @Test
    public void forTypeShouldShouldReturnNullWhenTheTypeHasNoConfiguredFormatAndThereAreNoFormatsThatCanHandleIt()
    {

        expect(mockUserFormatTypeConfiguration.containsType("a-non-configured-type")).
                andStubReturn(false);
        expect(mockUserFormatTypeConfiguration.getUserFormatKeyForType("a-non-configured-type")).
                andStubReturn(null);
        expect(mockUserFormatModuleDescriptors.defaultFor("a-non-configured-type")).
                andStubReturn(null);

        replay(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration);

        final DefaultUserFormats defaultUserFormats =
                new DefaultUserFormats(mockUserFormatTypeConfiguration, mockUserFormatModuleDescriptors, userKeyService);

        final CachingUserFormat cachingUserFormat = (CachingUserFormat) defaultUserFormats.forType("a-non-configured-type");
        final UserFormat actualUserFormatForANonConfiguredType = cachingUserFormat.getDelegate();

        assertNull(actualUserFormatForANonConfiguredType);

        verify(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration);
    }

    /**
     * When retrieving a user format for a type that has no configured format module descriptor a default should be
     * returned. A configuration entry should be set to the default format module descriptor that was found.
     */
    @Test
    public void forTypeShouldReturnTheDefaultFormatWhenThereIsNoConfigurationEntryAndThereIsAFormatThatCanHandleIt()
    {
        final UserFormatModuleDescriptor defaulModuleDescriptorForANonConfiguredType = EasyMock.createMock(UserFormatModuleDescriptor.class);
        final UserFormat defaultUserFormatForANonConfiguredType = EasyMock.createMock(UserFormat.class);
        expect(defaulModuleDescriptorForANonConfiguredType.getModule()).andStubReturn(defaultUserFormatForANonConfiguredType);
        expect(defaulModuleDescriptorForANonConfiguredType.getCompleteKey()).andStubReturn("default-module-descriptor-key");

        expect(mockUserFormatTypeConfiguration.containsType("a-non-configured-type")).
                andStubReturn(false);
        expect(mockUserFormatTypeConfiguration.getUserFormatKeyForType("a-non-configured-type")).
                andStubReturn(null);
        expect(mockUserFormatModuleDescriptors.defaultFor("a-non-configured-type")).
                andStubReturn(defaulModuleDescriptorForANonConfiguredType);

        mockUserFormatTypeConfiguration.setUserFormatKeyForType("a-non-configured-type", "default-module-descriptor-key");
        expectLastCall().once();

        replay(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration, defaulModuleDescriptorForANonConfiguredType,
                defaultUserFormatForANonConfiguredType);

        final DefaultUserFormats defaultUserFormats =
                new DefaultUserFormats(mockUserFormatTypeConfiguration, mockUserFormatModuleDescriptors, userKeyService);

        final CachingUserFormat cachingUserFormat = (CachingUserFormat) defaultUserFormats.forType("a-non-configured-type");
        final UserFormat actualUserFormatForANonConfiguredType = cachingUserFormat.getDelegate();


        assertTrue(actualUserFormatForANonConfiguredType.equals(defaultUserFormatForANonConfiguredType));

        verify(mockUserFormatModuleDescriptors, mockUserFormatTypeConfiguration, defaulModuleDescriptorForANonConfiguredType,
                defaultUserFormatForANonConfiguredType);
    }
}
