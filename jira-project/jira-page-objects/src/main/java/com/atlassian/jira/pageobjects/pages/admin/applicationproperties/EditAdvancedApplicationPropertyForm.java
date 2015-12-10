package com.atlassian.jira.pageobjects.pages.admin.applicationproperties;

import com.atlassian.jira.pageobjects.components.restfultable.AbstractEditRow;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * Represents an editable application property on the advanced configuration page
 *
 * @since v4.4
 */
public class EditAdvancedApplicationPropertyForm extends AbstractEditRow
{

    public EditAdvancedApplicationPropertyForm(final By rowSelector)
    {
        super(rowSelector);
    }

    public EditAdvancedApplicationPropertyForm setText(final String text)
    {
        PageElement textInput = findInRow("input.text");
        textInput.clear();
        textInput.type(text);
        return this;
    }

    public void cancel()
    {
        getCancelLink().click();
        waitUntilFalse(row.timed().hasClass("loading"));
    }


    public String getError()
    {
        return row.find(By.cssSelector(".error")).getText();
    }

    public void submit()
    {
        getAddButton().click();
        waitUntilFalse(row.timed().hasClass("loading"));
    }


}
