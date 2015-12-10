package com.atlassian.jira;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for TranslationsChecker since it's not trivial.
 *
 * @since v3.13
 */
public class TestTranslationsChecker
{
    private static final String PROPERTIES_FILE_ENCODING = "ISO-8859-1";

    @Test
    public void testDefaultSuccess()
    {
        assertTrue(new TranslationsChecker().success());
        assertEquals("Total problems: 0", new TranslationsChecker().getProblemsDescription());
    }

    @Test
    public void testDoPropertieschecks() throws IOException
    {
        assertPropertiesCheck(false, "foo=bar{0");
        assertPropertiesCheck(false, "foo=bar{0}{");
        assertPropertiesCheck(false, "foo={f}bar");
        assertPropertiesCheck(true, "foo=bar");
        assertPropertiesCheck(true, "{f}oo=bar{0}{1}");
        assertPropertiesCheck(true, "'{f}oo=bar{0}{1}");
        assertPropertiesCheck(false, "admin.roles.translations=\\u3000\\u30d7\\u30ed\\u30b8\\u30a7\\u30af\\u30c8\\u30ed\\u30fc\\u30eb\\u3092 {0\\u5909\\u63db\\u3059\\u308b{1}");
    }

    @Test
    public void testLineChecksHappy() throws IOException
    {
        assertLineCheck(true, "#multiline \\\n comment");
        assertLineCheck(true, "#comment");
        assertLineCheck(true, " #comment");
        assertLineCheck(true, " "); // blank line
        assertLineCheck(true, " \t"); // blank line
        assertLineCheck(true, " \n \n"); // blank lines

        assertLineCheck(true, "message format = with {0} replacements");
        assertLineCheck(true, "normal = property assignment ");
        assertLineCheck(true, "correct.line.continuation = fate''s \\\ndoginess"); // correct line continuation and single quote
        assertLineCheck(true, "gadget.admin.tasks = Probl\\u00e9my\n\n"); // legit unicode escape
        // correct  double backslash representing a literal backslash. note our string must do second escape for java string literal
        assertLineCheck(true, "some.path=Full path to the CSV file located on your server - e.g. D:\\\\temp\\\\myimport.csv");
    }

    @Test
    public void testLineChecksRainy() throws IOException
    {
        assertLineCheck(false, "incorrect.single.quote = fate's dodgyness");
        assertLineCheck(false, "incorrect.line.continuation = fate''s \ndodgyness");
        assertLineCheck(false, "no equals sign and not a comment");
        assertLineCheck(false, "# a comment but EOF with a \\");
        assertLineCheck(false, "something before # a comment does not work like this");
        assertLineCheck(false, "key = value \\# no need to escape");
        assertLineCheck(false, "key = value \\= no need to escape");
        assertLineCheck(false, "key = value \\: no need to escape");
        // cut keys from Slovakia
        assertLineCheck(false, "common.concepts.\n"
                               + "manageattachments = Spravova\\u0165 pr\\u00edlohy");
        // bad string escapes from Turkey
        assertLineCheck(false, "admin.schemes.workflow.warning = <font color\\=cc0000>D\\u0130KKAT\\: <webwork\\:property value\\=\"name\" /> ili\\u015Fiklendirildimi\\u015F durumda\\:\n");
        // bad single quote from France
        assertLineCheck(false, "wiki.renderer.help.advanced.noformat.desc=Cr\\u00e9e un bloc pr\\u00e9format\\u00e9 de texte sans faire ressortir la syntaxe. Tous les param\\u00e8tres en option de la macro '{panel'}' sont \\u00e9galement valides pour '{noformat'}.");
        assertLineCheck(false, "special case for properties file = \\b doesn''t mean backslash not an error but never desirable");
    }

    @Test
    public void testEscapeSequenceRegex()
    {
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, "\\:");
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, "\\=");
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, " \\=");
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, "\\=\\:");
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, " \\=cc ... \\: ");
        assertMatch(TranslationsChecker.INVALID_STRING_ESCAPE_SEQUENCE, "admin.schemes.workflow.warning = <font color\\=cc ... \\:");
    }

    private void assertMatch(String regex, String target)
    {
        assertTrue(regex + " didn't match '" + target + "'", target.matches(regex));
    }

    @Test
    public void testCommentBlankOrPropertyRegex()
    {
        assertTrue("property = value #comment".matches(TranslationsChecker.COMMENT_BLANK_OR_PROPERTY));
        assertFalse("should ignore postcomments and fail on pre-comment section", "property value #comment".matches(TranslationsChecker.COMMENT_BLANK_OR_PROPERTY));
    }

    private void assertLineCheck(final boolean expectedSuccess, final String fileContents) throws IOException
    {
        TranslationsChecker tc = new TranslationsChecker();
        tc.doLineChecks(new BufferedReader(new StringReader(fileContents)), "somefile");
        assertEquals(expectedSuccess ? tc.getProblemsDescription() : fileContents, expectedSuccess, tc.success());
    }

    private void assertPropertiesCheck(boolean expectedSuccess, final String fileContents) throws IOException
    {
        TranslationsChecker tc = new TranslationsChecker();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes(PROPERTIES_FILE_ENCODING));
        tc.doPropertiesChecks(inputStream, "thefile");
        assertEquals(expectedSuccess ? tc.getProblemsDescription() : fileContents, expectedSuccess, tc.success());
    }

}
