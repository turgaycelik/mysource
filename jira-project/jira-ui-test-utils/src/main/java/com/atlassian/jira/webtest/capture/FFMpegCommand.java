package com.atlassian.jira.webtest.capture;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an FFMpeg processing running on the OS.
 *
 * @since v4.2
 */
public class FFMpegCommand
{
    private static final Logger log = Logger.getLogger(FFMpegCommand.class);

    private final Process process;
    private final PrintWriter writer;
    private final Thread inputThread;
    private final Thread errorThread;
    private final FFMpegOutputProcessor processor;

    FFMpegCommand(final ProcessBuilder processBuilder, FFMpegCommandListener listener)
            throws FFMpegException
    {
        try
        {
            this.process = processBuilder.start();
        }
        catch (IOException e)
        {
            throw new FFMpegException(e);
        }

        //Use the system encoding as this is what the process will probably output.
        this.writer = new PrintWriter(process.getOutputStream(), true);
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.processor = new FFMpegOutputProcessor(listener, process, writer);
        this.inputThread = new Thread(new ReaderCallable(inputReader, processor), "FFMpeg input thread.");
        this.inputThread.setDaemon(true);
        this.errorThread = new Thread(new ReaderCallable(errorReader, processor), "FFMpeg error thread.");
        this.errorThread.setDaemon(true);
    }

    void start()
    {
        inputThread.start();
        errorThread.start();
    }

    public void quit()
    {
        sendLine("q");
    }

    public void forceQuit()
    {
        process.destroy();
        if (inputThread.isAlive())
        {
            inputThread.interrupt();
        }
        if (errorThread.isAlive())
        {
            errorThread.interrupt();
        }
    }

    public int waitForExit(long timeout) throws FFMpegException
    {
        if (!processor.waitForEnd(timeout))
        {
            throw new FFMpegException(String.format("FFMpeg was still running after %dms.", timeout));
        }
        else
        {
            return process.exitValue();
        }
    }

    public int exitValue()
    {
        try
        {
            return process.exitValue();
        }
        catch (IllegalThreadStateException e)
        {
            return Integer.MIN_VALUE;
        }
    }

    private void sendLine(String line)
    {
        writer.write(line);
        writer.flush();
    }

    /**
     * Runnable that reads the input from the passed reader and passes it to the passed processor.
     */
    private static final class ReaderCallable implements Runnable
    {
        private final BufferedReader reader;
        private final FFMpegOutputProcessor processor;

        public ReaderCallable(final BufferedReader reader, final FFMpegOutputProcessor processor)
        {
            this.reader = reader;
            this.processor = processor;
        }

