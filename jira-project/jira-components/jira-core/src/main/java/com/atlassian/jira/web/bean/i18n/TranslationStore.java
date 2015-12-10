package com.atlassian.jira.web.bean.i18n;

import javax.annotation.concurrent.Immutable;

/**
 * Store for i18n keys and their respective values.
 *
 * @since 6.2
 */
@Immutable
public interface TranslationStore
{
    /**
     * Returns the i18n value associated with a key.
     *
     * @param key a String key
     * @return a String
     */
    String get(String key);

    /**
     * Whether this TranslationStore contains the given key.
     *
     * @param key a String key
     * @return true if this TranslationStore contains the given key
     */
    boolean containsKey(String key);

    /**
     * Returns an Iterable over this TranslationStore's keys.
     *
     * @return an Iterable&lt;String&gt;
     */
    Iterable<String> keys();
}
