package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.jira.instrumentation.Instrumentation;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * The gauges related to memory
 *
 * @since v4.4
 */
class MemoryExternalGauges
{
    MemoryExternalGauges()
    {
        Instrumentation.putInstrument(new ExternalGauge("jmx.memory.heap.committed", new MemoryMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getHeapMemoryUsage().getCommitted();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.memory.heap.used", new MemoryMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getHeapMemoryUsage().getUsed();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.memory.nonheap.committed", new MemoryMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getNonHeapMemoryUsage().getCommitted();
            }
        }));
        Instrumentation.putInstrument(new ExternalGauge("jmx.memory.nonheap.used", new MemoryMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getNonHeapMemoryUsage().getUsed();
            }
        }));

        Instrumentation.putInstrument(new ExternalGauge("jmx.system.up.time", new ExternalValue()
        {
            @Override
            public long getValue()
            {
                return ManagementFactory.getRuntimeMXBean().getUptime();
            }
        }));

    }

    private static abstract class MemoryMXExternalValue implements ExternalValue
    {
        MemoryMXBean getMxBean()
        {
            return ManagementFactory.getMemoryMXBean();
        }
    }
}
