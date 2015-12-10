package com.atlassian.jira;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Utility program for testing the contents of translations message bundles looking for known categories of
 * error. Note that this checker does not accept the colon as a key value separator because we don't have a
 * habit of using it, even though it would work fine. The purpose of this checker is to verify the integrity
 * of language packs and translators are unlikely to decide to use the colon for this purpose (therefore it would
 * more likely indicate an error). Having said that it would be easy to add this form to the allowed patterns.
 *
 * This class actually has and deserves a unit test of it's own: {@link com.atlassian.jira.TestTranslationsChecker}
 *
 * TODO: remove special case for /Users/chris/work/jira/src/etc/languages/ja_JP/com/atlassian/jira/plugins/portlets/admin_ja_JP.properties which is currently KNOWN FUCKED
 *
 * @since v3.13
 */
public class TranslationsChecker implements FileChecker
{
    private static final Logger log = Logger.getLogger(TranslationsChecker.class);

    private static final int MAX_PROBLEMS = 100;

    private static final String PROPERTIES_FILE = "^[^.].*\\.properties$";

    private static final FilenameFilter IS_A_PROPERTIES_FILE = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return !"admin_ja_JP.properties".equals(name) && name.matches(PROPERTIES_FILE);
        }
    };

    /**
     * Regex for capturing single quote values which are not a part of a quote escape
     * sequence. TODO: only handles quoted brace subexpressions up to 20 chars in length
     * because zero-width negative lookbehind assertions must have maximum lengths
     */
    private static final String LONE_SINGLE_QUOTE_VALUE = "^[^#=]*=.*[^'](?<!'\\{[^']{0,20})'[^'{}].*";

    /**
     * Picks up invalid Java String escape sequences, note two levels of backslash escaping. Not anal about
     * unicode escapes here.
     */
    static final String INVALID_STRING_ESCAPE_SEQUENCE = ".*((?<!\\\\)\\\\[^\\\\ntfru']).*";

    /**
     * Matches a line that is blank, is a comment  or contains an equals.
     */
    static final String COMMENT_BLANK_OR_PROPERTY = "^(.*[=].*|\\s*)(#.*$|$)";

    private int problemCount = 0;
    private final RegexLineCheck[] propertiesFileCheckRegexes;
    private final ProblemLogger problemLogger;
    private final List<String> problems;

    public TranslationsChecker()
    {

        problemLogger = new ProblemLogger();
        propertiesFileCheckRegexes = new RegexLineCheck[] {
                new RegexLineCheck(LONE_SINGLE_QUOTE_VALUE, "lone single quote", false, problemLogger),
                new RegexLineCheck(INVALID_STRING_ESCAPE_SEQUENCE, "suspect escape sequence", false, problemLogger),
                new RegexLineCheck(COMMENT_BLANK_OR_PROPERTY, "line is none of comment, property assignment, or blank", true, problemLogger),
//                new RegexLineCheck("^[^#=]*=.*[^']'\\{[^']+($|('[^}])+$)", "'{ unmatched with '}", true)
        };
        problems = new ArrayList<String>();
    }

    /**
     * Describe the problems that have been found.
     *
     * @return a text description of found problems.
     */
    public String getProblemsDescription()
    {
        StringBuilder problemDescription = new StringBuilder();
        for (String problem : problems)
        {
            problemDescription.append(problem).append("\n");
        }
        problemDescription.append("Total problems: ").append(problemCount);
        if (problemCount >= MAX_PROBLEMS)
        {
            problemDescription.append(". Maximum problems reached.");
        }
        return problemDescription.toString();
    }

    /**
     * Returns true if the result of all checks is successful.
     *
     * @return true only if all checks that have run are successful.
     */
    public boolean success()
    {
        return problemCount == 0;
    }

    public List<String> checkFile(File propertiesFile)
    {
        doLineChecks(propertiesFile);
        doPropertiesChecks(propertiesFile);
        return null;
    }

    public void checkFile(InputStream propertiesStream, final String fileName)
    {
        try
        {
            doLineChecks(new BufferedReader(new InputStreamReader(propertiesStream)), fileName);
            doPropertiesChecks(propertiesStream, fileName);
        }
        catch (IOException e)
        {
            log.error("problem with " + fileName + ": " + e.getLocalizedMessage());
        }
    }

    public FilenameFilter getFilenameFilter()
    {
        return IS_A_PROPERTIES_FILE;
    }

    void doPropertiesChecks(final File propertiesFile)
    {
        try
        {
            final InputStream stream = new FileInputStream(propertiesFile);
            final String filename = propertiesFile.getName();
            doPropertiesChecks(stream, filename);
        }
        catch (IOException e)
        {
            log.error("problem with " + propertiesFile.getAbsolutePath() + ": " + e.getLocalizedMessage());
        }
    }

    void doPropertiesChecks(final InputStream stream, final String filename) throws IOException
    {
        Properties p = new Properties();
        p.load(stream);
        final Enumeration keys = p.keys();
        while (keys.hasMoreElements() && problemCount < MAX_PROBLEMS)
        {
            final String key = (String) keys.nextElement();
            try
            {
                new MessageFormat(p.getProperty(key));
            }
            catch (IllegalArgumentException e)
            {
                final String mesg = "parse exception : key " + key + ": " + e.getMessage();
                problemLogger.logProblem(mesg, filename);
            }
        }
    }

    void doLineChecks(final File propertiesFile)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(propertiesFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            final String filename = propertiesFile.getName();
            doLineChecks(br, filename);
        }
        catch (IOException e)
        {
            // this shouldn't happen
            throw new RuntimeException(propertiesFile + ": " + e.getMessage());
        }
        finally
        {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
            }
            catch (IOException e)
            {
                log.error(e.getMessage());
            }
        }
    }

    void doLineChecks(final BufferedReader br, final String filename) throws IOException
    {
        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null && problemCount < MAX_PROBLEMS)
        {
            lineNumber++;
            while (line.matches(".*\\\\$"))
            {
                String continuedLine = br.readLine();
                if (continuedLine == null)
                {
                    problemLogger.logProblem("file ends with '\\' ", filename);
                    return;
                }
                lineNumber++;
                line = line.substring(0, line.length() - 1) + continuedLine; // snip off the backslash
            }
            for (RegexLineCheck propertiesFileCheckRegexe : propertiesFileCheckRegexes)
            {
                propertiesFileCheckRegexe.check(filename, line, lineNumber);
            }
        }
    }

    static class RegexLineCheck
    {
        private Pattern regex;
        private String mesg;
        private boolean expectMatch;
        private ProblemLogger problemLogger;

        /**
         * Constructor for a check which uses a regex to decide if a line is OK.
         *
         * @param regexStr      the String form of the regular expression.
         * @param mesg          the message used to describe a problem when found.
         * @param expectMatch   whether or not the regexStr is expected to match valid or invalid lines.
         * @param problemLogger used to write problem descriptions to.
         */
        RegexLineCheck(String regexStr, String mesg, boolean expectMatch, ProblemLogger problemLogger)
        {
            this.regex = Pattern.compile(regexStr);
            this.mesg = mesg;
            this.expectMatch = expectMatch;
            this.problemLogger = problemLogger;
        }

        public void check(String filename, String line, int lineNumber)
        {
            Matcher m = regex.matcher(line);
            final boolean lineMatches = m.matches();
            if (lineMatches != expectMatch)
            {
                problemLogger.logProblem(mesg, filename, lineNumber, line);
            }
        }

        public String toString()
        {
            return mesg + (expectMatch ? " expecting" : " not expecting") + " to match " + regex.pattern();
        }
    }

    private class ProblemLogger
    {
        public void logProblem(final String mesg, String filename, int lineNumber, String line)
        {
            final String messageBuf = new StringBuilder(mesg).append(" in filename: ").append(filename)
                    .append(" line ").append(lineNumber).append(": ").append(line).toString();
            logProblem(messageBuf);
        }

        public void logProblem(final String message)
        {
            problemCount++;
            problems.add(message);
        }

        public void logProblem(final String message, final String filename)
        {
            logProblem(message + " in file: " + filename);
        }
    }
}
