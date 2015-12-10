package com.atlassian.jira.plugin.language;

import java.util.Locale;

/**
 * @since v6.2.3
 */
public class PrependTextTransform implements TranslationTransform
{
    private final String prefix;

    public PrependTextTransform(final String prefix) {this.prefix = prefix;}

    @Override
    public String apply(final Locale locale, final String key, final String rawMessage)
    {
        return prefix + rawMessage;
    }
}
