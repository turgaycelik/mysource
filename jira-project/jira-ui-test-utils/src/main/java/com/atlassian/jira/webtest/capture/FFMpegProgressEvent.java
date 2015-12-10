package com.atlassian.jira.webtest.capture;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Event used to indicate the progress made by FFMpeg.
 *
 * @since v4.2
 */
public class FFMpegProgressEvent
{
    private final int frame;
    private final long time;
    private final long size;
    private final int dropped;
    private final String line;

    FFMpegProgressEvent(final String line, final int frame, final long time, final long size, final int dropped)
    {
        this.line = line;
        this.frame = frame;
        this.time = time;
        this.size = size;
        this.dropped = dropped;
    }

    public int getFrame()
    {
        return frame;
    }

    public boolean hasFrame()
    {
        return frame > 0;
    }

    public long getTime()
    {
        return time;
    }

    public boolean hasTime()
    {
        return time >= 0;
    }

    public long getSize()
    {
        return size;
    }

    public boolean hasSize()
    {
        return size >= 0;
    }

    public String getLine()
    {
        return line;
    }

    public boolean hasDropped()
    {
        return dropped >= 0;
    }

    public int getDropped()
    {
        return dropped;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
