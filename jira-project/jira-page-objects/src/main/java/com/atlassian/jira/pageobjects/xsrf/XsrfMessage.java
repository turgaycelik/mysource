package com.atlassian.jira.pageobjects.xsrf;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * Represents the XSRF message block.
 *
 * @since v5.0.1
 */
public class XsrfMessage
{
    @Inject
    private PageBinder binder;

    @ElementBy(cssSelector = ".xsrf-session-expired")
    private PageElement sessionExpired;

    @ElementBy(cssSelector = ".xsrf-check-failed")
    private PageElement xsrfCheckFailed;

    @ElementBy (id = "xsrf-error")
    private PageElement container;

    @ElementBy (id = "xsrf-no-params")
    private PageElement noParamsMsg;

    @ElementBy (className = "request-parameters")
    private PageElement requestParameters;

    @ElementBy (id = "atl_token_retry_button")
    private PageElement retryButton;
    
    boolean hasParamaters()
    {
        return !noParamsMsg.isPresent();
    }

    boolean hasRequestParameters()
    {
        return requestParameters.isPresent();
    }

    boolean hasRequestParameter(String parameterName)
    {
        return requestParameters.find(By.xpath("//dt[contains(text(), '" + parameterName + "')]")).isPresent();
    }

    boolean canRetry()
    {
        return retryButton.isPresent();
    }

    void retry()
    {
        assertTrue("The retry form does not appear to be present.", canRetry());
        retryButton.click();
    }
    
    @WaitUntil
    public void isAt()
    {
        Poller.waitUntilTrue(container.timed().isPresent());
    }
    
    public boolean isSessionExpired()
    {
        return sessionExpired.isPresent();
    }
    
    public boolean isXsrfCheckFailed()
    {
        return xsrfCheckFailed.isPresent();
    }
}
