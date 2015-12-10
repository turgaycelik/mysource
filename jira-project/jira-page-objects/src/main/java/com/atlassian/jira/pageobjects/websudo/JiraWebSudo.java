package com.atlassian.jira.pageobjects.websudo;

/**
 * Page/Dialog that represents WebSudo in JIRA.
 *
 * @since v5.0.1
 */
public interface JiraWebSudo
{
    /**
     * Submit the password for websudo and bind the passed page.
     *
     * @param password the password to submit.
     * @param targetPage the page to bind after the submit.
     * @param args arguments for targetPage during the bind.
     * @param <T> the type of the page to bind.
     * @return a newly bound page after websudo.
     */
    <T> T authenticate(String password, Class<T> targetPage, Object...args);

    /**
     * Submit a password.
     *
     * @param password the password to submit.
     */
    void authenticate(String password);

    /**
     * Submit the wrong password for websudo.
     *
     * @param password the password to submit.
     * @return this websudo form.
     */
    JiraWebSudo authenticateFail(String password);

    /**
     * Cancel websudo and bind the passed page.
     *
     * @param expectedPage the page to bind after the cancel.
     * @param args arguments for targetPage during the bind.
     * @param <T> the type of the page to bind.
     * @return a newly bound page after cancel.
     */
    <T> T cancel(Class<T> expectedPage, Object... args);

    /**
     * Cancel websudo.
     */
    void cancel();
}
