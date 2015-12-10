package com.atlassian.jira.web.bean.i18n;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A more memory-efficient (and slower) string representation for I18n values.
 *
 * @since 6.2
 */
final class ByteArray
{
    private static final Charset utf8 = Charset.forName("utf-8");

    public static ByteArray fromString(String string)
    {
        return new ByteArray(string);
    }

    private final byte[] bytes;

    public ByteArray(String string)
    {
        this.bytes = string != null ? string.getBytes(utf8) : null;
    }

    @Override
    public String toString()
    {
        return new String(bytes);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof ByteArray)
        {
            return Arrays.equals(this.bytes, ((ByteArray) obj).bytes);
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(bytes);
    }
}
