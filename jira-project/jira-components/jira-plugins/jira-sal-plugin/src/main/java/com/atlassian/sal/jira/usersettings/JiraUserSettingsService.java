package com.atlassian.sal.jira.usersettings;

import java.util.Collection;
import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.usersettings.UserSettings;
import com.atlassian.sal.api.usersettings.UserSettingsBuilder;
import com.atlassian.sal.api.usersettings.UserSettingsService;
import com.atlassian.sal.core.usersettings.DefaultUserSettings;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.module.propertyset.PropertySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.fugue.Option.none;

/**
 * JIRA's SAL implementation of the {@link com.atlassian.sal.api.usersettings.UserSettingsService}.
 *
 * @since 6.0
 */
public class JiraUserSettingsService implements UserSettingsService
{
    private static final Logger log = LoggerFactory.getLogger(JiraUserSettingsService.class);

    private final UserPropertyManager userPropertyManager;
    private final UserManager userManager;

    public JiraUserSettingsService(UserPropertyManager userPropertyManager, final UserManager userManager)
    {
        this.userPropertyManager = userPropertyManager;
        this.userManager = userManager;
    }

    @Override
    public UserSettings getUserSettings(String username)
    {
        UserSettingsBuilder userSettingsBuilder = getUserSettingsBuilder(getUser(username));

        return userSettingsBuilder.build();
    }

    @Override
    public UserSettings getUserSettings(final UserKey userKey)
    {
        Assertions.is("userKey cannot be null", userKey != null);

        UserSettingsBuilder userSettingsBuilder = getUserSettingsBuilder(getUser(userKey));

        return userSettingsBuilder.build();
    }

    @Override
    public void updateUserSettings(String username, Function<UserSettingsBuilder, UserSettings> updateFunction)
    {
        UserSettingsBuilder userSettingsBuilder = getUserSettingsBuilder(getUser(username));

        updateFunction.apply(userSettingsBuilder);
    }

    @Override
    public void updateUserSettings(final UserKey userKey, final Function<UserSettingsBuilder, UserSettings> updateFunction)
    {
        Assertions.is("userKey cannot be null", userKey != null);

        UserSettingsBuilder userSettingsBuilder = getUserSettingsBuilder(getUser(userKey));

        updateFunction.apply(userSettingsBuilder);
    }

    private static UserSettings buildUserSettings(PropertySet propertySet)
    {
        Collection propertySetKeys = propertySet.getKeys(USER_SETTINGS_PREFIX);

        UserSettingsBuilder settings = DefaultUserSettings.builder();

        Collection<String> settingKeys = Collections2.transform(propertySetKeys, PrefixStrippingFunction.INSTANCE);

        for (String settingKey : settingKeys)
        {
            int type = propertySet.getType(USER_SETTINGS_PREFIX + settingKey);

            switch (type)
            {
                case PropertySet.BOOLEAN:
                    settings.put(settingKey, propertySet.getBoolean(USER_SETTINGS_PREFIX + settingKey));
                    break;
                case PropertySet.STRING:
                    settings.put(settingKey, propertySet.getString(USER_SETTINGS_PREFIX + settingKey));
                    break;
                case PropertySet.LONG:
                    settings.put(settingKey, propertySet.getLong(USER_SETTINGS_PREFIX + settingKey));
                    break;
                default:
                    log.info("Property type '{}' is not supported by the SAL UserSettingsService", type);
            }
        }

        return settings.build();
    }

    private static enum PrefixStrippingFunction implements Function<Object, String>
    {
        INSTANCE;

        @Override
        public String apply(Object input)
        {
            String val = input.toString();
            return val.substring(USER_SETTINGS_PREFIX.length());
        }
    }

    static class JiraPropertySetUserSettingsBuilder implements UserSettingsBuilder
    {
        private final PropertySet propertySet;

        public JiraPropertySetUserSettingsBuilder(PropertySet propertySet)
        {
            this.propertySet = propertySet;
        }

        @Override
        public UserSettingsBuilder put(String key, String value)
        {
            checkArgumentKey(key);
            checkArgumentValue(value);

            propertySet.setString(USER_SETTINGS_PREFIX + key, value);

            return this;
        }

        @Override
        public UserSettingsBuilder put(String key, boolean value)
        {
            checkArgumentKey(key);

            propertySet.setBoolean(USER_SETTINGS_PREFIX + key, value);
            return this;
        }

        @Override
        public UserSettingsBuilder put(String key, long value)
        {
            checkArgumentKey(key);

            propertySet.setLong(USER_SETTINGS_PREFIX + key, value);
            return this;
        }

        @Override
        public UserSettingsBuilder remove(String key)
        {
            checkArgumentKey(key);

            propertySet.remove(USER_SETTINGS_PREFIX + key);
            return this;
        }

        @Override
        public Option<Object> get(String key)
        {
            checkArgumentKey(key);

            int type = propertySet.getType(USER_SETTINGS_PREFIX + key);
            switch (type)
            {
                case PropertySet.LONG:
                    return Option.<Object>some(propertySet.getLong(USER_SETTINGS_PREFIX + key));
                case PropertySet.BOOLEAN:
                    return Option.<Object>some(propertySet.getBoolean(USER_SETTINGS_PREFIX + key));
                case PropertySet.STRING:
                    return Option.<Object>some(propertySet.getString(USER_SETTINGS_PREFIX + key));
                default:
                    return none();
            }
        }

        @Override
        public Set<String> getKeys()
        {
            @SuppressWarnings("unchecked")
            Collection<String> transform = Collections2.transform(propertySet.getKeys(USER_SETTINGS_PREFIX), PrefixStrippingFunction.INSTANCE);

            return ImmutableSet.copyOf(transform);
        }

        @Override
        public UserSettings build()
        {
            return buildUserSettings(propertySet);
        }

        private static void checkArgumentKey(String key)
        {
            Assertions.is("key cannot be null", key != null);
            Assertions.is(String.format("key cannot be longer than %s characters", MAX_KEY_LENGTH), key.length() <= MAX_KEY_LENGTH);
        }

        private static void checkArgumentValue(String value)
        {
            Assertions.is("value cannot be null", value != null);
            Assertions.is(String.format("value cannot be longer than %s characters", MAX_STRING_VALUE_LENGTH), value.length() <= MAX_STRING_VALUE_LENGTH);
        }
    }

    private UserSettingsBuilder getUserSettingsBuilder(ApplicationUser user)
    {
        PropertySet propertySet = userPropertyManager.getPropertySet(user);

        return new JiraPropertySetUserSettingsBuilder(propertySet);
    }

    private ApplicationUser getUser(String userName)
    {
        final ApplicationUser user = userManager.getUserByName(userName);

        Assertions.is(String.format("No user exists with the username %s", userName), user != null);

        return user;
    }

    private ApplicationUser getUser(UserKey userKey)
    {
        final ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());

        Assertions.is(String.format("No user exists with the key %s", userKey.getStringValue()), user != null);

        return user;
    }
}
