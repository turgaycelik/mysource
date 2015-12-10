package com.atlassian.jira.pageobjects.xsrf;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import org.apache.commons.lang.NotImplementedException;

/**
 * Represents the XSRF page. This is a page that can appear anywhere because of our XSRF magic. Because of this it is
 * not really a page.
 *
 * @since v5.0.1
 */
public class XsrfPage implements Xsrf, Page
{
    @Inject
    private PageBinder binder;

    private XsrfMessage message;

    @ElementBy (id = "xsrf-login-link")
    private PageElement loginLink;

    private final String urlToVisit;

    /**
     * Use this constructor when you need JIRA to follow a link which should take you to xsrf error page. This
     * method will be much quicker than to fail to bind a different page.
     *
     * @param urlToVisit
     */
    public XsrfPage(final String urlToVisit)
    {
        this.urlToVisit = urlToVisit;
    }

    public XsrfPage()
    {
        this(null);
    }

    @Init
    public void init()
    {
        message = binder.bind(XsrfMessage.class);
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
        return binder.bind(page, args);
    }

    public JiraLoginPage login()
    {
        loginLink.click();
        return binder.bind(JiraLoginPage.class);
    }

    @Override
    public String getUrl()
    {
        if (urlToVisit == null)
        {
            throw new NotImplementedException("I'm a page that can appear anywhere so I really don't have a URL.");
        }
        else
        {
            return urlToVisit;
        }
    }
}
