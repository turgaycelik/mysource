package com.atlassian.jira.mail;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 */
public class TestBotoCssInliner
{
    /**
     * This tests that boto works enough (e.g. doesn't throw antlr excpetions)
     * that we can get some basic input/putput working
     */
    @Test
    public void testBotoDoesntThrowException()
    {
        BotoCssInliner inliner = new BotoCssInliner();
        String input = "<span>bold</span>";
        String output = inliner.applyStyles(input);
        assertNotNull(output);
        assertThat(output, containsString(input));
    }
}
