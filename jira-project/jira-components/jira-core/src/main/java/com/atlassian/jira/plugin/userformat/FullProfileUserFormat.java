package com.atlassian.jira.plugin.userformat;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.component.webfragment.ContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.ViewUserProfileContextLayoutBean;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import webwork.action.ActionContext;
import com.atlassian.jira.security.groups.GroupManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides the full user's profile that is used to display the column on the left in the View Profile page.
 *
 * @since v3.13
 */
public class FullProfileUserFormat implements UserFormat
{
    private final EmailFormatter emailFormatter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final GroupPermissionChecker groupPermissionChecker;
    private final WebFragmentWebComponent webFragmentWebComponent;
    private final UserManager userManager;
    private final UserKeyService userKeyService;
    private final UserFormatModuleDescriptor moduleDescriptor;
    private final UserPropertyManager userPropertyManager;
    private final GroupManager groupManager;

    public FullProfileUserFormat(final EmailFormatter emailFormatter, final PermissionManager permissionManager,
            final JiraAuthenticationContext authenticationContext, final GroupPermissionChecker groupPermissionChecker,
            final WebFragmentWebComponent webFragmentWebComponent, final UserManager userManager,
            final UserKeyService userKeyService, final UserFormatModuleDescriptor moduleDescriptor,
            final UserPropertyManager userPropertyManager, final GroupManager groupManager)
    {
        this.emailFormatter = emailFormatter;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.groupPermissionChecker = groupPermissionChecker;
        this.webFragmentWebComponent = webFragmentWebComponent;
        this.userManager = userManager;
        this.userKeyService = userKeyService;
        this.moduleDescriptor = moduleDescriptor;
        this.userPropertyManager = userPropertyManager;
        this.groupManager = groupManager;
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id)
    {
        ApplicationUser user = null;
        String username = null;

        if (key != null)
        {
            user = userManager.getUserByKey(key);
            username = getUsername(key, user);
        }

        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("username", username)
                .add("user", ApplicationUsers.toDirectoryUser(user))
                .add("action", this)
                .add("navWebFragment", webFragmentWebComponent)
                .add("id", id)
                .toMap();

        return moduleDescriptor.getHtml(UserFormat.VIEW_TEMPLATE, params);
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id, final Map params)
    {
        return format(key, id);
    }

    public boolean isUserLoggedinUser(final User user)
    {
        return user.equals(authenticationContext.getLoggedInUser());
    }

    public boolean isEmailVisible(final User user)
    {
        return emailFormatter.emailVisible(user);
    }

    private String getUsername(final String key, final ApplicationUser user)
    {
        if (user != null)
        {
            return user.getUsername();
        }
        final String name = userKeyService.getUsernameForKey(key);
        if (name != null)
        {
            return name;
        }
        return key;
    }

    @HtmlSafe
    public String getDisplayEmail(final User user)
    {
        return emailFormatter.formatEmailAsLink(user.getEmailAddress(), user);
    }

    public boolean hasViewGroupPermission(final String group)
    {
        return groupPermissionChecker.hasViewGroupPermission(group, authenticationContext.getLoggedInUser());
    }

    @HtmlSafe
    public String getUserLinks(final User profileUser, final String template)
    {
        final HttpServletRequest servletRequest = ActionContext.getRequest();
        final ContextLayoutBean userNavLayout = new ViewUserProfileContextLayoutBean(profileUser, (String) ActionContext.getValueStack().findValue(
            "/actionName"));
        // Set the context user as a request attribute so we can fish it out in the condition through the JiraHelper
        servletRequest.setAttribute(UserIsTheLoggedInUserCondition.PROFILE_USER, profileUser);
                final Map<String, Object> params = MapBuilder.<String, Object>build(UserIsTheLoggedInUserCondition.PROFILE_USER, profileUser);

        final JiraHelper helper = new JiraHelper(servletRequest, null, params);

        return webFragmentWebComponent.getHtml(template, "system.user.profile.links", helper, userNavLayout);
    }

    /**
     * These user properties are currently only visible to JIRA Administrators
     *
     * @param user The user to get properties for.
     * @return java.util.Map of user properties
     */
    public Map getUserProperties(final User user)
    {
        final Map<String, String> userProperties = Maps.newHashMap();
        if ((user != null) && isHasPermission(Permissions.ADMINISTER))
        {
            final PropertySet userPropertySet = userPropertyManager.getPropertySet(user);

            for (Object userPropertyKeyAsObject : userPropertySet.getKeys(PropertySet.STRING))
            {
                final String userPropertyKeyAsString = (String) userPropertyKeyAsObject;
                if (userPropertyKeyAsString.startsWith(UserUtil.META_PROPERTY_PREFIX))
                {
                    userProperties.put
                            (
                                    userPropertyKeyAsString.substring(UserUtil.META_PROPERTY_PREFIX.length()),
                                    userPropertySet.getString(userPropertyKeyAsString)
                            );
                }
            }
        }
        return userProperties;
    }

    public boolean isHasPermission(final int permissionsId)
    {
        return permissionManager.hasPermission(permissionsId, authenticationContext.getUser());
    }

    public List<String> getGroupNames(final User user)
    {
        final String username = (user == null) ? null : user.getName();

        if (username == null)
        {
            return Collections.emptyList();
        }

        final Collection<Group> groups = groupManager.getGroupsForUser(username);
        final List<String> groupNames = new ArrayList<String>(groups.size());
        for (final Group group : groups)
        {
            groupNames.add(group.getName());
        }

        return groupNames;
    }
}
