package com.atlassian.jira.web.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.commons.lang.StringUtils;

/**
 * Serves avatar images for users.
 *
 * @since v4.2
 */
public class ViewUserAvatarServlet extends AbstractAvatarServlet
{
    @Override
    protected void defaultDoGet(HttpServletRequest request, HttpServletResponse response, String ownerId, Long avatarId, AvatarManager.ImageSize size)
            throws IOException, ServletException
    {
        final AvatarService avatarService = ComponentAccessor.getAvatarService();
        final ApplicationUser avatarUser = getUserUtil().getUserByName(ownerId);

        if (avatarService.isUsingExternalAvatar(getAuthenticationContext().getUser(), avatarUser))
        {
            redirectToExternalAvatar(response, ownerId, size);
            return;
        }

        super.defaultDoGet(request, response, ownerId, avatarId, size);
    }

    @Override
    protected Long validateInput(String ownerId, Long avatarId, final HttpServletResponse response) throws IOException
    {
        UserPropertyManager userPropertyManager = ComponentAccessor.getComponent(UserPropertyManager.class);
        if (StringUtils.isBlank(ownerId) && avatarId == null)
        {
            // no owner id or avatarId
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No avatar requested");
            return null;
        }
        else if (StringUtils.isNotBlank(ownerId))
        {
            final ApplicationUser user = getUserUtil().getUserByKey(ownerId);
            if (user == null)
            {
                //return the anonymous avatar if the user can't be found! Maybe the case for deleted users/anonymous users
                return Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_ANONYMOUS_USER_AVATAR_ID));
            }
            if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getUser(), user))
            {
                // no permission to see any avatar for this user. Simply return the default!
                avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
            }

            if (avatarId == null)
            {
                final PropertySet userPropertySet = userPropertyManager.getPropertySet(user);
                if (userPropertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
                {
                    avatarId = userPropertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY);
                }
                else
                {
                    avatarId = Long.parseLong(getApplicationProperties().getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID));
                }
            }
        }
        return avatarId;
    }

    private void redirectToExternalAvatar(HttpServletResponse response, String ownerId, AvatarManager.ImageSize size)
            throws IOException
    {
        AvatarService avatarService = ComponentAccessor.getAvatarService();
        if (!getAvatarManager().hasPermissionToView(getAuthenticationContext().getUser(), getUserUtil().getUserByKey(ownerId)))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ApplicationUser loggedInUser = getAuthenticationContext().getUser();
        URI gravatarURL = avatarService.getAvatarURL(loggedInUser, getUserUtil().getUserByKey(ownerId), size.getSize());

        response.sendRedirect(gravatarURL.toString());
    }

    @Override
    protected String getOwnerId(HttpServletRequest request)
    {
        String ownerId = super.getOwnerId(request);
        if (StringUtils.isBlank(ownerId)) {
            String username = StringUtils.trim(request.getParameter("username"));
            ApplicationUser user = getUserUtil().getUserByName(username);
            if (user != null) {
                ownerId = user.getKey();
            }
        }
        return ownerId;
    }

    @Override
    protected String getOwnerIdParamName()
    {
        return "ownerId";
    }

    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getApplicationProperties();
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }
}
