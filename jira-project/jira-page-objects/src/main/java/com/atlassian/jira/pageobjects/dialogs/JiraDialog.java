package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.util.TimedQueryFactory;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import java.util.List;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v6.0
 */
public class JiraDialog
{
    private static final Logger log = Logger.getLogger(JiraDialog.class);

    @Inject protected Timeouts timeouts;
    @Inject protected PageElementFinder locator;
    @Inject protected ExtendedElementFinder extendedFinder;
    @Inject protected TimedQueryFactory queryFactory;
    @Inject protected AtlassianWebDriver driver;
    @Inject protected PageBinder binder;

    public static final String DIALOG_CLASS = "jira-dialog";
    public static final String HEADING_AREA_CLASS = "jira-dialog-heading";
    public static final String CONTENT_AREA_CLASS = "jira-dialog-content";
    public static final String DIALOG_OPEN_CLASS = "jira-dialog-open";
    public static final String CONTENT_READY_CLASS = "jira-dialog-content-ready";

    protected String id;

    public JiraDialog(String id)
    {
        this.id = id;
    }

    public JiraDialog() {}

    @Init
    public void initAbstractDialog()
    {
    }

    /**
     * Check if the JIRA Dialog has been added to the DOM of the page.
     */
    protected TimedCondition isInDOM()
    {
        return getDialogElement().timed().isPresent();
    }

    /**
     * Check if the dialog's content has been loaded and is being displayed to the user.
     */
    public TimedCondition isOpen()
    {
        return getDialogElement().timed().hasClass(CONTENT_READY_CLASS);
    }

    /**
     * Check that the JIRA Dialog has been closed.
     */
    public TimedQuery<Boolean> isClosed()
    {
        // JIRA Dialogs get removed from the DOM when they are closed.
        return not(isInDOM());
    }

    protected PageElement getDialogElement()
    {
        final By selector;
        if (StringUtils.isEmpty(id))
        {
            log.warn("JIRA Dialog page object has no ID; will return the first '"+DIALOG_CLASS+"' element on the page. "
                    + "Expect weird results.");
            selector = By.className(DIALOG_CLASS);
        }
        else
        {
            selector = By.id(id);
        }
        return locator.find(selector, TimeoutType.DIALOG_LOAD);
    }

    public void waitUntilFinishedLoading()
    {
        final PageElement trobber = find(By.className("throbber"));
        if (!trobber.isPresent())
        {
            throw new RuntimeException("Loading indicator not present.");
        }
        waitUntilTrue(and(isOpen(), not(trobber.timed().hasClass("loading"))));
    }

    public PageElement find(By locator)
    {
        return getDialogElement().find(locator);
    }

    public <T extends PageElement> T find(final By locator, final Class<T> elementClass)
    {
        return getDialogElement().find(locator, elementClass);
    }

    public List<PageElement> findAll(By locator)
    {
        return getDialogElement().findAll(locator);
    }

    public <T extends PageElement> List<T> findAll(final By locator, final Class<T> elementClass)
    {
        return getDialogElement().findAll(locator, elementClass);
    }
}
