package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Set;

public class TextTermEscaper implements Function<CharSequence, String>
{
    // the full list of reserved chars in Lucene:
    // '\\', '+', '-', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}', '~', '*', '?', '|', '&'
    private static final Set<Character> ALLOWED_LUCENE_OPERATORS = CollectionBuilder.newBuilder('+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '~', '*', '?', '\"').asSet();

    public static String escape(final CharSequence input)
    {
        return new TextTermEscaper().get(input);
    }

    public String get(final CharSequence input)
    {
        final StringBuilder escaped = new StringBuilder(input.length() * 2);
        for (int i = 0; i < input.length(); i++)
        {
            final Character c = input.charAt(i);
            // JRA-16151: the ':' lucene operator is disallowed, so we always escape it
            if (c.equals(':'))
            {
                escaped.append('\\');
            }
            else if (c.equals('\\'))
            {
                final Character nextChar = i + 1 < input.length() ? input.charAt(i + 1) : null;

                if (shouldEscapeBackslash(nextChar))
                {
                    escaped.append('\\');
                }
            }

            escaped.append(c);
        }
        return escaped.toString();
    }

    private boolean shouldEscapeBackslash(final Character charAfterBackslash)
    {
        return !ALLOWED_LUCENE_OPERATORS.contains(charAfterBackslash);
    }
}
