package com.atlassian.jira.functest.framework;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.webtests.table.HtmlTable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Access to instrument counters visible throught ViewInstrumentation.jspa
 */
public class Instruments
{
    private static final String TYPE_HEADING = "Type";
    private static final String NAME_HEADING = "Name";
    private static final String VALUE_HEADING = "Value";
    private static final String INSTRUMENT_TABLE_ID = "instrument_table";
    private static final String VIEW_INSTRUMENTATION_PATH = "/secure/admin/ViewInstrumentation.jspa";
    private static final String INSTRUMENT_TYPE_COUNTER = "Counter";
    
    private final Navigation navigation;
    private final HtmlPage page;

    public Instruments(final Navigation navigation, final HtmlPage page)
    {
        this.navigation = navigation;
        this.page = page;
    }

    /**
     * This uses web interface to load indexes count - loaded page changes to /secure/admin/ViewInstrumentation.jspa
     *
     * @throws java.text.ParseException if there is problem reading numbers
     */
    @Nonnull
    public Instruments.Counters readAllCounters() throws ParseException
    {
        navigation.gotoPage(VIEW_INSTRUMENTATION_PATH);
        List<HtmlTable.Row> allRows = page.getHtmlTable(INSTRUMENT_TABLE_ID).getRows();
        List<HtmlTable.Row> dataRows = allRows.subList(1, allRows.size()); // skipping the header

        Map<String, Instruments.Counter> counters = Maps.newHashMap();
        for (HtmlTable.Row row : dataRows)
        {
            final String instrumentType = row.getCellForHeading(TYPE_HEADING);
            if (INSTRUMENT_TYPE_COUNTER.equals(instrumentType))
            {
                final String counterName = row.getCellForHeading(NAME_HEADING);
                final String counterValueString = row.getCellForHeading(VALUE_HEADING);
                final long counterValue;
                try
                {
                    counterValue = NumberFormat.getIntegerInstance().parse(counterValueString).longValue();
                }
                catch (final NumberFormatException badFormat)
                {
                    throw new NumberFormatException("Can't parse counter number: " + counterName)
                    {
                        @Override
                        public Throwable getCause()
                        {
                            // can't pass it to constructor wrr...
                            return badFormat;
                        }
                    };
                }


                counters.put(counterName, new Instruments.Counter(counterName, counterValue));
            }
        }

        return new Instruments.Counters(counters);
    }

    public static class Counter
    {
        private final String name;
        private final Optional<Long> value;

        public Counter(final String name)
        {
            this.name = name;
            this.value = Optional.absent();
        }

        public Counter(final String name, long value)
        {
            this.name = name;
            this.value = Optional.of(value);
        }

        public String getName()
        {
            return name;
        }

        /**
         * Counter may not have value
         */
        public Optional<Long> getValue()
        {
            return value;
        }
    }

    public static class Counters
    {
        private final Map<String, Counter> counters;

        public Counters(final Map<String, Counter> counters)
        {
            this.counters = ImmutableMap.copyOf(counters);
        }

        /**
         * In case when there is no counter with given name defined returns counter without value.
         *
         * @param name counter name
         */
        @Nonnull
        public Counter getCounter(String name)
        {
            if (counters.containsKey(name))
            {
                return counters.get(name);
            }
            else
            {
                return new Counter(name);
            }
        }
    }
}
