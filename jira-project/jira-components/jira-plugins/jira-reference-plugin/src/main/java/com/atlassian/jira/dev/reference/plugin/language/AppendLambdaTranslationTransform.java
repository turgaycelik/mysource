package com.atlassian.jira.dev.reference.plugin.language;

import com.atlassian.jira.plugin.language.TranslationTransform;

import java.util.Locale;

/**
 * A simple translation transform function that appends a 'lambda' character to the end of each translation.
 *
 * @since v5.1
 */
public class AppendLambdaTranslationTransform implements TranslationTransform
{
    private static final char START_HIGHLIGHT_CHAR = '\uFEFF';  // BOM
    private static final char END_HIGHLIGHT_CHAR = '\u2060';    // zero width word joiner
    private static final char LAMBDA_CHAR = '\u03bb';           // lambda

    @Override
    public String apply(Locale locale, String key, String rawMessage)
    {
        return START_HIGHLIGHT_CHAR + rawMessage + " " + LAMBDA_CHAR + END_HIGHLIGHT_CHAR;
    }
}
