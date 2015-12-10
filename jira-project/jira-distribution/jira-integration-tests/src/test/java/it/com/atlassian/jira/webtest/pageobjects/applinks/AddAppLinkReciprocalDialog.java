package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.components.ActivatedComponent;
import com.atlassian.pageobjects.elements.CheckboxElement;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the second page in add application link dialog when the remote
 * side supports application links.
 */
public class AddAppLinkReciprocalDialog implements ActivatedComponent<AddAppLinkReciprocalDialog>
{
    private final String serverUrl;

    @Inject
    private PageBinder pageBinder;

    @ElementBy(cssSelector = "#add-application-link-dialog h2.step-1-header ~ div.dialog-button-panel .applinks-next-button")
    private PageElement triggerElement;

    @ElementBy(cssSelector = "#add-application-link-dialog .step-2-ual")
    PageElement viewElement;

    @ElementBy(cssSelector = "#add-application-link-dialog .create-reciprocal-link")
    CheckboxElement reciprocalLinkCheckbox;

    @ElementBy(cssSelector = "#add-application-link-dialog .reciprocal-link-username")
    PageElement usernameInput;

    @ElementBy(cssSelector = "#add-application-link-dialog .reciprocal-link-password")
    PageElement passwordInput;

    @ElementBy(cssSelector = "#add-application-link-dialog .reciprocal-rpc-url")
    PageElement reciprocalRpcUrlInput;

    public AddAppLinkReciprocalDialog(String serverUrl) {
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
    public AddAppLinkReciprocalDialog open()
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

    public AddAppLinkReciprocalDialog setReciprocalLinkEnabled(boolean enabled)
    {
        if (reciprocalLinkCheckbox.isSelected() != enabled)
        {
            reciprocalLinkCheckbox.click();
        }
        return this;
    }

    public AddAppLinkReciprocalDialog setUsername(String username)
    {
        usernameInput.clear().type(username);
        return this;
    }

    public AddAppLinkReciprocalDialog setPassword(String password)
    {
        passwordInput.clear().type(password);
        return this;
    }

    public AddAppLinkReciprocalDialog setReciprocalRpcUrl(String reciprocalRpcUrl)
    {
        reciprocalRpcUrlInput.clear().type(reciprocalRpcUrl);
        return this;
    }

    public AddAppLinkUsersDialog next()
    {
        return pageBinder.bind(AddAppLinkUsersDialog.class, serverUrl).open();
    }
}
