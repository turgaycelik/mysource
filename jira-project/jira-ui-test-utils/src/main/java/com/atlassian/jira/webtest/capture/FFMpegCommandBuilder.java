package com.atlassian.jira.webtest.capture;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * A command line builder for FFMpeg.
 *
 * @since v4.3
 */
public class FFMpegCommandBuilder
{
    private static final String CMD_FFMPEG = "ffmpeg";

    private final List<CommandBuilder> commands = new LinkedList<CommandBuilder>();
    private FFMpegCommandListener listener;

    public FFMpegCommandBuilder()
    {
    }

    public FFMpegCommand start() throws FFMpegException
    {
        ProcessBuilder builder = new ProcessBuilder(getCommands());
        builder.redirectErrorStream(true);
        final FFMpegCommand command = new FFMpegCommand(builder, listener);
        command.start();
        return command;
    }

    public FFMpegCommandBuilder setListener(final FFMpegCommandListener listener)
    {
        this.listener = new SafeCommandListener(listener);
        return this;
    }

    public FFMpegInputBuilder addInput(final String location)
    {
        final FFMpegInputBuilder builder = new FFMpegInputBuilder();
        commands.add(builder);
        return builder.setInputLocation(location);
    }

    public FFMpegOutputBuilder addOutput(final String location)
    {
        final FFMpegOutputBuilder builder = new FFMpegOutputBuilder();
        commands.add(builder);
        return builder.setOutputLocation(location);
    }

    private List<String> getCommands()
    {
        final List<String> arguments = new LinkedList<String>();
        arguments.add(CMD_FFMPEG);

        for (CommandBuilder command : commands)
        {
            command.addArguments(arguments);
        }

        return arguments;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    private interface CommandBuilder
    {
        void addArguments(List<String> arguments);
    }

    public static class FFMpegInputBuilder implements CommandBuilder
    {
        private static final String INPUT_SWITCH = "-i";

        private final FFMpegMediaBuilder mediaBuilder = new FFMpegMediaBuilder(INPUT_SWITCH);

        public FFMpegInputBuilder setRate(int rate)
        {
            mediaBuilder.setRate(rate);
            return this;
        }

        public FFMpegInputBuilder setSize(int x, int y)
        {
            mediaBuilder.setSize(x, y);
            return this;
        }

        public FFMpegInputBuilder setInputLocation(final String location)
        {
            mediaBuilder.setLocation(location);
            return this;
        }

        public FFMpegInputBuilder setFormat(final String format)
        {
            mediaBuilder.setFormat(format);
            return this;
        }

        public void addArguments(final List<String> arguments)
        {
            mediaBuilder.addArguments(arguments);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public class FFMpegOutputBuilder implements CommandBuilder
    {
        private static final String SWITCH_OVERWRITE = "-y";
        private static final String SWITCH_VCODEC = "-vcodec";
        private static final String SWITCH_VIDEO_PRESET = "-vpre";
        private static final String SWITCH_GOP = "-g";
        public static final String SWITCH_TIME = "-t";

        private final FFMpegMediaBuilder mediaBuilder = new FFMpegMediaBuilder(null);
        private String videoCodec;
        private boolean overwrite;
        private String videoPreset;
        private long gop;
        private long maxTime;

        public FFMpegOutputBuilder setRate(int rate)
        {
            mediaBuilder.setRate(rate);
            return this;
        }

        public FFMpegOutputBuilder setSize(int x, int y)
        {
            mediaBuilder.setSize(x, y);
            return this;
        }

        public FFMpegOutputBuilder setOutputLocation(final String location)
        {
            mediaBuilder.setLocation(location);
            return this;
        }

        public FFMpegOutputBuilder setFormat(final String format)
        {
            mediaBuilder.setLocation(format);
            return this;
        }

        public FFMpegOutputBuilder setOutputCodec(final String codec)
        {
            this.videoCodec = StringUtils.trimToNull(codec);
            return this;
        }

        public FFMpegOutputBuilder setOverwrite(final boolean overwrite)
        {
            this.overwrite = overwrite;
            return this;
        }

        public FFMpegOutputBuilder setPreset(final String preset)
        {
            this.videoPreset = StringUtils.trimToNull(preset);
            return this;
        }

        public FFMpegOutputBuilder setGop(final long gop)
        {
            this.gop = gop;
            return this;
        }

        public FFMpegOutputBuilder setMaxTime(TimeUnit unit, long time)
        {
            this.maxTime = unit.toSeconds(time);
            return this;
        }

        public void addArguments(final List<String> arguments)
        {
            if (this.overwrite)
            {
                arguments.add(SWITCH_OVERWRITE);
            }

            if (videoCodec != null)
            {
                arguments.add(SWITCH_VCODEC);
                arguments.add(videoCodec);
            }

            if (videoPreset != null)
            {
                arguments.add(SWITCH_VIDEO_PRESET);
                arguments.add(videoPreset);
            }

            if (gop > 0)
            {
                arguments.add(SWITCH_GOP);
                arguments.add(String.valueOf(gop));
            }

            if (maxTime > 0)
            {
                arguments.add(SWITCH_TIME);
                arguments.add(String.valueOf(maxTime));
            }

            mediaBuilder.addArguments(arguments);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    private static class FFMpegMediaBuilder
    {
        private static final String SWITCH_RATE = "-r";
        private static final String SWITCH_SIZE = "-s";
        private static final String SWITCH_FORMAT = "-f";

        private final String fileSwitch;

        private int rate = -1;
        private int x = -1;
        private int y = -1;
        private String format;
        private String location;

        public FFMpegMediaBuilder(final String fileSwitch)
        {
            this.fileSwitch = fileSwitch;
        }

        public FFMpegMediaBuilder setRate(final int rate)
        {
            this.rate = rate;
            return this;
        }

        public FFMpegMediaBuilder setSize(final int x, final int y)
        {
            if ((x <= 0 && y > 0) || (y <= 0 && x > 0))
            {
                throw new IllegalArgumentException("Both arguments must be positive or negative.");
            }
            this.x = x;
            this.y = y;
            return this;
        }

        public FFMpegMediaBuilder setLocation(final String location)
        {
            this.location = StringUtils.trimToNull(location);
            return this;
        }

        public FFMpegMediaBuilder setFormat(final String format)
        {
            this.format = StringUtils.trimToNull(format);
            return this;
        }

        public void addArguments(final List<String> arguments)
        {
            if (location == null)
            {
                throw new IllegalStateException("No input location specified.");
            }

            if (rate > 0)
            {
                arguments.add(SWITCH_RATE);
                arguments.add(String.valueOf(rate));
            }

            if (x > 0 && y > 0)
            {
                arguments.add(SWITCH_SIZE);
                arguments.add(format("%dx%d", x, y));
            }

            if (format != null)
            {
                arguments.add(SWITCH_FORMAT);
                arguments.add(format);
            }

            if (fileSwitch != null)
            {
                arguments.add(fileSwitch);
            }
            arguments.add(location);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
