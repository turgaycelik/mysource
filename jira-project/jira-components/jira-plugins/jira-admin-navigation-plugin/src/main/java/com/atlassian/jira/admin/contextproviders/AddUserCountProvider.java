package com.atlassian.jira.admin.contextproviders;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class AddUserCountProvider implements ContextProvider
{
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory requestContextFactory;
    private final UserUtil userUtil;

    public AddUserCountProvider(JiraAuthenticationContext authenticationContext, VelocityRequestContextFactory requestContextFactory, UserUtil userUtil)
    {
        this.authenticationContext = authenticationContext;
        this.requestContextFactory = requestContextFactory;
        this.userUtil = userUtil;
    }

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> params)
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();

        MapBuilder<String, Object> builder = MapBuilder.newBuilder();

        final String baseUrl = requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
        final String viewLicense = baseUrl + "/secure/admin/ViewLicense!default.jspa";
        final String viewLicenseHref = String.format("<a href=\"%s\">", viewLicense);
        builder.add("limitWarningHtml", i18n.getText("admin.adduser.user.limit.warning", viewLicenseHref, "</a>"));

        builder.add("userCountHtml", i18n.getText("admin.userbrowser.how.many.users", String.valueOf(userUtil.getTotalUserCount()), String.valueOf(userUtil.getActiveUserCount())));

        builder.add("hasReachedUserLimit", !userUtil.canActivateNumberOfUsers(1));
        builder.toMap();
        return builder.toMap();
    }
}