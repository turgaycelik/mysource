package com.atlassian.jira.web.action.admin.instrumentation;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.core.ManagedClusterLock;
import com.atlassian.beehive.core.ManagedClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.ManagedCache;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.Gauge;
import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.operations.OpInstrument;
import com.atlassian.jira.cache.ManagedCacheInstruments;
import com.atlassian.jira.cluster.lock.ClusterLockInstruments;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import webwork.action.ActionContext;

import static com.google.common.collect.Sets.newTreeSet;

/**
 * An initial quick and dirty listing of the instrumentation objects in JIRA
 */
@WebSudoRequired
public class ViewInstrumentation extends JiraWebActionSupport
{
    private final CacheManager cacheManager;
    private final ClusterLockService clusterLockService;

    private Collection<InstrumentDisplayBean> displayBeanList;

    private boolean threadContentionMonitoring = false;

    public ViewInstrumentation(CacheManager cacheManager, ClusterLockService clusterLockService)
    {
        this.cacheManager = cacheManager;
        this.clusterLockService = clusterLockService;
    }

    @Override
    public String doDefault() throws Exception
    {
        return SUCCESS;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (ActionContext.getParameters().get("threadContentionMonitoring") != null)
        {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            if (threadMXBean.isThreadContentionMonitoringSupported())
            {
                threadMXBean.setThreadContentionMonitoringEnabled(threadContentionMonitoring);
            }
            if (threadMXBean.isThreadCpuTimeSupported())
            {
                threadMXBean.setThreadCpuTimeEnabled(threadContentionMonitoring);
            }
        }
        return SUCCESS;
    }

    public Collection<InstrumentDisplayBean> getInstruments()
    {
        if (displayBeanList == null)
        {
            final SortedSet<Instrument> sortedInstruments = getSortedInstruments();
            displayBeanList = Collections2
                    .transform(sortedInstruments, new Function<Instrument, InstrumentDisplayBean>()
                    {
                        @Override
                        public InstrumentDisplayBean apply(@Nullable Instrument instrument)
                        {
                            return new InstrumentDisplayBean(instrument);
                        }
                    });
        }
        return displayBeanList;
    }

    private SortedSet<Instrument> getSortedInstruments()
    {
        final SortedSet<Instrument> sortedInstruments = newTreeSet(new Comparator<Instrument>()
        {
            @Override
            public int compare(Instrument in1, Instrument in2)
            {
                return in1.getName().compareTo(in2.getName());
            }
        });

        collectBasicInstruments(sortedInstruments);
        collectCacheInstruments(sortedInstruments);
        collectClusterLockInstruments(sortedInstruments);
        return sortedInstruments;
    }

    private static boolean collectBasicInstruments(final SortedSet<Instrument> sortedInstruments)
    {
        return sortedInstruments.addAll(Instrumentation.snapshotInstruments());
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    private void collectCacheInstruments(SortedSet<Instrument> sortedInstruments)
    {
        for (final ManagedCache cache : cacheManager.getManagedCaches())
        {
            sortedInstruments.addAll(new ManagedCacheInstruments(cache).getInstruments());
        }
    }

    private void collectClusterLockInstruments(SortedSet<Instrument> sortedInstruments)
    {
        if (clusterLockService instanceof ManagedClusterLockService)
        {
            final ManagedClusterLockService mcls = (ManagedClusterLockService)clusterLockService;
            for (final ManagedClusterLock clusterLock : mcls.getAllKnownClusterLocks())
            {
                sortedInstruments.addAll(new ClusterLockInstruments(clusterLock).getInstruments());
            }
        }
        else
        {
            log.warn("Cluster lock service is not managed; no instrumentation is available");
        }
    }


    public JmxStateInfoDisplayBean getJmxStateInfo()
    {
        return new JmxStateInfoDisplayBean();
    }

    public void setThreadContentionMonitoring(boolean threadContentionMonitoring)
    {
        this.threadContentionMonitoring = threadContentionMonitoring;
    }

    public class JmxStateInfoDisplayBean
    {
        public boolean isThreadContentionMonitoringSupported()
        {
            return ManagementFactory.getThreadMXBean().isThreadContentionMonitoringSupported();
        }

        public boolean isThreadContentionMonitoringEnabled()
        {
            return ManagementFactory.getThreadMXBean().isThreadContentionMonitoringEnabled();
        }

        public boolean isThreadCpuTimeSupported()
        {
            return ManagementFactory.getThreadMXBean().isThreadCpuTimeSupported();
        }

        public boolean isThreadCpuTimeEnabled()
        {
            return ManagementFactory.getThreadMXBean().isThreadCpuTimeEnabled();
        }
    }

    public class InstrumentDisplayBean
    {
        private final Instrument instrument;

        public InstrumentDisplayBean(Instrument instrument)
        {
            this.instrument = instrument;
        }

        public String getName()
        {
            return instrument.getName();
        }

        public String getType()
        {
            if (instrument instanceof Counter)
            {
                return getText("admin.instrumentation.instrument.type.counter");
            }
            if (instrument instanceof Gauge)
            {
                return getText("admin.instrumentation.instrument.type.gauge");
            }
            if (instrument instanceof OpInstrument)
            {
                return getText("admin.instrumentation.instrument.type.operation");
            }
            return "Unknown";
        }

        public String getValue()
        {
            if (instrument instanceof OpInstrument)
            {
                return ""; // handled by the other column
            }
            return valueOf(instrument.getValue());
        }

        public String getInvocationCount()
        {
            long v = -1;
            if (instrument instanceof OpInstrument)
            {
                v = ((OpInstrument)instrument).getInvocationCount();
            }
            return valueOf(v);
        }

        public String getMillisecondsTaken()
        {
            long v = -1;
            if (instrument instanceof OpInstrument)
            {
                v = ((OpInstrument)instrument).getMillisecondsTaken();
            }
            return valueOf(v);
        }

        public String getCpuTime()
        {
            long v = -1;
            if (instrument instanceof OpInstrument)
            {
                v = ((OpInstrument)instrument).getCpuTime();
            }
            return valueOf(v);
        }

        public String getResultSetSize()
        {
            long v = -1;
            if (instrument instanceof OpInstrument)
            {
                v = ((OpInstrument)instrument).getResultSetSize();
            }
            return valueOf(v);
        }

        private String valueOf(long v)
        {
            if (v == -1)
            {
                return "";
            }
            NumberFormat df = DecimalFormat.getNumberInstance(getLocale());
            return df.format(v);
        }
    }
}
