package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.hamcrest.Matchers.equalTo;

/**
 * Step 4 in the JIRA setup process - administrator setup.
 *
 * @since v4.4
 */
public class AdminSetupPage extends AbstractJiraPage
{

    @ElementBy (cssSelector = "#jira-setupwizard h2")
    private PageElement formTitle;

    @FindBy (name = "username")
    private WebElement usernameField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(name = "confirm")
    private WebElement confirmPasswordField;

    @FindBy(name = "fullname")
    private WebElement fullNameField;

    @FindBy(name = "email")
    private WebElement emailField;

    @FindBy(id = "jira-setupwizard-submit")
    private WebElement submitButton;

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("You can't go to this page by entering URI");
    }

    @Override
    public TimedCondition isAt()
    {
        return formTitlePresent();
    }

    private TimedCondition formTitlePresent()
    {
        return Conditions.forMatcher(formTitle.timed().getText(), equalTo("Set Up Administrator Account"));
    }

    public AdminSetupPage setUsername(String username)
    {
        usernameField.sendKeys(username);
        return this;
    }

    public AdminSetupPage setPasswordAndConfirmation(String password)
    {
        setPassword(password);
        return setPasswordConfirmation(password);
    }

    public AdminSetupPage setPassword(String password)
    {
        passwordField.sendKeys(password);
        return this;
    }

    public AdminSetupPage setPasswordConfirmation(String confirmation)
    {
        confirmPasswordField.sendKeys(confirmation);
        return this;
    }

    public AdminSetupPage setFullName(String fullName)
    {
        fullNameField.sendKeys(fullName);
        return this;
    }

    public AdminSetupPage setEmail(String email)
    {
        emailField.sendKeys(email);
        return this;
    }

    public MailSetupPage submit()
    {
        submitButton.click();
        return pageBinder.bind(MailSetupPage.class);
    }

    // TODO error handling
}
