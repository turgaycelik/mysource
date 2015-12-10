package com.atlassian.jira.issue.fields;

/**
 * Reads the maximum character length from the application properties.
 *
 * @since v5.0.3
 */
public class DefaultTextFieldCharacterLengthValidator implements TextFieldCharacterLengthValidator
{
    static final int UNLIMITED_TEXT_FIELD_CHARACTER_LIMIT = 0;
    private final TextFieldLimitProvider textFieldLimitProvider;

    public DefaultTextFieldCharacterLengthValidator(final TextFieldLimitProvider textFieldLimitProvider)
    {
        this.textFieldLimitProvider = textFieldLimitProvider;
    }

    @Override
    public boolean isTextTooLong(final String text)
    {
        if (text == null)
        {
            return false;
        }
        else
        {
            final long textFieldCharacterLimit = getMaximumNumberOfCharacters();
            if (textFieldCharacterLimit == UNLIMITED_TEXT_FIELD_CHARACTER_LIMIT)
            {
                return false;
            }
            return text.length() > textFieldCharacterLimit;
        }
    }

    @Override
    public long getMaximumNumberOfCharacters()
    {
        return textFieldLimitProvider.getTextFieldLimit();
    }

}
