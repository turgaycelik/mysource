package com.atlassian.jira.mention;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.util.collect.CollectionBuilder.newBuilder;
import static junit.framework.Assert.assertEquals;

public class TestMentionFinder
{
    private final static Set<String> EMPTY_SET = Collections.<String>emptySet();
    private MentionFinder mentionFinder;

    @Before
    public void setUp()
    {
        mentionFinder = new MentionFinderImpl();
    }

    private Iterable<String> getMentionedUsernames(String content)
    {
        return mentionFinder.getMentionedUsernames(content);
    }

    @Test
    public void testNoMentions()
    {
        assertEquals(EMPTY_SET, getMentionedUsernames(null));
        assertEquals(EMPTY_SET, getMentionedUsernames(""));
        assertEquals(EMPTY_SET, getMentionedUsernames("Some text with no mentions."));
    }

    @Test
    public void testInvalidMentions()
    {
        assertEquals(EMPTY_SET, getMentionedUsernames("~username"));
        assertEquals(EMPTY_SET, getMentionedUsernames("@username"));
        assertEquals(EMPTY_SET, getMentionedUsernames("[username]"));
        assertEquals(EMPTY_SET, getMentionedUsernames("[Xusername]"));
    }

    @Test
    public void testValidMentions()
    {
        assertEquals(newBuilder("username").asSet(), getMentionedUsernames("[~username]"));
        assertEquals(newBuilder("username").asSet(), getMentionedUsernames("[@username]"));
        assertEquals(newBuilder("username").asSet(), getMentionedUsernames("[~username] [@username]"));
        assertEquals(newBuilder("username", "another_username").asSet(), getMentionedUsernames("[~username] [@another_username]"));
        assertEquals(newBuilder("dude%sweet").asSet(), getMentionedUsernames("some funny @dude@stuff chars @fred&george and [~dude%sweet]"));
        assertEquals(newBuilder("john@example.com", "george~hello").asSet(), getMentionedUsernames("testing wiki markup with funny chars [~john@example.com] and [~george~hello]"));
        assertEquals(newBuilder("brad", "stuff").asSet(), getMentionedUsernames("this tests newlines after mention [~brad]\nhello world [~stuff]"));
        assertEquals(newBuilder("dave").asSet(), getMentionedUsernames("this mention has a full stop after it [~dave]."));
        assertEquals(newBuilder("dave").asSet(), getMentionedUsernames("this mention is at the end [~dave]"));
        assertEquals(newBuilder("stuff").asSet(), getMentionedUsernames("[~stuff] mention at the start"));
        assertEquals(newBuilder("stuff").asSet(), getMentionedUsernames("[~stuff]"));
    }
}
