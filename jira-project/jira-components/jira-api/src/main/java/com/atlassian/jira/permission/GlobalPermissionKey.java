package com.atlassian.jira.permission;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * An identifier for a Global Permission.
 *
 * You can use the pre-defined constants for the well-known system global permissions eg:
 * <pre>GlobalPermissionKey.ADMINISTER</pre>
 * or create a new GlobalPermissionKey with the static initializer eg:
 * <pre>GlobalPermissionKey.of("foo")</pre>
 *
 * @see com.atlassian.jira.security.GlobalPermissionManager#hasPermission(GlobalPermissionKey, com.atlassian.jira.user.ApplicationUser)
 *
 * @since 6.2.5
 */
public final class GlobalPermissionKey
{
    public static final GlobalPermissionKey ADMINISTER = new GlobalPermissionKey("ADMINISTER");
    public static final GlobalPermissionKey SYSTEM_ADMIN = new GlobalPermissionKey("SYSTEM_ADMIN");
    public static final GlobalPermissionKey USE = new GlobalPermissionKey("USE");
    public static final GlobalPermissionKey USER_PICKER = new GlobalPermissionKey("USER_PICKER");
    public static final GlobalPermissionKey CREATE_SHARED_OBJECTS = new GlobalPermissionKey("CREATE_SHARED_OBJECTS");
    public static final GlobalPermissionKey MANAGE_GROUP_FILTER_SUBSCRIPTIONS = new GlobalPermissionKey("MANAGE_GROUP_FILTER_SUBSCRIPTIONS");
    public static final GlobalPermissionKey BULK_CHANGE = new GlobalPermissionKey("BULK_CHANGE");

    /**
     * This map is used to map the old Integer/Long based ID to a String based key. Once all existing code has been
     * refactored to use the keys directly, this method won't be needed any more.
     */
    @Internal
    public static final BiMap<Integer, GlobalPermissionKey> GLOBAL_PERMISSION_ID_TRANSLATION = ImmutableBiMap.<Integer, GlobalPermissionKey>builder()
            .put(0, ADMINISTER)
            .put(1, USE)
            .put(44, SYSTEM_ADMIN)
            .put(22, CREATE_SHARED_OBJECTS)
            .put(24, MANAGE_GROUP_FILTER_SUBSCRIPTIONS)
            .put(33, BULK_CHANGE)
            .put(27, USER_PICKER)
            .build();

    private final String key;

    @Internal
    private GlobalPermissionKey(@Nonnull final String key)
    {
        this.key = key;
    }

    @Nonnull
    public String getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return key;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final GlobalPermissionKey that = (GlobalPermissionKey) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    /**
     * Static initializer to construct a GlobalPermissionKey from a given String key.
     */
    @Nonnull
    public static GlobalPermissionKey of(@Nonnull String key)
    {
        return new GlobalPermissionKey(key);
    }
}
