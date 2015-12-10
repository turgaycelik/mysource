package com.atlassian.sal.jira.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.user.UserResolutionException;
import org.apache.commons.lang.StringUtils;
import webwork.util.URLCodec;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * User operations
 */
public class DefaultUserManager implements UserManager
{
    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CrowdService crowdService;
    private final AvatarService avatarService;
    private final com.atlassian.jira.user.util.UserManager userManager;

    public DefaultUserManager(final GlobalPermissionManager globalPermissionManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final CrowdService crowdService,
            AvatarService avatarService, final com.atlassian.jira.user.util.UserManager userManager)
    {
        this.globalPermissionManager = globalPermissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.crowdService = crowdService;
        this.avatarService = avatarService;
        this.userManager = userManager;
    }

    @Override
    public String getRemoteUsername()
    {
        final ApplicationUser user = jiraAuthenticationContext.getUser();
        if (user == null)
        {
            return null;
        }
        else
        {
            return user.getUsername();
        }
    }

    @Override
    public UserProfile getRemoteUser()
    {
        final ApplicationUser user = jiraAuthenticationContext.getUser();
        if (user == null)
        {
            return null;
        }
        else
        {
            return new JiraUserProfile(user);
        }
    }

    @Nullable
    @Override
    public UserKey getRemoteUserKey()
    {
        final ApplicationUser user = jiraAuthenticationContext.getUser();
        if (user == null)
        {
            return null;
        }
        else
        {
            return new UserKey(user.getKey());
        }
    }

    @Override
    public String getRemoteUsername(final HttpServletRequest request)
    {
        return getRemoteUsername();
    }

    @Override
    public UserProfile getRemoteUser(final HttpServletRequest httpServletRequest)
    {
        return getRemoteUser();
    }

    @Nullable
    @Override
    public UserKey getRemoteUserKey(final HttpServletRequest request)
    {
        return getRemoteUserKey();
    }

    @Override
    public boolean isSystemAdmin(final String username)
    {
        if (StringUtils.isNotEmpty(username))
        {
            final ApplicationUser user = userManager.getUserByName(username);
            return user != null && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
        }
        return false;
    }

    @Override
    public boolean isSystemAdmin(final UserKey userKey)
    {
        if (userKey != null)
        {
            final ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());
            return user != null && globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
        }

        return false;
    }

    @Override
    public boolean isAdmin(final String username)
    {
        if (StringUtils.isNotEmpty(username))
        {
            final ApplicationUser user = userManager.getUserByName(username);
            return user != null && globalPermissionManager.hasPermission(Permissions.ADMINISTER, user);
        }
        return false;
    }

    @Override
    public boolean isAdmin(final UserKey userKey)
    {
        if (userKey != null)
        {
            final ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());
            return user != null && globalPermissionManager.hasPermission(Permissions.ADMINISTER, user);
        }
        return false;
    }

    @Override
    public boolean authenticate(final String username, final String password)
    {
        try
        {
            return crowdService.authenticate(username, password) != null;
        }
        catch (FailedAuthenticationException e)
        {
            return false;
        }
    }

    @Override
    public Principal resolve(final String username) throws UserResolutionException
    {
        return crowdService.getUser(username);
    }

    @Override
    public Iterable<String> findGroupNamesByPrefix(String prefix, int startIndex, int maxResults)
    {
        return crowdService.search(getGroupQuery(prefix, startIndex, maxResults));
    }

    private GroupQuery<String> getGroupQuery(String prefix, int startIndex, int maxResults)
    {
        return new GroupQuery<String>(String.class,
                GroupType.GROUP,
                StringUtils.isBlank(prefix) ? NullRestrictionImpl.INSTANCE : Restriction.on(GroupTermKeys.NAME).startingWith(prefix),
                startIndex,
                maxResults);
    }

    /**
     * Returns whether the user is in the specify group
     *
     * @param username The username to check
     * @param groupName The group to check
     * @return True if the user is in the specified group
     */
    @Override
    public boolean isUserInGroup(final String username, final String groupName)
    {
        final User user = crowdService.getUser(username);
        final Group group = crowdService.getGroup(groupName);


        return user != null && group != null && crowdService.isUserMemberOfGroup(user, group);
    }

    @Override
    public boolean isUserInGroup(final UserKey userKey, final String groupName)
    {
        final ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());
        final Group group = crowdService.getGroup(groupName);

        return user != null && group != null && crowdService.isUserMemberOfGroup(user.getDirectoryUser(), group);
    }

    @Override
    public UserProfile getUserProfile(String username)
    {
        final ApplicationUser user = userManager.getUserByName(username);
        if (user != null)
        {
            return new JiraUserProfile(user);
        }
        return null;
    }

    @Override
    public UserProfile getUserProfile(final UserKey userKey)
    {
        if (userKey != null) {
            final ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());
            if (user != null)
            {
                return new JiraUserProfile(user);
            }
        }
        return null;
    }

    class JiraUserProfile implements UserProfile
    {
        private final ApplicationUser user;

        /**
         * @param user the application user
         */
        JiraUserProfile(final ApplicationUser user)
        {
            this.user = user;
        }

        @Override
        public UserKey getUserKey()
        {
            return new UserKey(user.getKey());
        }

        @Override
        public String getUsername()
        {
            return user.getUsername();
        }

        @Override
        public String getFullName()
        {
            return user.getDisplayName();
        }

        @Override
        public String getEmail()
        {
            return user.getEmailAddress();
        }

        @Override
        public URI getProfilePictureUri(int width, int height)
        {
            Avatar.Size theSize = Avatar.Size.biggerThan(Math.max(width,height));
            if (null == theSize)
            {
                return null;
            }
            else
            {
                return getProfilePictureUri(theSize);
            }
        }

        @Override
        public URI getProfilePictureUri()
        {
            return getProfilePictureUri(Avatar.Size.LARGE);
        }

        private URI getProfilePictureUri(Avatar.Size size)
        {
            final ApplicationUser remoteUser = jiraAuthenticationContext.getUser();
            return avatarService.getAvatarURL(remoteUser, user, size);
        }

        @Override
        public URI getProfilePageUri()
        {
            final String username = getUsername();
            if (username == null)
            {
                return null;
            }

            try
            {
                return new URI(String.format("/secure/ViewProfile.jspa?name=%s", URLCodec.encode(username, "UTF-8")));
            }
            catch (URISyntaxException e)
            {
                return null;
            }
            catch (UnsupportedEncodingException e)
            {
                return null;
            }
        }
    }
}
