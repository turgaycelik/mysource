package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.ExternalCounter;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.jira.instrumentation.Instrumentation;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * A set of External {@link com.atlassian.instrumentation.Instrument}s around {@link ClassLoader}s in the JVM
 */
class ClassLoadingExternalGauges
{
    ClassLoadingExternalGauges()
    {
        Instrumentation.putInstrument(new ExternalGauge("jmx.class.loaded.current", new ClassLoaderMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getLoadedClassCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.class.unloaded.total", new ClassLoaderMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getUnloadedClassCount();
            }
        }));
        Instrumentation.putInstrument(new ExternalCounter("jmx.class.loaded.total", new ClassLoaderMXExternalValue()
        {
            public long getValue()
            {
                return getMxBean().getTotalLoadedClassCount();
            }
        }));
    }

    private static abstract class ClassLoaderMXExternalValue implements ExternalValue
    {
        ClassLoadingMXBean getMxBean()
        {
            return ManagementFactory.getClassLoadingMXBean();
        }
    }
}
