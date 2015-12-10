package com.atlassian.jira.config.managedconfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Store layer of {@link ManagedConfigurationItemService}. Plugin developers should not need to invoke these methods
 * directly.
 *
 * @see ManagedConfigurationItemService
 * @since v5.2
 */
public interface ManagedConfigurationItemStore
{
    /**
     * Persist the specified {@link ManagedConfigurationItem}.
     *
     * @param item the item
     * @return the persisted item
     * @throws com.atlassian.jira.exception.DataAccessException
     */
    @Nonnull
    public ManagedConfigurationItem updateManagedConfigurationItem(@Nonnull ManagedConfigurationItem item);

    /**
     * Remove the specified {@link ManagedConfigurationItem}.
     *
     * @param item the item
     * @return true if the item was removed; false otherwise
     */
    public boolean deleteManagedConfigurationItem(@Nonnull ManagedConfigurationItem item);

    /**
     * Retrieve the {@link ManagedConfigurationItem} description of the specified item ID and type.
     *
     * @param itemId the item ID; note that each type describes its IDs individually
     * @param type the type
     * @return the item if found; null otherwise
     */
    @Nullable
    public ManagedConfigurationItem getManagedConfigurationItem(@Nonnull String itemId, @Nonnull ManagedConfigurationItemType type);

    /**
     * Retrieve all the {@link ManagedConfigurationItem}s of the specified type. This includes items which may have
     * been owned by a plugin which is no longer installed.
     *
     * @param type the type
     * @return the items
     */
    @Nonnull
    public Collection<ManagedConfigurationItem> getManagedConfigurationItems(@Nonnull ManagedConfigurationItemType type);
}
