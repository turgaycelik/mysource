package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.common.collect.Lists;

import java.util.List;

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * @since v5.0.1
 */
public class AssociateIssueOperationToScreenForm
{
    @ElementBy(id = "add-screen-scheme-item-form")
    private PageElement form;

    @ElementBy(id = "add-screen-scheme-item-form-submit")
    private PageElement formSubmit;

    @ElementBy(id = "add-screen-scheme-item-form-operation")
    private SelectElement issueOperationElement;

    @ElementBy(id = "add-screen-scheme-item-form-screen")
    private SelectElement screenElement;

    @ElementBy(cssSelector = "#add-screen-scheme-item-form .cancel")
    private PageElement cancelElement;

    @WaitUntil
    public void waitUtilReady()
    {
        Poller.waitUntilTrue(form.timed().isPresent());
    }

    List<String> getScreens()
    {
        return getOptionTexts(screenElement);
    }

    String getScreen()
    {
        return getOptionText(screenElement);
    }

    AssociateIssueOperationToScreenForm setScreen(String name)
    {
        screenElement.select(Options.text(name));
        return this;
    }

    List<ScreenOperation> getOperations()
    {
        final List<Option> allOptions = issueOperationElement.getAllOptions();
        final List<ScreenOperation> operators = Lists.newArrayListWithCapacity(allOptions.size());
        for (Option option : allOptions)
        {
            long id = parseLong(option.value());
            operators.add(ScreenOperation.fromOperationId(id));
        }
        return operators;
    }

    ScreenOperation getSelectedOperation()
    {
        final String value = issueOperationElement.getSelected().value();
        return ScreenOperation.fromOperationId(parseLong(value));
    }

    AssociateIssueOperationToScreenForm setOperation(ScreenOperation name)
    {
        issueOperationElement.select(Options.value(valueOf(name.getOperationId())));
        return this;
    }

    void submit()
    {
        formSubmit.click();
    }

    void cancel()
    {
        cancelElement.click();
    }

    public static String getOptionText(SelectElement element)
    {
        final Option selected = element.getSelected();
        return selected == null ? null : stripToNull(selected.text());
    }

    public static List<String> getOptionTexts(SelectElement element)
    {
        final List<Option> options = element.getAllOptions();
        if (options == null || options.isEmpty())
        {
            return Lists.newArrayList();
        }
        else
        {
            List<String> result = Lists.newArrayListWithCapacity(options.size());
            for (Option option : options)
            {
                final String optionText = stripToNull(option.text());
                if (optionText != null)
                {
                    result.add(option.text());
                }
            }
            return result;
        }
    }
}
