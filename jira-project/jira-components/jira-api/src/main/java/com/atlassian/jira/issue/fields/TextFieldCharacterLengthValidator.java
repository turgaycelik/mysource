package com.atlassian.jira.issue.fields;

/**
 * Checks, whether a string's length exceeds a specific limit.
 *
 * @since 5.0.3
 */
public interface TextFieldCharacterLengthValidator
{
    /**
     * Validate the length of the given string against the maximum number.
     *
     * @param text the text to be checked; can be <code>null</code>
     *
     * @return whether the given text is longer than the maximum number of characters; <code>null</code> is always valid
     */
    boolean isTextTooLong(String text);

    /**
     * Returns the maximum number of characters to be entered for a single field.
     *
     * @return the maximum number of characters to be entered for a single field.
     */
    long getMaximumNumberOfCharacters();
}
