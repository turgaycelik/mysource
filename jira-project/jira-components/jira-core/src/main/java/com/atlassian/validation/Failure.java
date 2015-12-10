package com.atlassian.validation;

import com.opensymphony.util.TextUtils;

/**
 * A {@link Validator.Result} that represents epic validation failure.
 *
 * @since v4.4
 */
public final class Failure implements Validator.Result
{
    private String message;
    private String htmlMessage;

    /**
     * Creates failure from a given text message, encoding the text message as a whole into html for the html message.
     *
     * @param message the failure message in text.
     */
    public Failure(String message)
    {
        this(message, TextUtils.htmlEncode(message));
    }

    /**
     * Creates failure from a text message and an html equivalent message.
     *
     * @param textError the error to be displayed in plain text output (such as logs).
     * @param htmlError the error to be displayed in a browser or html email.
     */
    public Failure(String textError, String htmlError)
    {
        this.message = textError;
        this.htmlMessage = htmlError;
    }

    @Override
    public boolean isValid()
    {
        return false;
    }

    @Override
    public String getErrorMessage()
    {
        return message;
    }

    @Override
    public String getErrorMessageHtml()
    {
        return htmlMessage;
    }

    @Override
    public String get() throws IllegalStateException
    {
        // TODO i18n
        throw new IllegalStateException("Validation failed");
    }
}
