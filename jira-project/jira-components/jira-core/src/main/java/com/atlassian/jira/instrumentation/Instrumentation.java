package com.atlassian.jira.instrumentation;

import java.util.Collections;
import java.util.List;

import com.atlassian.instrumentation.AbsoluteCounter;
import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.DerivedCounter;
import com.atlassian.instrumentation.Gauge;
import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.instrumentation.operations.OpCounter;
import com.atlassian.instrumentation.operations.OpSnapshot;
import com.atlassian.instrumentation.operations.OpTimer;
import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.instrumentation.operations.ThreadOpTimerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.instrumentation.external.ExternalGauges;
import com.atlassian.jira.util.log.OneShotLogger;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.apache.log4j.Logger;

/**
 * A static singleton style class that exposes {@link com.atlassian.instrumentation.DefaultInstrumentRegistry}
 * functionality.  You can instrument JIRA code something like this
 * <pre>
 *
 *  OpTimer timer = Instrumentation.pullTimer("some.operation");
 *  try {
 *      ... some operation in JIRA
 *      ...
 *  } finally {
 *      timer.end();
 *  }
 * </pre>
 * <p/>
 * This will cause invocation count and time taken information to be stored away in a {@link
 * com.atlassian.instrumentation.operations.OpCounter} called "some.operation".  Simple.
 * <p/>
 * You do not have to worry about recording this data, it will be done for you later.
 *
 * @see com.atlassian.jira.instrumentation.InstrumentationName
 * @see com.atlassian.instrumentation.DefaultInstrumentRegistry   \
 * @since v5.0
 */
public class Instrumentation implements Startable
{
    private static final OneShotLogger log = new OneShotLogger(Logger.getLogger(Instrument.class));
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final DatabaseConfigurationLoader databaseConfigurationLoader;

    public Instrumentation(VelocityRequestContextFactory velocityRequestContextFactory, DatabaseConfigurationLoader databaseConfigurationLoader)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.databaseConfigurationLoader = databaseConfigurationLoader;
    }

    private static InstrumentRegistry getInstance()
    {
        return ComponentAccessor.getComponent(InstrumentRegistry.class);
    }

    public static Instrument putInstrument(Instrument instrument)
    {
        return getInstance().putInstrument(instrument);
    }

    public static Instrument getInstrument(String name)
    {
        return getInstance().getInstrument(name);
    }

    public static AbsoluteCounter pullAbsoluteCounter(String name)
    {
        return getInstance().pullAbsoluteCounter(name);
    }

    public static AbsoluteCounter pullAbsoluteCounter(InstrumentationName instrumentationName)
    {
        return getInstance().pullAbsoluteCounter(instrumentationName.getInstrumentName());
    }

    public static Counter pullCounter(String name)
    {
        return getInstance().pullCounter(name);
    }

    public static Counter pullCounter(InstrumentationName instrumentationName)
    {
        return getInstance().pullCounter(instrumentationName.getInstrumentName());
    }

    public static DerivedCounter pullDerivedCounter(String name)
    {
        return getInstance().pullDerivedCounter(name);
    }

    public static DerivedCounter pullDerivedCounter(InstrumentationName instrumentationName)
    {
        return getInstance().pullDerivedCounter(instrumentationName.getInstrumentName());
    }

    public static Gauge pullGauge(String name)
    {
        return getInstance().pullGauge(name);
    }

    public static Gauge pullGauge(InstrumentationName instrumentationName)
    {
        return getInstance().pullGauge(instrumentationName.getInstrumentName());
    }

    public static OpCounter pullOpCounter(String name)
    {
        return getInstance().pullOpCounter(name);
    }

    public static OpCounter pullOpCounter(InstrumentationName instrumentationName)
    {
        return getInstance().pullOpCounter(instrumentationName.getInstrumentName());
    }

    public static OpTimer pullTimer(String name)
    {
        return getInstance().pullTimer(name);
    }

    public static OpTimer pullTimer(InstrumentationName instrumentationName)
    {
        return getInstance().pullTimer(instrumentationName.getInstrumentName());
    }

    public static List<Instrument> snapshotInstruments()
    {
        return getInstance().snapshotInstruments();
    }

    public static List<OpSnapshot> snapshotThreadLocalOperationsAndClear(final OpTimerFactory opTimerFactory)
    {
        if (opTimerFactory instanceof ThreadOpTimerFactory)
        {
            ThreadOpTimerFactory threadLocalOpTimerFactory = (ThreadOpTimerFactory) opTimerFactory;
            return threadLocalOpTimerFactory.snapshotAndClear();
        }

        log.error("Unable to snapshot thread local operations (implementation of OpTimerFactory is not a ThreadLocalOpTimerFactory): " + opTimerFactory);
        return Collections.emptyList();
    }


    @Override
    public void start()
    {
        //
        // load up the external gauges
        //
        new ExternalGauges(velocityRequestContextFactory, databaseConfigurationLoader).installInstruments();

        //
        // we want to put some named instruments into the registry because although they will be lazily created
        // we want them in there to show zero values
        //
        Instrumentation.pullOpCounter(InstrumentationName.WEB_REQUESTS);
        Instrumentation.pullOpCounter(InstrumentationName.DB_READS);
        Instrumentation.pullOpCounter(InstrumentationName.DB_WRITES);
        Instrumentation.pullOpCounter(InstrumentationName.ISSUE_INDEX_READS);
        Instrumentation.pullOpCounter(InstrumentationName.ISSUE_INDEX_WRITES);

    }
}
