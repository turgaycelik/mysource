package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.operations.ExternalOpInstrument;
import com.atlassian.instrumentation.operations.ExternalOpValue;
import com.atlassian.instrumentation.operations.OpSnapshot;
import com.atlassian.jira.instrumentation.Instrumentation;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * The gauges related to garbage collections
 *
 * @since v4.4
 */
class GarbageCollectionsExternalOp
{

    private static final String JMX_GC = "jmx.gc";

    GarbageCollectionsExternalOp()
    {
        // garbage collections is essentially an operation since it takes time and is invoked n times,
        // so we can record it as one
        ExternalOpInstrument gc = new ExternalOpInstrument(JMX_GC, new ExternalOpValue()
        {
            @Override
            public OpSnapshot getSnapshot()
            {

                long count = 0;
                long time = 0;
                for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans())
                {
                    count += gc.getCollectionCount();
                    time += gc.getCollectionTime();
                }
                return new OpSnapshot(JMX_GC, count, time);
            }
        });
        Instrumentation.putInstrument(gc);
    }
}
