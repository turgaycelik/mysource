package com.atlassian.jira.webtest.capture;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.functest.framework.WebTestListener;
import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.testkit.client.log.MavenEnvironment;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.apache.commons.io.output.NullOutputStream;

import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * Listens to the selenium tests and records them.
 *
 * @since v4.2
 */
public class FFMpegSuiteListener implements WebTestListener
{
    private static final long COMMAND_TIMEOUT = 20000;

    //WARNING: Changing any of these names will require a change in unTestRunner-Linux.sh.
    private static final String OUTPUT_CAPTURE = "capture_raw.mkv";
    private static final String OUTPUT_SUBTITLES = "capture.srt";
    //We don't use XML extension because bamboo will try and parse it as test results file.
    private static final String OUTPUT_CHAPTERS_XML = "capture.chap";
    private static final String OUTPUT_CHAPTERS_COMMON = "chapters.txt";
    public static final int MAX_FFMPDEG_TIME = 60;

    private static enum State
    {
        INITIAL, STARTING, RUNNING, EXITED, FAIL
    }

    private static final Predicate<WebTestDescription> ALWAYS_RECORD = new Predicate<WebTestDescription>()
    {
        @Override
        public boolean apply(@Nullable WebTestDescription input)
        {
            return true;
        }
    };

    private final Predicate<WebTestDescription> shouldRecord;

    private FFMpegCommand ffmpeg;
    private State state;
    private FudgerTimedTestListener timedListener;
    private long clock;

    //We only want to record tests that have a failure.
    private boolean error = false;
    private final Set<File> files = new LinkedHashSet<File>();

    //True when the tests are running.
    private boolean running = false;

    public FFMpegSuiteListener(final Predicate<WebTestDescription> shouldRecord)
    {
        this.shouldRecord = shouldRecord;
    }

    public FFMpegSuiteListener()
    {
        this.shouldRecord = ALWAYS_RECORD;
    }

    public void suiteStarted(WebTestDescription suiteDescription)
    {
        running = true;

        setState(State.INITIAL);
    }

    public synchronized void suiteFinished(WebTestDescription suiteDescription)
    {
        running = false;

        if (state == State.RUNNING || state == State.STARTING)
        {
            ffmpeg.quit();
            if (!waitForStateCondition(createStateCondition(State.EXITED), COMMAND_TIMEOUT))
            {
                FuncTestOut.log(String.format("FFMpeg did not finish within %dms.", COMMAND_TIMEOUT));
                ffmpeg.forceQuit();
                close();
            }
        }

        if (state == State.EXITED)
        {
            final int i = ffmpeg.exitValue();
            if (i != 0)
            {
                FuncTestOut.log("FFMpeg finished with exit code: " + i);
            }
        }

        if (state != State.FAIL && !error)
        {
            deleteFiles();
        }
    }

    @Override
    public synchronized void testError(WebTestDescription test, Throwable throwable)
    {
        if (!shouldRecord(test))
        {
            return;
        }

        error = true;

        if (state != State.RUNNING)
        {
            return;
        }

        timedListener.addError(test, throwable, clock);
    }

    @Override
    public synchronized void testFailure(final WebTestDescription test, final Throwable assertionFailedError)
    {
        if (!shouldRecord(test))
        {
            return;
        }

        error = true;

        if (state != State.RUNNING)
        {
            return;
        }

        timedListener.addFailure(test, assertionFailedError, clock);
    }

    @Override
    public synchronized void testFinished(WebTestDescription test)
    {
        if (!shouldRecord(test) || state != State.RUNNING)
        {
            return;
        }
        timedListener.endTest(test, clock);
    }

