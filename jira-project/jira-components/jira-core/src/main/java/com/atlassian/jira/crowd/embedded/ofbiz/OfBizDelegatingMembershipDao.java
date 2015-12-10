package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.jira.exception.DataAccessException;

import com.google.common.base.Function;

import org.apache.log4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * This class implements the MembershipDao from the Crowd Embedded SPI, but needs to delegate most work to the
 * internal DAO to avoid circular dependencies with the User & Groupp DAOs.
 */
public class OfBizDelegatingMembershipDao implements MembershipDao
{
    private static final Logger LOG = Logger.getLogger(OfBizDelegatingMembershipDao.class);
    private static final Function<UserOrGroupStub,String> GET_NAME = new Function<UserOrGroupStub,String>()
    {
        @Override
        public String apply(final UserOrGroupStub input)
        {
            return input.getName();
        }
    };

    private final InternalMembershipDao membershipDao;
    private final OfBizGroupDao groupDao;
    private final OfBizUserDao userDao;

    public OfBizDelegatingMembershipDao(final InternalMembershipDao membershipDao, final UserDao userDao, final GroupDao groupDao)
    {
        this.membershipDao = membershipDao;
        this.groupDao = (OfBizGroupDao)groupDao;
        this.userDao = (OfBizUserDao)userDao;
    }

    public BatchResult<String> addAllUsersToGroup(long directoryId, Collection<String> userNames, String groupName) throws GroupNotFoundException
    {
        final BatchResult<String> allResults = new BatchResult<String>(userNames.size());
        final List<UserOrGroupStub> users = new ArrayList<UserOrGroupStub>(userNames.size());
        for (String userName : userNames)
        {
            try
            {
                final UserOrGroupStub user = userDao.findByNameOrNull(directoryId, userName);
                if (user != null)
                {
                    users.add(user);
                }
                else
                {
                    allResults.addFailure(userName);
                    LOG.debug("User '" + userName + "' not found");
                }
            }
            catch (DataAccessException e)
            {
                LOG.debug("Data error trying to resolve user '" + userName + '\'', e);
            }
        }

        if (!users.isEmpty())
        {
            final UserOrGroupStub group = groupDao.findOfBizGroup(directoryId, groupName);
            final BatchResult<String> delegateResults = membershipDao.addAllUsersToGroup(directoryId, users, group);
            allResults.addFailures(delegateResults.getFailedEntities());
            allResults.addSuccesses(delegateResults.getSuccessfulEntities());
        }
        return allResults;
    }

    public boolean isUserDirectMember(final long directoryId, final String userName, final String groupName)
    {
        return membershipDao.isUserDirectMember(directoryId, userName, groupName);
    }

    public boolean isGroupDirectMember(final long directoryId, final String childGroup, final String parentGroup)
    {
        return membershipDao.isGroupDirectMember(directoryId, childGroup, parentGroup);
    }

    public void addUserToGroup(final long directoryId, final String user, final String group)
            throws UserNotFoundException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        final UserOrGroupStub idUser = userDao.findOfBizUser(directoryId, user);
        final UserOrGroupStub idGroup = groupDao.findOfBizGroup(directoryId, group);
        membershipDao.addUserToGroup(directoryId, idUser, idGroup);
    }

    public void addGroupToGroup(final long directoryId, final String child, final String parent)
            throws GroupNotFoundException
    {
        final UserOrGroupStub idChild = groupDao.findOfBizGroup(directoryId, child);
        final UserOrGroupStub idParent = groupDao.findOfBizGroup(directoryId, parent);

        membershipDao.addGroupToGroup(directoryId, idChild, idParent);
    }

    public void removeUserFromGroup(final long directoryId, final String user, final String group)
            throws UserNotFoundException, GroupNotFoundException, MembershipNotFoundException
    {
        final UserOrGroupStub idUser = userDao.findOfBizUser(directoryId, user);
        final UserOrGroupStub idGroup = groupDao.findOfBizGroup(directoryId, group);
        membershipDao.removeUserFromGroup(directoryId, idUser, idGroup);
    }

    public void removeGroupFromGroup(final long directoryId, final String child, final String parent)
            throws GroupNotFoundException, MembershipNotFoundException
    {
        final UserOrGroupStub idChild = groupDao.findOfBizGroup(directoryId, child);
        final UserOrGroupStub idParent = groupDao.findOfBizGroup(directoryId, parent);
        membershipDao.removeGroupFromGroup(directoryId, idChild, idParent);
    }

    public <T> List<T> search(final long directoryId, final MembershipQuery<T> query)
    {
        return result(directoryId, query, membershipDao.search(directoryId, query));
    }

    /**
     * deprecated Use {@link com.atlassian.jira.crowd.embedded.ofbiz.OfBizInternalMembershipDao#flushCache()} instead. Since v1.3.8
     */
    public void flushCache()
    {
        membershipDao.flushCache();
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> result(final long directoryId, final MembershipQuery<T> query, final List<String> entityNames)
            throws IllegalStateException
    {
        // If the required return type is String just return the list we have been given.
        if (query.getReturnType().isAssignableFrom(String.class))
        {
            return (List<T>)doNameQuery(directoryId, entityNames, query.getEntityToReturn());
        }

        // If the required return type is User.class then transform to User Objects
        if (query.getReturnType().isAssignableFrom(User.class))
        {
            return (List<T>)doUserQuery(directoryId, entityNames);
        }
        // If the required return type is Group.class then transform to Group Objects
        if (query.getReturnType().isAssignableFrom(Group.class))
        {
            return (List<T>)doGroupQuery(directoryId, entityNames);
        }
        throw new IllegalArgumentException("Class type '" + query.getReturnType() + "' for return values is not 'String', 'User' or 'Group'");

    }

    private List<OfBizGroup> doGroupQuery(final long directoryId, final List<String> entityNames)
    {
        final List<OfBizGroup> groups = new ArrayList<OfBizGroup>(entityNames.size());
        for (String groupName : entityNames)
        {
            // Transform from the lower case name to the case preserving name
            final OfBizGroup group = groupDao.findByNameOrNull(directoryId, groupName);
            if (group != null)
            {
                groups.add(group);
            }
        }
        return groups;
    }

    private List<OfBizUser> doUserQuery(final long directoryId, final List<String> entityNames)
    {
        final List<OfBizUser> users = new ArrayList<OfBizUser>(entityNames.size());
        for (String userName : entityNames)
        {
            final OfBizUser user = userDao.findByNameOrNull(directoryId, userName);
            if (user != null)
            {
                users.add(user);
            }
        }
        return users;
    }

    private List<String> doNameQuery(final long directoryId, final List<String> entityNames, EntityDescriptor descriptor)
    {
        if (descriptor.equals(EntityDescriptor.user()))
        {
            return getNames(doUserQuery(directoryId, entityNames));
        }
        if (descriptor.equals(EntityDescriptor.group()))
        {
            return getNames(doGroupQuery(directoryId, entityNames));
        }
        return newArrayList();
    }

    private static List<String> getNames(List<? extends UserOrGroupStub> list)
    {
        return newArrayList(transform(list, GET_NAME));
    }
}
