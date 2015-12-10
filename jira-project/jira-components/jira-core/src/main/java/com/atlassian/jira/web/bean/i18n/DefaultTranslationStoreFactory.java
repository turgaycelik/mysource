package com.atlassian.jira.web.bean.i18n;

import com.atlassian.jira.config.properties.JiraProperties;
import com.google.common.annotations.VisibleForTesting;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Factory for TranslationStore instances. The default behaviour is: <ul> <li>in OnDemand, creates a byte[]-backed
 * translation store to save memory (at the cost of extra CPU), and <li>in BTF, creates a standard string-backed
 * translation store </ul> The default can be overridden by setting the <code>-D{@value #COMPRESS_I18N_PROPERTY}</code>
 * system property to "true" or "false".
 */
public class DefaultTranslationStoreFactory implements TranslationStoreFactory
{
    /**
     * System property that can be used to override i18n string compression.
     */
    @VisibleForTesting
    static String COMPRESS_I18N_PROPERTY = "jira.i18n.compress";

    private final JiraProperties properties;

    public DefaultTranslationStoreFactory(final JiraProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public TranslationStore createTranslationStore(Map<String, String> bundles)
    {
        boolean compress = false;
        String compressOverride = properties.getProperty(COMPRESS_I18N_PROPERTY);
        if (isNotBlank(compressOverride))
        {
            compress = Boolean.valueOf(compressOverride);
        }

        return compress ? new CompressedKeyStore(bundles) : new StringBackedStore(bundles);
    }
}
