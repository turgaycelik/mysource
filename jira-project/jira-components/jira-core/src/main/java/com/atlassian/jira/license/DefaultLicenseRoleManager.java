package com.atlassian.jira.license;

import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.plugin.license.LicenseRoleModuleDescriptor;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import static com.atlassian.jira.license.LicenseRoleGroupEntityFactory.GROUP_ID;
import static com.atlassian.jira.license.LicenseRoleGroupEntityFactory.LICENSE_ROLE_NAME;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.license.LicenseRoleManager}.
 *
 * @since 6.3
 */
public class DefaultLicenseRoleManager implements LicenseRoleManager
{
    private final Logger log = Logger.getLogger(DefaultLicenseRoleManager.class);

    private final LicenseRoleGroupsCache licenseRoleGroupsCache;
    private final EntityEngine entityEngine;
    private final PluginAccessor pluginAccessor;

    public DefaultLicenseRoleManager(@Nonnull final LicenseRoleGroupsCache licenseRoleGroupsCache,
            @Nonnull final EntityEngine entityEngine, @Nonnull final PluginAccessor pluginAccessor)
    {
        this.licenseRoleGroupsCache = notNull("licenseRoleGroupsCache", licenseRoleGroupsCache);
        this.entityEngine = notNull("entityEngine", entityEngine);
        this.pluginAccessor = notNull("pluginAccessor", pluginAccessor);
    }

    @Nonnull
    @Override
    public ImmutableSet<String> getGroupsFor(@Nonnull final LicenseRoleId licenseRoleId)
    {
        Assertions.notNull("licenseRoleId", licenseRoleId);
        return licenseRoleGroupsCache.getGroupsFor(licenseRoleId);
    }

    @Override
    public boolean licenseRoleHasGroup(@Nonnull final LicenseRoleId licenseRoleId, @Nonnull final String groupId)
    {
        Assertions.notNull("licenseRoleId", licenseRoleId);
        Assertions.notNull("groupId", groupId);

        return licenseRoleGroupsCache.getGroupsFor(licenseRoleId)
                .contains(groupId);
    }

    @Nonnull
    @Override
    public Set<LicenseRoleDefinition> getDefinedLicenseRoles()
    {
        return ImmutableSet.copyOf(getUniqueLicenseRoleDefinitions());
    }

    @Override
    public boolean isLicenseRoleDefined(@Nonnull final LicenseRoleId licenseRoleId)
    {
        Assertions.notNull("licenseRoleId", licenseRoleId);

        return Iterables.any(getUniqueLicenseRoleDefinitions(), findId(licenseRoleId));
    }

    @Nonnull
    @Override
    public Optional<LicenseRoleDefinition> getLicenseRoleDefinition(@Nonnull final LicenseRoleId licenseRoleId)
    {
        Assertions.notNull("licenseRoleId", licenseRoleId);

        return Iterables.tryFind(getDefinedLicenseRoles(), findId(licenseRoleId));
    }

    @Override
    public void setGroups(@Nonnull final LicenseRoleId licenseRoleId, @Nonnull final Iterable<String> groups)
    {
        Assertions.notNull("licenseRoleId", licenseRoleId);
        Assertions.containsNoNulls("groups", groups);

        final ImmutableSet<String> wantedGroups = ImmutableSet.copyOf(groups);
        final ImmutableSet<String> currentGroups = getGroupsFor(licenseRoleId);

        boolean invalidate = true;
        try
        {
            invalidate = addGroups(licenseRoleId, Sets.difference(wantedGroups, currentGroups)) |
                    removeGroups(licenseRoleId, Sets.difference(currentGroups, wantedGroups));
        }
        finally
        {
            //Invalid the cache even when a SQL error occurs.
            if (invalidate)
            {
                licenseRoleGroupsCache.invalidateCacheEntry(licenseRoleId);
            }
        }
    }

    private boolean addGroups(LicenseRoleId licenseRoleId, Set<String> groups)
    {
        if (!groups.isEmpty())
        {
            for (String group : groups)
            {
                entityEngine.createValue(Entity.LICENSE_ROLE_GROUP,
                        new LicenseRoleGroupEntry(licenseRoleId.getName(), group));
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean removeGroups(LicenseRoleId licenseRoleId, Set<String> groups)
    {
        if (!groups.isEmpty())
        {
            entityEngine.delete(Delete.from(Entity.LICENSE_ROLE_GROUP)
                    .whereEqual(LICENSE_ROLE_NAME, licenseRoleId.getName())
                    .andCondition(new EntityExpr(GROUP_ID, EntityOperator.IN, groups)));
            return true;
        }
        else
        {
            return false;
        }
    }

    private Set<LicenseRoleDefinition> getUniqueLicenseRoleDefinitions()
    {
        final List<LicenseRoleModuleDescriptor> moduleDescriptors =
                pluginAccessor.getEnabledModuleDescriptorsByClass(LicenseRoleModuleDescriptor.class);

        final Set<LicenseRoleDefinition> uniqueModuleDescriptors = Sets.newHashSet();
        for (LicenseRoleModuleDescriptor moduleDescriptor: moduleDescriptors)
        {
            final LicenseRoleDefinition definition = moduleDescriptor.getModule();
            final boolean alreadyAdded = uniqueModuleDescriptors.add(definition);
            if (alreadyAdded)
            {
                log.debug(String.format("The license role module descriptor with id '%s' has a duplicate definition.",
                        definition.getLicenseRoleId()));
            }
        }
        return uniqueModuleDescriptors;
    }

    private static Predicate<LicenseRoleDefinition> findId(final LicenseRoleId licenseRoleId)
    {
        return new Predicate<LicenseRoleDefinition>()
        {
            @Override
            public boolean apply(final LicenseRoleDefinition licenseRoleModuleDescriptor)
            {
                return licenseRoleModuleDescriptor.getLicenseRoleId().equals(licenseRoleId);
            }
        };
    }
}
