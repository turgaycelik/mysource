package com.atlassian.sal.jira.features;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.user.UserKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestJiraDarkFeaturesManager
{
    private static final String FEATURE_KEY = "some.feature.key";
    private static final String USER_KEY = "foobar";
    private static final UserKey userKey = new UserKey(USER_KEY);

    @Mock private FeatureManager featureManager;
    @Mock private UserManager userManager;
    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private ApplicationUser applicationUser;

    @Test
    public void isFeatureEnabledForAllUsers()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.singleton(FEATURE_KEY), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertTrue(makeFeatureManager().isFeatureEnabledForAllUsers(FEATURE_KEY));
    }

    @Test
    public void isFeatureDisabledForAllUsers()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertFalse(makeFeatureManager().isFeatureEnabledForAllUsers(FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForCurrentUserSystemWide()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.singleton(FEATURE_KEY), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertTrue(makeFeatureManager().isFeatureEnabledForCurrentUser(FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForCurrentUserSiteWide()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.singleton(FEATURE_KEY), Collections.<String>emptySet()));
        assertTrue(makeFeatureManager().isFeatureEnabledForCurrentUser(FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForUserOnly()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.singleton(FEATURE_KEY)));
        assertTrue(makeFeatureManager().isFeatureEnabledForCurrentUser(FEATURE_KEY));
    }

    @Test
    public void isFeatureDisabledForUserOnly()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertFalse(makeFeatureManager().isFeatureEnabledForCurrentUser(FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForExistingUserOnly()
    {
        givenUserWithNameExisting();
        when(featureManager.getDarkFeaturesForUser(applicationUser)).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.singleton(FEATURE_KEY)));
        assertTrue(makeFeatureManager().isFeatureEnabledForUser(userKey, FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForExistingUserSiteWide()
    {
        givenUserWithNameExisting();
        when(featureManager.getDarkFeaturesForUser(applicationUser)).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.singleton(FEATURE_KEY), Collections.<String>emptySet()));
        assertTrue(makeFeatureManager().isFeatureEnabledForUser(userKey, FEATURE_KEY));
    }

    @Test
    public void isFeatureEnabledForExistingUserSystemWide()
    {
        givenUserWithNameExisting();
        when(featureManager.getDarkFeaturesForUser(applicationUser)).thenReturn(new DarkFeatures(Collections.singleton(FEATURE_KEY), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertTrue(makeFeatureManager().isFeatureEnabledForUser(userKey, FEATURE_KEY));
    }

    @Test
    public void isFeatureDisabledForExistingUser()
    {
        givenUserWithNameExisting();
        when(featureManager.getDarkFeaturesForUser(applicationUser)).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertFalse(makeFeatureManager().isFeatureEnabledForUser(userKey, FEATURE_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isFeatureEnabledWhenUserNotFound()
    {
        givenUserWithNameNotExisting();
        makeFeatureManager().isFeatureEnabledForUser(userKey, FEATURE_KEY);
    }

    @Test
    public void currentUserCanManageFeaturesForAllUsers()
    {
        when(featureManager.hasSiteEditPermission()).thenReturn(Boolean.TRUE);
        assertTrue(makeFeatureManager().canManageFeaturesForAllUsers());
    }

    @Test
    public void currentUserCannotManageFeaturesForAllUsers()
    {
        when(featureManager.hasSiteEditPermission()).thenReturn(Boolean.FALSE);
        assertFalse(makeFeatureManager().canManageFeaturesForAllUsers());
    }

    @Test
    public void enableFeatureForAllUsers()
    {
        makeFeatureManager().enableFeatureForAllUsers(FEATURE_KEY);
        verify(featureManager).enableSiteDarkFeature(FEATURE_KEY);
    }

    @Test
    public void disableFeatureForAllUsers()
    {
        makeFeatureManager().disableFeatureForAllUsers(FEATURE_KEY);
        verify(featureManager).disableSiteDarkFeature(FEATURE_KEY);
    }

    @Test(expected = IllegalStateException.class)
    public void enableFeatureForAnonymousThrowsException()
    {
        givenUserIsAnonymous();
        makeFeatureManager().enableFeatureForCurrentUser(FEATURE_KEY);
        verifyZeroInteractions(featureManager);
    }

    @Test
    public void enableFeatureForCurrentUser()
    {
        givenUserIsAuthenticated();
        makeFeatureManager().enableFeatureForCurrentUser(FEATURE_KEY);
        verify(featureManager).enableUserDarkFeature(applicationUser, FEATURE_KEY);
    }

    @Test
    public void enableFeatureForExistingUser()
    {
        givenUserWithNameExisting();
        makeFeatureManager().enableFeatureForUser(userKey, FEATURE_KEY);
        verify(featureManager).enableUserDarkFeature(applicationUser, FEATURE_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void enableFeatureForNotExistingUser()
    {
        givenUserWithNameNotExisting();
        makeFeatureManager().enableFeatureForUser(userKey, FEATURE_KEY);
        verifyZeroInteractions(featureManager);
    }

    @Test(expected = IllegalStateException.class)
    public void disableFeatureForAnonymousThrowsException()
    {
        givenUserIsAnonymous();
        makeFeatureManager().disableFeatureForCurrentUser(FEATURE_KEY);
    }

    @Test
    public void disableFeatureForCurrentUser()
    {
        givenUserIsAuthenticated();
        makeFeatureManager().disableFeatureForCurrentUser(FEATURE_KEY);
        verify(featureManager).disableUserDarkFeature(applicationUser, FEATURE_KEY);
    }

    @Test
    public void disableFeatureForExistingUser()
    {
        givenUserWithNameExisting();
        makeFeatureManager().disableFeatureForUser(userKey, FEATURE_KEY);
        verify(featureManager).disableUserDarkFeature(applicationUser, FEATURE_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disableFeatureForNonExistingUser()
    {
        givenUserWithNameNotExisting();
        makeFeatureManager().disableFeatureForUser(userKey, FEATURE_KEY);
    }

    @Test
    public void getEnabledFeaturesForCurrentUser()
    {
        when(featureManager.getDarkFeatures()).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertThat(makeFeatureManager().getFeaturesEnabledForAllUsers(), is(notNullValue()));
    }

    @Test
    public void getEnabledFeaturesForExistingUser()
    {
        givenUserWithNameExisting();
        when(featureManager.getDarkFeaturesForUser(applicationUser)).thenReturn(new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertThat(makeFeatureManager().getFeaturesEnabledForUser(userKey), is(notNullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllEnabledFeaturesForNonExistingUser()
    {
        givenUserWithNameNotExisting();
        makeFeatureManager().getFeaturesEnabledForUser(userKey);
    }

    private void givenUserIsAnonymous()
    {
        when(authenticationContext.getUser()).thenReturn(null);
    }

    private void givenUserIsAuthenticated()
    {
        when(authenticationContext.getUser()).thenReturn(applicationUser);
    }

    private void givenUserWithNameExisting()
    {
        when(userManager.getUserByKey(USER_KEY)).thenReturn(applicationUser);
    }

    private void givenUserWithNameNotExisting()
    {
        when(userManager.getUserByKey(USER_KEY)).thenReturn(null);
    }

    @Nonnull
    private JiraDarkFeaturesManager makeFeatureManager()
    {
        return new JiraDarkFeaturesManager(featureManager, userManager, authenticationContext);
    }
}
