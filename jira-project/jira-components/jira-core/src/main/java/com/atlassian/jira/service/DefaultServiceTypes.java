package com.atlassian.jira.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

import static com.atlassian.jira.user.util.Users.isAnonymous;

public class DefaultServiceTypes implements ServiceTypes
{
    private final InBuiltServiceTypes inBuiltServiceTypes;

    private final PermissionManager permissionManager;

    public DefaultServiceTypes(final InBuiltServiceTypes inBuiltServiceTypes, PermissionManager permissionManager)
    {
        this.inBuiltServiceTypes = inBuiltServiceTypes;
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean isCustom(final String serviceClassName)
    {
        return !isInBuilt(serviceClassName);
    }

    @Override
    public boolean isInBuilt(final String serviceClassName)
    {
        return Iterables.any(inBuiltServiceTypes.all(), new MatchByServiceClassNamePredicate(serviceClassName));

    }

    @Override
    public boolean isManageableBy(final User user, final String serviceClassName)
    {
        if (isCustom(serviceClassName))
        {
            if (isAnonymous(user))
            {
                return false;
            }
            else
            {
                return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
            }
        }
        else if (isInBuilt(serviceClassName))
        {
             return Iterables.any(inBuiltServiceTypes.manageableBy(user), new MatchByServiceClassNamePredicate(serviceClassName));
        }

        return false;
    }

    private static class MatchByServiceClassNamePredicate implements Predicate<InBuiltServiceTypes.InBuiltServiceType>
    {
        private final String serviceClassName;

        public MatchByServiceClassNamePredicate(String serviceClassName)
        {
            this.serviceClassName = serviceClassName;
        }

        @Override
        public boolean apply(InBuiltServiceTypes.InBuiltServiceType anInBuiltServiceType)
        {
            return anInBuiltServiceType.getType().getName().equals(serviceClassName);
        }
    }

}
