package com.atlassian.jira.web.bean.i18n;

import java.util.Map;

/**
 * @since v6.2.3
 */
public class MockTranslationStoreFactory implements TranslationStoreFactory
{
    @Override
    public TranslationStore createTranslationStore(final Map<String, String> bundles)
    {
        return new MockTranslationStore(bundles);
    }
}
