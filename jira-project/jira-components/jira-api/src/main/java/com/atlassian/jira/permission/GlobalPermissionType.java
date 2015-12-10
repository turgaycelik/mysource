package com.atlassian.jira.permission;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.Internal;
import com.atlassian.jira.security.Permissions;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Represents a global permission in JIRA.
 */
@ExperimentalApi
public final class GlobalPermissionType
{
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#ADMINISTER} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String ADMINISTER = "ADMINISTER";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#SYSTEM_ADMIN} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#USE} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String USE = "USE";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#USER_PICKER} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String USER_PICKER = "USER_PICKER";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#CREATE_SHARED_OBJECTS} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String CREATE_SHARED_OBJECTS = "CREATE_SHARED_OBJECTS";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#MANAGE_GROUP_FILTER_SUBSCRIPTIONS} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String MANAGE_GROUP_FILTER_SUBSCRIPTIONS = "MANAGE_GROUP_FILTER_SUBSCRIPTIONS";
    /** @deprecated Use {@link com.atlassian.jira.permission.GlobalPermissionKey#BULK_CHANGE} instead. This was never public API. Since v6.2.5. */
    @Internal
    public static final String BULK_CHANGE = "BULK_CHANGE";

    /**
     * This map is used to map the old Integer/Long based ID to a String based key. Once all existing code has been
     * refactored to use the keys directly, this method won't be needed any more.
     *
     * @deprecated Use {@link GlobalPermissionKey#GLOBAL_PERMISSION_ID_TRANSLATION} instead. This was never public API. Since v6.2.5.
     */
    @Internal
    public static BiMap<Integer, String> GLOBAL_PERMISSION_ID_TRANSLATION = ImmutableBiMap.<Integer, String>builder()
            .put(0, ADMINISTER)
            .put(1, USE)
            .put(44, SYSTEM_ADMIN)
            .put(22, CREATE_SHARED_OBJECTS)
            .put(24, MANAGE_GROUP_FILTER_SUBSCRIPTIONS)
            .put(33, BULK_CHANGE)
            .put(27, USER_PICKER)
            .build();

    //Using a sorted set here to make things a bit easier for unit tests
    private static final Set<String> USE_PERMISSIONS = ImmutableSortedSet.of(ADMINISTER, SYSTEM_ADMIN, USE);

    /**
     * Returns a Set of all the permissions that grant a user the permission to log into
     * JIRA (i.e. 'Use' JIRA). This is a port of {@link com.atlassian.jira.security.Permissions#getUsePermissions()}
     *
     * @return A set containing all the permissions or an empty set otherwise
     * @deprecated USE permissions will be changing significantly in JIRA 7.0 . If you are using this method, then you will need to watch out for announcements in 7.0 Developer upgrade guide. Since v6.2.5
     */
    @Internal
    public static Set<String> getUsePermissions()
    {
        return USE_PERMISSIONS;
    }


    private final String key;
    private final GlobalPermissionKey globalPermissionKey;
    private final String nameI18nKey;
    private final String descriptionI18nKey;
    private final boolean anonymousAllowed;

    @Internal
    public GlobalPermissionType(final String key, final String nameI18nKey, final String descriptionI18nKey, final boolean anonymousAllowed)
    {
        this.key = key;
        this.globalPermissionKey = GlobalPermissionKey.of(key);
        this.nameI18nKey = nameI18nKey;
        this.descriptionI18nKey = descriptionI18nKey;
        this.anonymousAllowed = anonymousAllowed;
    }

    @Internal
    public GlobalPermissionType(Permissions.Permission permission, final boolean anonymousAllowed)
    {
        this.key = permission.name();
        this.globalPermissionKey = GlobalPermissionKey.of(key);
        this.nameI18nKey = permission.getNameKey();
        this.descriptionI18nKey = permission.getDescriptionKey();
        this.anonymousAllowed = anonymousAllowed;
    }

    public String getKey()
    {
        return key;
    }

    public GlobalPermissionKey getGlobalPermissionKey()
    {
        return globalPermissionKey;
    }

    public String getNameI18nKey()
    {
        return nameI18nKey;
    }

    public String getDescriptionI18nKey()
    {
        return descriptionI18nKey;
    }

    public boolean isAnonymousAllowed()
    {
        return anonymousAllowed;
    }
}
