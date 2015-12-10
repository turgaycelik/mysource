package it.com.atlassian.jira.webtest.pageobjects.applinks;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.components.ActivatedComponent;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the delete application link dialog.
 */
public class DeleteApplicationLinkDialog implements ActivatedComponent<DeleteApplicationLinkDialog>
{
    private final PageElement triggerElement;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy(id = "delete-application-link-dialog")
    private PageElement viewElement;

    public DeleteApplicationLinkDialog(PageElement triggerElement)
    {
        this.triggerElement = triggerElement;
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
    public DeleteApplicationLinkDialog open()
    {
        triggerElement.click();
        waitUntilTrue(viewElement.timed().isVisible());
        return this;
    }

    @Override
    public boolean isOpen()
    {
        return viewElement.isVisible();
    }

    public ViewApplinksPage confirm()
    {
        PageElement confirmButton = findVisibleBy(By.cssSelector("#delete-application-link-dialog .wizard-submit"), elementFinder);
        confirmButton.click();
        elementFinder.find(By.cssSelector(".success"), TimeoutType.AJAX_ACTION);
        return pageBinder.bind(ViewApplinksPage.class);
    }

    private static PageElement findVisibleBy(By by, PageElementFinder elementFinder)
    {
        Iterable<PageElement> elements = elementFinder.findAll(by);

        elements = Iterables.filter(elements, new Predicate<PageElement>()
        {
            public boolean apply(PageElement input)
            {
                return input.isVisible();
            }
        });

        if (Iterables.size(elements) == 0)
        {
            throw new IllegalStateException("can't find element by:" + by.toString());
        }

        if (Iterables.size(elements) > 1)
        {
            throw new IllegalStateException("there are more than one visible elements identified by:" + by.toString());
        }

        return Iterables.getOnlyElement(elements);
    }
}
