package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.MockApplicationProperties;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
public class TestBootstrapUserLocaleStore
{
    public static final Locale LOCALE = Locale.CHINESE;

    private MockApplicationProperties properties = new MockApplicationProperties().setDefaultLocale(LOCALE);
    private BootstrapUserLocaleStore store = new BootstrapUserLocaleStore(properties);

    private ApplicationUser appUser = new MockApplicationUser("appUser");
    private User user = new MockUser("user");

    @Test
    public void getUserAlwaysReturnsDefaultLocale()
    {
        assertThat(store.getLocale(appUser), equalTo(LOCALE));
        assertThat(store.getLocale(user), equalTo(LOCALE));
    }

    @Test
    public void getDefaultReturnsCorrectLocale()
    {
        assertThat(store.getDefaultLocale(), equalTo(LOCALE));
    }
}
