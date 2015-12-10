package com.atlassian.jira.web.bean.i18n;

import java.util.Map;

/**
 * Byte array-backed store for translations.
 *
 * @since 6.2
 */
public class ByteArrayBackedStore extends TranslationStoreTemplate<ByteArray, ByteArray>
{
    public ByteArrayBackedStore(final Map<String, String> map)
    {
        super(map);
    }

    @Override
    protected ByteArray makeKeyFromString(final String key)
    {
        return ByteArray.fromString(key);
    }

    @Override
    protected ByteArray makeValueFromString(final String value)
    {
        return ByteArray.fromString(value);
    }
}
