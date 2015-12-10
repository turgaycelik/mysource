package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

/**
 * Gets an i18ned description of the custom content link admin page, differing by whether this is a stand alone instance
 */
public class ContentLinkAdminDescriptionProvider
{
    private I18nResolver i18nResolver;
    private ApplicationLinkService applicationLinkService;

    public ContentLinkAdminDescriptionProvider(I18nResolver i18nResolver, ApplicationLinkService applicationLinkService) {
        this.i18nResolver = i18nResolver;
        this.applicationLinkService = applicationLinkService;
    }
    public String getDescription() {
        return i18nResolver.getText(
                applicationLinkService.getApplicationLinks(ConfluenceApplicationType.class).iterator().hasNext()
                        ? "custom-content-links.page.description.withConfluence"
                        : "custom-content-links.page.description.withoutConfluence");
    }
}
