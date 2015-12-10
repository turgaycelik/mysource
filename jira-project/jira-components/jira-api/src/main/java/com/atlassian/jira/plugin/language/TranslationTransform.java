package com.atlassian.jira.plugin.language;

import java.util.Locale;

/**
 * Implementors of this interface have the opportunity to transform the result of translation lookups made by {@link com.atlassian.jira.util.I18nHelper#getUnescapedText(String)}.
 * (and hence {@link com.atlassian.jira.util.I18nHelper#getText(String)} and all its derivatives).
 * <p/>
 * This transform will occur dynamically when the translation is requested.
 *
 * @since v5.1
 */
public interface TranslationTransform
{
    /**
     * Given a {@link Locale}, i18n <tt>key</tt> and the <tt>rawMessage</tt> corresponding to them, apply a transformation
     * on the message to produce a different translation.
     *
     * @param locale the locale that was used to translate the input
     * @param key the key that was requested for translation
     * @param rawMessage the result of the translation lookup
     * @return a potentially modified raw message
     */
    public String apply(Locale locale, String key, String rawMessage);
}
