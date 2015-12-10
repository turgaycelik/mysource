package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.jira.testkit.client.log.FuncTestLoggerImpl;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

public class ViewApplinksPage implements Page
{
    private final FuncTestLoggerImpl logger = new FuncTestLoggerImpl(2);

    @Inject
    protected PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    private AddAppLinkDialog addApplinkDialog;

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/applinks/listApplicationLinks";
    }

    @Init
    public void initComponents()
    {
        addApplinkDialog = pageBinder.bind(AddAppLinkDialog.class);
        waitUntilFalse(elementFinder.find(By.className("links-loading")).timed().isVisible());
    }

    public ViewApplinksPage createTrustedAppLink(String localBaseUrl, String remoteBaseUrl, String username, String password)
    {
        return addApplinkDialog.open()
                .setServerUrlInput(remoteBaseUrl)
                .nextReciprocal()
                .setUsername(username)
                .setPassword(password)
                .setReciprocalRpcUrl(localBaseUrl)
                .next()
                .create();
    }

    public ViewApplinksPage createOAuthAppLink(String localBaseUrl, String remoteBaseUrl, String username, String password)
    {
        return addApplinkDialog.open()
                .setServerUrlInput(remoteBaseUrl)
                .nextReciprocal()
                .setUsername(username)
                .setPassword(password)
                .setReciprocalRpcUrl(localBaseUrl)
                .next()
                .differentUser()
                .create();
    }

    public ViewApplinksPage createAppLinkWithNoAuthConfigured(String localBaseUrl, String remoteBaseUrl, String username, String password)
    {
        return addApplinkDialog.open()
                .setServerUrlInput(remoteBaseUrl)
                .nextReciprocal()
                .setUsername(username)
                .setPassword(password)
                .setReciprocalRpcUrl(localBaseUrl)
                .next()
                .noTrust()
                .create();
    }

    public ViewApplinksPage deleteAllAppLinks()
    {
        final List<PageElement> deleteLinks = elementFinder.findAll(By.className("app-delete-link"));

        int index = 0;
        for (PageElement deleteLink : deleteLinks)
        {
            logger.log("Deleting application link (index=" + index + ")");
            pageBinder.bind(DeleteApplicationLinkDialog.class, deleteLink).open().confirm();
            logger.log("Application link deleted  (index=" + index + ")");
            index++;
        }


        return this;
    }
}
