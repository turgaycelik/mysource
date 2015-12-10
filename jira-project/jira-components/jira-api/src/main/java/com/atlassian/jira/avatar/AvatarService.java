package com.atlassian.jira.avatar;

import java.net.URI;

import javax.annotation.Nonnull;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar.Size;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Service for manipulating {@link Avatar}'s.
 *
 * @since v4.3
 */
@PublicApi
public interface AvatarService
{
    /**
     * Returns the Avatar for the given user, if configured. If the user does not have a custom avatar, or if the
     * calling user does not have permission to view the Avatar, this method returns the default avatar. If the user
     * does not exist, this method returns the anonymous avatar.
     * <p/>
     * If this method would return the default user avatar but none is configured, or if this method would return the
     * anonymous avatar but none is configured, this method returns null.
     *
     * @param remoteUser the User that wants to view an Avatar
     * @param username a String containing a username (may have been deleted)
     * @return an Avatar, or null
     * @throws AvatarsDisabledException if avatars are disabled
     * @deprecated Use {@link #getAvatar(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser)}. Since v6.0
     * @since v4.3
     */
    Avatar getAvatar(User remoteUser, String username) throws AvatarsDisabledException;


    /**
     * Returns the Avatar for the given user, if configured. If the user does not have a custom avatar, or if the
     * calling user does not have permission to view the Avatar, this method returns the default avatar. If the user
     * does not exist, this method returns the anonymous avatar.
     * <p/>
     * If this method would return the default user avatar but none is configured, or if this method would return the
     * anonymous avatar but none is configured, this method returns null.
     *
     * @param remoteUser the User that wants to view an Avatar
     * @param avatarUser the User that the avatar will be returned for (if null it will return a default avatar if it's set or null)
     * @return an Avatar, or null
     * @throws AvatarsDisabledException if avatars are disabled
     * @since v6.0
     */
    Avatar getAvatar(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException;


    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser. This method returns a URL for an avatar with the default size.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username a String containing a username (may have been deleted)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @deprecated Use {@link #getAvatarURL(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser)}. Since v6.0
     * @since v5.0.3
     */
    URI getAvatarURL(User remoteUser, String username) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser. This method returns a URL for an avatar with the default size.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param avatarUser a User object to get the avatar for (if null, the default avatar is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @since v6.0
     */
    URI getAvatarURL(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username a String containing a username (may have been deleted)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     * @deprecated Use {@link #getAvatarURL(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.avatar.Avatar.Size)}. Since v6.0
     * @since v4.3
     */
    URI getAvatarURL(User remoteUser, String username, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the user having the given username for displaying on a page that should be
     * shown for the passed in remoteUser.
     * <p/>
     * If the user does not have a custom avatar, or if the calling user does not have permission to view the Avatar,
     * this method returns the URL of the default avatar. If the user does not exist, this method returns the URL of the
     * anonymous avatar.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param avatarUser a User to get the avatar for (if null, the default avatar is used)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     * @since v6.0
     */
    URI getAvatarURL(ApplicationUser remoteUser, ApplicationUser avatarUser, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String,
     * com.atlassian.jira.avatar.Avatar.Size)} but does no permission checking.
     *
     * @param username a String containing a username (may have been deleted)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @deprecated Use {@link #getAvatarUrlNoPermCheck(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.avatar.Avatar.Size)}. Since v6.0
     * @since v5.0
     */
    URI getAvatarUrlNoPermCheck(String username, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String,
     * com.atlassian.jira.avatar.Avatar.Size)} but does no permission checking.
     *
     * @param avatarUser a String containing a username (if null, the default avatar is used)
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     *
     * @since v6.0
     */
    URI getAvatarUrlNoPermCheck(ApplicationUser avatarUser, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * Returns the URL for an avatar.
     *
     * @param applicationUser
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     * @throws AvatarsDisabledException if avatars are disabled
     * @see #getAvatar(com.atlassian.crowd.embedded.api.User, String)
     * @since v6.3
     */
    URI getAvatarUrlNoPermCheck(ApplicationUser applicationUser, Avatar avatar, @Nonnull Size size) throws AvatarsDisabledException;

    /**
     * This is the same as
     * {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String, com.atlassian.jira.avatar.Avatar.Size)}
     * but returns an absolute URL.
     *
     * @deprecated Use {@link #getAvatarAbsoluteURL(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.avatar.Avatar.Size)}. Since v6.0
     */
    URI getAvatarAbsoluteURL(User remoteUser, String username, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * This is the same as
     * {@link #getAvatarURL(com.atlassian.crowd.embedded.api.User, String, com.atlassian.jira.avatar.Avatar.Size)}
     * but returns an absolute URL.
     * @since v6.0
     */
    URI getAvatarAbsoluteURL(ApplicationUser remoteUser, ApplicationUser avatarUser, Avatar.Size size) throws AvatarsDisabledException;

    /**
     * Returns the URL for the avatar of the given project.
     * <p/>
     * If running in the context of a web request, this will return a URL relative to the server root (ie "/jira/...").
     * Otherwise, it will return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param project the Project of which to get the avatar URL
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectAvatarURL(Project project, Avatar.Size size);

    /**
     * Returns the URL for the avatar of the given project.
     * <p/>
     * This will always return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param project the Project of which to get the avatar URL
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectAvatarAbsoluteURL(Project project, Avatar.Size size);

    /**
     * Returns the URL for the default avatar of a project.
     * <p/>
     * If running in the context of a web request, this will return a URL relative to the server root (ie "/jira/...").
     * Otherwise, it will return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectDefaultAvatarURL(Avatar.Size size);

    /**
     * Returns the URL for the default avatar of a project.
     * <p/>
     * This will always return an absolute URL (eg. "http://example.com/jira/...").
     *
     * @param size the size of the avatar to be displayed (if null, the default size is used)
     * @return a URL that can be used to display the avatar
     */
    URI getProjectDefaultAvatarAbsoluteURL(Avatar.Size size);

    /**
     * Returns true if the user has configured a custom avatar, false otherwise.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will check
     * @return a boolean indicating whether the given user has configued a custom avatar
     *
     * @deprecated Use {@link #hasCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser username)}. Since v6.0
     * @since v5.0.3
     */
    @ExperimentalApi
    boolean hasCustomUserAvatar(User remoteUser, String username);

    /**
     * Returns true if the user has configured a custom avatar, false otherwise.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will check
     * @return a boolean indicating whether the given user has configued a custom avatar
     *
     * @since v6.0
     */
    @ExperimentalApi
    boolean hasCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser username);

    /**
     * Returns true if Gravatar support is enabled.
     *
     * @return a boolean indicating whether Gravatar support is on
     */
    @ExperimentalApi
    public boolean isGravatarEnabled();

    /**
     * Check whether a user currently has an external avatar (for example, a Gravatar). Use {@link #getAvatarURL(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser)}
     * to get the avatar URL.
     *
     * @return a boolean indicating whether the given currently user has an external avatar
     * @since JIRA 6.3
     */
    public boolean isUsingExternalAvatar(final ApplicationUser remoteUser, ApplicationUser avatarUser);

    /**
     * Sets a custom avatar for a given user.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will configure
     * @param avatarId the id of the avatar to configure for the user
     * @throws AvatarsDisabledException if avatars are disabled
     * @throws NoPermissionException if the remote user does not have permission to update the given user's avatar
     *
     * @see #canSetCustomUserAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @deprecated Use {@link #setCustomUserAvatar(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser, Long)}. Since v6.0
     * @since v5.0.3
     */
    @ExperimentalApi
    void setCustomUserAvatar(User remoteUser, String username, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException;


    /**
     * Sets a custom avatar for a given user.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param user the user whose avatar we will configure
     * @param avatarId the id of the avatar to configure for the user
     * @throws AvatarsDisabledException if avatars are disabled
     * @throws NoPermissionException if the remote user does not have permission to update the given user's avatar
     *
     * @see #canSetCustomUserAvatar(com.atlassian.crowd.embedded.api.User, String)
     *
     * @since v6.0
     */
    @ExperimentalApi
    void setCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser user, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException;

    /**
     * Returns a boolean indicating whether the calling user can edit the custom user avatar for the user with the given
     * username.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param username the username of the user whose avatar we will configure
     * @return a indicating whether the calling user can edit the custom user avatar for another user
     *
     * @deprecated Use {@link #canSetCustomUserAvatar(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.user.ApplicationUser)}. Since v6.0
     * @since v5.0.3
     */
    @ExperimentalApi
    boolean canSetCustomUserAvatar(User remoteUser, String username);

    /**
     * Returns a boolean indicating whether the calling user can edit the custom user avatar for the user with the given
     * username.
     *
     * @param remoteUser a User object for the currently logged in user
     * @param user the user whose avatar we will configure
     * @return a indicating whether the calling user can edit the custom user avatar for another user
     *
     * @since v6.0
     */
    @ExperimentalApi
    boolean canSetCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser user);

    Avatar getAvatarTagged(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException;
}