    @Override
    public synchronized void testStarted(final WebTestDescription test)
    {
        if (!shouldRecord(test))
        {
            return;
        }

        if (state == State.INITIAL)
        {
            ffmpeg = startFFMpeg();
            if (ffmpeg != null)
            {
                FudgerTimedTestListener tmpListener = createTimedListener();
                setState(State.STARTING);
                if (waitForStateCondition(createNotStateCondition(State.STARTING), COMMAND_TIMEOUT))
                {
                    if (this.state == State.RUNNING)
                    {
                        tmpListener.start(clock);
                        this.timedListener = tmpListener;
                    }
                }
                else
                {
                    FuncTestOut.log(String.format("FFMpeg did not start within %dms.", COMMAND_TIMEOUT));
                    ffmpeg.quit();
                    if (!waitForStateCondition(createNotStateCondition(State.STARTING), COMMAND_TIMEOUT))
                    {
                        FuncTestOut.log(String.format("FFMpeg did not exit within %dms.", COMMAND_TIMEOUT));
                        ffmpeg.forceQuit();
                    }
                    setState(State.FAIL);
                }
            }
            else
            {
                setState(State.FAIL);
            }
        }

        if (state == State.RUNNING)
        {
            timedListener.startTest(test, clock);
        }
    }

    private void deleteFiles()
    {
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); )
        {
            File file = iterator.next();
            if (file.exists() && !file.delete())
            {
                FuncTestOut.log(String.format("Unable to delete '%s' on passing test.", file.getAbsolutePath()));
            }
            else
            {
                iterator.remove();
            }
        }
    }

    private FFMpegCommand startFFMpeg()
    {
        final String display = trimToNull(System.getenv("DISPLAY"));
        if (display == null)
        {
            FuncTestOut.log("Unable to record selenium tests. No DISPLAY set.");
            //No display = No XServer so no recording.
            return null;
        }

        final FFMpegCommandBuilder builder = new FFMpegCommandBuilder();

        //We need to pass "+nomouse" as otherwise we will get a segfault error in FFMPEG when it connects to
        // XServers that don't implement the XFixes extension.
        builder.addInput(display + "+nomouse").setFormat("x11grab").setRate(4).setSize(1024, 768);
        builder.addOutput(createOutputAndSave(OUTPUT_CAPTURE).getAbsolutePath()).setOutputCodec("libx264")
                .setPreset("libx264-normal").setSize(512, 384).setOverwrite(true).setGop(40)
                .setMaxTime(TimeUnit.MINUTES, MAX_FFMPDEG_TIME);
        builder.setListener(new ProcessListener(createOutputAndSave("ffmpeg.log")));

        try
        {
            return builder.start();
        }
        catch (FFMpegException e)
        {
            FuncTestOut.log("Unable to record selenium tests: " + e.getMessage());
            return null;
        }
    }

    private File createOutputAndSave(final String name)
    {
        final File file = createOutputFile(name);
        files.add(file);
        return file;
    }

    private static File createOutputFile(final String name)
    {
        return new File(MavenEnvironment.getMavenAwareOutputDir(), name);
    }

    private boolean shouldRecord(final WebTestDescription test)
    {
        return shouldRecord.apply(test);
    }

    private synchronized void setState(State state)
    {
        this.state = state;
        notifyAll();
    }

    private synchronized void exited()
    {
        close();
        setState(State.EXITED);

        if (running)
        {
            FuncTestOut.log("Video recording finished before test completed. Did the test hit the "+MAX_FFMPDEG_TIME+"min limit?.");
        }
    }

    private synchronized void setClock(long time)
    {
        clock = time;
        if (this.state == State.STARTING)
        {
            setState(State.RUNNING);
        }
    }

    private static Function<State, Boolean> createNotStateCondition(final State notState)
    {
        return new Function<State, Boolean>()
        {
            public Boolean apply(final State currentState)
            {
                return currentState != notState;
            }
        };
    }

    private static Function<State, Boolean> createStateCondition(final State state)
    {
        return new Function<State, Boolean>()
        {
            public Boolean apply(final State currentState)
            {
                return currentState == state;
            }
        };
    }

    private synchronized boolean waitForStateCondition(Function<State, Boolean> condition, long timeout)
    {
        if (condition.apply(state))
        {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        final long endTime = timeout + currentTime;

        try
        {
            while (currentTime < endTime && !condition.apply(state))
            {
                wait(endTime - currentTime);
                currentTime = System.currentTimeMillis();
            }
        }
        catch (InterruptedException ignored)
        {
            //ignore
        }
        return condition.apply(state);
    }

    private synchronized void close()
    {
        if (timedListener != null)
        {
            timedListener.close(clock);
        }
    }

    private FudgerTimedTestListener createTimedListener()
    {
        Collection<TimedTestListener> listeners = new ArrayList<TimedTestListener>(5);
        try
        {
            listeners.add(new SubRipSubtitleListener(createOutputAndSave(OUTPUT_SUBTITLES)));
        }
        catch (IOException e)
        {
            FuncTestOut.log("Unable to record subtitles");
            e.printStackTrace(FuncTestOut.out);
        }
        try
        {
            listeners.add(new MkvChapterTimedListener(createOutputAndSave(OUTPUT_CHAPTERS_XML)));
        }
        catch (IOException e)
        {
            FuncTestOut.log("Unable to report chapters");
            e.printStackTrace(FuncTestOut.out);
        }

        try
        {
            listeners.add(new CommonChapterTimedListener(createOutputAndSave(OUTPUT_CHAPTERS_COMMON)));
        }
        catch (IOException e)
        {
            FuncTestOut.log("Unable to report chapters");
            e.printStackTrace(FuncTestOut.out);
        }

        return new FudgerTimedTestListener(createListenerFrom(listeners));
    }

    private static TimedTestListener createListenerFrom(Collection<? extends TimedTestListener> listeners)
    {
        if (listeners.isEmpty())
        {
            return new NoopTimedTestListener();
        }
        else if (listeners.size() == 1)
        {
            return new SafeTimedTestListener(listeners.iterator().next());
        }
        else
        {
            return new CompositeTimedTestListener(listeners);
        }
    }

    private class ProcessListener implements FFMpegCommandListener
    {
        private final PrintWriter writer;

        public ProcessListener(final File outputFile)
        {
            PrintWriter writer;
            try
            {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)), true);
            }
            catch (IOException ignore)
            {
                writer = new PrintWriter(new NullOutputStream());
                //ignore me
            }
            this.writer = writer;
        }

        public void start()
        {
        }

        public void outputLine(final String line)
        {
            writer.println(line);
        }

        public void progress(final FFMpegProgressEvent event)
        {
            setClock(event.getTime());
            if (event.hasDropped())
            {
                writer.printf("Dropped %d frames.%n", event.getDropped());
            }
        }

        public void end(final int exitCode)
        {
            writer.close();
            exited();
        }
    }

    /**
     * The selenium tests have a hack that forces a 10 second wait before between the actual start of the test and when
     * things start happening. We need to fudge the test times slightly so that things subtitles/chapters align with
     * what people expect to see.
     */
    private static class FudgerTimedTestListener implements TimedTestListener
    {
        private final TimedTestListener listener;
        private long testStart = -1;
        private long lastTestEnd = -1;
        private WebTestDescription lastTest = null;

        private FudgerTimedTestListener(final TimedTestListener listener)
        {
            this.listener = listener;
        }

        public void start(final long clockMs)
        {
            listener.start(clockMs);
        }

        public void startTest(final WebTestDescription test, final long clockMs)
        {
            testStart = clockMs;
        }

        public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
        {
            fireDelayedEvents(test);
            listener.addError(test, t, clockMs);
        }

        public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
        {
            fireDelayedEvents(test);
            listener.addFailure(test, t, clockMs);
        }

        public void endTest(final WebTestDescription test, final long clockMs)
        {
            fireDelayedEvents(test);
            lastTestEnd = clockMs;
            lastTest = test;
        }

        public void close(final long clockMs)
        {
            fireDelayedEvents(null);
            listener.close(clockMs);
        }

        /*
         * Fire off any events we have held up to see if we can get more accurate timings.
         */
        private void fireDelayedEvents(final WebTestDescription test)
        {
            //If we have the lastTestEnd time then we must fire the end event for the last test.
            if (lastTestEnd >= 0)
            {
                if (testStart >= 0)
                {
                    //We now know the start time of the current test. This allows us to set the finishing time of the last
                    //1 second before the start of the current test.
                    listener.endTest(lastTest, Math.max(lastTestEnd, testStart - 1000));
                }
                else
                {
                    //We don't know the stat time of the current test so lets just report lastTestEnd as the end
                    //of the last test.
                    listener.endTest(lastTest, lastTestEnd);
                }
                lastTestEnd = -1;
                lastTest = null;
            }

            //We can now fire the start of the current test.
            if (test != null && testStart >= 0)
            {
                listener.startTest(test, testStart);
            }

            testStart = -1;
        }
    }
}
