package com.atlassian.jira.web.util;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the HostileAttachmentsHelper class.
 *
 * @since v3.13
 */
public class TestHostileAttachmentsHelper
{
    @Test
    public void testParseConfiguration() {
        assertStringParsedAs(new String[] {"foo"}, "foo");
        assertStringParsedAs(new String[] {"foo"}, "foo ");
        assertStringParsedAs(new String[] {"foo"}, " foo ");
        assertStringParsedAs(new String[] {"foo"}, " foo ");
        assertStringParsedAs(new String[] {"foo"}, " \tfoo \t\t\r\n");
        assertStringParsedAs(new String[] {"foo", "bar"}, " \tfoo \t\t\r\nbar");
        assertStringParsedAs(new String[] {"foo", "bar"}, " \tfoo \t\t\r\nbar ");
        assertStringParsedAs(new String[] {"foo", "bar", ".html", ".htm"}, "foo bar .html .htm");
        assertStringParsedAs(new String[] {"foo", "bar", ".html", ".htm"}, " foo  bar  .html  .htm  ");
    }

    @Test
    public void testEndsWithIgnoreCase() {
        HostileAttachmentsHelper hah = new HostileAttachmentsHelper();

        // happy pathy
        assertTrue(hah.endsWithIgnoreCase("foo", "foo"));
        assertTrue(hah.endsWithIgnoreCase(".", "."));
        assertTrue(hah.endsWithIgnoreCase("fOo", "foO"));
        assertTrue(hah.endsWithIgnoreCase("(*&", "(*&"));
        assertTrue(hah.endsWithIgnoreCase("rhubarb(*&", "(*&"));
        assertTrue(hah.endsWithIgnoreCase("degenerate", ""));
        assertTrue(hah.endsWithIgnoreCase("", ""));
        assertTrue(hah.endsWithIgnoreCase("degenerate", "E"));
        assertTrue(hah.endsWithIgnoreCase("degenerate", "e"));
        assertTrue(hah.endsWithIgnoreCase("degenerate.exe", "e"));
        assertTrue(hah.endsWithIgnoreCase("degenerate.exe", "exe"));
        assertTrue(hah.endsWithIgnoreCase("degenerate.exe", ".exe"));
        assertTrue(hah.endsWithIgnoreCase("index.HTML", ".html"));
        assertTrue(hah.endsWithIgnoreCase("index.html", ".html"));

        // muy mal
        assertFalse(hah.endsWithIgnoreCase("noDotInThis-com", ".com"));
        assertFalse(hah.endsWithIgnoreCase("com", ".com"));
        assertFalse(hah.endsWithIgnoreCase("_", ".com"));
        assertFalse(hah.endsWithIgnoreCase("", ".html"));
        assertFalse(hah.endsWithIgnoreCase("HTML", ".html"));
        assertFalse(hah.endsWithIgnoreCase("index.html", ".HTM"));
        assertFalse(hah.endsWithIgnoreCase("index.htm", ".html"));
        assertFalse(hah.endsWithIgnoreCase("index.html.htm", ".html"));
    }

    @Test
    public void testIsExecutableMethods() {
        HostileAttachmentsHelper hostileAttachmentsHelper = new HostileAttachmentsHelper();
        Properties config = new Properties();
        config.setProperty(HostileAttachmentsHelper.KEY_EXECUTABLE_FILE_EXTENSIONS, ".html .htm .swf");
        config.setProperty(HostileAttachmentsHelper.KEY_EXECUTABLE_CONTENT_TYPES, "text/html application/x-shockwave-flash");
        hostileAttachmentsHelper.parseConfiguration(config);
        assertTrue(hostileAttachmentsHelper.isExecutableContentType("text/HTML"));
        assertTrue(hostileAttachmentsHelper.isExecutableContentType("text/html"));
        assertFalse(hostileAttachmentsHelper.isExecutableContentType("text/css"));
        assertFalse(hostileAttachmentsHelper.isExecutableContentType("text/plain"));
        assertTrue(hostileAttachmentsHelper.isExecutableFileExtension("index.html"));
        assertTrue(hostileAttachmentsHelper.isExecutableFileExtension("Default.htm"));
        assertTrue(hostileAttachmentsHelper.isExecutableFileExtension("tedious-graphic-gratuity.swf"));
        assertFalse(hostileAttachmentsHelper.isExecutableFileExtension("tedious-graphic-gratuity.swf~"));
        assertFalse(hostileAttachmentsHelper.isExecutableFileExtension("tedious-graphic-gratuity.swf.bak"));
        assertFalse(hostileAttachmentsHelper.isExecutableFileExtension("tedious-graphic-gratuity.fla"));
        assertFalse(hostileAttachmentsHelper.isExecutableFileExtension("innocent.txt"));
    }

    private void assertStringParsedAs(String[] expected, String value)
    {
        HostileAttachmentsHelper hostileAttachmentsHelper = new HostileAttachmentsHelper();
        Properties config = new Properties();
        config.setProperty(HostileAttachmentsHelper.KEY_EXECUTABLE_FILE_EXTENSIONS, value);
        config.setProperty(HostileAttachmentsHelper.KEY_EXECUTABLE_CONTENT_TYPES, value);
        hostileAttachmentsHelper.parseConfiguration(config);
        final String[] extensions = hostileAttachmentsHelper.getExecutableFileExtensions();
        assertArraysEqual(expected, extensions);
        final String[] contentTypes = hostileAttachmentsHelper.getExecutableContentTypes();
        assertArraysEqual(expected, contentTypes);
    }

    private void assertArraysEqual(String[] expected, String[] extensions)
    {
        assertTrue("Expected '"  + ArrayUtils.toString(expected) + "' got '" + ArrayUtils.toString(extensions) + "'", Arrays.equals(expected, extensions));
    }
}
