package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import java.util.Map;

/**
 * Very simple implementation that only renders the users full name with a link to the user's profile page. If the
 * username is null, it will display 'Anonymous'.  If no user matching the username can be found, ony the username will
 * be printed.
 *
 * @since v3.13
 */
public class ProfileLinkUserFormat implements UserFormat
{
    public static final String TYPE = "profileLink";

    private final AvatarService avatarService;
    private final I18nHelper i18nHelper;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserFormatModuleDescriptor moduleDescriptor;
    private final UserKeyService userKeyService;
    private final UserUtil userUtil;

    public ProfileLinkUserFormat(
            final AvatarService avatarService,
            final I18nHelper i18nHelper,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final UserFormatModuleDescriptor moduleDescriptor,
            final UserKeyService userKeyService,
            final UserUtil userUtil)
    {
        this.avatarService = avatarService;
        this.i18nHelper = i18nHelper;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.moduleDescriptor = moduleDescriptor;
        this.userKeyService = userKeyService;
        this.userUtil = userUtil;
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id)
    {
        final Map<String, Object> params = getInitialParams(key, id);
        return moduleDescriptor.getHtml(VIEW_TEMPLATE, params);
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id, final Map<String, Object> params)
    {
        final Map<String, Object> velocityParams = getInitialParams(key, id);
        velocityParams.putAll(params);
        return moduleDescriptor.getHtml(VIEW_TEMPLATE, velocityParams);
    }

    private Map<String, Object> getInitialParams(final String key, final String id)
    {
        ApplicationUser user = null;
        String username = null;
        String fullName = null;

        if (key != null)
        {
            user = userUtil.getUserByKey(key);
            if (user == null)
            {
                // Attempt to show the correct username even for deleted users
                username = userKeyService.getUsernameForKey(key);
                if (username == null)
                {
                    // Well, we need to show *something*...
                    username = key;
                }
                fullName = username;
            }
            else
            {
                username = user.getUsername();
                fullName = userUtil.getDisplayableNameSafely(user);
                if (!user.isActive())
                {
                    fullName += " (" + jiraAuthenticationContext.getI18nHelper().getText("admin.common.words.inactive") + ')';
                }
            }
        }

        final Avatar.Size avatarSize = Avatar.Size.SMALL;
        final String avatarURL = avatarService.getAvatarURL(jiraAuthenticationContext.getUser(), user, avatarSize).toString();

        return MapBuilder.<String, Object>newBuilder()
                .add("avatarURL", avatarURL)
                .add("avatarSize", avatarSize.getParam())
                .add("defaultFullName", i18nHelper.getText("common.words.anonymous"))
                .add("fullname", fullName)
                .add("id", id)
                .add("user", user)
                .add("username", username)
                .add("soyRenderer", getSoyRenderer())
                .toMutableMap();
    }

    public SoyTemplateRenderer getSoyRenderer()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(SoyTemplateRendererProvider.class).getRenderer();
    }
}
