package com.atlassian.jira.plugin.language;

import java.util.Locale;

/**
 * @since v6.2.3
 */
public class AppendTextTransform implements TranslationTransform
{
    private final String suffix;

    public AppendTextTransform(final String suffix) {this.suffix = suffix;}

    @Override
    public String apply(final Locale locale, final String key, final String rawMessage)
    {
        return rawMessage + suffix;
    }
}
