package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.components.ActivatedComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the first page in add application link dialog.
 */
public class AddAppLinkDialog implements ActivatedComponent<AddAppLinkDialog>
{
    @Inject
    protected PageBinder pageBinder;

    @ElementBy(id = "add-application-link")
    private PageElement triggerElement;

    @ElementBy(cssSelector = "#add-application-link-dialog .step-1")
    private PageElement viewElement;

    @ElementBy(id = "application-url")
    private PageElement serverUrlInput;

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
    public AddAppLinkDialog open()
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

    public AddAppLinkDialog setServerUrlInput(String serverUrl)
    {
        serverUrlInput.clear().type(serverUrl);
        return this;
    }

    public AddAppLinkReciprocalDialog nextReciprocal()
    {
        return pageBinder.bind(AddAppLinkReciprocalDialog.class, serverUrlInput.getValue()).open();
    }
}
