package com.atlassian.jira.web.bean.i18n;

import java.util.Map;

/**
 * Factory for TranslationStore instances.
 *
 * @since 6.2
 */
public interface TranslationStoreFactory
{
    /**
     * Creates the appropriate TranslationStore depending on whether we're running in OnDemand or not.
     *
     * @param bundles a Map containing the bundles
     * @return a new TranslationStore
     */
    TranslationStore createTranslationStore(Map<String, String> bundles);
}
