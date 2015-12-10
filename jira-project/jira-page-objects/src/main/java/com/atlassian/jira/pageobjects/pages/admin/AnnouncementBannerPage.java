package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Author: Geoffrey Wong
 * JIRA Administration page to set the announcement banner for JIRA
 */
public class AnnouncementBannerPage extends AbstractJiraPage
{
    @ElementBy (id = "set_banner")
    PageElement setBanner;

    @ElementBy (name = "announcement")
    PageElement announcement;

    @ElementBy (id = "bannerVisibility_public")
    PageElement publicVisibilityOption;

    @ElementBy (id = "bannerVisibility_private")
    PageElement privateVisibilityOption;

    private String URI = "/secure/admin/user/EditAnnouncementBanner!default.jspa";

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return setBanner.timed().isPresent();
    }
    
    public String getCurrentAnnouncement()
    {
        return announcement.getValue();
    }
    
    public AnnouncementBannerPage fillNewAnnouncement(String newAnnouncement)
    {
        announcement.clear().type(newAnnouncement);
        return this;
    }

    public AnnouncementBannerPage selectPublicVisibility()
    {
        publicVisibilityOption.click();
        return this;
    }

    public AnnouncementBannerPage selectPrivateVisibility()
    {
        privateVisibilityOption.click();
        return this;
    }

    public AnnouncementBannerPage setAnnouncement()
    {
        setBanner.click();
        return this;
    }
}
