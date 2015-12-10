package com.atlassian.jira.pageobjects.websudo;

/**
 * @since v5.2
 */
public abstract class DecoratedJiraWebSudo implements JiraWebSudo
{
    private final JiraWebSudo sudo;

    public DecoratedJiraWebSudo(JiraWebSudo sudo)
    {
        this.sudo = sudo;
    }

    @Override
    public void authenticate(String password)
    {
        sudo.authenticate(password);
        afterAuthenticate();
    }

    @Override
    public <T> T authenticate(String password, Class<T> targetPage, Object... args)
    {
        final T authenticate = sudo.authenticate(password, targetPage, args);
        afterAuthenticate();
        return authenticate;
    }

    @Override
    public JiraWebSudo authenticateFail(String password)
    {
        sudo.authenticateFail(password);
        return this;
    }

    @Override
    public <T> T cancel(Class<T> expectedPage, Object... args)
    {
        return sudo.cancel(expectedPage, args);
    }

    @Override
    public void cancel()
    {
        sudo.cancel();
    }

    protected abstract void afterAuthenticate();
}
