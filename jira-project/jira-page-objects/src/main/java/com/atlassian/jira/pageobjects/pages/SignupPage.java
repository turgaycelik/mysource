package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

public class SignupPage extends AbstractJiraPage
{
    private static final String URL = "/secure/Signup!default.jspa";

    @ElementBy (id = "signup")
    private PageElement signupForm;

    @ElementBy (name = "fullname", within = "signupForm")
    private PageElement fullNameField;

    @ElementBy (name = "email", within = "signupForm")
    private PageElement emailField;

    @ElementBy (name = "username", within = "signupForm")
    private PageElement userNameField;

    @ElementBy (name = "password", within = "signupForm")
    private PageElement passwordField;

    @ElementBy (name = "confirm", within = "signupForm")
    private PageElement confirmPasswordField;

    @ElementBy (id = "signup-submit")
    private PageElement submitButton;


    @Override
    public TimedCondition isAt()
    {
        return signupForm.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URL;
    }

    public SignupPage fillFullName(final String fullName)
    {
        fullNameField.clear().type(fullName);
        return this;
    }

    public SignupPage fillEmail(final String fullName)
    {
        emailField.clear().type(fullName);
        return this;
    }

    public SignupPage fillUserName(final String userName)
    {
        userNameField.clear().type(userName);
        return this;
    }

    public SignupPage fillPassword(final String password)
    {
        passwordField.clear().type(password);
        return this;
    }

    public SignupPage fillConfirmPassword(final String password)
    {
        confirmPasswordField.clear().type(password);
        return this;
    }

    public SignupPage fillPasswordAndConfirm(final String password)
    {
        return fillPassword(password).fillConfirmPassword(password);
    }

    /**
     * Submits user data for signup. This methods DOES NOT ensure the signup process is successful.
     */
    public void submit()
    {
        submitButton.click();
    }

    public static class SignupBlocked extends AbstractJiraPage
    {

        @ElementBy (cssSelector = ".aui-message.error")
        private PageElement error;

        private static final Function<PageElement,String> TEXT_OF_ELEMENT = new Function<PageElement, String>()
        {
            @Override
            public String apply(final PageElement input)
            {
                return input.getText();
            }
        };

        private static final Predicate<String> NONEMPTY_STRING = new Predicate<String>()
        {
            public boolean apply(final String input)
            {
                return !input.isEmpty();
            }
        };

        @Override
        public TimedCondition isAt()
        {
            return error.timed().isPresent();
        }

        @Override
        public String getUrl()
        {
            return URL;
        }

        public String getErrorText()
        {
            final Iterable<String> errorTexts = Iterables.transform(error.findAll(By.tagName("p")), TEXT_OF_ELEMENT);
            return Joiner.on("\n").join(Iterables.filter(errorTexts, NONEMPTY_STRING));
        }
    }
}
