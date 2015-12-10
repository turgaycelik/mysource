package com.atlassian.jira.crowd.embedded;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.util.BatchResult;
import org.apache.commons.lang3.StringUtils;

// CWD-4028
// This mock has been stolen from crowd to help ensure the correctness of JiraDirectoryManager.
// If CWD-4028 gets fixed, then JIRA should be able to switch back to using DirectoryManagerGeneric
// and will not need this mock anymore.
public class MockInternalRemoteDirectory extends MockRemoteDirectory implements InternalRemoteDirectory
{

    private Map<String, Map<String, Set<String>>> userAttributesMap = new HashMap<String, Map<String, Set<String>>>();

    private boolean localUserStatusEnabled;

    public MockInternalRemoteDirectory()
    {
        setSupportsInactiveAccounts(true);
    }

    @Override
    public TimestampedUser findUserByName(String name) throws UserNotFoundException
    {
        return super.findUserByName(name);
    }

    @Override
    public TimestampedUser findUserByExternalId(String externalId) throws UserNotFoundException
    {
        for (TimestampedUser user : userMap.values())
        {
            if (StringUtils.equals(user.getExternalId(), externalId))
            {
                return user;
            }
        }
        throw new UserNotFoundException("no such user " + externalId);
    }

    @Override
    public InternalDirectoryGroup findGroupByName(String name) throws GroupNotFoundException
    {
        return super.findGroupByName(name);
    }

    @Override
    public Group addLocalGroup(final GroupTemplate group) throws InvalidGroupException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public BatchResult<User> addAllUsers(Set<UserTemplateWithCredentialAndAttributes> users)
    {
        final BatchResult<User> result = new BatchResult<User>(users.size());
        for (UserTemplateWithCredentialAndAttributes user : users)
        {
            try
            {
                result.addSuccess(addUser(user, user.getCredential()));
            } catch (UserAlreadyExistsException e)
            {
                result.addFailure(user);
            }
        }
        return result;
    }

    @Override
    public BatchResult<Group> addAllGroups(Set<GroupTemplate> groups)
    {
        final BatchResult<Group> result = new BatchResult<Group>(groups.size());
        for (GroupTemplate group : groups)
        {
            try
            {
                result.addSuccess(addGroup(group));
            } catch (InvalidGroupException e)
            {
                result.addFailure(group);
            } catch (OperationFailedException e)
            {
                result.addFailure(group);
            }
        }
        return result;
    }

    @Override
    public BatchResult<String> addAllUsersToGroup(Set<String> userNames, String groupName)
            throws GroupNotFoundException
    {
        final BatchResult<String> result = new BatchResult<String>(userNames.size());
        for (String userName : userNames)
        {
            try
            {
                addUserToGroup(userName, groupName);
                result.addSuccess(userName);
            } catch (UserNotFoundException e)
            {
                result.addFailure(userName);
            } catch (OperationFailedException e)
            {
                result.addFailure(userName);
            }
            catch (MembershipAlreadyExistsException e)
            {
                result.addFailure(userName);
            }
        }
        return result;
    }

    @Override
    protected User putUser(final UserTemplate user)
    {
        InternalUser newUser = new InternalUser(user, getDirectory(), null);
        userMap.put(user.getName(), newUser);
        return newUser;
    }

    @Override
    public BatchResult<String> removeAllUsers(Set<String> usernames)
    {
        BatchResult<String> result = new BatchResult<String>(usernames.size());
        for (String username : usernames)
        {
            try
            {
                removeUser(username);
                result.addSuccess(username);
            }
            catch (UserNotFoundException e)
            {
                result.addFailure(username);
            }
        }
        return result;
    }

    @Override
    public BatchResult<String> removeAllGroups(Set<String> groupNames)
    {
        BatchResult<String> result = new BatchResult<String>(groupNames.size());
        for (String groupName : groupNames)
        {
            try
            {
                removeGroup(groupName);
                result.addSuccess(groupName);
            }
            catch (GroupNotFoundException e)
            {
                result.addFailure(groupName);
            }
            catch (OperationFailedException e)
            {
                result.addFailure(groupName);
            }
        }
        return result;
    }

    public void setLocalUserStatusEnabled(boolean localUserStatus)
    {
        this.localUserStatusEnabled = localUserStatus;
    }

    @Override
    public boolean isLocalUserStatusEnabled()
    {
        return localUserStatusEnabled;
    }

    @Override
    public User forceRenameUser(User oldUser, String newName) throws UserNotFoundException
    {
        final User existingUser;
        // Check if this is a trivial rename of case only (eg 'bob' -> 'Bob')
        if (IdentifierUtils.equalsInLowerCase(oldUser.getName(), newName))
        {
            existingUser = null;
        }
        else
        {
            // Check if new name is already in use
            existingUser = findUserByNameOrNull(newName);
        }

        if (existingUser != null)
        {
            // New name is already taken - first we move the existing user to a vacant username
            try
            {
                rename(existingUser, newName + "#1");
            }
            catch (UserNotFoundException ex)
            {
                // Strange - we just found this user a few lines ago.
                // Anyway, if there is no user in the way, we can just try the desired rename
            } catch (UserAlreadyExistsException e)
            {
                throw new IllegalStateException(e);
            }
        }
        try
        {
            return rename(oldUser, newName);
        }
        catch (UserAlreadyExistsException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @Override
    public Set<String> getAllUserExternalIds() throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getUserCount() throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
