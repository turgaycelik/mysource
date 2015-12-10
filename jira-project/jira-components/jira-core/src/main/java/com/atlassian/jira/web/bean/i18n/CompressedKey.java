package com.atlassian.jira.web.bean.i18n;

import javax.annotation.Nullable;

import com.atlassian.jira.util.cache.WeakInterner;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;

import static com.atlassian.jira.util.cache.WeakInterner.newWeakInterner;

/**
 * This class is used to achieve interning of words in i18n keys as opposed to entire keys.
 *
 * @since 6.2
 */
final class CompressedKey
{
    private final static WeakInterner<CompressedKey> WORD_INTERNER = newWeakInterner();
    private static final CompressedKey ROOT = new CompressedKey(null, null);

    /**
     * Returns an interned CompressedKey instance for the given string.
     *
     * @param string a String
     * @return a CompressedKey, or null if {@code string} is null
     */
    public static CompressedKey fromString(@Nullable String string)
    {
        if (string == null)
        {
            return null;
        }

        CompressedKey compressedKey = ROOT;

        String[] words = StringUtils.split(string, ".");
        for (String word : words)
        {
            compressedKey = WORD_INTERNER.intern(new CompressedKey(compressedKey, ByteArray.fromString(word)));
        }

        return compressedKey;
    }

    private final CompressedKey parent;
    private final ByteArray word;
    private int hash; // cached hashCode. intentionally not volatile/synchronized

    CompressedKey(final CompressedKey parent, final ByteArray word)
    {
        this.parent = parent;
        this.word = word;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        appendWords(sb, this);

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final CompressedKey compressedKey = (CompressedKey) o;

        if (parent != null ? !parent.equals(compressedKey.parent) : compressedKey.parent != null) { return false; }
        if (word != null ? !word.equals(compressedKey.word) : compressedKey.word != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int h = hash;
        if (h == 0 && this != ROOT)
        {
            h = parent.hashCode();
            h = 31 * h + (word != null ? word.hashCode() : 0);
            hash = h;
        }

        return h;
    }

    /**
     * Used in unit tests only.
     *
     * @return the parent
     */
    @VisibleForTesting
    CompressedKey parent()
    {
        return parent;
    }

    // recursive toString implementation
    private static void appendWords(StringBuilder sb, CompressedKey compressedKey)
    {
        if (compressedKey != ROOT)
        {
            appendWords(sb, compressedKey.parent);
            sb.append(compressedKey.word).append('.');
        }
    }
}
