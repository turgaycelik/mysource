package com.atlassian.jira.startup;

/**
 * Thrown when errors occur trying to validate the jira.home.
 *
 * @since v4.0
 */
public class JiraHomeException extends Exception
{
    private final String htmlText;

    public JiraHomeException(final String message)
    {
        super(message);
        htmlText = message;
    }

    public JiraHomeException(final String plainText, final String htmlText)
    {
        super(plainText);
        this.htmlText = htmlText;
    }

    public String getHtmlMessage()
    {
        return this.htmlText;
    }
}
