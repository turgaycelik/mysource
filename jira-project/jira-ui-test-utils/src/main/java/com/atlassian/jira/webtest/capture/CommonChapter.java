package com.atlassian.jira.webtest.capture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Outputs writes common chapter output. That is:
 * CHAPTERXX=hh:mm:ss.sss
 * CHPATERXXNAME=Chapter Title
 *
 * @since v4.2
 */
class CommonChapter
{
    private final PrintWriter writer;
    private int count = 1;

    CommonChapter(final File file) throws IOException
    {
        final FileOutputStream fileOutputStream = new FileOutputStream(file, false);
        try
        {
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
            writer = new PrintWriter(bufferedWriter);
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(fileOutputStream);
            throw e;
        }
    }

    void writeChapter(final String text, final long startTime)
    {
        if (StringUtils.isBlank(text))
        {
            throw new IllegalArgumentException("text cannot be empty or blank.");
        }

        long time = startTime;
        long milliSeconds = time % 1000L;

        //Time is now in seconds.
        time = time / 1000L;
        long seconds = time % 60;

        //Time is now in minutes.
        time = time / 60;
        long minutes = time % 60;
        long hours = time / 60;

        writer.printf("CHAPTER%02d=%02d:%02d:%02d.%03d%n", count, hours, minutes, seconds, milliSeconds);
        writer.printf("CHAPTER%02dNAME=%s%n", count, text);
        writer.flush();

        count = count + 1;
    }

    void close()
    {
        writer.close();
    }
}
