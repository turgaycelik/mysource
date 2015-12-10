package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.CachedExternalValue;
import com.atlassian.instrumentation.ExternalCounter;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.jira.instrumentation.Instrumentation;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

/**
 * The gauges related to memory
 *
 * @since v4.4
 */
class ThreadExternalGauges
{
    ThreadExternalGauges()
    {
        Instrumentation.putInstrument(new ExternalGauge("jmx.thread.total.count", new ThreadMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getThreadCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.thread.daemon.count", new ThreadMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getDaemonThreadCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.thread.nondaemon.count", new ThreadMXExternalValue()
        {
            public long getValue()
            {
                final ThreadMXBean threadMXBean = getMxBean();
                return threadMXBean.getThreadCount() - threadMXBean.getDaemonThreadCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.thread.peak.count", new ThreadMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getPeakThreadCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.thread.ever.count", new ThreadMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getTotalStartedThreadCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.time", new CachedThreadMXExternalValue()
        {

            @Override
            protected long computeValue()
            {
                return safelyGetValue(new CalculateThreadValue()
                {
                    @Override
                    public long calculate(long threadId, ThreadMXBean mxBean)
                    {
                        return mxBean.getThreadCpuTime(threadId);
                    }
                });
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.user.time", new CachedThreadMXExternalValue()
        {
            @Override
            protected long computeValue()
            {
                return safelyGetValue(new CalculateThreadValue()
                {
                    @Override
                    public long calculate(long threadId, ThreadMXBean mxBean)
                    {
                        return mxBean.getThreadUserTime(threadId);
                    }
                });
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.block.time", new CachedThreadMXExternalValue()
        {
            @Override
            protected long computeValue()
            {
                if (isThreadContentionAvailable())
                {
                    return safelyGetValue(new CalculateThreadValue()
                    {
                        @Override
                        public long calculate(long threadId, ThreadMXBean mxBean)
                        {
                            return mxBean.getThreadInfo(threadId).getBlockedTime();
                        }
                    });
                }
                return 0;
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.block.count", new CachedThreadMXExternalValue()
        {
            @Override
            protected long computeValue()
            {
                if (isThreadContentionAvailable())
                {
                    return safelyGetValue(new CalculateThreadValue()
                    {
                        @Override
                        public long calculate(long threadId, ThreadMXBean mxBean)
                        {
                            return mxBean.getThreadInfo(threadId).getBlockedCount();
                        }
                    });
                }
                return 0;
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.wait.time", new CachedThreadMXExternalValue()
        {
            @Override
            protected long computeValue()
            {
                if (isThreadContentionAvailable())
                {
                    return safelyGetValue(new CalculateThreadValue()
                    {
                        @Override
                        public long calculate(long threadId, ThreadMXBean mxBean)
                        {
                            return mxBean.getThreadInfo(threadId).getWaitedTime();
                        }
                    });
                }
                return 0;
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.thread.cpu.wait.count", new CachedThreadMXExternalValue()
        {
            @Override
            protected long computeValue()
            {
                if (isThreadContentionAvailable())
                {
                    return safelyGetValue(new CalculateThreadValue()
                    {
                        @Override
                        public long calculate(long threadId, ThreadMXBean mxBean)
                        {
                            return mxBean.getThreadInfo(threadId).getWaitedCount();
                        }
                    });
                }
                return 0;
            }
        }));
    }

    private static abstract class ThreadMXExternalValue implements ExternalValue
    {
        ThreadMXBean getMxBean()
        {
            return ManagementFactory.getThreadMXBean();
        }
    }

    private abstract static class CachedThreadMXExternalValue extends CachedExternalValue
    {

        protected CachedThreadMXExternalValue()
        {
            super(2, TimeUnit.MINUTES);
        }

        ThreadMXBean getMxBean()
        {
            return ManagementFactory.getThreadMXBean();
        }

        long safelyGetValue(CalculateThreadValue calculateThreadValue)
        {
            try
            {
                long value = 0;
                for (long threadId : getMxBean().getAllThreadIds())
                {
                    value += calculateThreadValue.calculate(threadId, getMxBean());
                }
                return Math.max(0,value);
            }
            catch (Exception e)
            {
                return 0;
            }
        }

        boolean isThreadContentionAvailable()
        {
            return getMxBean().isThreadContentionMonitoringSupported() && getMxBean().isThreadContentionMonitoringEnabled();
        }

        interface CalculateThreadValue
        {
            long calculate(long threadId, ThreadMXBean mxBean);
        }
    }

}
