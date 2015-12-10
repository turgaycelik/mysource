package com.atlassian.jira.velocity;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.FileChecker;

import org.apache.commons.io.IOUtils;

/**
 * Checks that velocity files have an explicit html escaping directive.
 *
 * @since v5.0
 */
class HtmlEscapeDirectiveChecker implements FileChecker
{
    Pattern ESCAPE_DIRECTIVE_PATTERN = Pattern.compile("^\\s*#(en|dis)able_html_escaping\\(\\)");

    @Override
    public List<String> checkFile(File file)
    {
        List<String> fails = new ArrayList<String>();
        LineNumberReader reader = null;
        try
        {
            boolean foundDirective = false;
            reader = new LineNumberReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                Matcher matcher = ESCAPE_DIRECTIVE_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    foundDirective = true;
                    break;
                }
            }
            if (!foundDirective)
            {
                String message = "No html escape directive, #enable_html_escaping() or #disable_html_escaping() in '"
                        + file.getAbsolutePath() + "'";
                fails.add(message);
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

    @Override
    public FilenameFilter getFilenameFilter()
    {
        return NonGlobalVelocityTemplateFilter.INSTANCE;
    }
}
