package com.atlassian.jira.velocity;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.FileChecker;

import org.apache.commons.io.IOUtils;

/**
 * Ensures a macro is not defined twice (macro with the name already exists) in one of the global velocity macro files.
 *
 * @since v4.0
 */
class VelocityMacrosTemplatenamesChecker implements FileChecker
{
    static final Pattern MACRO_PATTERN = Pattern.compile("#macro\\s*\\([\\w\\d\\s]*\\)\\s*(?!##LOCAL_MACRO_EXCEPTION.*)$");
    static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*##.*");
    private static final Pattern MACRO_NAME_PATTERN = Pattern.compile("\\([\\s]*[\\w]*");
    Map<String,File> macroNames = new HashMap<String,File>();

    public List<String> checkFile(final File file)
    {
        List<String> fails = new ArrayList<String>();
        LineNumberReader reader = null;
        try
        {
            reader = new LineNumberReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null)
            {
                Matcher macroMatcher = MACRO_PATTERN.matcher(line);
                Matcher commentMatcher = COMMENT_PATTERN.matcher(line);

                if (macroMatcher.find() && !commentMatcher.find())
                {
                    Pattern macroNamePattern = MACRO_NAME_PATTERN;
                    final Matcher macroNameMatcher = macroNamePattern.matcher(macroMatcher.group());
                    if (macroNameMatcher.find())
                    {
                        final String macroName = macroNameMatcher.group().substring(1);
                        if (!macroNames.containsKey(macroName))
                        {
                            macroNames.put(macroName, file);
                        }
                        else
                        {
                            String alreadyPath = macroNames.get(macroName).getAbsolutePath();
                            fails.add("macro name: '" + macroName + "' in '" + file.getAbsolutePath() + "' is already defined in '" + alreadyPath + "'.");
                        }
                    }
                }
            }
        }
        catch (IOException ignore)
        {
            fails.add("ioexception for file '" + file.getPath() + "' : " + ignore.getMessage());
        }
        finally
        {
            if (reader != null)
            {
                IOUtils.closeQuietly(reader);
            }
        }
        return fails;
    }

    public FilenameFilter getFilenameFilter()
    {
        return NonGlobalVelocityTemplateFilter.INSTANCE;
    }

}
