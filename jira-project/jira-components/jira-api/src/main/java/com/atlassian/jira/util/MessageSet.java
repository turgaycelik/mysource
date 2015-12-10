package com.atlassian.jira.util;

import java.io.Serializable;
import java.util.Set;

import com.atlassian.annotations.PublicApi;

/**
 * Used to communicate error and warning messages.
 *
 * @since v3.13
 */
@PublicApi
public interface MessageSet extends Serializable
{
    /**
     * Message levels
     */
    public enum Level
    {
        WARNING, ERROR
    }

    /**
     * @return a unique set of error messages, these messages are separate from the warning messages, never null.
     */
    Set<String> getErrorMessages();

    /**
     * @param errorMsg the unique error message
     * @return the MessageLink that is associated with the error, if one exists, null otherwise.
     */
    MessageLink getLinkForError(String errorMsg);

    /**
     * Returns a unique set of all the English error messages.
     * @return a unique set of all the English error messages.
     * @see #addErrorMessageInEnglish(String)
     */
    Set<String> getErrorMessagesInEnglish();

    /**
     * @return a unique set of warning messages, these messages are separate from the error messages, never null.
     */
    Set<String> getWarningMessages();

    /**
     * @param warningMsg the unique warning message
     * @return the MessageLink that is associated with the warning, if one exists, null otherwise.
     */
    MessageLink getLinkForWarning(String warningMsg);

    /**
     * Returns a unique set of all the English warning messages.
     * @return a unique set of all the English warning messages.
     * @see #addWarningMessageInEnglish(String)
     */
    Set<String> getWarningMessagesInEnglish();

    /**
     * Returns <code>true</code> if there are error messages, <code>false</code> otherwise.
     * @return <code>true</code> if there are error messages, <code>false</code> otherwise.
     */
    boolean hasAnyErrors();

    /**
     * Returns <code>true</code> if there are warning messages, <code>false</code> otherwise.
     * @return <code>true</code> if there are warning messages, <code>false</code> otherwise.
     */
    boolean hasAnyWarnings();

    /**
     * Returns <code>true</code> if there are messages of any type, <code>false</code> otherwise.
     * That is, it will return true if hasAnyErrors() is <code>true</code> <em>or</em> hasAnyWarnings() is <code>true</code>.
     * @return <code>true</code> if there are messages of any type, <code>false</code> otherwise.
     */
    boolean hasAnyMessages();
    
    /**
     * Will concatenate this message set with the provided message set. All new errors and warnings will be added
     * to the existing errors and warnings.
     *
     * @param messageSet contains the new errors and warnings to add to this set.
     */
    void addMessageSet(MessageSet messageSet);

    /**
     * Adds a message with the given warning / error level
     *
     * @param level message level
     * @param errorMessage the message to add.
     */
    void addMessage(Level level, String errorMessage);

    /**
     * Adds an error message  with the given warning / error level and associates a link with the error.
     *
     * @param level message level
     * @param errorMessage the message to add.
     * @param link the link to show the users associated with this error.
     */
    void addMessage(Level level, String errorMessage, MessageLink link);
    
    /**
     * Adds an error message.
     *
     * @param errorMessage the message to add.
     */
    void addErrorMessage(String errorMessage);

    /**
     * Adds an error message and associates a link with the error.
     *
     * @param errorMessage the message to add.
     * @param link the link to show the users associated with this error.
     */
    void addErrorMessage(String errorMessage, MessageLink link);

    /**
     * Adds an error message in English.
     * <p>
     * This is useful when the {@link #addErrorMessage(String)} method is used to add translated messages, and you want
     * to be able to get a set of the messages in English as well.
     * Eg, in the Project Import we show the translated messages on screen during validation, and log the English versions
     * in case Atlassian Support needs to read the logs later.
     * </p>
     *
     * @param errorMessage the message to add.
     * @see #addErrorMessage(String)
     * @see #addWarningMessageInEnglish(String)
     */
    void addErrorMessageInEnglish(String errorMessage);

    /**
     * Adds a warning message.
     *
     * @param warningMessage the message to add.
     */
    void addWarningMessage(String warningMessage);

    /**
     * Adds a warning message and associates a link with the warning.
     *
     * @param warningMessage the message to add.
     * @param link the link to show the users associated with this warning.
     */
    void addWarningMessage(String warningMessage, MessageLink link);

    /**
     * Adds a warning message in English.
     * <p>
     * This is useful when the {@link #addWarningMessage(String)} method is used to add translated messages, and you want
     * to be able to get a set of the messages in English as well.
     * Eg, in the Project Import we show the translated messages on screen during validation, and log the English versions
     * in case Atlassian Support needs to read the logs later.
     * </p>
     *
     * @param warningMessage the message to add.
     * @see #addWarningMessage(String)
     * @see #addErrorMessageInEnglish(String)
     */
    void addWarningMessageInEnglish(String warningMessage);

    /**
     * A simple class for holding link text and a link url.
     */
    public static class MessageLink
    {
        private final String linkText;
        private final String linkUrl;
        private final boolean absolutePath;

        public MessageLink(final String linkText, final String linkUrl)
        {
            this.linkText = linkText;
            this.linkUrl = linkUrl;
            absolutePath = false;
        }

        public MessageLink(final String linkText, final String linkUrl, final boolean absolutePath)
        {
            this.linkText = linkText;
            this.linkUrl = linkUrl;
            this.absolutePath = absolutePath;
        }

        public String getLinkText()
        {
            return linkText;
        }

        public String getLinkUrl()
        {
            return linkUrl;
        }

        public boolean isAbsolutePath()
        {
            return absolutePath;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final MessageLink link = (MessageLink) o;

            if (absolutePath != link.absolutePath)
            {
                return false;
            }
            if (linkText != null ? !linkText.equals(link.linkText) : link.linkText != null)
            {
                return false;
            }
            if (linkUrl != null ? !linkUrl.equals(link.linkUrl) : link.linkUrl != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = (linkText != null ? linkText.hashCode() : 0);
            result = 31 * result + (linkUrl != null ? linkUrl.hashCode() : 0);
            result = 31 * result + (absolutePath ? 1 : 0);
            return result;
        }
    }
}
