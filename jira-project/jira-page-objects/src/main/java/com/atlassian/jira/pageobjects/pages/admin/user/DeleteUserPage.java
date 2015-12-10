package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 4.4
 */
public class DeleteUserPage extends AbstractJiraPage
{
    private static String URI = "/secure/admin/user/DeleteUser.jspa";

    private static String ERROR_SELECTOR = ".aui-message.error ul li";

    @ElementBy (id = "user-cannot-delete-explain")
    private PageElement userCannotBeDeleteExplain;

    @FindBy (id = "user-errors")
    private WebElement errorsList;

    @FindBy (id = "user-warnings")
    private WebElement warningsList;

    @ElementBy (id = "delete_user_confirm-submit")
    private PageElement deleteUserButton;

    @FindBy (id = "delete_user_confirm-cancel")
    private WebElement cancelDeleteUserButton;

    private WebElement numberOfAssignedIssuesElement;
    private WebElement numberOfReportedIssuesElement;

    private Set<String> errors = new HashSet<String>();

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(deleteUserButton.timed().isPresent(), userCannotBeDeleteExplain.timed().isPresent());
    }

    public String getUrl()
    {
        return URI;
    }

    @Init
    public void parsePage()
    {
        //Check for errors on the page
        if (Check.elementExists(ByJquery.$(ERROR_SELECTOR), driver))
        {
            for (WebElement el : driver.findElements(ByJquery.$(ERROR_SELECTOR)))
            {
                errors.add(el.getText());
            }
        }

        numberOfAssignedIssuesElement = driver.findElement(ByJquery.$("li.user-errors:contains(Assigned Issue)").siblings("li"));
        numberOfReportedIssuesElement = driver.findElement(ByJquery.$("li.user-errors:contains(Reported Issue)").siblings("li"));
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public boolean hasError(String errorString)
    {
        return errors.contains(errorString);
    }

    public UserBrowserPage deleteUser()
    {
        deleteUserButton.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    public DeleteUserPage deleteUserExpectingError()
    {
        deleteUserButton.click();

        return pageBinder.bind(DeleteUserPage.class);
    }

    public int getDeleteUserButton()
    {
        return textAsInt(deleteUserButton);
    }

    public int getCancelDeleteUserButton()
    {
        return getIntValue(cancelDeleteUserButton);
    }

    public int getNumberOfAssignedIssuesElement()
    {
        return getIntValue(numberOfAssignedIssuesElement);
    }

    public int getNumberOfReportedIssuesElement()
    {
        return getIntValue(numberOfReportedIssuesElement);
    }

    private int getIntValue(WebElement element)
    {
        return Integer.parseInt(element.getText());
    }

    private int textAsInt(PageElement element)
    {
        return Integer.parseInt(element.getText());
    }
}
