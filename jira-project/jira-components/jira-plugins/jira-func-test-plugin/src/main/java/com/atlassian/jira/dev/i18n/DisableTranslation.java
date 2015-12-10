package com.atlassian.jira.dev.i18n;

import java.util.Locale;

import com.atlassian.jira.plugin.language.TranslationTransform;

/**
 * A translation transform that 'disables' the translation by returning the original key, if the en_MOON locale has been
 * used.
 *
 * @since v6.3
 */
public class DisableTranslation implements TranslationTransform
{
    private final QunitLocaleSwitcher qunitLocaleSwitcher;

    public DisableTranslation(QunitLocaleSwitcher qunitLocaleSwitcher)
    {
        this.qunitLocaleSwitcher = qunitLocaleSwitcher;
    }

    @Override
    public String apply(Locale locale, String key, String rawMessage)
    {
        if (qunitLocaleSwitcher.shouldDisableTranslationFor(locale))
        {
            return key;
        }
        else
        {
            return rawMessage;
        }
    }
}