        public void run()
        {
            processor.started();
            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    processor.processLine(line);
                }
            }
            catch (IOException e)
            {
                log.error("Error occurred while reading from FFMpeg process stream.", e);
            }
            final Runnable runnable = processor.processEof();
            if (runnable != null)
            {
                runnable.run();
            }
        }
    }

    /**
     * Parses the output of FFMpeg and produces make callbacks to the passed listener.
     */
    private static class FFMpegOutputProcessor
    {
        private static final Pattern PATTERN_FRAME = Pattern.compile("frame\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        private static final Pattern PATTERN_SIZE = Pattern.compile("size\\s*=\\s*(\\d+)kB", Pattern.CASE_INSENSITIVE);
        private static final Pattern PATTERN_DROP = Pattern.compile("drop\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        private static final Pattern PATTERN_TIME = Pattern.compile("time\\s*=\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);

        private static final long BAD_TIME_LIMIT = 1000000L;

        enum State
        {
            INITIAL, RUNNING, ENDED
        }

        private final FFMpegCommandListener listener;
        private final Process process;
        private final Closeable closeOnExit;
        
        private State currentState = State.INITIAL;
        private int currentDrop = 0;
        private int endCount = 0;

        private long timeDelta = -1;
        private long timeStart = -1;

        public FFMpegOutputProcessor(final FFMpegCommandListener listener, final Process process, final Closeable closeOnExit)
        {
            this.listener = listener == null ? new NoopCommandListener() : listener;
            this.process = process;
            this.closeOnExit = closeOnExit;
        }

        public synchronized void started()
        {
            if (currentState == State.INITIAL)
            {
                listener.start();
                currentState = State.RUNNING;
            }
        }

        public synchronized Runnable processEof()
        {
            endCount++;

            //We only want to signal when both threads finish.
            if (endCount < 2)
            {
                return null;
            }

            try
            {
                int exitValue = process.exitValue();
                signalExit(exitValue);
                return null;
            }
            catch (IllegalThreadStateException ignored)
            {
                //It appears that both streams are closed and the process is still running, how strange.
                return new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            signalExit(process.waitFor());
                        }
                        catch (InterruptedException ignored)
                        {
                            //ignored
                        }
                    }
                };
            }
        }

        private synchronized void signalExit(final int exitValue)
        {
            if (currentState == State.RUNNING)
            {
                listener.end(exitValue);
            }
            closeQuietly(closeOnExit);
            currentState = State.ENDED;
            notifyAll();
        }

        public synchronized void processLine(final String line)
        {
            if (currentState != State.RUNNING)
            {
                return;
            }

            final double parsedTime = parseDouble(PATTERN_TIME, line);
            if (!Double.isNaN(parsedTime))
            {
                /**
                 * For some reason, ffmpeg does not give accurate times to start. I guess this is because the x264 codec
                 * does not start output frames until are around ~ 10sec of video is input.
                 *
                 * To get around this we use the wall clock to an estimate of the time at the start.
                 */
                long actualTime;
                if (timeStart < 0)
                {
                    timeStart = System.currentTimeMillis();
                    if (parsedTime > BAD_TIME_LIMIT)
                    {
                        actualTime = 0;
                    }
                    else
                    {
                        timeDelta = 0;
                        actualTime = Math.round(parsedTime * 1000);
                    }
                }
                else if (timeDelta < 0)
                {
                    if (parsedTime > BAD_TIME_LIMIT)
                    {
                        actualTime = System.currentTimeMillis() - timeStart;
                    }
                    else
                    {
                        timeDelta = System.currentTimeMillis() - timeStart;
                        actualTime = Math.round(parsedTime * 1000 + timeDelta);
                    }
                }
                else
                {
                    actualTime = Math.round(parsedTime * 1000 + timeDelta);
                }

                long size = parseLong(PATTERN_SIZE, line);
                if (size > 0)
                {
                    //lets assume that the overflow does not occur because I am lazy.
                    size = size * 1024;
                }
                final int drops = parseInt(PATTERN_DROP, line);
                int newDrops = -1;
                if (drops > currentDrop)
                {
                    newDrops = currentDrop - drops;
                    currentDrop = drops;
                }
                final int frame = parseInt(PATTERN_FRAME, line);
                listener.progress(new FFMpegProgressEvent(line, frame, actualTime, size, newDrops));
            }
            else
            {
                listener.outputLine(line);
            }
        }

        public synchronized boolean waitForEnd(long timeout)
        {
            if (currentState == State.ENDED)
            {
                return true;
            }

            long currentTime = System.currentTimeMillis();
            final long endTime = currentTime + timeout;

            try
            {
                while (currentTime < endTime && currentState != State.ENDED)
                {
                    wait(endTime - currentTime);
                    currentTime = System.currentTimeMillis();
                }
            }
            catch (InterruptedException ignored)
            {
                //ignored.
            }
            return currentState == State.ENDED;
        }

        private static int parseInt(Pattern pattern, CharSequence sequence)
        {
            final Matcher matcher = pattern.matcher(sequence);
            if (matcher.find())
            {
                try
                {
                    return Integer.parseInt(matcher.group(1));
                }
                catch (NumberFormatException ignored)
                {
                    //fall through.
                }
            }

            return Integer.MIN_VALUE;
        }

        private static long parseLong(Pattern pattern, CharSequence sequence)
        {
            final Matcher matcher = pattern.matcher(sequence);
            if (matcher.find())
            {
                try
                {
                    return Long.parseLong(matcher.group(1));
                }
                catch (NumberFormatException ignored)
                {
                    //fall through.
                }
            }

            return Long.MIN_VALUE;
        }

        private static double parseDouble(Pattern pattern, CharSequence sequence)
        {
            final Matcher matcher = pattern.matcher(sequence);
            if (matcher.find())
            {
                try
                {
                    return Double.parseDouble(matcher.group(1));
                }
                catch (NumberFormatException ignored)
                {
                    //fall through.
                }
            }

            return Double.NaN;
        }

        private static void closeQuietly(Closeable closeMe)
        {
            if (closeMe != null)
            {
                try
                {
                    closeMe.close();
                }
                catch (IOException ignored)
                {
                    //ignored.
                }
            }
        }
    }
}
