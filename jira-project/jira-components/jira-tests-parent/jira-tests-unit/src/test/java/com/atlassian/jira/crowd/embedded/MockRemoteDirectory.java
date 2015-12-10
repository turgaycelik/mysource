package com.atlassian.jira.crowd.embedded;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.impl.IdentifierMap;
import com.atlassian.crowd.embedded.impl.IdentifierSet;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

// CWD-4028
// This mock has been stolen from crowd to help ensure the correctness of JiraDirectoryManager.
// If CWD-4028 gets fixed, then JIRA should be able to switch back to using DirectoryManagerGeneric
// and will not need this mock anymore.
public class MockRemoteDirectory implements RemoteDirectory
{
    protected Map<String, TimestampedUser> userMap = new IdentifierMap<TimestampedUser>();
    protected Map<String, Map<String, Set<String>>> userAttributesMap = new IdentifierMap<Map<String, Set<String>>>();
    private Map<String, InternalDirectoryGroup> groupMap = new IdentifierMap<InternalDirectoryGroup>();
    private Map<String, InternalDirectoryGroup> roleMap = new IdentifierMap<InternalDirectoryGroup>();
    private Map<String, String> attributeMap = new HashMap<String, String>();
    private Map<String, Set<String>> userMemberships = new IdentifierMap<Set<String>>();
    private long directoryId = 1;

    private boolean supportsInactiveAccounts = false;

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(final long directoryId)
    {
        this.directoryId = directoryId;
    }

    public String getDescriptiveName()
    {
        return "MockRemoteDirectory";
    }

    public void setAttributes(final Map<String, String> attributes)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public TimestampedUser findUserByName(final String name) throws UserNotFoundException
    {
        TimestampedUser user = userMap.get(name);
        if (user == null)
        {
            throw new UserNotFoundException(name);
        }
        return user;
    }

    public User findUserByNameOrNull(String name)
    {
        return userMap.get(name);
    }

