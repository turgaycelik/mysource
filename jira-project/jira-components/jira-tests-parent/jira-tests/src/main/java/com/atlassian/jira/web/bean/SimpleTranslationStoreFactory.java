package com.atlassian.jira.web.bean;

import com.atlassian.jira.web.bean.i18n.StringBackedStore;
import com.atlassian.jira.web.bean.i18n.TranslationStore;
import com.atlassian.jira.web.bean.i18n.TranslationStoreFactory;

import java.util.Map;

public class SimpleTranslationStoreFactory implements TranslationStoreFactory
{
    @Override
    public TranslationStore createTranslationStore(final Map<String, String> bundles)
    {
        return new StringBackedStore(bundles);
    }
}
