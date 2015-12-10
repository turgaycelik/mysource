package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.FormMessages;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.TimedElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Represents a JIRA from dialog. These type of Dialogs basically slurp HTML from the server and display it on the
 * client side. Input is submitted to a back end action. This action will either indicate that the action was a
 * success or will return HTML with errors in it for display to the user.
 *
 * @since v4.4
 */
public class FormDialog extends JiraDialog
{
    public static final String FORM_SUBMITTING_CLASS = "submitting";
    protected PageElement loading;
    protected PageElement form;
    protected PageElement header;
    @Inject
    private Timeouts timeouts;

    protected FormMessages messages;

    public FormDialog(String id)
    {
        super(id);
    }

    public FormDialog() {}

    /**
     * Called when the dialog is first displayed. Waits util the dialog is ready to work with before exiting.
     */
    //Don't overwrite as you will probably end up calling the same method twice.
    @WaitUntil
    final public void ready()
    {
         waitUntilTrue(isOpen());
    }

    //Don't overwrite as you will probably end up calling the same method twice.
    @Init
    final public void initAbstractDialog()
    {
        super.initAbstractDialog();
        PageElement element = getDialogElement();
        form = element.find(By.tagName("form"));
        loading = element.find(By.cssSelector(String.format("form.%s", FORM_SUBMITTING_CLASS)));
        header = element.find(By.className(HEADING_AREA_CLASS));
        messages = binder.bind(FormMessages.class, element);
    }

    public AuiMessage getAuiMessage()
    {
        return binder.bind(AuiMessage.class, By.id(this.id));

    }

    /**
     * Do a submit on the passed element. The method will then wait for the response from the server before returning.
     *
     * @param pageElement the page element to submit.
     * @return true if the dialog is still open.
     */
    protected boolean submit(final PageElement pageElement)
    {
        assertDialogOpen();
        pageElement.click();
        waitWhileSubmitting();
        return isOpen().now();
    }

    /**
     * Do a submit on the passed element found using the passed locator. The method will then wait for the response
     * from the server before returning.
     *
     * @param locator for the element to select.
     * @return true if the dialog is still open.
     */
    protected boolean submit(By locator)
    {
        return submit(find(locator));
    }

    /**
     * Do a submit on the passed the form element with the passed name. The method will then wait for the response
     * from the server before returning.
     *
     * @param name the name of the element to submit.
     * @return true if the dialog is still open.
     */
    protected boolean submit(String name)
    {
        return submit(By.name(name));
    }

    /**
     * Closes the dialog by hitting ESC
     */
    public void escape()
    {
        if(isOpen().now())
        {
            locator.find(By.tagName("body")).type(Keys.ESCAPE);
        }
    }

    /**
     * Close the dialog by clicking on the kindly ever-present cancel button.
     */
    public void close()
    {
        if (isOpen().now())
        {
            getDialogElement().find(By.className("cancel")).click();
        }
    }

    public FormMessages messages()
    {
        return messages;
    }

    /**
     * Return true iff the dialog has error messages contained in its associated form.
     *
     * @return true iff the dialog has error messages contained in its associated form.
     */
    public boolean hasFormErrors()
    {
        return !getFormErrorList().isEmpty();
    }

    /**
     * Return a list of the form errors currently on the dialog.
     *
     * @return a list of form errors currently on the dialog.
     */
    public List<String> getFormErrorList()
    {
        assertDialogOpen();

        List<PageElement> all = form.findAll(By.cssSelector("div.error"));
        List<String> errors = Lists.newArrayListWithExpectedSize(all.size());
        for (PageElement element : all)
        {
            if (element.isVisible())
            {
                errors.add(StringUtils.stripToNull(element.getText()));
            }
        }
        return errors;
    }

    /**
     * Waits for at least one error to be visible
     * @return this
     */
    public FormDialog waitForFormErrors()
    {
        waitUntilTrue(form.find(By.tagName("div")).find(By.className("error")).timed().isVisible());
        return this;
    }

    /**
     * Return a mapping of the errors currently on the form. The mapping if from parameterName -> error.
     *
     * @return a mapping from parameterName -> error of all the errors currently on the form.
     */
    public Map<String, String> getFormErrors()
    {
        assertDialogOpen();

        Map<String, String> errors = Maps.newLinkedHashMap();
        List<PageElement> errorNodes = form.findAll(By.cssSelector("div.error"));

        for (PageElement errorNode : errorNodes)
        {
            errors.put(errorNode.getAttribute("data-field"), errorNode.getText().trim());
        }

        return errors;
    }

    public TimedQuery<Iterable<PageElement>> getFormErrorElements()
    {
        assertDialogOpen();
        return queryFactory.forSupplier(extendedFinder.within(form).newQuery(By.cssSelector("div.error")).supplier());
    }

    public String getTitle()
    {
        return header.getText();
    }

    public TimedQuery<String> getTimedTitle()
    {
        return header.timed().getText();
    }

    protected void assertDialogOpen()
    {
        assertTrue("Dialog is not open.", isOpen().now());
    }

    protected void assertDialogClosed()
    {
        assertFalse("Dialog is not closed.", isOpen().now());
    }

    protected void waitWhileSubmitting()
    {
        final TimedCondition timedCondition = loading.timed().hasClass(FORM_SUBMITTING_CLASS);
        Poller.waitUntil(timedCondition, Matchers.is(false),by(timeouts.timeoutFor(TimeoutType.AJAX_ACTION)));
    }

    protected void waitUntilClosed()
    {
        TimedElement timed = getDialogElement().timed();
        waitUntilFalse(and(timed.isPresent(), timed.isVisible()));
    }

    protected void waitUntilHidden()
    {
        waitUntilFalse(getDialogElement().timed().isVisible());
    }

    protected static void setElement(final PageElement element, final String value)
    {
        if (value != null)
        {
            element.clear();
            if (StringUtils.isNotBlank(value))
            {
                element.type(value);
            }
        }
    }

    @Override
    protected PageElement getDialogElement()
    {
        return locator.find(By.id(id), TimeoutType.DIALOG_LOAD);
    }
}
