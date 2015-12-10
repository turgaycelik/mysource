package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalGroup;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class TestData
{
    public static final long DIRECTORY_ID = 123L;

    public static class User
    {
        public static final String NAME = "AUserName";
        public static final boolean ACTIVE = true;
        public static final PasswordCredential CREDENTIAL = PasswordCredential.encrypted("some-password");
        public static final String DISPLAY_NAME = "TestDisplayName";
        public static final String EMAIL = "test@example.com";
        public static final String FIRST_NAME = "TestFirstName";
        public static final String LAST_NAME = "TestLastName";

        public static com.atlassian.crowd.model.user.User getTestData()
        {
            return getUser(NAME, DIRECTORY_ID, ACTIVE, FIRST_NAME, LAST_NAME, DISPLAY_NAME, EMAIL);
        }

        public static com.atlassian.crowd.model.user.User getTestData(String externalId)
        {
            return getUser(NAME, DIRECTORY_ID, ACTIVE, FIRST_NAME, LAST_NAME, DISPLAY_NAME, EMAIL, externalId);
        }

        public static com.atlassian.crowd.model.user.User getTestData2(Long directoryId)
        {
            return getUser("Bethany", directoryId, ACTIVE, "Bethany", "Brown", "Bethany Brown", "bbrown@bethany.test");
        }

        public static com.atlassian.crowd.model.user.User getTestData(Long directoryId)
        {
            return getUser(NAME, directoryId, ACTIVE, FIRST_NAME, LAST_NAME, DISPLAY_NAME, EMAIL);
        }

        public static com.atlassian.crowd.model.user.User getTestData(Long directoryId, String externalId)
        {
            return getUser(NAME, directoryId, ACTIVE, FIRST_NAME, LAST_NAME, DISPLAY_NAME, EMAIL, externalId);
        }

        public static com.atlassian.crowd.model.user.User getUser(String userName, Long directoryId, boolean active, String firstName, String lastName, String displayName, String email)
        {
            return getUser(userName, directoryId, active, firstName, lastName, displayName, email, null);
        }

        public static com.atlassian.crowd.model.user.User getUser(String userName, Long directoryId, boolean active, String firstName, String lastName, String displayName, String email, String externalId)
        {
            final com.atlassian.crowd.model.user.User user = mock(com.atlassian.crowd.model.user.User.class);
            when(user.getName()).thenReturn(userName);
            when(user.getDirectoryId()).thenReturn(directoryId);
            when(user.isActive()).thenReturn(active);
            when(user.getFirstName()).thenReturn(firstName);
            when(user.getLastName()).thenReturn(lastName);
            when(user.getDisplayName()).thenReturn(displayName);
            when(user.getEmailAddress()).thenReturn(email);
            when(user.getExternalId()).thenReturn(externalId);
            return user;
        }

        public static void assertEqualsTestUser(com.atlassian.crowd.model.user.User user)
        {
            assertEquals(NAME, user.getName());
            assertEquals(ACTIVE, user.isActive());
            assertEquals(FIRST_NAME, user.getFirstName());
            assertEquals(LAST_NAME, user.getLastName());
            assertEquals(DISPLAY_NAME, user.getDisplayName());
            assertEquals(EMAIL, user.getEmailAddress());
        }
    }

    public static class Group
    {
        public static final String NAME = "test-group";
        public static final GroupType TYPE = GroupType.GROUP;
        public static final String DESCRIPTION = "test description";
        public static final boolean ACTIVE = true;

        public static void assertEqualsTestGroup(com.atlassian.crowd.model.group.Group group)
        {
            assertEqualsTestGroup(getTestData().getDirectoryId(), group);
        }

        public static void assertEqualsTestGroup(long directoryId, com.atlassian.crowd.model.group.Group group)
        {
            assertEquals(getTestData().getName(), group.getName());
            assertEquals(directoryId, group.getDirectoryId());
            assertEquals(getTestData().getDescription(), group.getDescription());
            assertEquals(getTestData().getType(), group.getType());
            assertEquals(getTestData().isActive(), group.isActive());
        }

        public static com.atlassian.crowd.model.group.Group getTestData()
        {
            return getGroup(NAME, DIRECTORY_ID, ACTIVE, DESCRIPTION, TYPE);
        }

        public static com.atlassian.crowd.model.group.Group getTestData(Long directoryId)
        {
            return getGroup(NAME, directoryId, ACTIVE, DESCRIPTION, TYPE);
        }

        public static com.atlassian.crowd.model.group.Group getGroup(String groupName, Long directoryId, boolean active, String description, GroupType type)
        {
            final com.atlassian.crowd.model.group.Group group = mock(com.atlassian.crowd.model.group.Group.class);
            when(group.getName()).thenReturn(groupName);
            when(group.getDirectoryId()).thenReturn(directoryId);
            when(group.getDescription()).thenReturn(description);
            when(group.getType()).thenReturn(type);
            when(group.isActive()).thenReturn(active);

            return group;
        }

        public static com.atlassian.crowd.model.group.Group getUnmockedTestData(com.atlassian.crowd.embedded.api.Directory directory)
        {
            return new InternalGroup(getTestData(directory.getId()), directory);
        }

        public static com.atlassian.crowd.model.group.Group getTestData(Long directoryId, String groupName)
        {
            return getGroup(groupName, directoryId, true, DESCRIPTION, TYPE);
        }
    }

    public static class Attributes
    {
        public static final String ATTRIBUTE1 = "attribute1";
        public static final String ATTRIBUTE2 = "attribute2";
        public static final String ATTRIBUTE3 = "attribute3";
        public static final String VALUE_11 = "value11";
        public static final String VALUE_21 = "value21";
        public static final String VALUE_22 = "value22";

        public static Map<String, Set<String>> getTestData()
        {
            final Map<String, Set<String>> attributes = Maps.newHashMap();
            attributes.put(ATTRIBUTE1, Sets.newHashSet(VALUE_11));
            attributes.put(ATTRIBUTE2, Sets.newHashSet(VALUE_21, VALUE_22));
            attributes.put(ATTRIBUTE3, Sets.<String>newHashSet());
            return ImmutableMap.copyOf(attributes);
        }

        public static void assertEqualsTestData(com.atlassian.crowd.embedded.api.Attributes attributes)
        {
            final String attribute4 = "attribute4";

            assertNotNull(attributes.getValue(ATTRIBUTE1));
            assertNotNull(attributes.getValues(ATTRIBUTE2));
            assertNull(attributes.getValue(ATTRIBUTE3));
            assertNull(attributes.getValue(attribute4));

            assertEquals(VALUE_11, attributes.getValue(ATTRIBUTE1));
            assertEquals(Sets.newHashSet(VALUE_21, VALUE_22), attributes.getValues(ATTRIBUTE2));
            assertEquals(null, attributes.getValue(ATTRIBUTE3));
            assertEquals(null, attributes.getValue(attribute4));

            assertEquals(1, attributes.getValues(ATTRIBUTE1).size());
            assertTrue(attributes.getValues(ATTRIBUTE1).contains(VALUE_11));

            assertEquals(2, attributes.getValues(ATTRIBUTE2).size());
            assertTrue(attributes.getValues(ATTRIBUTE2).contains(VALUE_21));
            assertTrue(attributes.getValues(ATTRIBUTE2).contains(VALUE_22));

            assertEquals(null, attributes.getValues(ATTRIBUTE3));
            assertEquals(null, attributes.getValues(attribute4));
        }
    }

    public static class Directory
    {
        public static final String NAME = "Test Directory";
        public static final boolean ACTIVE = true;
        public static final DirectoryType TYPE = DirectoryType.INTERNAL;
        private static final String IMPL_NAME = InternalDirectory.class.getCanonicalName();
        private static final HashSet<OperationType> OPERATIONS = Sets.newHashSet(OperationType.CREATE_GROUP, OperationType.CREATE_USER);
        private static final String DESCRIPTION = "test description";
        // Moved away from immutable map due to change in EMBCWD-614
        private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>()
        {
            {
                put(Attributes.ATTRIBUTE1, Attributes.VALUE_11);
                put(Attributes.ATTRIBUTE2, Attributes.VALUE_21);
            }
        };

        public static com.atlassian.crowd.model.directory.DirectoryImpl getTestData()
        {
            return getDirectory(NAME, ACTIVE, TYPE, IMPL_NAME, DESCRIPTION, OPERATIONS, ATTRIBUTES);
        }

        public static com.atlassian.crowd.model.directory.DirectoryImpl getUnmockedTestData()
        {
            return getUnmockedTestData(NAME);
        }

        public static com.atlassian.crowd.model.directory.DirectoryImpl getDirectory(String name, boolean active, DirectoryType type, String implClass, String description, Set<OperationType> operations, Map<String, String> attributes)
        {
            final com.atlassian.crowd.model.directory.DirectoryImpl directory = mock(com.atlassian.crowd.model.directory.DirectoryImpl.class);
            when(directory.getName()).thenReturn(name);
//            when(directory.getLowerName()).thenReturn(name.toLowerCase());
            when(directory.isActive()).thenReturn(active);
            when(directory.getType()).thenReturn(type);
            when(directory.getImplementationClass()).thenReturn(implClass);
//            when(directory.getLowerImplementationClass()).thenReturn(implClass.toLowerCase());
            when(directory.getDescription()).thenReturn(description);
            when(directory.getAttributes()).thenReturn(attributes);
            when(directory.getAllowedOperations()).thenReturn(operations);

            return directory;
        }


        public static void assertEqualsTestDirectory(com.atlassian.crowd.embedded.api.Directory directory)
        {
            assertEquals(NAME, directory.getName());
//            assertEquals(NAME.toLowerCase(), directory.getLowerName());
            assertEquals(ACTIVE, directory.isActive());
            assertEquals(TYPE, directory.getType());
            assertEquals(IMPL_NAME, directory.getImplementationClass());
//            assertEquals(IMPL_NAME.toLowerCase(), directory.getLowerImplementationClass());
            assertEquals(DESCRIPTION, directory.getDescription());

            assertEquals(ATTRIBUTES.size(), directory.getAttributes().size());

            assertNotNull(directory.getValue(Attributes.ATTRIBUTE1));
            assertNotNull(directory.getValues(Attributes.ATTRIBUTE1).contains(Attributes.VALUE_11));

            assertNotNull(directory.getValue(Attributes.ATTRIBUTE2));
            assertTrue(directory.getValues(Attributes.ATTRIBUTE2).contains(Attributes.VALUE_21));

            assertEquals(OPERATIONS.size(), directory.getAllowedOperations().size());
            assertTrue(OPERATIONS.containsAll(directory.getAllowedOperations()));

        }

        public static com.atlassian.crowd.model.directory.DirectoryImpl getUnmockedTestData(String directoryName)
        {
            com.atlassian.crowd.model.directory.DirectoryImpl directory = new com.atlassian.crowd.model.directory.DirectoryImpl();
            directory.setName(directoryName);
            directory.setActive(ACTIVE);
            directory.setType(TYPE);
            directory.setImplementationClass(IMPL_NAME);
            directory.setDescription(DESCRIPTION);
            directory.setAttributes(ATTRIBUTES);
            directory.setAllowedOperations(OPERATIONS);
            return directory;
        }
    }

    public static class Application
    {
        public static final String NAME = "Test Application";
        public static final boolean ACTIVE = true;
        public static final String DESCRIPTION = "test description";
        public static final ApplicationType TYPE = ApplicationType.JIRA;
        public static final HashSet<RemoteAddress> ADDRESSES = Sets.newHashSet(new RemoteAddress("10.10.10.10"), new RemoteAddress("10.10.10.11"));
        public static final PasswordCredential CREDENTIAL = new PasswordCredential("secret", true);


        public static com.atlassian.crowd.model.application.ApplicationImpl getTestData()
        {
            return getApplication(NAME, ACTIVE, DESCRIPTION, TYPE, CREDENTIAL, ADDRESSES);
        }

        public static com.atlassian.crowd.model.application.ApplicationImpl getUnmockedTestData()
        {
            return getUnmockedTestData(NAME);
        }

        public static com.atlassian.crowd.model.application.ApplicationImpl getApplication(String name, boolean active, String description, ApplicationType type, PasswordCredential credential, Set<RemoteAddress> remoteAddresses)
        {
            final com.atlassian.crowd.model.application.ApplicationImpl application = mock(com.atlassian.crowd.model.application.ApplicationImpl.class);
            when(application.getName()).thenReturn(name);
            when(application.isActive()).thenReturn(active);
            when(application.getCredential()).thenReturn(credential);
            when(application.getDescription()).thenReturn(description);
            when(application.getType()).thenReturn(type);
            when(application.getRemoteAddresses()).thenReturn(remoteAddresses);

            return application;
        }


        public static void assertEqualsTestApplication(com.atlassian.crowd.model.application.Application application)
        {
            assertEquals(NAME, application.getName());
            assertEquals(ACTIVE, application.isActive());
            assertEquals(DESCRIPTION, application.getDescription());

            assertEquals(ADDRESSES.size(), application.getRemoteAddresses().size());
            assertTrue(ADDRESSES.containsAll(application.getRemoteAddresses()));

        }

        public static com.atlassian.crowd.model.application.ApplicationImpl getUnmockedTestData(String applicationName)
        {
            com.atlassian.crowd.model.application.ApplicationImpl application =
                    com.atlassian.crowd.model.application.ApplicationImpl.newInstance(applicationName, ApplicationType.JIRA);
            application.setActive(ACTIVE);
            application.setDescription(DESCRIPTION);
            application.setCredential(CREDENTIAL);
            application.setRemoteAddresses(ADDRESSES);
            return application;
        }
    }
}
