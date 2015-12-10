package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
public class PropertySetUserLocaleStoreTest
{
    public static final Locale DEFAULT_LOCALE = Locale.CANADA;
    public static final Locale APP_USER_LOCALE = Locale.FRENCH;
    public static final Locale USER_LOCALE = Locale.ENGLISH;

    private MockApplicationProperties properties = new MockApplicationProperties();
    private MockUserPropertyManager userPropertyManager = new MockUserPropertyManager();
    private MockUserKeyService userKeys = new MockUserKeyService();

    private ApplicationUser appUserWithoutProperties = new MockApplicationUser("appUserWithoutProperties");
    private ApplicationUser appUserWithoutLocale = new MockApplicationUser("appUserWithoutLocale");
    private ApplicationUser appUserWithLocale = new MockApplicationUser("appUserWithLocale");

    private User userWithoutProperties = new MockUser("userWithoutProperties");
    private User userWithoutLocale = new MockUser("userWithoutLocale");
    private User userWithLocale = new MockUser("userWithLocale");
    private User userWithoutKey = new MockUser("userWithoutKey");

    private PropertySetUserLocaleStore userLocaleStore = new PropertySetUserLocaleStore(properties, userPropertyManager, userKeys);

    @Before
    public void setup()
    {
        properties.setDefaultLocale(DEFAULT_LOCALE);

        //user's with empty property set.
        userPropertyManager.createOrGetForUser(appUserWithoutLocale);
        userPropertyManager.createOrGetForUser(userWithoutLocale);

        //user's with locale.
        userPropertyManager.createOrGetForUser(appUserWithLocale)
                .setString(PreferenceKeys.USER_LOCALE, APP_USER_LOCALE.toString());

        userPropertyManager.createOrGetForUser(userWithLocale)
                .setString(PreferenceKeys.USER_LOCALE, USER_LOCALE.toString());

        //user with no key.
        userKeys.map(null, userWithoutKey);
    }

    @Test
    public void returnsDefaultLocaleForNullUser()
    {
        assertThat(userLocaleStore.getLocale((ApplicationUser) null), equalTo(DEFAULT_LOCALE));
        assertThat(userLocaleStore.getLocale((User) null), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsDefaultLocaleForUserWithoutProperties()
    {
        assertThat(userLocaleStore.getLocale(appUserWithoutProperties), equalTo(DEFAULT_LOCALE));
        assertThat(userLocaleStore.getLocale(userWithoutProperties), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsDefaultLocaleForUserWithoutLocale()
    {
        assertThat(userLocaleStore.getLocale(appUserWithoutLocale), equalTo(DEFAULT_LOCALE));
        assertThat(userLocaleStore.getLocale(userWithoutLocale), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsLocaleForUserWithConfiguredLocale()
    {
        assertThat(userLocaleStore.getLocale(appUserWithLocale), equalTo(APP_USER_LOCALE));
        assertThat(userLocaleStore.getLocale(userWithLocale), equalTo(USER_LOCALE));
    }

    @Test
    public void returnsDefaultLocaleForUserWithoutKey()
    {
        assertThat(userLocaleStore.getLocale(userWithoutKey), equalTo(DEFAULT_LOCALE));
    }

    @Test
    public void returnsDefaultUserLocale()
    {
        assertThat(userLocaleStore.getDefaultLocale(), equalTo(DEFAULT_LOCALE));
    }

    private static class MockUserKeyService implements UserKeyService
    {
        private final Map<String, String> userNameToKey = Maps.newHashMap();

        @Override
        public String getUsernameForKey(final String key)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getKeyForUsername(final String username)
        {
            if (userNameToKey.containsKey(username))
            {
                return userNameToKey.get(username);
            }
            else
            {
                return username;
            }
        }

        @Override
        public String getKeyForUser(final User user)
        {
            return getKeyForUsername(user.getName());
        }

        private String map(User user)
        {
            final String key = IdentifierUtils.toLowerCase(user.getName());
            map(key, user);
            return key;
        }

        private MockUserKeyService map(String key, User user)
        {
            userNameToKey.put(user.getName(), key);
            return this;
        }
    }
}
