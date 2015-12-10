package com.atlassian.jira.instrumentation.external;

import com.atlassian.instrumentation.ExternalGauge;
import com.atlassian.instrumentation.ExternalValue;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.instrumentation.Instrumentation;
import com.atlassian.jira.instrumentation.InstrumentationName;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextProvider;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ofbiz.core.entity.config.JndiDatasourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URI;

import static com.atlassian.jira.instrumentation.InstrumentationName.DBCP_ACTIVE;
import static com.atlassian.jira.instrumentation.InstrumentationName.DBCP_IDLE;
import static com.atlassian.jira.instrumentation.InstrumentationName.DBCP_MAX;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.replace;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;

/**
 * Holder class for database-related gauges.
 *
 * @since v5.0.2
 */
public class DatabaseExternalGauges
{
    private static final Logger log = LoggerFactory.getLogger(DatabaseExternalGauges.class);
    private static final String JIRA_MBEAN_NAME = "com.atlassian.jira:name=BasicDataSource";

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final DatabaseConfigurationLoader databaseConfigurationLoader;

    public DatabaseExternalGauges(VelocityRequestContextFactory velocityRequestContextFactory, DatabaseConfigurationLoader databaseConfigurationLoader)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.databaseConfigurationLoader = databaseConfigurationLoader;
    }

    /**
     * Installs the JIRA database gauges. These gauges read the connection pool metrics from JMX.
     *
     * @return this
     */
    public DatabaseExternalGauges installInstruments()
    {
        DbcpInstruments instruments = createInstruments();
        log.info("Installing DBCP monitoring instruments: {}", instruments);
        instruments.install();

        return this;
    }

    /**
     * Creates a DbcpInstruments instance that can be used to install the DBCP instruments.
     *
     * @return a new DbcpInstruments
     */
    private DbcpInstruments createInstruments()
    {
        DbcpInstruments instruments = new JiraDbcpInstruments(JIRA_MBEAN_NAME);
        try
        {
            DatabaseConfig dbConfig = databaseConfigurationLoader.loadDatabaseConfiguration();
            JndiDatasourceInfo jndiInfo = dbConfig != null ? (dbConfig.getDatasourceInfo() != null ? dbConfig.getDatasourceInfo().getJndiDatasource() : null) : null;
            if (jndiInfo != null)
            {
                // if using JNDI then we need to find Tomcat DBCP's MBean
                instruments = new TomcatDbcpInstruments(jndiInfo);
            }
        }
        catch (RuntimeException e)
        {
            log.debug("Couldn't read database configuration at this point. Does dbconfig.xml exist?", e);
        }

        return instruments;
    }

    /**
     * Returns the JIRA context path as configured in Tomcat.
     *
     * @return a String containing the context path
     */
    String getContextPath()
    {
        String contextPath = determineContextPath();

        return "".equals(contextPath) ? "/" : contextPath;
    }

    private String determineContextPath()
    {
        // first try to call ServletContext.getContextPath() from the Servlet 2.5 API
        ServletContext servletContext = ServletContextProvider.getServletContext();
        try
        {
            Method getContextPath = servletContext.getClass().getMethod("getContextPath");

            return String.valueOf(getContextPath.invoke(servletContext, (Object[]) null));
        }
        catch (Exception e)
        {
            // fall back to the context in the base URL if running in Servlet <2.5
            VelocityRequestContext ctx = velocityRequestContextFactory.getJiraVelocityRequestContext();
            try
            {
                return ctx != null ? URI.create(ctx.getBaseUrl()).getPath() : "/";
            }
            catch (Exception e1)
            {
                return "/";
            }
        }
    }

    /**
     * Abstraction for a set of DBCP instruments that are read from JMX. Use this class and its subclasses to install
     * the instruments in JIRA.
     */
    abstract static class DbcpInstruments
    {
        private final ImmutableList<InstrumentationName> instruments = ImmutableList.of(DBCP_MAX, DBCP_ACTIVE, DBCP_IDLE);
        private final String objectName;

        protected DbcpInstruments(String objectName)
        {
            this.objectName = objectName;
        }

        public final DbcpInstruments install()
        {
            for (InstrumentationName name : instruments)
            {
                install(name);
            }

            return this;
        }

        @Override
        public final String toString()
        {
            return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        /**
         * "dbcp.maxActive" becomes "maxActive"
         */
        protected String attributeName(InstrumentationName name)
        {
            return replace(name.getInstrumentName(), "dbcp.", "");
        }

        private void install(InstrumentationName instrument)
        {
            Instrumentation.putInstrument(new ExternalGauge(instrument.getInstrumentName(), new DbcpGauge(objectName, attributeName(instrument))));
        }
    }

    static class JiraDbcpInstruments extends DbcpInstruments
    {
        JiraDbcpInstruments(String objectName)
        {
            super(objectName);
        }

        /**
         * "dbcp.maxActive" becomes "MaxActive"
         */
        @Override
        protected String attributeName(InstrumentationName name)
        {
            return capitalize(super.attributeName(name));
        }
    }

    class TomcatDbcpInstruments extends DbcpInstruments
    {
        TomcatDbcpInstruments(@Nonnull JndiDatasourceInfo jndiInfo)
        {
            super(format("Catalina:type=DataSource,path=%s,host=%s,class=javax.sql.DataSource,name=\"%s\"", getContextPath(), "localhost", replace(jndiInfo.getJndiName(), "java:comp/env/", "")));
        }
    }

    /**
     * Maps a JMX value into an ExternalValue as required by Atlassian instrumentation.
     */
    static class DbcpGauge implements ExternalValue
    {
        private final String objectName;
        private final String attribute;

        public DbcpGauge(String objectName, String attribute)
        {
            this.objectName = objectName;
            this.attribute = attribute;
        }

        @Override
        public long getValue()
        {
            try
            {
                MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

                return (Integer) platformMBeanServer.getAttribute(new ObjectName(objectName), attribute);
            }
            catch (Exception e)
            {
                // ignore
                return -1L;
            }
        }
    }
}
