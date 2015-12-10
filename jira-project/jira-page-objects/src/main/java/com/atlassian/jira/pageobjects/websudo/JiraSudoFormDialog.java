package com.atlassian.jira.pageobjects.websudo;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * @since v5.0.1
 */
public class JiraSudoFormDialog extends FormDialog implements JiraWebSudo
{
    public static final String ID_SMART_WEBSUDO = "smart-websudo";

    private PageElement passwordElement;
    private PageElement submitElement;
    private PageElement cancelLink;

    public JiraSudoFormDialog()
    {
        this(ID_SMART_WEBSUDO);
    }

    public JiraSudoFormDialog(String id)
    {
        super(id);
    }

    @Init
    public void init()
    {
        passwordElement = findPasswordElement();
        submitElement = find(By.id("login-form-submit"));
        cancelLink = find(By.id("login-form-cancel"));
    }

    @WaitUntil
    public void waitForBind()
    {
        waitUntilTrue(findPasswordElement().timed().isPresent());
    }

    private PageElement findPasswordElement()
    {
        return find(By.id("login-form-authenticatePassword"));
    }

    public <T> T authenticate(String password, Class<T> targetPage, Object... args)
    {
        submitPassword(password);
        return binder.bind(targetPage, args);
    }

    @Override
    public void authenticate(String password)
    {
        submitPassword(password);
    }

    @Override
    public JiraWebSudo authenticateFail(String password)
    {
        return submitPassword(password);
    }

    private JiraWebSudo submitPassword(String password)
    {
        passwordElement.clear();
        if (StringUtils.isNotBlank(password))
        {
            passwordElement.type(password);
        }
        submit(submitElement);
        return this;
    }

    @Override
    public <T> T cancel(Class<T> expectedPage, Object... args)
    {
        cancelLink.click();
        return binder.bind(expectedPage, args);
    }

    @Override
    public void cancel()
    {
        cancelLink.click();
    }
}
