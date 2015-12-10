package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.dbc.Assertions;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.mockito.Mockito.when;

public class JiraAuthenticationContextRule extends TestWatcher
{
    private final JiraAuthenticationContext authenticationContext;
    private final User user;
    @Nullable
    private final ApplicationUser applicationUser;

    /**
     * @param user
     * @deprecated Probably should not use user anymore
     */
    public JiraAuthenticationContextRule(@Nonnull final JiraAuthenticationContext authenticationContext, @Nullable final User user)
    {
        this.authenticationContext = Assertions.notNull(authenticationContext);
        this.user = user;
        this.applicationUser = null;
    }

    public JiraAuthenticationContextRule(@Nonnull final JiraAuthenticationContext authenticationContext, @Nullable final ApplicationUser applicationUser)
    {
        this.authenticationContext = Assertions.notNull(authenticationContext);
        this.applicationUser = applicationUser;
        this.user = applicationUser == null ? null : applicationUser.getDirectoryUser();
    }

    @Override
    protected void starting(@Nonnull final Description description)
    {
        updateAuthenticationContextForAnonymous(description);
        updateAuthenticationContextForAuthenticatedUser(description);
    }

    private void updateAuthenticationContextForAnonymous(@Nonnull final Description description)
    {
        final AnonymousUser anonymousUserAnnotation = description.getAnnotation(AnonymousUser.class);
        if (anonymousUserAnnotation != null) {
            when(authenticationContext.getLoggedInUser()).thenReturn(null);
            when(authenticationContext.getUser()).thenReturn(null);
        }
    }

    private void updateAuthenticationContextForAuthenticatedUser(@Nonnull final Description description)
    {
        final AuthenticatedUser authenticatedUserAnnotation = description.getAnnotation(AuthenticatedUser.class);
        if (authenticatedUserAnnotation != null) {
            when(authenticationContext.getLoggedInUser()).thenReturn(user);
            when(authenticationContext.getUser()).thenReturn(applicationUser);
        }
    }
}
