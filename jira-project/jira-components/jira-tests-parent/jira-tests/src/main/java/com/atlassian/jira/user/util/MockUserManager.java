package com.atlassian.jira.user.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.directory.InternalDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUserKeyService;

import com.google.common.collect.Sets;

/**
 * Really simple mock implementation
 *
 * @since v4.1
 */
public class MockUserManager implements UserManager
{
    final MockUserKeyService mockUserKeyService;
    final Directory mockDirectory = new MockDirectory();
    final Map<String, User> userMap = new HashMap<String, User>();

    private boolean writableDirectory = true;
    private boolean groupWritableDirectory = true;

    public MockUserManager()
    {
        this(new MockUserKeyService());
    }

    public MockUserManager(final MockUserKeyService mockUserKeyService)
    {
        this.mockUserKeyService = mockUserKeyService;
    }



    @Override
    public int getTotalUserCount()
    {
        return userMap.size();
    }

    @Override
    @Nonnull
    public Set<User> getAllUsers()
    {
        return Sets.newHashSet(getUsers());
    }

    @Override
    public User getUser(final @Nullable String userName)
    {
        return userMap.get(IdentifierUtils.toLowerCase(userName));
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return userMap.get(IdentifierUtils.toLowerCase(userName));
    }

    @Override
    public ApplicationUser getUserByKey(@Nullable String userKey)
    {
        String username = mockUserKeyService.getUsernameForKey(userKey);
        User user = userMap.get(username);
        if (user == null)
        {
            return null;
        }
        return new DelegatingApplicationUser(userKey, user);
    }

    @Override
    public ApplicationUser getUserByName(@Nullable String userName)
    {
        if (userName == null)
        {
            return null;
        }
        final String lowerUserName = IdentifierUtils.toLowerCase(userName);
        User user = userMap.get(lowerUserName);
        if (user == null)
        {
            return null;
        }
        String userKey = mockUserKeyService.getKeyForUsername(lowerUserName);
        return new DelegatingApplicationUser(userKey, user);
    }

    @Override
    public ApplicationUser getUserByKeyEvenWhenUnknown(@Nullable String userKey)
    {
        if (userKey == null)
        {
            return null;
        }
        ApplicationUser user = getUserByKey(userKey);
        if (user != null)
        {
            return user;
        }
        return new DelegatingApplicationUser(userKey, new ImmutableUser(-1, userKey, userKey, "?", false));
    }

    @Override
    public ApplicationUser getUserByNameEvenWhenUnknown(@Nullable String userName)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public User findUserInDirectory(String userName, Long directoryId)
    {
        if (directoryId == 1L)
        {
            return userMap.get(IdentifierUtils.toLowerCase(userName));
        }
        return null;
    }

    @Override
    public User getUserEvenWhenUnknown(final String userName)
    {
        return userMap.get(userName);
    }

    @Override
    public boolean canUpdateUser(User user)
    {
        return user != null && writableDirectory;
    }

    @Override
    public boolean canUpdateUser(@Nonnull ApplicationUser user)
    {
        return writableDirectory;
    }

    @Override
    public boolean userCanUpdateOwnDetails(@Nonnull final ApplicationUser user)
    {
        return canUpdateUser(user);
    }

    @Override
    public boolean canRenameUser(ApplicationUser user)
    {
        return canUpdateUser(user);
    }

    @Override
    public void updateUser(User user)
    {
        userMap.put(user.getName(), user);
    }

    @Override
    public void updateUser(ApplicationUser user)
    {
        userMap.put(user.getKey(), user.getDirectoryUser());
    }

    @Override
    public boolean canUpdateUserPassword(User user)
    {
        return true;
    }

    @Override
    public boolean canUpdateGroupMembershipForUser(User user)
    {
        return true;
    }

    @Override
    public Set<Group> getAllGroups()
    {
        return Collections.emptySet();
    }

    @Override
    public Group getGroup(final @Nullable String groupName)
    {
        return null;
    }

    @Override
    public Group getGroupObject(@Nullable String groupName)
    {
        return null;
    }

