package com.atlassian.jira.avatar;

import java.net.URI;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar.Size;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.util.concurrent.LazyReference;

import com.google.common.base.Objects;
import com.opensymphony.module.propertyset.PropertySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

/**
 * Implementation of the AvatarService.
 *
 * @since v4.3
 */
public class AvatarServiceImpl implements AvatarService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AvatarServiceImpl.class);

    private final UserManager userManager;
    private final AvatarManager avatarManager;
    private final UserPropertyManager userPropertyManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final GravatarSettings gravatarSettings;
    @ClusterSafe
    private final LazyReference<Avatar.Size> defaultAvatarSize = new LazyReference<Avatar.Size>()
    {
        @Override
        protected Avatar.Size create() throws Exception
        {
            return Avatar.Size.defaultSize();
        }
    };

    public AvatarServiceImpl(UserManager userManager, AvatarManager avatarManager, UserPropertyManager userPropertyManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            GravatarSettings gravatarSettings)
    {
        this.userManager = userManager;
        this.avatarManager = avatarManager;
        this.userPropertyManager = userPropertyManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.gravatarSettings = gravatarSettings;
    }

    protected static ApplicationUser fromStaleUser(User user) {
        try {
            return ApplicationUsers.from(user);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    public Avatar getAvatar(User remoteUser, String username) throws AvatarsDisabledException
    {
        ApplicationUser user = userManager.getUserByName(username);
        if (user == null) {
            Avatar anonymousAvatar = getAnonymousAvatar();
            LOGGER.debug("User with key '{}' does not exist, using anonymous avatar id {}", username, anonymousAvatar != null ? anonymousAvatar.getId() : null);
            return anonymousAvatar;
        }
        return getAvatarImpl(fromStaleUser(remoteUser), false, user, false);
    }

    @Override
    public Avatar getAvatar(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException
    {
        return getAvatarImpl(remoteUser, false, avatarUser, false);
    }

    @Override
    public Avatar getAvatarTagged(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException
    {
        return getAvatarImpl(remoteUser, false, avatarUser, true);
    }

    private Avatar getAvatarImpl(ApplicationUser remoteUser, boolean skipPermissionCheck, ApplicationUser user, boolean tagged)
    {
        if (userManager.isUserExisting(user))
        {
            // try to use the configured avatar
            Long customAvatarId = configuredAvatarIdFor(user);
            if (customAvatarId != null)
            {
                Avatar avatar = tagged ? avatarManager.getByIdTagged(customAvatarId) : avatarManager.getById(customAvatarId);
                if (avatar != null && (skipPermissionCheck || canViewAvatar(remoteUser, avatar)))
                {
                    return avatar;
                }
            }

            // fall back to the default user avatar
            Avatar defaultAvatar = getDefaultAvatar();
            LOGGER.debug("Avatar not configured for user '{}', using default id {}", user.getUsername(), defaultAvatar != null ? defaultAvatar.getId() : null);

            return defaultAvatar;
        }

        Avatar anonymousAvatar = getAnonymousAvatar();
        LOGGER.debug("User is null, using anonymous avatar id {}", anonymousAvatar != null ? anonymousAvatar.getId() : null);

        return anonymousAvatar;
    }

    @Override
    public URI getAvatarURL(User remoteUser, String username) throws AvatarsDisabledException
    {
        return getAvatarURL(fromStaleUser(remoteUser), userManager.getUserByName(username));
    }

    @Override
    public URI getAvatarURL(ApplicationUser remoteUser, ApplicationUser avatarUser) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUser, defaultAvatarSize.get());
    }

    @Override
    public URI getAvatarURL(User remoteUser, String username, Avatar.Size size) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(fromStaleUser(remoteUser), false, userManager.getUserByName(username), size);
    }

    @Override
    public URI getAvatarURL(ApplicationUser remoteUser, ApplicationUser avatarUser, Avatar.Size size)
            throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUser, size);
    }

    @Override
    public URI getAvatarUrlNoPermCheck(String username, Avatar.Size size) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(null, true, userManager.getUserByName(username), size);
    }

    @Override
    public URI getAvatarUrlNoPermCheck(ApplicationUser avatarUser, Avatar.Size size) throws AvatarsDisabledException
    {
        return getAvatarURLImpl(null, true, avatarUser, size);
    }

    /**
     * Builds a URI for a JIRA avatar with the requested size.
     *
     * @param avatarUser the ApplicationUser whose avatar we'd like to display
     * @param avatar the Avatar whose URI we want
     * @param size the size in which the avatar should be displayed
     * @return a URI that can be used to display the avatar
     */
    public URI getAvatarUrlNoPermCheck(final ApplicationUser avatarUser, Avatar avatar, @Nonnull Size size)
    {
        if (useGravatarFor(avatar))
        {
            return new GravatarUrlStrategy().get(avatarUser, avatarUser, size);
        }

        return buildUriForAvatar(avatar, size, true);
    }

    @Override
    public URI getAvatarAbsoluteURL(User remoteUser, String username, Avatar.Size size)
            throws AvatarsDisabledException
    {
        return getAvatarURLImpl(fromStaleUser(remoteUser), false, userManager.getUserByName(username), size, true);
    }

    @Override
    public URI getAvatarAbsoluteURL(ApplicationUser remoteUser, ApplicationUser avatarUser, Avatar.Size size)
            throws AvatarsDisabledException
    {
        return getAvatarURLImpl(remoteUser, false, avatarUser, size, true);
    }

    private URI getAvatarURLImpl(ApplicationUser remoteUser, boolean skipPermissionCheck, ApplicationUser avatarUser, Avatar.Size size)
    {
        return getAvatarURLImpl(remoteUser, skipPermissionCheck, avatarUser, size, false);
    }

    private URI getAvatarURLImpl(ApplicationUser remoteUser, boolean skipPermissionCheck, ApplicationUser avatarUser, Avatar.Size size, boolean buildAbsoluteURL)
    {
        final boolean useGravatars = isUsingExternalAvatar(remoteUser, avatarUser);

        UrlStrategy urlStrategy = useGravatars ? new GravatarUrlStrategy() : new JiraUrlStrategy(skipPermissionCheck, buildAbsoluteURL);

        return urlStrategy.get(remoteUser, avatarUser, size != null ? size : defaultAvatarSize.get());
    }

    @Override
    public boolean hasCustomUserAvatar(User remoteUser, String username)
    {
        ApplicationUser user = userManager.getUserByName(username);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("User '%s' does not exist", username));
        }
        return hasCustomUserAvatar(fromStaleUser(remoteUser), user);
    }

    @Override
    public boolean hasCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser user)
    {
        return remoteUser != null && configuredAvatarIdFor(user) != null;
    }

    @Override
    public void setCustomUserAvatar(User remoteUser, String username, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException
    {
        ApplicationUser user = userManager.getUserByName(username);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("User '%s' does not exist", username));
        }
        setCustomUserAvatar(fromStaleUser(remoteUser), user, avatarId);
    }

    @Override
    public void setCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser user, Long avatarId)
            throws AvatarsDisabledException, NoPermissionException
    {
        if (!canSetCustomUserAvatar(remoteUser, user))
        {
            throw new NoPermissionException();
        }

        setConfiguredAvatarIdFor(user, avatarId);
    }

    @Override
    public boolean canSetCustomUserAvatar(User remoteUser, String username)
    {
        ApplicationUser user = userManager.getUserByName(username);
        if (user == null)
        {
            throw new IllegalArgumentException(String.format("User '%s' does not exist", username));
        }
        return canSetCustomUserAvatar(fromStaleUser(remoteUser), user);
    }

    @Override
    public boolean canSetCustomUserAvatar(ApplicationUser remoteUser, ApplicationUser user)
    {
        return avatarManager.hasPermissionToEdit(remoteUser, user);
    }

    @Override
    public URI getProjectAvatarURL(final Project project, final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
        return getProjectAvatarURLImpl(project, size, baseUrl);
    }

    @Override
    public URI getProjectAvatarAbsoluteURL(final Project project, final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        return getProjectAvatarURLImpl(project, size, baseUrl);
    }

    private URI getProjectAvatarURLImpl(final Project project, final Avatar.Size size, final String baseUrl)
    {
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + "/secure/projectavatar", applicationProperties.getEncoding(), false);

        if (size != null && !size.isDefault)
        {
            urlBuilder.addParameter("size", size.param);
        }

        urlBuilder.addParameter("pid", project.getId());

        // optional avatarId
        final Avatar avatar = project.getAvatar();
        Long avatarId = avatar != null ? avatar.getId() : null;
        if (avatarId != null)
        {
            urlBuilder.addParameter("avatarId", avatarId.toString());
        }

        return urlBuilder.asURI();
    }

    @Override
    public URI getProjectDefaultAvatarURL(final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
        return getProjectDefaultAvatarURLImpl(size, baseUrl);
    }

    @Override
    public URI getProjectDefaultAvatarAbsoluteURL(final Avatar.Size size)
    {
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        return getProjectDefaultAvatarURLImpl(size, baseUrl);
    }

    private URI getProjectDefaultAvatarURLImpl(final Avatar.Size size, final String baseUrl)
    {
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + "/secure/projectavatar", applicationProperties.getEncoding(), false);

        if (size != null && !size.isDefault)
        {
            urlBuilder.addParameter("size", size.param);
        }

        urlBuilder.addParameter("avatarId", avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT).toString());

        return urlBuilder.asURI();
    }

    /**
     * Returns the avatar id that is configured for the given User. If the user has not configured an avatar, this
     * method returns null.
     *
     * @param user the user whose avatar we want
     * @return an avatar id, or null
     * @see AvatarManager#getDefaultAvatarId(com.atlassian.jira.avatar.Avatar.Type)
     * @see com.atlassian.jira.avatar.AvatarManager#getAnonymousAvatarId()
     */
    protected Long configuredAvatarIdFor(ApplicationUser user)
    {
        PropertySet userProperties = userPropertyManager.getPropertySet(user);
        if (userProperties.exists(AvatarManager.USER_AVATAR_ID_KEY))
        {
            long avatarId = userProperties.getLong(AvatarManager.USER_AVATAR_ID_KEY);
            LOGGER.debug("Avatar configured for user '{}' is {}", user.getUsername(), avatarId);

            return avatarId;
        }

        return null;
    }

    /**
     * Returns true if Gravatar support is enabled.
     *
     * @return a boolean indicating whether Gravatar support is on
     */
    @Override
    public boolean isGravatarEnabled()
    {
        return gravatarSettings.isAllowGravatars();
    }

    @Override
    public boolean isUsingExternalAvatar(final ApplicationUser remoteUser, final ApplicationUser avatarUser)
    {
        if (isGravatarEnabled()) {
            Avatar avatar = getAvatarImpl(remoteUser, true, avatarUser, false);

            return useGravatarFor(avatar);
        }

        return false;
    }

    /**
     * Sets the given avatar id as the configured avatar id for a user.
     *
     * @param user the User whose avatar is being configured
     * @param avatarId the avatar id to configure
     */
    protected void setConfiguredAvatarIdFor(ApplicationUser user, Long avatarId)
    {
        PropertySet userProperties = userPropertyManager.getPropertySet(user);
        userProperties.setLong(AvatarManager.USER_AVATAR_ID_KEY, avatarId);
        LOGGER.debug("Set configured avatar id for user '{}' to {}", user.getUsername(), avatarId);
    }

    /**
     * Returns true if the passed in user has permission to view the passed in avatar. By definition, any user can view
     * the system avatars (e.g. avatars with no owner).
     *
     * @param user a User
     * @param avatar an Avatar
     * @return a boolean indicating whether the passed in user has permission to view the passed in avatar
     */
    protected boolean canViewAvatar(ApplicationUser user, Avatar avatar)
    {
        boolean hasPermission = avatarManager.hasPermissionToView(user != null ? user.getDirectoryUser() : null, avatar.getAvatarType(), avatar.getOwner());
        if (!hasPermission)
        {
            LOGGER.debug("User '{}' is not allowed to view avatar {}", user, avatar.getId());
        }

        return hasPermission;
    }

    /**
     * Returns the default avatar, if configured. Otherwise returns null.
     *
     * @return the default Avatar, or null
     */
    protected Avatar getDefaultAvatar()
    {
        Long defaultAvatarId = avatarManager.getDefaultAvatarId(Avatar.Type.USER);

        return defaultAvatarId != null ? avatarManager.getById(defaultAvatarId) : null;
    }

    /**
     * Returns the anonymous avatar, if configured. Otherwise returns null.
     *
     * @return the anonymous avatar, or null
     */
    protected Avatar getAnonymousAvatar()
    {
        Long anonAvatarId = avatarManager.getAnonymousAvatarId();

        return anonAvatarId != null ? avatarManager.getById(anonAvatarId) : null;
    }

    /**
     * Whether to use Gravatar instead of an internal Avatar.
     *
     * @param avatar an Avatar
     * @return true if JIRA should use a Gravatar instead of the given internal Avatar.
     */
    private boolean useGravatarFor(final Avatar avatar)
    {
        Avatar defaultAvatar = getDefaultAvatar();

        return isGravatarEnabled() && (avatar == null || Objects.equal(avatar, defaultAvatar));
    }

    /**
     * Builds a URI for a given JIRA avatar, with the requested size.
     *
     * @param avatar the Avatar whose URI we want
     * @param size the size in which the avatar should be displayed
     * @param absoluteUrl a boolean indicating whether to biuld an absolute URL
     * @return a URI that can be used to display the avatar
     */
    private URI buildUriForAvatar(Avatar avatar, @Nonnull Avatar.Size size, boolean absoluteUrl)
    {
        VelocityRequestContext jiraVelocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        String baseUrl = absoluteUrl ? jiraVelocityRequestContext.getCanonicalBaseUrl() : jiraVelocityRequestContext.getBaseUrl();
        UrlBuilder builder = new UrlBuilder(baseUrl + "/secure/useravatar", applicationProperties.getEncoding(), false);

        if (!size.isDefault)
        {
            builder.addParameter("size", size.param);
        }

        String ownerId = avatar != null ? avatar.getOwner() : null;
        if (ownerId != null)
        {
            builder.addParameter("ownerId", ownerId);
        }

        // optional avatarId
        Long avatarId = avatar != null ? avatar.getId() : null;
        if (avatarId != null)
        {
            builder.addParameter("avatarId", avatarId.toString());
        }

        return builder.asURI();
    }

    /**
     * Interface for avatar URL building strategy.
     */
    private interface UrlStrategy
    {
        URI get(ApplicationUser remoteUser, ApplicationUser user, @Nonnull Avatar.Size size);
    }

    /**
     * Build avatar URLs that point to JIRA avatars.
     */
    private class JiraUrlStrategy implements UrlStrategy
    {
        private final boolean skipPermissionCheck;
        private final boolean buildAbsoluteURL;

        public JiraUrlStrategy(boolean skipPermissionCheck, boolean buildAbsoluteURL)
        {
            this.skipPermissionCheck = skipPermissionCheck;
            this.buildAbsoluteURL = buildAbsoluteURL;
        }

        @Override
        public URI get(ApplicationUser remoteUser, ApplicationUser user, @Nonnull Avatar.Size size)
        {
            Avatar avatar = getAvatarImpl(remoteUser, skipPermissionCheck, user, false);

            return buildUriForAvatar(avatar, size, buildAbsoluteURL);
        }
    }

    /**
     * Build avatar URLs that point to Gravatar avatars. If the user does not exist, we serve the anonymous avatar
     * directly. If the user has not configured Gravatar, they will get the default avatar provided by Gravatar.
     */
    private class GravatarUrlStrategy implements UrlStrategy
    {
        private static final String HTTP_API = "http://www.gravatar.com/avatar/";
        private static final String HTTPS_API = "https://secure.gravatar.com/avatar/";

        @Override
        public URI get(ApplicationUser remoteUser, ApplicationUser user, @Nonnull Avatar.Size size)
        {
            // JRADEV-12195: email should not be null, but we have seen it in the wild (EAC/J connected to Crowd).
            if (user != null && user.getEmailAddress() != null)
            {
                // JRA-28913: Must use lower-case to get the correct hash
                final String hash = MD5Util.md5Hex(user.getEmailAddress().toLowerCase());
                final String apiAddress = firstNonNull(emptyToNull(gravatarSettings.getCustomApiAddress()), useSSL() ? HTTPS_API : HTTP_API);

                // JRA-29934: since September 2012, gravatar.com no longer handles the d= query parameter like it used
                // to, which breaks default avatars for non-publicly accessible JIRA instances. so we just use the
                // "mystery man" avatar provided by gravatar.com.
                return new UrlBuilder(apiAddress, applicationProperties.getEncoding(), false)
                        .addPath(hash)
                        .addParameter("d", "mm")
                        .addParameter("s", size.pixels.toString())
                        .asURI();
            }

            return buildUriForAvatar(getAnonymousAvatar(), size, true);
        }

        /**
         * @return whether we should use the Gravatar SSL servers or not
         */
        private boolean useSSL()
        {
            String baseURL = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            try
            {
                return "https".equalsIgnoreCase(URI.create(baseURL).getScheme());
            }
            catch (Exception e)
            {
                // base URL is messed up, nothing we can do about it here...
                return false;
            }
        }
    }
}
