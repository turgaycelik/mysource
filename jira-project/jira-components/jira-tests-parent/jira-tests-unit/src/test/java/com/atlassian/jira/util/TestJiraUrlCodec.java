package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestJiraUrlCodec
{
    @Test
    public void testSystemEncodeStringWithSpacesAsPlus() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam+a%26nasty-string", JiraUrlCodec.systemDefaultEncoding(value, false));
    }

    @Test
    public void testSystemEncodeStringWithSpacesAsHex() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam%20a%26nasty-string", JiraUrlCodec.systemDefaultEncoding(value, true));
    }

    @Test
    public void testJiraPropertiesEncodingWithSpacesAsPlus() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam+a%26nasty-string", JiraUrlCodec.encode(value, "UTF-8", false));
    }

    @Test
    public void testJiraPropertiesEncodingWithSpacesAsHex() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam%20a%26nasty-string", JiraUrlCodec.encode(value, "UTF-8", true));
    }

    @Test
    public void testDecode() throws Exception
    {
        String value = "i%2Bam+a%26nasty-string";
        assertEquals("i+am a&nasty-string", JiraUrlCodec.decode(value, "UTF-8"));
    }
}