    @Override
    @Nonnull
    public List<Directory> getWritableDirectories()
    {
        if (writableDirectory)
        {
            return Collections.singletonList(mockDirectory);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasWritableDirectory()
    {
        return writableDirectory;
    }

    public void setWritableDirectory(boolean writableDirectory)
    {
        this.writableDirectory = writableDirectory;
    }

    @Override
    public boolean hasPasswordWritableDirectory()
    {
        return true;
    }

    @Override
    public boolean hasGroupWritableDirectory()
    {
        return groupWritableDirectory;
    }

    public void setGroupWritableDirectory(boolean groupWritableDirectory)
    {
        this.groupWritableDirectory = groupWritableDirectory;
    }

    @Override
    public boolean canDirectoryUpdateUserPassword(Directory directory)
    {
        return true;
    }

    @Override
    public Directory getDirectory(Long directoryId)
    {
        return (directoryId == 1L) ? mockDirectory : null;
    }

    @Override
    public boolean isUserExisting(@Nullable ApplicationUser user)
    {
        return user != null && user.getDirectoryId() != -1;
    }

    @Override
    @Nonnull
    public Collection<User> getUsers()
    {
        return userMap.values();
    }

    @Override
    @Nonnull
    public Collection<ApplicationUser> getAllApplicationUsers()
    {
        return ApplicationUsers.from(getAllUsers());
    }

    @Override
    public Collection<com.atlassian.crowd.embedded.api.Group> getGroups()
    {
        return Collections.emptySet();
    }

    public void addUser(User user)
    {
        final String username = IdentifierUtils.toLowerCase(user.getName());
        userMap.put(username, user);
        mockUserKeyService.setMapping(username, username);
    }

    public void addUser(ApplicationUser user)
    {
        final String username = IdentifierUtils.toLowerCase(user.getUsername());
        userMap.put(username, user.getDirectoryUser());
        mockUserKeyService.setMapping(user.getKey(), username);
    }

    public MockUserKeyService getMockUserKeyService()
    {
        return mockUserKeyService;
    }

    @Nonnull
    @Override
    public UserState getUserState(@Nullable final User user)
    {
        if (user == null)
        {
            return UserState.INVALID_USER;
        }
        return getUserState(user.getName(), user.getDirectoryId());
    }

    @Nonnull
    @Override
    public UserState getUserState(@Nullable final ApplicationUser user)
    {
        if (user == null)
        {
            return UserState.INVALID_USER;
        }
        return getUserState(user.getUsername(), user.getDirectoryId());
    }

    @Nonnull
    @Override
    public UserState getUserState(@Nonnull final String username, final long directoryId)
    {
        if (findUserInDirectory(username, directoryId) != null)
        {
            return UserState.NORMAL_USER;
        }
        return UserState.INVALID_USER;
    }

    class MockDirectory implements Directory
    {
        private final Date CREATED = new Date();

        @Override
        public Long getId()
        {
            return 1L;
        }

        @Override
        public String getName()
        {
            return "Mock Internal Directory";
        }

        @Override
        public boolean isActive()
        {
            return true;
        }

        @Override
        public String getEncryptionType()
        {
            return "plaintext";
        }

        @Override
        public Map<String, String> getAttributes()
        {
            return Collections.emptyMap();
        }

        @Override
        public Set<OperationType> getAllowedOperations()
        {
            if (writableDirectory)
            {
                return EnumSet.allOf(OperationType.class);
            }
            return EnumSet.noneOf(OperationType.class);
        }

        @Override
        public String getDescription()
        {
            return "Mock internal directory for unit tests";
        }

        @Override
        public DirectoryType getType()
        {
            return DirectoryType.INTERNAL;
        }

        @Override
        public String getImplementationClass()
        {
            return InternalDirectory.class.getName();
        }

        @Override
        public Date getCreatedDate()
        {
            return CREATED;
        }

        @Override
        public Date getUpdatedDate()
        {
            return CREATED;
        }

        @Override
        public Set<String> getValues(final String s)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getValue(final String s)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Set<String> getKeys()
        {
            return userMap.keySet();
        }

        @Override
        public boolean isEmpty()
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
