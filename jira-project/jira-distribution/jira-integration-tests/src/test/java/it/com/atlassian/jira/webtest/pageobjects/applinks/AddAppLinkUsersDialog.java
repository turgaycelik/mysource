package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.components.ActivatedComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the third page in add application link dialog.
 */
public class AddAppLinkUsersDialog implements ActivatedComponent<AddAppLinkUsersDialog>
{
    private final String serverUrl;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy(cssSelector = "#add-application-link-dialog h2.step-2-ual-header ~ div.dialog-button-panel .applinks-next-button")
    private PageElement triggerElement;

    @ElementBy(cssSelector = "#add-application-link-dialog .step-3")
    private PageElement viewElement;

    @ElementBy(cssSelector = "#add-application-link-dialog h2.step-3-header ~ div.dialog-button-panel .wizard-submit")
    private PageElement createButton;

    @ElementBy(cssSelector = "#add-application-link-dialog .different-user-radio-btn")
    private PageElement differentUserRadio;

    @ElementBy(cssSelector = "#add-application-link-dialog .no-trust-radio-btn")
    private PageElement noTrustRadio;

    public AddAppLinkUsersDialog(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }

    @Override
    public PageElement getTrigger()
    {
        return triggerElement;
    }

    @Override
    public PageElement getView()
    {
        return viewElement;
    }

    @Override
    public AddAppLinkUsersDialog open()
    {
        getTrigger().click();
        waitUntilTrue(viewElement.timed().isVisible());
        return this;
    }

    @Override
    public boolean isOpen()
    {
        return viewElement.isVisible();
    }

    public AddAppLinkUsersDialog differentUser()
    {
        differentUserRadio.select();
        return this;
    }

    public AddAppLinkUsersDialog noTrust()
    {
        noTrustRadio.select();
        return this;
    }

    public ViewApplinksPage create()
    {
        createButton.click();
        elementFinder.find(By.id("ual-row-" + serverUrl), TimeoutType.AJAX_ACTION);
        return pageBinder.bind(ViewApplinksPage.class);
    }
}
