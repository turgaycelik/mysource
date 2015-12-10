package com.atlassian.jira.webtest.capture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Class that produces <a href="http://en.wikipedia.org/wiki/SubRip">SubRip</a> subtitles.
 *
 * @since v4.2
 */
class SubRipSubtitle
{
    private final PrintWriter writer;

    private int currentCount = 1;

    SubRipSubtitle(File file) throws IOException
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

    void writeSubTitle(final String text, long startTime, long endTime)
    {
        if (StringUtils.isBlank(text))
        {
            throw new IllegalArgumentException("text cannot be empty or blank.");
        }

        if (startTime < 0)
        {
            throw new IllegalArgumentException("startTime must be >= 0");
        }

        if (endTime < startTime)
        {
            throw new IllegalArgumentException("duration must be >= startTime");
        }

        if (currentCount > 1)
        {
            writer.println();
        }

        writer.println(currentCount);
        printPeriod(startTime, endTime);
        writer.print(StringUtils.trim(text));
        writer.println();
        writer.flush();

        currentCount++;
    }

    void close()
    {
        writer.close();
    }

    private void printPeriod(long startTime, long endTime)
    {
        if (startTime < endTime)
        {
            outputTime(startTime);
            writer.print(" --> ");
            outputTime(endTime);
            writer.println();
        }
    }

    private void outputTime(long time)
    {
        long milliSeconds = time % 1000L;

        //Time is now in seconds.
        time = time / 1000L;
        long seconds = time % 60;

        //Time is now in minutes.
        time = time / 60;
        long minutes = time % 60;
        long hours = time / 60;

        writer.printf("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliSeconds);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
