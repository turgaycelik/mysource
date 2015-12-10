package com.atlassian.jira.bc.license;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.license.ImmutableLicenseRole;
import com.atlassian.jira.license.LicenseRole;
import com.atlassian.jira.license.LicenseRoleDefinition;
import com.atlassian.jira.license.LicenseRoleId;
import com.atlassian.jira.license.LicenseRoleManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.3
 */
public class DefaultLicenseRoleService implements LicenseRoleService
{
    private final GroupManager groupManager;
    private final LicenseRoleManager licenseRoleManager;
    private final JiraAuthenticationContext ctx;
    private final GlobalPermissionManager permissionManager;

    public DefaultLicenseRoleService(@Nonnull final GroupManager groupManager,
            @Nonnull final LicenseRoleManager licenseRoleManager,
            @Nonnull final JiraAuthenticationContext ctx,
            @Nonnull final GlobalPermissionManager permissionManager)
    {
        this.ctx = notNull("ctx", ctx);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.groupManager = notNull("groupManager", groupManager);
        this.licenseRoleManager = notNull("licenseRoleManager", licenseRoleManager);
    }

    @Override
    public boolean userHasRole(@Nullable ApplicationUser user, @Nonnull LicenseRoleId licenseRoleId)
    {
        if (user == null)
        {
            return false;
        }

        for (String groupName : licenseRoleManager.getGroupsFor(licenseRoleId))
        {
            if (groupManager.isUserInGroup(user.getUsername(), groupName))
            {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public ServiceOutcome<Set<LicenseRole>> getRoles()
    {
        final ServiceOutcome<Set<LicenseRole>> outcome = checkPermission();
        if (!outcome.isValid())
        {
            return outcome;
        }

        final ImmutableSet.Builder<LicenseRole> role = ImmutableSet.builder();
        for (LicenseRoleDefinition licenseRole : licenseRoleManager.getDefinedLicenseRoles())
        {
            role.add(constructRole(licenseRole));
        }

        return ServiceOutcomeImpl.<Set<LicenseRole>>ok(role.build());
    }

    @Nonnull
    @Override
    public ServiceOutcome<LicenseRole> getRole(@Nonnull final LicenseRoleId licenseRoleId)
    {
        final ServiceOutcome<LicenseRole> outcome = checkPermission();
        if (!outcome.isValid())
        {
            return outcome;
        }

        final ServiceOutcome<LicenseRoleDefinition> definition = getDefinition(licenseRoleId);
        if (!definition.isValid())
        {
            return ServiceOutcomeImpl.error(definition);
        }

        return ServiceOutcomeImpl.ok(constructRole(definition.get()));
    }

    @Nonnull
    @Override
    public ServiceOutcome<LicenseRole> setGroups(@Nonnull final LicenseRoleId licenseRoleId, @Nonnull final Iterable<String> groups)
    {
        ServiceOutcome<LicenseRole> outcome = checkPermission();
        if (!outcome.isValid())
        {
            return outcome;
        }

        final ServiceOutcome<LicenseRoleDefinition> definition = getDefinition(licenseRoleId);
        if (!definition.isValid())
        {
            return ServiceOutcomeImpl.error(definition);
        }

        outcome = checkGroups(groups);
        if (!outcome.isValid())
        {
            return outcome;
        }

        licenseRoleManager.setGroups(licenseRoleId, groups);
        return ServiceOutcomeImpl.ok(constructRole(definition.get()));
    }

    /**
     * Materialise the {@link LicenseRole} associated with the passed
     * {@link com.atlassian.jira.license.LicenseRoleDefinition}.
     *
     * @param definition the definition to user.
     *
     * @return the {@code LicenseRole} associated with the passed {@code LicenseRoleDefinition}.
     */
    private LicenseRole constructRole(final LicenseRoleDefinition definition)
    {
        return new ImmutableLicenseRole(definition,
                Iterables.filter(licenseRoleManager.getGroupsFor(definition.getLicenseRoleId()), new Predicate<String>()
                {
                    @Override
                    public boolean apply(@Nullable final String input)
                    {
                        return groupManager.groupExists(input);
                    }
                }));
    }

    private <T> ServiceOutcome<T> checkPermission()
    {
        if (!permissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, ctx.getUser()))
        {
            return ServiceOutcomeImpl.error(ctx.getI18nHelper().getText("licenserole.service.permission.denied"),
                    ErrorCollection.Reason.FORBIDDEN);
        }
        else
        {
            return ServiceOutcomeImpl.ok(null);
        }
    }

    private <T> ServiceOutcome<T> checkGroups(Iterable<String> groups)
    {
        for (String group : groups)
        {
            if (group == null || !groupManager.groupExists(group))
            {
                //Making sure null is "null".
                group = String.valueOf(group);
                final String message = ctx.getI18nHelper().getText("licenserole.service.group.does.not.exist", group);

                final SimpleErrorCollection collection = new SimpleErrorCollection();
                collection.addError(LicenseRoleService.ERROR_GROUPS, message);
                collection.addReason(ErrorCollection.Reason.VALIDATION_FAILED);

                return new ServiceOutcomeImpl<T>(collection);
            }
        }
        return ServiceOutcomeImpl.ok(null);
    }

    private ServiceOutcome<LicenseRoleDefinition> getDefinition(LicenseRoleId id)
    {
        final Optional<LicenseRoleDefinition> roleDefinition = licenseRoleManager.getLicenseRoleDefinition(id);
        if (!roleDefinition.isPresent())
        {
            final String message = ctx.getI18nHelper().getText("licenserole.service.role.does.not.exist", id.getName());
            return ServiceOutcomeImpl.error(message, ErrorCollection.Reason.NOT_FOUND);
        }
        else
        {
            return ServiceOutcomeImpl.ok(roleDefinition.get());
        }
    }
}
