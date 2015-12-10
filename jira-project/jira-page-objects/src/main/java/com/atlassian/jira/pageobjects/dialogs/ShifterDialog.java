package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * @since v5.1
 */
public class ShifterDialog
{
    private static final By SHIFTER_DIALOG = By.id("shifter-dialog");
    private static final By QUERYABLE_CONTAINER = By.id("shifter-dialog-queryable-container");
    private static final By SUGGESTIONS = By.id("shifter-dialog-suggestions");
    private static final By ERROR_LOADING_ISSUE_MESSAGE = By.className("aui-list-item-li-error-loading-issue");

    @Inject private PageBinder binder;
    @Inject protected PageElementFinder locator;

    public AutoComplete getAutoComplete()
    {
        return binder.bind(QueryableDropdownSelect.class, QUERYABLE_CONTAINER, SUGGESTIONS);
    }

    /**
     * Types search string and selects the first suggestion.
     * Waits for shifter to hide.
     * @param search
     */
    public void queryAndSelect(String search)
    {
        getAutoComplete().query(search).getActiveSuggestion().click();
        Poller.waitUntilFalse("Wait until shifter is hidden", isOpenTimed());
    }

    /**
     * Types search string and selects the first suggestion.
     * Waits for shifter to hide and the dialog for the suggestion to open.
     * @param search
     */
    public <T> T queryAndSelect(String search, Class<T> dialogClass, Object... args)
    {
        queryAndSelect(search);
        return binder.bind(dialogClass, args);
    }

    public TimedCondition isOpenTimed()
    {
        return getDialogElement().timed().isPresent();
    }

    public boolean isOpen()
    {
        return getDialogElement().isPresent();
    }

    protected PageElement getDialogElement()
    {
        return locator.find(SHIFTER_DIALOG, TimeoutType.DIALOG_LOAD);
    }

    protected PageElement getMessage()
    {
        return locator.find(ERROR_LOADING_ISSUE_MESSAGE, TimeoutType.DIALOG_LOAD);
    }

    public boolean messageIsVisible()
    {
        return getMessage().isVisible();
    }

    public boolean isFooterHintVisible()
    {
        PageElement hintContainer = getDialogElement().find(By.className("hint-container"));

        return !hintContainer.getText().isEmpty();
    }
}
