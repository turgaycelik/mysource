package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.admin.AnnouncementBanner;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class EditAnnouncementBanner extends JiraWebActionSupport
{
    public static final String ANNOUNCEMENT_PREVIEW = "announcement_preview_banner_st";

    private String announcement;
    private String bannerVisibility;
    private ApplicationProperties applicationProperties;
    private final AnnouncementBanner announcementBanner;

    public static final String PUBLIC_BANNER = "public";
    public static final String PRIVATE_BANNER = "private";

    public EditAnnouncementBanner(ApplicationProperties applicationProperties, AnnouncementBanner announcementBanner)
    {
        this.applicationProperties = applicationProperties;
        this.announcementBanner = announcementBanner;
    }

    public String doDefault() throws Exception
    {
        String preview = request.getParameter(ANNOUNCEMENT_PREVIEW);
        announcement = (preview == null) ? applicationProperties.getDefaultBackedText(APKeys.JIRA_ALERT_HEADER) : preview;
        this.bannerVisibility = applicationProperties.getDefaultBackedString(APKeys.JIRA_ALERT_HEADER_VISIBILITY);
        return INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        applicationProperties.setText(APKeys.JIRA_ALERT_HEADER, announcement);
        applicationProperties.setString(APKeys.JIRA_ALERT_HEADER_VISIBILITY, bannerVisibility);
        return INPUT;
    }

    public AnnouncementBanner getAnnouncementBanner()
    {
        return announcementBanner;
    }

    public Collection<TextOption> getVisibilityModes()
    {
        return ImmutableList.of
                (
                        new TextOption(PUBLIC_BANNER, getText("admin.menu.optionsandsettings.announcement.banner.visibility.public")),
                        new TextOption(PRIVATE_BANNER, getText("admin.menu.optionsandsettings.announcement.banner.visibility.private"))
                );
    }

    public String getAnnouncement()
    {
        return announcement;
    }

    public void setAnnouncement(String announcement)
    {
        this.announcement = announcement;
    }

    public boolean isPreview()
    {
        return request.getParameter(ANNOUNCEMENT_PREVIEW) != null;
    }

    public String getBannerVisibility()
    {
        return bannerVisibility;
    }

    public void setBannerVisibility(String bannerVisibility)
    {
        this.bannerVisibility = bannerVisibility;
    }
}