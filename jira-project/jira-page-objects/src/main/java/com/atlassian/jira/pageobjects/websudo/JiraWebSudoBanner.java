package com.atlassian.jira.pageobjects.websudo;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.component.WebSudoBanner;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

/**
 *
 * @since v4.4
 */
public class JiraWebSudoBanner implements WebSudoBanner
{
    @Inject
    PageBinder pageBinder;

    @ElementBy(id = "websudo-banner")
    PageElement webSudoBanner;

    @ElementBy(id = "websudo-drop-from-protected-page")
    PageElement protectedDropWebSudoLink;

    @ElementBy(id = "websudo-drop-from-normal-page")
    PageElement normalDropWebSudoLink;

    @Override
    public boolean isShowing()
    {
        return webSudoBanner.isPresent() && webSudoBanner.isVisible();
    }

    @Override
    public String getMessage()
    {
        return isShowing() ? webSudoBanner.getText() : null;
    }

    @Override
    public <P extends Page> P dropWebSudo(Class<P> nextPage)
    {
        Preconditions.checkNotNull(nextPage, "Next page can not be null.");

        if (isShowing())
        {
            if (hasProdectedDropLink())
            {
                protectedDropWebSudoLink.click();
                Poller.waitUntilFalse(webSudoBanner.timed().isVisible());
                return pageBinder.bind(nextPage);
            }
            else
            {
                normalDropWebSudoLink.click();
                Poller.waitUntilFalse(webSudoBanner.timed().isVisible());
                return pageBinder.bind(nextPage);
            }
        }
        else
        {
            return pageBinder.navigateToAndBind(nextPage);
        }
    }

    public boolean hasProdectedDropLink()
    {
        return protectedDropWebSudoLink.isPresent();
    }

    public boolean hasNormalDropLink()
    {
        return normalDropWebSudoLink.isPresent();
    }
}
