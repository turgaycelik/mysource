package com.atlassian.jira.mention;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionFinderImpl implements MentionFinder
{
    private static final Pattern USER_PROFILE_WIKI_MARKUP_LINK_PATTERN = Pattern.compile("\\[[~@][^\\\\,]+?\\]");

    public Iterable<String> getMentionedUsernames(String content)
    {
        if (StringUtils.isBlank(content))
        {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<String> mentions = ImmutableSet.builder();

        Matcher wikiMatcher = USER_PROFILE_WIKI_MARKUP_LINK_PATTERN.matcher(content);
        while (wikiMatcher.find())
        {
            String markup = content.substring(wikiMatcher.start(), wikiMatcher.end());
            // get rid of [~ and ]
            String username = markup.substring(2, markup.length() - 1);
            mentions.add(username);
        }

        return mentions.build();
    }
}
