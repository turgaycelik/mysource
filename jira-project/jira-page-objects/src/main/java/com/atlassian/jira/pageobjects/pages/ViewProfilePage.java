package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * @since v5.1
 */
public class ViewProfilePage extends AbstractJiraPage implements TabPage<ViewProfileTab>
{
    @ElementBy(id = "up-user-title")
    private PageElement userTitle;

    @ElementBy(id = "user_avatar_image")
    private PageElement userAvatarImage;

    @ElementBy(id = "edit_profile_lnk")
    private PageElement editLink;
    
    @ElementBy(id = "filters")
    private PageElement filtersLink;

    @ElementBy(id = "activity-profile-fragment")
    private PageElement activityStream;

    @ElementBy(id = "details-profile-fragment")
    private PageElement profileDetails;

    @ElementBy(id = "preferences-profile-fragment")
    private PageElement profilePreferences;

    @ElementBy(id = "up-p-mimetype")
    private PageElement mimeType;

    @ElementBy(id="user_profile_tabs")
    PageElement profileTabs;

    @Override
    public TimedCondition isAt()
    {
        return userTitle.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/ViewProfile.jspa";
    }

    public UserAvatarDialog userAvatar()
    {
        userAvatarImage.click();
        return pageBinder.bind(UserAvatarDialog.class);
    }

    public EditProfilePage edit()
    {
        editLink.click();
        return pageBinder.bind(EditProfilePage.class);
    }

    /**
     * @since v6.2
     * @return true if the filters button is presented on the profile page
     */
    public boolean hasFilters()
    {
        return filtersLink.isPresent();
    }

    /**
     * since v6.2
     * @return true if the activity stream panel is presented on the profile page
     */
    public boolean hasActivityStream()
    {
        return activityStream.isPresent();
    }

    /**
     * @since v6.2
     * @return true if the user profile panel is presented on the profile page
     */
    public boolean hasProfileDetails()
    {
        return profileDetails.isPresent();
    }

    /**
     * @since v6.2
     * @return true if the profile preferences panel is presented on the profile page
     */
    public boolean hasProfilePreferences()
    {
        return profilePreferences.isPresent();
    }

    public PageElement getMimeType()
    {
        return mimeType;
    }

    public UserSummaryTab openSummaryTab()
    {
        return openTab(UserSummaryTab.class);
    }

    @Override
    public <T extends ViewProfileTab> T openTab(final Class<T> tabClass)
    {
        final T tab = pageBinder.delayedBind(tabClass).inject().get();
        profileTabs.find(By.id(tab.linkId())).click();
        Poller.waitUntilTrue(tab.isOpen());
        assertCorrectUrl(tab.getUrlPart());
        return tab;
    }

    public <T extends ViewProfileTab> T goBackTo(final Class<T> tabClass)
    {
        final T tab = pageBinder.delayedBind(tabClass).inject().get();
        driver.navigate().back();
        Poller.waitUntilTrue(tab.isOpen());
        assertCorrectUrl(tab.getUrlPart());
        return tab;
    }

    private void assertCorrectUrl(final String linkPart)
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(final WebDriver webDriver)
            {
                return webDriver.getCurrentUrl().contains(linkPart);
            }
        });
    }

    @Override
    public boolean hasTab(final Class<? extends ViewProfileTab> tabClass)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
