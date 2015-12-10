package com.atlassian.jira.util;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestURLCodec
{
    @Test
    public void testDecodeWithUnicodeCharacters() throws UnsupportedEncodingException
    {
        String unicodeString = "\u0126\u0118\u0139\u0139\u0150";
        String asciiVersionOfUnicodeString = "%C4%A6%C4%98%C4%B9%C4%B9%C5%90";
        assertEquals(unicodeString, URLCodec.decode(asciiVersionOfUnicodeString, "UTF-8"));
    }

    @Test
    public void testDecodeSpaces() throws UnsupportedEncodingException
    {
        assertEquals("Test String", URLCodec.decode("Test+String", "UTF-8"));
        assertEquals("Test String", URLCodec.decode("Test%20String", "UTF-8"));
    }

    @Test
    public void testEncodeNullArg() throws UnsupportedEncodingException
    {
        assertNull(URLCodec.encode((String) null));
    }

    @Test
    public void testDecodeUrlNullArg() throws UnsupportedEncodingException
    {
        assertNull(URLCodec.decodeUrl(null));
    }

    @Test
    public void testEncodeNullObject() throws UnsupportedEncodingException
    {
        assertNull(URLCodec.encode((Object) null));
    }

    @Test
    public void testDecodeNullString() throws UnsupportedEncodingException
    {
        assertNull(URLCodec.decode(null, "UTF-8"));
    }

    @Test
    public void testEncodeUrlNull() throws UnsupportedEncodingException
    {
        assertNull(URLCodec.encodeUrl(null, null));
    }

    @Test
    public void testEncodeStringWithSpacesAsPlus() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam+a%26nasty-string", URLCodec.encode(value, "UTF-8", false));
        assertEquals("i%2Bam+a%26nasty-string", URLCodec.encode(value, "UTF-8"));
    }

    @Test
    public void testEncodeStringWithSpacesAsHex() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam%20a%26nasty-string", URLCodec.encode(value, "UTF-8", true));
    }

    @Test
    public void testEncodeBytesWithSpacesAsPlus() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam+a%26nasty-string", new String(URLCodec.encode(value.getBytes(), false), "US-ASCII"));
        assertEquals("i%2Bam+a%26nasty-string", new String(URLCodec.encode(value.getBytes()), "US-ASCII"));
    }

    @Test
    public void testEncodeBytesWithSpacesAsHex() throws Exception
    {
        String value = "i+am a&nasty-string";
        assertEquals("i%2Bam%20a%26nasty-string", new String(URLCodec.encode(value.getBytes(), true), "US-ASCII"));
    }

    @Test
    public void testDecodeStringWithHexValue() throws Exception
    {
        String value = "i%2Bam%20a%26nasty-string";
        assertEquals("i+am a&nasty-string", URLCodec.decode(value, "UTF-8"));
    }

    @Test
    public void testDecodeStringWithPlus() throws Exception
    {
        String value = "i%2Bam+a%26nasty-string";
        assertEquals("i+am a&nasty-string", URLCodec.decode(value, "UTF-8"));
    }

    @Test
    public void testDecodeBytesWithHexValue() throws Exception
    {
        String value = "i%2Bam%20a%26nasty-string";
        assertEquals("i+am a&nasty-string", new String(URLCodec.decode(value.getBytes()), "US-ASCII"));
    }

    @Test
    public void testDecodeBytesWithPlus() throws Exception
    {
        String value = "i%2Bam+a%26nasty-string";
        assertEquals("i+am a&nasty-string", new String(URLCodec.decode(value.getBytes()), "US-ASCII"));
    }
}
