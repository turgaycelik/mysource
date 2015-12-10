package com.atlassian.jira.help;

import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * @since v6.2.4
 */
public class ImmutableHelpUrlTest
{
    @Test
    public void ableToConstructUrl()
    {
        ImmutableHelpUrl url = new ImmutableHelpUrl("key", "url", "title", "alt", true);
        HelpUrlMatcher matcher = new HelpUrlMatcher().url("url").title("title").key("key").alt("alt").local(true);

        assertThat(url, matcher);
    }

    //HIROL-62
    @Test
    public void ableToTakeStrangeArguments()
    {
        ImmutableHelpUrl url = new ImmutableHelpUrl("      key", "   url    ", "\ntitle\n\n", "        ", true);
        HelpUrlMatcher matcher = new HelpUrlMatcher()
                .url("   url    ")
                .title("\ntitle\n\n")
                .key("      key")
                .alt("        ")
                .local(true);
        assertThat(url, matcher);
    }
}
