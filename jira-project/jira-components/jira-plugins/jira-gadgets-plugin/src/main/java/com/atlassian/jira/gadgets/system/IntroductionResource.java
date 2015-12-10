package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.admin.IntroductionProperty;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to retrieve the introduction html.
 *
 * @since v4.0
 */
@Path("/intro")
@AnonymousAllowed
@Produces({MediaType.TEXT_HTML})
public class IntroductionResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final IntroductionProperty introductionProperty;
    private final ApplicationProperties applicationProperties;

    public IntroductionResource(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final VelocityRequestContextFactory velocityRequestContextFactory, IntroductionProperty introductionProperty, ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.introductionProperty = introductionProperty;
        this.applicationProperties = applicationProperties;
    }

    @GET
    public Response getIntro() throws Exception
    {
        String html = introductionProperty.getViewHtml();

        if (StringUtils.isBlank(html))
        {
            final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();

            StringBuilder builder = new StringBuilder();
            final String image = String.format("<img class=\"intro-logo\" height=\"64\" width=\"64\" src=\"%s\"/>", baseUrl + "/images/64jira.png");
            builder.append("<div class=\"intro\">");
            builder.append(image);
            final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
            final I18nHelper i18n = authenticationContext.getI18nHelper();
            if (isAdmin)
            {
                final HelpUtil.HelpPath jira101HelpPath = HelpUtil.getInstance().getHelpPath("jira101");
                final String jira101 = String.format("<a id=\"jira101\" alt=\"%s\" title=\"%s\" href=\"%s\">", jira101HelpPath.getAlt(), jira101HelpPath.getTitle(), jira101HelpPath.getUrl());
                final String jiraTraining = String.format("<a id=\"jiraTraining\" title=\"%s\" href=\"%s\">", i18n.getText("gadget.introduction.jira.training.title"), "http://www.atlassian.com/training/");

                final String editIntroHref = baseUrl + "/secure/admin/jira/EditApplicationProperties!default.jspa";

                builder.append("<h3>").append(i18n.getText("gadget.introduction.welcome.title", getInstanceTitle())).append("</h3>");

                builder.append("<p>");
                builder.append(i18n.getText("gadget.introduction.where.to.start", jira101, "</a>", jiraTraining, "</a>"));
                builder.append("</p>");

                builder.append("<p>");
                builder.append(i18n.getText("gadget.introduction.editintro", "<a id=\"edit-introduction\" href=\"" + editIntroHref + "\">", "</a>"));
                builder.append("</p>");
            }
            else
            {
                final HelpUtil.HelpPath introHelpPath = HelpUtil.getInstance().getHelpPath("introduction");
                final String link = String.format("<a id=\"%s\" href=\"%s\" alt=\"%s\" title=\"%s\">", "user-docs", introHelpPath.getUrl(), introHelpPath.getAlt(), introHelpPath.getTitle());
                builder.append("<h3>").append(i18n.getText("gadget.introduction.welcome.title", getInstanceTitle())).append("</h3>");

                builder.append("<p>");
                builder.append(i18n.getText("gadget.introduction.userguide", link, "</a>"));
                builder.append("</p>");
            }

            html = builder.toString();
        }

        return Response.ok(html).cacheControl(NO_CACHE).build();
    }

    private String getInstanceTitle()
    {
        return TextUtils.htmlEncode(applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE));
    }

}
