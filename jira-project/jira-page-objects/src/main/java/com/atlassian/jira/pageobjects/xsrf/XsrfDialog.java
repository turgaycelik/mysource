package com.atlassian.jira.pageobjects.xsrf;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.inject.Inject;

/**
 * Represents an XSRF form in a dialog.
 *
 * @since v5.0.2
 */
public class XsrfDialog extends FormDialog implements Xsrf
{
    @Inject
    private PageBinder pageBinder;

    private XsrfMessage message;
    
    @ElementBy (id = "xsrf-login-link")
    private PageElement loginLink;

    public XsrfDialog(String id)
    {
        super(id);
    }

    @Init
    public void init()
    {
        message = pageBinder.bind(XsrfMessage.class);
    }

    public boolean isSessionExpired()
    {
        return message.isSessionExpired();
    }
    
    public boolean isXsrfCheckFailed()
    {
        return message.isXsrfCheckFailed();
    }

    @Override
    public boolean hasParamaters()
    {
        return message.hasParamaters();
    }

    @Override
    public boolean hasRequestParameters()
    {
        return message.hasRequestParameters();
    }

    @Override
    public boolean hasRequestParameter(String parameterName)
    {
        return message.hasRequestParameter(parameterName);
    }

    @Override
    public boolean canRetry()
    {
        return message.canRetry();
    }

    @Override
    public <P> P retry(Class<P> page, Object... args)
    {
        message.retry();
        waitUntilClosed();
        return pageBinder.bind(page, args);
    }

    public JiraLoginPage login()
    {
        loginLink.click();
        return pageBinder.bind(JiraLoginPage.class);
    }
}
