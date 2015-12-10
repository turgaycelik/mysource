package com.atlassian.jira.web.bean.i18n;

import java.util.Map;

/**
 * Uses {@link CompressedKey} for i18n keys and {@link ByteArray} for values.
 *
 * @since 6.2
 */
public class CompressedKeyStore extends TranslationStoreTemplate<CompressedKey, ByteArray>
{
    public CompressedKeyStore(final Map<String, String> map)
    {
        super(map);
    }

    @Override
    protected CompressedKey makeKeyFromString(final String key)
    {
        return CompressedKey.fromString(key);
    }

    @Override
    protected ByteArray makeValueFromString(final String value)
    {
        return ByteArray.fromString(value);
    }
}
