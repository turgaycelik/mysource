package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * @since v6.3
 */
public class LicenseBanner
{
    @Inject
    private TraceContext context;

    @ElementBy(id = "license-banner")
    private PageElement banner;

    @ElementBy(id = "license-banner-content")
    private PageElement content;

    public boolean isPresent()
    {
        return isPresentAndVisible(banner) && isPresentAndVisible(content);
    }

    public String getMacUrl()
    {
        final PageElement pageElement = content.find(By.id("license-banner-my-link"));
        if (isPresentAndVisible(pageElement))
        {
            return pageElement.getAttribute("href");
        }
        return null;
    }

    public String getSalesUrl()
    {
        final PageElement pageElement = content.find(By.id("license-banner-sales-link"));
        if (isPresentAndVisible(pageElement))
        {
            return pageElement.getAttribute("href");
        }
        return null;
    }

    public boolean canRemindLater()
    {
        if (isSubscription())
        {
            return isPresentAndVisible(getCloseIcon());
        }
        else
        {
            return isPresentAndVisible(getRemindMeLaterLink());
        }
    }

    public LicenseBanner remindLater()
    {
        if (!canRemindLater())
        {
            throw new IllegalStateException("Unable to remind later.");
        }

        clickElementAndWaitForBanner(isSubscription() ? getCloseIcon() : getRemindMeLaterLink(), "license-later-done");

        return this;
    }

    public boolean canRemindNever()
    {
        return isPresentAndVisible(getRemindMeNever());
    }

    public LicenseBanner remindNever()
    {
        if (!canRemindNever())
        {
            throw new IllegalStateException("Unable to remind never.");
        }

        clickElementAndWaitForBanner(getRemindMeNever(), "license-never-done");

        return this;
    }

    public int days()
    {
        if (content.isPresent())
        {
            return Integer.parseInt(content.getAttribute("data-days"));
        }
        else
        {
            return Integer.MIN_VALUE;
        }
    }

    public boolean isSubscription()
    {
        return content.isPresent() && Boolean.parseBoolean(content.getAttribute("data-subscription"));
    }

    private void clickElementAndWaitForBanner(final PageElement element, final String traceKey)
    {
        final Tracer checkpoint = context.checkpoint();

        element.click();

        //Wait until the REST call and animation is done.
        Poller.waitUntilTrue(context.condition(checkpoint, traceKey));
        //Wait until the animation is done.
        Poller.waitUntilFalse(content.timed().isPresent());
    }

    private PageElement getCloseIcon()
    {
        return banner.find(By.className("icon-close"));
    }

    private PageElement getRemindMeLaterLink()
    {
        return banner.find(By.id("license-banner-later"));
    }

    private PageElement getRemindMeNever()
    {
        return banner.find(By.id("license-banner-never"));
    }

    private static boolean isPresentAndVisible(final PageElement element)
    {
        return element.isPresent() && element.isVisible();
    }
}
