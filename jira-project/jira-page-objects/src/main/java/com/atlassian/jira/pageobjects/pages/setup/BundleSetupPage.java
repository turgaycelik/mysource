package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Step in the JIRA setup process - select bundle.
 *
 * @since v6.3
 */
public class BundleSetupPage extends AbstractJiraPage
{
    @ElementBy (cssSelector = ".jira-setup-choice-box[data-choice-value=\"TRACKING\"]")
    private PageElement trackingSelection;

    @FindBy (name = "next")
    private WebElement submitButton;

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("You can't go to this page by entering URI");
    }

    @Override
    public TimedCondition isAt()
    {
        return trackingSelection.timed().isPresent();
    }

    public BundleSetupPage chooseTrackingBundle()
    {
        trackingSelection.click();
        return this;
    }

    public LicenseSetupPage submit()
    {
        Poller.waitUntilTrue(trackingSelection.timed().hasClass("jira-setup-choice-box-active"));

        submitButton.click();
        return pageBinder.bind(LicenseSetupPage.class);
    }

}
