/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 4, 2004
 * Time: 3:52:32 PM
 */
package com.atlassian.jira.web.filters;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadLocalQueryProfiler
{
    private static final Logger log = Logger.getLogger(ThreadLocalQueryProfiler.class);

    public static final String LUCENE_GROUP = "lucene";

    private static final ThreadLocal<ThreadLocalQueryProfiler> THREAD_LOCAL = new ThreadLocal<ThreadLocalQueryProfiler>();

    public static void start()
    {
        if (UtilTimerStack.isActive())
        {
            getProfiler().clearData();
            getProfiler().begin();
        }
    }

    public static void store(final String group, final String query, final long time)
    {
        if (UtilTimerStack.isActive())
        {
            getProfiler().storeDataPoint(group, query, time);
        }
    }

    public static void end() throws IOException
    {
        final ThreadLocalQueryProfiler profiler = getProfiler();

        if (UtilTimerStack.isActive())
        {
            profiler.printData();
            profiler.clearData();
        }
    }

    private static ThreadLocalQueryProfiler getProfiler()
    {
        ThreadLocalQueryProfiler profiler = THREAD_LOCAL.get();

        if (profiler == null)
        {
            profiler = new ThreadLocalQueryProfiler();
            THREAD_LOCAL.set(profiler);
        }

        return profiler;
    }

    // --------------------------------------------------------------------------------------------------------- members

    private final DataPointStore store = new DataPointStore();
    private long startTime;

    // ----------------------------------------------------------------------------------------------------------- ctors

    /**
     * do not ctor
     */
    private ThreadLocalQueryProfiler()
    {}

    // ------------------------------------------------------------------------------------------------ instance methods

    private void storeDataPoint(final String group, final String key, final long time)
    {
        store.store(group, key, time);
    }

    private void clearData()
    {
        store.clear();
    }

    private void printData()
    {
        final long totalRequest = System.currentTimeMillis() - startTime;
        long totalProfiled = 0;
        int numDataPoints = 0;
        int numUniquePoints = 0;

        for (final Object element : store.getGroups())
        {
            final String groupKey = (String) element;
            log.debug("RESULT GROUP: " + groupKey);
            final DataPointGroup group = store.getGroup(groupKey);

            int numGroupPoints = 0;
            int totalGroup = 0;

            final Collection<String> pointKeys = group.getPointKeys();
            for (final String key : pointKeys)
            {
                final Collection<DataPoint> points = group.getPoints(key);

                final long totalTime = getTotalTime(points);
                totalGroup += totalTime;
                final StringBuilder builder = new StringBuilder();
                builder.append("  ").append(points.size()).append(":").append(totalTime).append("ms ").append(key).append(" [");
                boolean comma = false;
                for (final DataPoint point : points)
                {
                    numGroupPoints++;
                    if (comma)
                    {
                        builder.append(",");
                    }
                    else
                    {
                        comma = true;
                    }
                    builder.append(point.getTimeTaken());
                }
                log.debug(builder.append("]").toString());
            }

            numDataPoints += numGroupPoints;
            numUniquePoints += pointKeys.size();
            totalProfiled += totalGroup;

            log.debug(new StringBuilder().append(groupKey).append(": ").append(numGroupPoints).append(" keys (").append(pointKeys.size()).append(" unique) took ").append(totalGroup).append("ms/").append(totalRequest).append("ms : ").append(((float) totalGroup / (float) totalRequest) * 100).append("% ").append(pointKeys.size() > 0 ? (totalGroup / pointKeys.size()) + "ms/query avg." : "").toString());
            log.debug("");
        }

        log.debug(new StringBuilder().append("PROFILED : ").append(numDataPoints).append(" keys (").append(numUniquePoints).append(" unique) took ").append(totalProfiled).append("ms/").append(totalRequest).append("ms : ").append(((float) totalProfiled / (float) totalRequest) * 100).append("% ").append(numDataPoints > 0 ? (totalProfiled / numDataPoints) + "ms/query avg." : "").toString());
    }

    private long getTotalTime(final Collection<DataPoint> points)
    {
        long total = 0;

        for (final DataPoint element : points)
        {
            total = total + element.getTimeTaken();
        }

        return total;
    }

    private void begin()
    {
        startTime = System.currentTimeMillis();
    }

    // --------------------------------------------------------------------------------------------------- inner classes

    private static class DataPoint
    {
        private final String group;
        private final String key;
        private final long timeTaken;
        private final long timeExecuted;

        public DataPoint(final String group, final String query, final long timeTaken)
        {
            this.group = group;
            key = query;
            this.timeTaken = timeTaken;
            timeExecuted = System.currentTimeMillis();
        }

        public String getGroup()
        {
            return group;
        }

        public String getKey()
        {
            return key;
        }

        public long getTimeTaken()
        {
            return timeTaken;
        }

        public long getTimeExecuted()
        {
            return timeExecuted;
        }
    }

    private static class DataPointStore
    {
        private final Map<String, DataPointGroup> groups = new HashMap<String, DataPointGroup>();

        public void clear()
        {
            groups.clear();
        }

        public void store(final String group, final String key, final long time)
        {
            DataPointGroup grp = groups.get(group);

            if (grp == null)
            {
                grp = new DataPointGroup();
                groups.put(group, grp);
            }

            grp.store(new DataPoint(group, key, time));
        }

        public Collection<String> getGroups()
        {
            return groups.keySet();
        }

        public DataPointGroup getGroup(final String groupKey)
        {
            return groups.get(groupKey);
        }
    }

    private static class DataPointGroup
    {
        private final Map<String, List<DataPoint>> points = new HashMap<String, List<DataPoint>>();

        public void store(final DataPoint dataPoint)
        {
            final List<DataPoint> keyPoints = points.get(dataPoint.getKey());

            if (keyPoints == null)
            {
                points.put(dataPoint.getKey(), CollectionBuilder.<DataPoint> newBuilder().add(dataPoint).asArrayList());
            }
            else
            {
                keyPoints.add(dataPoint);
            }
        }

        public Collection<String> getPointKeys()
        {
            return points.keySet();
        }

        public Collection<DataPoint> getPoints(final String key)
        {
            return points.get(key);
        }
    }
}