package com.atlassian.jira.webwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.util.Function;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This test verifies that i18n keys used in the webwork text tag e.g. <ww:text name="'sample.key'"/> are escaped.
 *
 * @since v4.0
 */
public class TestWebworkI18N
{
    private static final File JSP_DIR = new File("../../jira-webapp/src/main/webapp");

    //Regex that will file all "name" attributes in a ww:text tag.
    private final static Pattern KEY_PATTEN = Pattern.compile("<(?:webwork|ww):text[^>]*?name=\"([^\"]*)\"");
    private final static List<Function<String, String>> CHECKS = Arrays.asList(new EscapeCheck(), new DoubleCheck());

    @Test
    public void testi18NWebworksTags()
    {
        assertTrue(JSP_DIR.exists());
        assertTrue(JSP_DIR.isDirectory());
        processDirectory(JSP_DIR);
    }

    private void processDirectory(final File jspDirectory)
    {
        final List<File> subDirectories = new LinkedList<File>();
        final File[] jspFiles = jspDirectory.listFiles(new FileFilter()
        {
            public boolean accept(final File path)
            {
                if (path.isDirectory())
                {
                    subDirectories.add(path);
                    return false;
                }
                return path.getName().endsWith(".jsp");
            }
        });
        validateFiles(jspFiles);
        for (File directory : subDirectories)
        {
            processDirectory(directory);
        }
    }

    private void validateFiles(File[] jspFiles)
    {
        for (File jspFile : jspFiles)
        {
            LineNumberReader reader = null;
            try
            {
                reader = new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(jspFile), "iso-8859-1")));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    final Matcher matcher = KEY_PATTEN.matcher(line);
                    while (matcher.find())
                    {
                        final String key = matcher.group(1);
                        for (Function<String, String> check : CHECKS)
                        {
                            final String msg = check.get(key);
                            if (msg != null)
                            {
                                fail(String.format("Error in %s(%d:%d) around %s: %s", jspFile, reader.getLineNumber(), matcher.start() + 1, matcher.group(), msg));
                            }
                        }
                    }
                }
            }
            catch (IOException ignored)
            {
                /**
                 * Ignoring
                 */
            }
            finally
            {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    /**
     * Check to see that ww:text tag argument is a string when referring to a constant key.
     */
    private static class EscapeCheck implements Function<String, String>
    {
        //Find all arguments that look like keys. We do this by looking for a word followed by a DOT.
        private static final Pattern PATTERN_IS_KEY = Pattern.compile("[^']\\w+\\..*");
        private static final char QUOTE_CHAR = '\'';

        public String get(final String input)
        {
            if (PATTERN_IS_KEY.matcher(input).matches())
            {
                return String.format("Unescaped i18n key \"%s\".", input);
            }
            return null;
        }
    }

    /**
     * Check to see if text() method is called within webwork:text tag.
     */
    private static class DoubleCheck implements Function<String, String>
    {
        //Find all text() calls.
        private static final Pattern PATTERN_TEXT_CALL = Pattern.compile("(?:get)?text\\(.*\\)", Pattern.CASE_INSENSITIVE);

        public String get(final String input)
        {
            if (PATTERN_TEXT_CALL.matcher(input).find())
            {
                return String.format("Call to text() within ww:text key '%s'.", input);
            }
            return null;
        }
    }
}

