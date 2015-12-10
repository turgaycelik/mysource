package com.atlassian.jira.auditing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Locale;

/**
 * @since v6.2
 */
public class SearchTokenizer
{

    final ImmutableSet.Builder<String> tokens = ImmutableSet.builder();

    public String getTokenizedString()
    {
        return Joiner.on(" ").join(getTokens());
    }

    @VisibleForTesting
    ImmutableSet<String> getTokens()
    {
        return tokens.build();
    }

    public SearchTokenizer put(String stringToTokenize)
    {
        if (stringToTokenize != null)
        {
            tokens.addAll(Iterables.transform(Splitter.on(" ").omitEmptyStrings().split(stringToTokenize), new Function<String, String>()
            {
                @Override
                public String apply(String input)
                {
                    return input.toLowerCase(Locale.ENGLISH);
                }
            }));
        }
        return this;
    }

    public static ImmutableSet<String> tokenize(String stringToTokenize)
    {
        return new SearchTokenizer().put(stringToTokenize).getTokens();
    }


}
