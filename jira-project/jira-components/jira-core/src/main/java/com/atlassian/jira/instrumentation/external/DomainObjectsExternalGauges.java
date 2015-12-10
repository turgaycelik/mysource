package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.CachedExternalValue;
import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.instrumentation.Instrument;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;

import java.util.concurrent.TimeUnit;

/**
 * The guages of domain objects in JIRA
 *
 * @since v4.4
 */
public class DomainObjectsExternalGauges
{

    public DomainObjectsExternalGauges()
    {
        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of issues in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_ISSUES, new OfBizViewExternalValue("Issue"));

        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of projects in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_PROJECTS, new OfBizViewExternalValue("Project"));

        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of custom fields in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_CUSTOMFIELDS, new OfBizViewExternalValue("CustomField"));

        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of workflows in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_WORKFLOWS, new OfBizViewExternalValue("Workflow"));

        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of users in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_USERS, new OfBizViewExternalValue("OSUser"));

        /**
         * This is the {@link com.atlassian.instrumentation.Gauge} for the number of groups in JIRA
         */
        newExternalGauge(InstrumentationName.TOTAL_GROUPS, new OfBizViewExternalValue("OSGroup"));

    }

    private Instrument newExternalGauge(final InstrumentationName name, ExternalValue externalValue)
    {
        ExternalGauge externalGauge = new ExternalGauge(name.getInstrumentName(), externalValue);
        Instrumentation.putInstrument(externalGauge);
        return externalGauge;
    }

    /**
     * Calculating the values represented by this class is expensive so we use
     * {@link com.atlassian.instrumentation.CachedExternalValue} so that the value stands
     * for some time before being calculated again.
     * <p/>
     * Also the base class is LAZY and wont calculate the value until asked to.
     */
    private static class OfBizViewExternalValue extends CachedExternalValue
    {
        private final String entityName;

        private OfBizViewExternalValue(final String entityName)
        {
            super(120, TimeUnit.SECONDS); // jdk 1.5 only has SECONDS defined
            this.entityName = entityName;
        }

        protected long computeValue()
        {
            try
            {
                return ComponentAccessor.getOfBizDelegator().getCount(entityName);
            } catch (DataAccessException e)
            {
                return -1;
            }
        }
    }
}
