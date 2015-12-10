package com.atlassian.jira.auditing;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2
 */
public class TestSearchTokenizer
{

    SearchTokenizer searchTokenizer = new SearchTokenizer();

    @Test
    public void testWorksWithoutAnyPuts() throws Exception
    {
        assertThat(searchTokenizer.getTokenizedString(), is(""));
    }

    @Test
    public void testAcceptsNullAndEmptyString() throws Exception
    {
        searchTokenizer.put(null);
        searchTokenizer.put("");
        assertThat(searchTokenizer.getTokens().size(), is(0));
        assertThat(searchTokenizer.getTokenizedString(), is(""));
    }

    @Test
    public void testReturnTokensSeparatedBySpaces() throws Exception
    {
        searchTokenizer.put("aaa");
        searchTokenizer.put("bbb");
        searchTokenizer.put("ccc");
        searchTokenizer.put("bbb");
        assertThat(searchTokenizer.getTokens(), is(ImmutableSet.of("aaa", "bbb", "ccc")));
        assertThat(searchTokenizer.getTokenizedString(), is("aaa bbb ccc"));
    }

    @Test
    public void testSplitTokensContainingSpaces() throws Exception
    {
        searchTokenizer.put("aaa");
        searchTokenizer.put("bbb ccc");
        searchTokenizer.put("ddd  eee");
        assertThat(searchTokenizer.getTokens(), is(ImmutableSet.of("aaa", "bbb", "ccc", "ddd", "eee")));
        assertThat(searchTokenizer.getTokenizedString(), is("aaa bbb ccc ddd eee"));
    }

    @Test
    public void testTokensAreNormalizedToLowerCase() throws Exception
    {
        searchTokenizer.put("Aaa");
        searchTokenizer.put("bbB ccc");
        searchTokenizer.put("Eee");
        assertThat(searchTokenizer.getTokens(), is(ImmutableSet.of("aaa", "bbb", "ccc", "eee")));
        assertThat(searchTokenizer.getTokenizedString(), is("aaa bbb ccc eee"));
    }
}
