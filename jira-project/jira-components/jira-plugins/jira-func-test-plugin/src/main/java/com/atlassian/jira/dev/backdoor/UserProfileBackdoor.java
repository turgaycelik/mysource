package com.atlassian.jira.dev.backdoor;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Use this backdoor to manipulate User Profiles as part of setup for tests.
 *
 * This class should only be called by the {@code com.atlassian.jira.functest.framework.backdoor.UserProfileControl}.
 *
 * @since v5.0
 */
@Path ("userProfile")
public class UserProfileBackdoor
{
    private UserPreferencesManager userPreferencesManager;
    private UserUtil userUtil;
    private final AvatarService avatarService;
    private final AvatarManager avatarManager;

    public UserProfileBackdoor(UserPreferencesManager userPreferencesManager, UserUtil userUtil, AvatarService avatarService, AvatarManager avatarManager)
    {
        this.userPreferencesManager = userPreferencesManager;
        this.userUtil = userUtil;
        this.avatarService = avatarService;
        this.avatarManager = avatarManager;
    }

    @GET
    @AnonymousAllowed
    @Path("notificationType/set")
    public Response addGlobalPermission(@QueryParam ("username") String username,
            @QueryParam ("format") String format)
    {
        ApplicationUser user = userUtil.getUserByName(username);
        ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(user);
        try
        {
            preferences.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, format);
        }
        catch (AtlassianCoreException e)
        {
            throw new RuntimeException(e);
        }

        // Clear any caches, to ensure they are refreshed (defensive code - see UpdateUserPreferences)
        return Response.ok(null).build();
    }

    @PUT
    @AnonymousAllowed
    @Path("language")
    public void setUserLanguage(@QueryParam("username") String username, @QueryParam("language") String language)
            throws AtlassianCoreException
    {
        ApplicationUser user = userUtil.getUserByName(username);
        userPreferencesManager.getExtendedPreferences(user).setString(PreferenceKeys.USER_LOCALE, language);
    }

    /**
     * @since 5.2.2
     */
    @PUT
    @AnonymousAllowed
    @Path("autowatch")
    public Response setUserAutoWatch(@QueryParam("username") final String username,
            @QueryParam("autowatchvalue") final String autoWatch) throws AtlassianCoreException
    {
        final ApplicationUser user = userUtil.getUserByName(username);
        final ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(user);
        if (autoWatch == null || autoWatch.isEmpty())
        {
            if (preferences.containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED))
            {
                preferences.remove(PreferenceKeys.USER_AUTOWATCH_DISABLED);
            }
        }
        else
        {
            preferences.setBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED, !Boolean.parseBoolean(autoWatch));
        }
        return Response.ok().build();
    }

    @GET
    @AnonymousAllowed
    @Path("avatar/url")
    public Response getAvatarUrl(@QueryParam("username") final String username) throws AtlassianCoreException
    {
        return Response.ok(avatarService.getAvatarUrlNoPermCheck(userUtil.getUserByName(username), Avatar.Size.LARGE).toString()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("avatar/customIds")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getCustomAvatarIds(@QueryParam("username") final String username) throws AtlassianCoreException
    {
        List<Avatar> avatars = avatarManager.getCustomAvatarsForOwner(Avatar.Type.USER, userUtil.getUserByName(username).getKey());
        return Response.ok(Collections2.transform(avatars, new Function<Avatar, String>()
        {
            @Override
            public String apply(Avatar input)
            {
                return input.getId().toString();
            }
        }).toArray(new String[avatars.size()])).build();
    }
}
