package com.atlassian.jira.license;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.event.ClearCacheEvent;

import com.google.common.collect.ImmutableSet;

import static com.atlassian.jira.entity.Entity.LICENSE_ROLE_GROUP;
import static com.atlassian.jira.license.LicenseRoleGroupEntityFactory.GROUP_ID;
import static com.atlassian.jira.license.LicenseRoleGroupEntityFactory.LICENSE_ROLE_NAME;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This is a very basic cache that stores license role to group mappings.
 *
 * @since 6.3
 */
@EventComponent
public class LicenseRoleGroupsCache
{
    @ClusterSafe
    private final Cache<String, ImmutableSet<String>> licenseRoleGroups;

    public LicenseRoleGroupsCache(final CacheManager cacheManager, final EntityEngine entityEngine)
    {
        notNull("cacheManager", cacheManager);
        notNull("entityEngine", entityEngine);

        licenseRoleGroups = cacheManager.getCache(LicenseRoleGroupsCache.class.getName() + ".licenseRoleGroups",
                new LicenseRoleToGroupMappingsLoader(entityEngine));
    }

    /**
     * Resets the cache entry for the provided license role.
     *
     * @param licenseRoleId the license role entry to invalidate.
     */
    void invalidateCacheEntry(final LicenseRoleId licenseRoleId)
    {
        licenseRoleGroups.remove(licenseRoleId.getName());
    }

    /**
     * Retrieves all of the groups for the provided license role.
     *
     * @param licenseRoleId the license role to get groups for.
     * @return a collection of all the groups for the provided license role.
     */
    ImmutableSet<String> getGroupsFor(final LicenseRoleId licenseRoleId)
    {
        return licenseRoleGroups.get(licenseRoleId.getName());
    }

    /**
     * Loads the license role's groups in the cache.
     */
    private class LicenseRoleToGroupMappingsLoader implements CacheLoader<String, ImmutableSet<String>>
    {
        private final EntityEngine engine;

        private LicenseRoleToGroupMappingsLoader(final EntityEngine engine) {this.engine = engine;}

        @Override
        public ImmutableSet<String> load(@Nonnull final String licenseRole)
        {
            final List<String> groups = Select.stringColumn(GROUP_ID)
                    .from(LICENSE_ROLE_GROUP)
                    .whereEqual(LICENSE_ROLE_NAME, licenseRole)
                    .runWith(engine)
                    .asList();

            return ImmutableSet.copyOf(groups);
        }
    }

    @EventListener
    public void clearCache(ClearCacheEvent event)
    {
        licenseRoleGroups.removeAll();
    }
}
