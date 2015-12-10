package com.atlassian.jira.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.CacheManager;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.permission.GlobalPermissionAddedEvent;
import com.atlassian.jira.event.permission.GlobalPermissionDeletedEvent;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.plugin.GlobalPermissionTypesManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Collections.unmodifiableCollection;

@EventComponent
public class DefaultGlobalPermissionManager implements GlobalPermissionManager
{
    private final GlobalPermissionsCache globalPermissionsCache;
    private final CrowdService crowdService;
    private final OfBizDelegator ofBizDelegator;
    private final EventPublisher eventPublisher;
    private final GlobalPermissionTypesManager globalPermissionTypesManager;

    public DefaultGlobalPermissionManager(final CrowdService crowdService, final OfBizDelegator ofBizDelegator,
            final EventPublisher eventPublisher, GlobalPermissionTypesManager globalPermissionTypesManager, final CacheManager cacheManager)
    {
        this.crowdService = crowdService;
        this.ofBizDelegator = ofBizDelegator;
        this.eventPublisher = eventPublisher;
        this.globalPermissionTypesManager = globalPermissionTypesManager;
        this.globalPermissionsCache = new GlobalPermissionsCache(ofBizDelegator, cacheManager);
    }

    @SuppressWarnings("unused")
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        globalPermissionsCache.clearCache();
    }

    @Override
    public Collection<GlobalPermissionType> getAllGlobalPermissions()
    {
        return globalPermissionTypesManager.getAll();
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(final int permissionId)
    {
        final GlobalPermissionKey translatedPermissionKey = GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.get(Integer.valueOf(permissionId));
        if (translatedPermissionKey == null)
        {
            return Option.none();
        }
        return getGlobalPermission(translatedPermissionKey);
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull final String permissionKey)
    {
        return globalPermissionTypesManager.getGlobalPermission(permissionKey);
    }

    @Override
    public Option<GlobalPermissionType> getGlobalPermission(@Nonnull final GlobalPermissionKey permissionKey)
    {
        return globalPermissionTypesManager.getGlobalPermission(permissionKey);
    }

    /**
     * Adds a global permission
     *
     * @param permissionId must be a global permission type
     * @param group        can be null if it is anyone permission
     * @return True if the permission was added
     */
    @Override
    public boolean addPermission(final int permissionId, final String group)
    {
        return getGlobalPermission(permissionId).fold(new Supplier<Boolean>()
              {
                  @Override
                  public Boolean get()
                  {
                      throw new IllegalArgumentException("Permission id passed must be a global permission, " + permissionId + " is not");
                  }
              }, new Function<GlobalPermissionType, Boolean>()
              {
                  @Override
                  public Boolean apply(final GlobalPermissionType globalPermissionType)
                  {
                      return addPermission(globalPermissionType, group);
                  }
              }
        );
    }

    @Override
    public boolean addPermission(final GlobalPermissionType globalPermissionType, final String group)
    {
        // as a final check we don't allow the group Anyone (null) to be added to a permission without anonymous allowed.  It should be protected by the UI
        // so as a last resort we check it here.
        if (!globalPermissionType.isAnonymousAllowed() && (group == null))
        {
            throw new IllegalArgumentException("The group Anyone cannot be added to the global permission JIRA Users");
        }
        ofBizDelegator.createValue("GlobalPermissionEntry",
                FieldMap.build("permission", globalPermissionType.getKey())
                        .add("group_id", group));
        globalPermissionsCache.clearCache();
        clearActiveUserCountIfNecessary(globalPermissionType.getGlobalPermissionKey());

        eventPublisher.publish(new GlobalPermissionAddedEvent(globalPermissionType, group));
        return true;
    }

    @Override
    public Collection<JiraPermission> getPermissions(final int permissionType)
    {
        Option<GlobalPermissionType> globalPermissionOpt = getGlobalPermission(permissionType);
        if(globalPermissionOpt.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            Collection<GlobalPermissionEntry> permissionEntries = getPermissions(globalPermissionOpt.get().getGlobalPermissionKey());
            // Translate these into JiraPermission objects for the sake of backwards compatibility;
            Collection<JiraPermission> translatedEntries = Lists.newArrayList();
            for(GlobalPermissionEntry permissionEntry : permissionEntries)
            {
                translatedEntries.add(new JiraPermissionImpl(permissionType, permissionEntry.getGroup(), GroupDropdown.DESC));
            }
            return translatedEntries;
        }
    }

    @Override
    public Collection<GlobalPermissionEntry> getPermissions(final GlobalPermissionType globalPermissionType)
    {
        return getPermissions(globalPermissionType.getGlobalPermissionKey());
    }

    @Override
    @Nonnull
    public Collection<GlobalPermissionEntry> getPermissions(@Nonnull final GlobalPermissionKey globalPermissionKey)
    {
        return globalPermissionsCache.getPermissions(globalPermissionKey.getKey());
    }

    @Override
    public boolean removePermission(final int permissionId, final String group)
    {
        return getGlobalPermission(permissionId).fold(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                throw new IllegalArgumentException("Permission id passed must be a global permission, " + permissionId + " is not");
            }
        }, new Function<GlobalPermissionType, Boolean>()
        {
            @Override
            public Boolean apply(final GlobalPermissionType globalPermissionType)
            {
                return removePermission(globalPermissionType, group);
            }
        });
    }

    @Override
    public boolean removePermission(final GlobalPermissionType globalPermissionType, final String group)
    {
        final GlobalPermissionEntry jiraPermission = new GlobalPermissionEntry(globalPermissionType.getKey(), group);
        if (hasPermission(jiraPermission))
        {
            removePermission(jiraPermission);
            globalPermissionsCache.clearCache();
            clearActiveUserCountIfNecessary(globalPermissionType.getGlobalPermissionKey());

            eventPublisher.publish(new GlobalPermissionDeletedEvent(globalPermissionType, group));
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean removePermissions(final String group)
    {
        if (group == null)
        {
            throw new IllegalArgumentException("Group passed must NOT be null");
        }
        if (crowdService.getGroup(group) == null)
        {
            throw new IllegalArgumentException("Group passed must exist");
        }

        final Set<GlobalPermissionEntry> permissions = globalPermissionsCache.getPermissions();
        for (final GlobalPermissionEntry permission : permissions)
        {
            if (group.equals(permission.getGroup()))
            {
                removePermission(permission);
                clearActiveUserCountIfNecessary(GlobalPermissionKey.of(permission.getPermissionKey()));
            }
        }
        globalPermissionsCache.clearCache();
        return true;
    }

    private void removePermission(GlobalPermissionEntry permission)
    {
        ofBizDelegator.removeByAnd("GlobalPermissionEntry",
                FieldMap.build("permission", permission.getPermissionKey())
                        .add("group_id", permission.getGroup()));
    }

    /**
     * Check if a global anonymous permission exists
     *
     * @param permissionId must be global permission
     */
    @Override
    public boolean hasPermission(final int permissionId)
    {
        Option<GlobalPermissionType> globalPermissionOpt = getGlobalPermission(permissionId);
        if(globalPermissionOpt.isEmpty())
        {
            // Permission doesn't exist, therefore no one can have permission to it
            return false;
        }
        else
        {
            return hasPermission(globalPermissionOpt.get().getGlobalPermissionKey(), null);
        }
    }

    @Override
    public boolean hasPermission(@Nonnull final GlobalPermissionType globalPermissionType)
    {
        return hasPermission(new GlobalPermissionEntry(globalPermissionType.getKey()));
    }

    @Override
    public boolean hasPermission(final int permissionId, final User user)
    {
        return hasPermission(permissionId, ApplicationUsers.from(user));
    }

    @Override
    public boolean hasPermission(final int permissionId, final ApplicationUser user)
    {
        final GlobalPermissionKey globalPermissionKey = GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.get(permissionId);
        if (globalPermissionKey == null)
        {
            // Permission doesn't exist, therefore no one can have permission to it
            return false;
        }
        else
        {
            return hasPermission(globalPermissionKey, user);
        }
    }

    @Override
    public boolean hasPermission(@Nonnull final GlobalPermissionKey globalPermissionKey, @Nullable final ApplicationUser user)
    {
        //Check the anonymous global permission first
        if (hasPermission(new GlobalPermissionEntry(globalPermissionKey.getKey(), null)))
        {
            return true;
        }
        if (user == null)
        {
            // permission for anonymous already failed above
            return false;
        }

        if (!user.isActive())
        {
            // inactive users have no permissions
            return false;
        }

        // Loop through the users groups and see if there is a global permission for one of them
        final Iterable<String> userGroups = crowdService.search(getGroupMembershipQuery(user.getDirectoryUser()));
        return Iterables.any(userGroups, new Predicate<String>()
        {
            @Override
            public boolean apply(final String groupName)
            {
                return hasPermission(new GlobalPermissionEntry(globalPermissionKey.getKey(), groupName));
            }
        });
    }

    @Override
    public boolean hasPermission(@Nonnull GlobalPermissionType globalPermissionType, @Nullable final ApplicationUser user)
    {
        return hasPermission(globalPermissionType.getGlobalPermissionKey(), user);
    }

    private MembershipQuery<String> getGroupMembershipQuery(final User user)
    {
        return QueryBuilder.queryFor(String.class, EntityDescriptor.group())
                .parentsOf(EntityDescriptor.user())
                .withName(user.getName())
                .returningAtMost(EntityQuery.ALL_RESULTS);
    }

    @Override
    public Collection<Group> getGroupsWithPermission(int permissionId)
    {
        Option<GlobalPermissionType> globalPermissionOpt = getGlobalPermission(permissionId);
        if(globalPermissionOpt.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return getGroupsWithPermission(globalPermissionOpt.get().getGlobalPermissionKey());
        }
    }

    @Override
    public Collection<Group> getGroupsWithPermission(@Nonnull final GlobalPermissionType globalPermissionType)
    {
        return getGroupsWithPermission(globalPermissionType.getGlobalPermissionKey());
    }

    @Override
    @Nonnull
    public Collection<Group> getGroupsWithPermission(@Nonnull final GlobalPermissionKey permissionKey)
    {
        final Collection<Group> groups = new ArrayList<Group>();
        final Collection<String> groupNames = getGroupNamesWithPermission(permissionKey);
        for (final String groupName : groupNames)
        {
            Group group = crowdService.getGroup(groupName);
            if (group != null)
            {
                groups.add(group);
            }
        }
        return unmodifiableCollection(groups);
    }

    public Collection<String> getGroupNames(final int permissionId)
    {
        Option<GlobalPermissionType> globalPermissionOpt = getGlobalPermission(permissionId);
        if(globalPermissionOpt.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return getGroupNamesWithPermission(globalPermissionOpt.get().getGlobalPermissionKey());
        }
    }

    @Override
    public Collection<String> getGroupNames(@Nonnull final GlobalPermissionType globalPermissionType)
    {
        return getGroupNamesWithPermission(globalPermissionType.getGlobalPermissionKey());
    }

    @Override
    @Nonnull
    public Collection<String> getGroupNamesWithPermission(@Nonnull final GlobalPermissionKey permissionKey)
    {
        final Set<String> groupNames = new HashSet<String>();
        final Collection<GlobalPermissionEntry> permissions = globalPermissionsCache.getPermissions(permissionKey.getKey());
        for (final GlobalPermissionEntry permissionEntry : permissions)
        {
            final String group = permissionEntry.getGroup();
            if (group != null)
            {
                groupNames.add(group);
            }
        }
        return unmodifiableCollection(groupNames);
    }

    @Override
    public boolean isGlobalPermission(final int permissionId)
    {
        return GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.containsKey(permissionId);
    }

    @Override
    public void clearCache()
    {
        globalPermissionsCache.clearCache();
    }

    /////////////// Cache Access methods ////////////////////////////////////////////////
    protected boolean hasPermission(final GlobalPermissionEntry permissionEntry)
    {
        // HACK - Since the JIRA System Administer permission implies the JIRA Administrator permission we
        // need to check if a user has the Sys perm if we are asking about the Admin perm
        if (GlobalPermissionKey.ADMINISTER.getKey().equals(permissionEntry.getPermissionKey()))
        {
            // Do a check where if they have the current "Admin" permission we will short circuit, otherwise do
            // a second check with the sam group and PermType against the "SYS" permission.
            return globalPermissionsCache.hasPermission(permissionEntry) || globalPermissionsCache.hasPermission(new GlobalPermissionEntry(GlobalPermissionKey.SYSTEM_ADMIN, permissionEntry.getGroup()));
        }
        else
        {
            return globalPermissionsCache.hasPermission(permissionEntry);
        }
    }

    private void clearActiveUserCountIfNecessary(final GlobalPermissionKey permissionKey)
    {
        if (permissionKey.equals(GlobalPermissionKey.USE) || permissionKey.equals(GlobalPermissionKey.ADMINISTER) || permissionKey.equals(GlobalPermissionKey.SYSTEM_ADMIN))
        {
            ComponentAccessor.getUserUtil().clearActiveUserCount();
        }
    }
}
