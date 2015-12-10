package com.atlassian.jira.web.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class TestFileNameCharacterCheckerUtil
{
    public static final char[] INVALID_CHARS = { '\\', '/','\"', ':','?', '*', '<','|','>' };

    @Test
    public void testFileNameDoesNotContainInvalidChars()
    {
        FileNameCharacterCheckerUtil fileNameCharacterCheckerUtil = new FileNameCharacterCheckerUtil();

        //valid filenames
        assertNull(fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars(null));
        assertNull(fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars(""));
        assertNull(fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("valid_name"));

        //filenames with one invalid character
        assertEquals("/", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("/invalidname"));
        assertEquals(":", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("in:validname"));
        assertEquals("?", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("inva?lidname"));
        assertEquals("*", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("inval*idname"));
        assertEquals("<", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invali<dname"));
        assertEquals("|", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invalid|name"));
        assertEquals(">", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invalidn>ame"));
        assertEquals("\\", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invalidna\\me"));
        assertEquals("\"", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invalidnam\"e"));
        assertEquals("/", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("invalidname/"));

        //filenames with multiple invalid character
        assertEquals("/", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("/i%nvalidname"));
        assertEquals(":", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("in:v$al*idn<ame"));
        assertEquals("?", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("inva?li<*d>name"));
        assertEquals("*", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("*inval*idname*"));
        assertEquals("<", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("<invalidname>"));
        //Note test expects / instead of $ because the implementation of the method checks / before $ 
        assertEquals("/", fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars("inv$/\"alidname"));
    }

    @Test
    public void testReplaceInvalidChars()
    {
        FileNameCharacterCheckerUtil fileNameCharacterCheckerUtil = new FileNameCharacterCheckerUtil();

        assertNull(fileNameCharacterCheckerUtil.replaceInvalidChars(null, '_' ));
        assertEquals("", fileNameCharacterCheckerUtil.replaceInvalidChars("", '_'));
        assertEquals("valid", fileNameCharacterCheckerUtil.replaceInvalidChars("valid", '_'));
        assertEquals("valid_name.eXt", fileNameCharacterCheckerUtil.replaceInvalidChars("valid_name.eXt", '_'));

        assertEquals("invalid", fileNameCharacterCheckerUtil.replaceInvalidChars("|nvalid", 'i'));
        assertEquals("invalid", fileNameCharacterCheckerUtil.replaceInvalidChars("|nval|d", 'i'));
        assertEquals("invalid_name", fileNameCharacterCheckerUtil.replaceInvalidChars("invalid?name", '_'));
        assertEquals("invalid.extension", fileNameCharacterCheckerUtil.replaceInvalidChars("invalid.exten?ion", 's'));
        assertEquals("_ends_", fileNameCharacterCheckerUtil.replaceInvalidChars("<ends>", '_'));
        assertEquals("_________.PDF", fileNameCharacterCheckerUtil.replaceInvalidChars("\\/\":?*<|>.PDF", '_'));

        try
        {
            fileNameCharacterCheckerUtil.replaceInvalidChars("invalid_replacement_char", '*');
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expectedException)
        {
        }
    }

    @Test
    public void testPrintableInvalidCharacters()
    {
        assertEquals("'\\', '/', '\"', ':', '?', '*', '<', '|', '>'", new FileNameCharacterCheckerUtil().getPrintableInvalidCharacters());
    }
}