    public UserWithAttributes findUserWithAttributesByName(final String name)
            throws UserNotFoundException, OperationFailedException
    {
        UserTemplateWithAttributes user = UserTemplateWithAttributes.ofUserWithNoAttributes(findUserByName(name));
        Map<String, Set<String>> attributes = userAttributesMap.get(name);
        if (attributes != null)
        {
            for (Map.Entry<String, Set<String>> entry : attributes.entrySet())
            {
                user.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        return user;
    }

    @Override
    public User findUserByExternalId(String externalId) throws UserNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public User authenticate(final String name, final PasswordCredential credential)
            throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        if (credential == null)
        {
            throw InvalidAuthenticationException.newInstanceWithName(name);
        }
        if (credential.isEncryptedCredential())
        {
            throw InvalidAuthenticationException.newInstanceWithName(name);
        }
        return userMap.get(name);
    }

    public User addUser(final UserTemplate user, final PasswordCredential credential)
            throws UserAlreadyExistsException
    {
        if (!userMap.containsKey(user.getName()))
        {
            return putUser(user);
        } else
        {
            throw new UserAlreadyExistsException(directoryId, user.getName());
        }
    }

    protected Directory getDirectory()
    {
        return new DirectoryImpl()
        {
            @Override
            public Long getId()
            {
                return directoryId;
            }
        };
    }

    public User updateUser(final UserTemplate user)
            throws InvalidUserException, UserNotFoundException, OperationFailedException
    {
        if (userMap.containsKey(user.getName()))
        {
            return putUser(user);
        } else
        {
            throw new UserNotFoundException(user.getName());
        }
    }

    protected User putUser(final UserTemplate user)
    {
        UserTemplate userTemplate = new UserTemplate(user);
        if (!supportsInactiveAccounts())
        {
            userTemplate.setActive(true);
        }
        InternalUser newUser = new InternalUser(userTemplate, getDirectory(), null);
        userMap.put(user.getName(), newUser);
        return newUser;
    }

    public void updateUserCredential(final String username, final PasswordCredential credential)
            throws UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public User renameUser(String oldName, String newName) throws UserNotFoundException, InvalidUserException, UserAlreadyExistsException
    {
        return rename(findUserByName(oldName), newName);
    }

    protected User rename(User oldUser, String newName) throws UserNotFoundException, UserAlreadyExistsException
    {
        UserTemplate newUser = new UserTemplate(oldUser);
        newUser.setName(newName);
        removeUser(oldUser.getName());
        return addUser(newUser, PasswordCredential.NONE);
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes)
            throws UserNotFoundException, OperationFailedException
    {
        if (!userMap.containsKey(username))
        {
            throw new UserNotFoundException(username);
        }

        Map<String, Set<String>> cachedAttributes = userAttributesMap.get(username);
        if (cachedAttributes == null)
        {
            cachedAttributes = new HashMap<String, Set<String>>();
            userAttributesMap.put(username, cachedAttributes);
        }
        cachedAttributes.putAll(attributes);
    }

    public void removeUserAttributes(final String username, final String attributeName)
            throws UserNotFoundException, OperationFailedException
    {
        userAttributesMap.get(username).remove(attributeName);
    }

    public void removeUser(final String name) throws UserNotFoundException
    {
        userMap.remove(name);
        userAttributesMap.remove(name);
        for (Set<String> users : userMemberships.values())
        {
            users.remove(name);
        }
    }

    public <T> List<T> searchUsers(final EntityQuery<T> query) throws OperationFailedException
    {
        if (query.getEntityDescriptor().getEntityType() == Entity.USER)
            return new ArrayList<T>((Collection<? extends T>) userMap.values());
        if (query.getEntityDescriptor().getEntityType() == Entity.GROUP)
        {
            ArrayList<T> result = new ArrayList<T>();
            result.addAll((Collection<? extends T>) groupMap.values());
            result.addAll((Collection<? extends T>) roleMap.values());
            return result;
        }
        return Collections.emptyList();
    }

    public InternalDirectoryGroup findGroupByName(final String name) throws GroupNotFoundException
    {
        InternalDirectoryGroup group = groupMap.get(name);
        if (group == null)
        {
            group = roleMap.get(name);
            if (group == null)
            {
                throw new GroupNotFoundException(name);
            }
        }
        return group;
    }

    public GroupWithAttributes findGroupWithAttributesByName(final String name)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Group addGroup(final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException
    {
        switch (group.getType())
        {
            case GROUP:
                groupMap.put(group.getName(), new InternalGroup(group, getDirectory()));
                return group;
            case LEGACY_ROLE:
                roleMap.put(group.getName(), new InternalGroup(group, getDirectory()));
                return group;
        }
        throw new IllegalArgumentException("Unknown Group Type");
    }

    public Group updateGroup(final GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException
    {
        if (groupMap.containsKey(group.getName()) || roleMap.containsKey(group.getName()))
        {
            return addGroup(group);
        } else
        {
            throw new GroupNotFoundException(group.getName());
        }
    }

    public Group renameGroup(final String oldName, final String newName)
            throws GroupNotFoundException, InvalidGroupException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroupAttributes(final String groupName, final String attributeName)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroup(final String name) throws GroupNotFoundException, OperationFailedException
    {
        groupMap.remove(name);
        roleMap.remove(name);
    }

    @SuppressWarnings({"unchecked"})
    public <T> List<T> searchGroups(final EntityQuery<T> query) throws OperationFailedException
    {
        // assume "search all" for Group objects
        switch (query.getEntityDescriptor().getGroupType())
        {
            case GROUP:
                return new ArrayList(groupMap.values());
            case LEGACY_ROLE:
                return new ArrayList(roleMap.values());
        }
        throw new IllegalArgumentException("Unknown Group Type");
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws OperationFailedException
    {
        final Set<String> members = userMemberships.get(groupName);
        return members != null && members.contains(username);
    }

    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
            throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void addUserToGroup(final String username, final String groupName)
        throws GroupNotFoundException, UserNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        if (!userMemberships.containsKey(groupName))
        {
            final Set<String> users = new IdentifierSet();
            userMemberships.put(groupName, users);
        }
        if (userMemberships.get(groupName).contains(username))
        {
            throw new MembershipAlreadyExistsException(getDirectoryId(), username, groupName);
        }
        userMemberships.get(groupName).add(username);
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public <T> List<T> searchGroupRelationships(final MembershipQuery<T> query) throws OperationFailedException
    {
        // Trivial implementation
        return Collections.emptyList();
    }

    public void testConnection() throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setSupportsInactiveAccounts(boolean supportsInactiveAccounts)
    {
        this.supportsInactiveAccounts = supportsInactiveAccounts;
    }

    public boolean supportsInactiveAccounts()
    {
        return supportsInactiveAccounts;
    }

    public boolean supportsNestedGroups()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean supportsSettingEncryptedCredential()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean isRolesDisabled()
    {
        return false;
    }

    public Set<String> getValues(final String key)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String getValue(final String key)
    {
        return attributeMap.get(key);
    }

    public Set<String> getKeys()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Group getGroup(final String name)
    {
        return groupMap.get(name);
    }

    public Group getRole(final String name)
    {
        return roleMap.get(name);
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
