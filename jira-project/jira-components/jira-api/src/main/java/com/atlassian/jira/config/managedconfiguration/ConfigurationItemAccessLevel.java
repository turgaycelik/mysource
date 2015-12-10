package com.atlassian.jira.config.managedconfiguration;

import com.atlassian.annotations.PublicApi;

/**
 * These values define the different access levels that exist for altering a {@link ManagedConfigurationItem}.
 * <p/>
 * Note: this setting does not affect configuration changes made at the API level. It only determines the visibility/access
 * of configuration options within JIRA's administration interface.
 *
 * @see ManagedConfigurationItem
 * @see ManagedConfigurationItemService#doesUserHavePermission(com.atlassian.crowd.embedded.api.User, ManagedConfigurationItem)
 * @since v5.2
 */
@PublicApi
public enum ConfigurationItemAccessLevel
{
    /**
     * No user is permitted to alter the item's configuration from the JIRA administration interface.
     */
    LOCKED,

    /**
     * Only system administrators are permitted to alter the item's configuration from the JIRA administration interface.
     */
    SYS_ADMIN,

    /**
     * Administrators or system administrators are permitted to alter the item's configuration from the JIRA administration interface.
     * However they will be shown a message saying that the item is "managed".
     */
    ADMIN
}
