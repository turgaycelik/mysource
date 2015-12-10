package com.atlassian.jira.util.system;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformation;
import com.atlassian.jdk.utilities.runtimeinformation.RuntimeInformationFactory;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.web.ContextKeys;
import com.opensymphony.module.sitemesh.util.Container;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ServletActionContext;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Supply specific system information of the current JIRA installation. For Example: Returning the current database
 * type.
 */
public class SystemInfoUtilsImpl implements SystemInfoUtils
{
    private static final Logger log = Logger.getLogger(SystemInfoUtilsImpl.class);

    private final Runtime rt = Runtime.getRuntime();

    private final RuntimeInformation runtimeInformation = RuntimeInformationFactory.getRuntimeInformation();

    private final OfBizConnectionFactory connectionFactory;

    public SystemInfoUtilsImpl()
    {
        this.connectionFactory = DefaultOfBizConnectionFactory.getInstance();
    }

    public String getDatabaseType()
    {
        final DatabaseConfig config = getDbConfig();
        return config == null ? null : config.getDatabaseType();
    }

    public String getDbDescriptorValue()
    {
        final DatabaseConfig dbConfig = getDbConfig();
        if (dbConfig == null)
        {
            return null;
        }
        String desc = dbConfig.getDescriptorValue();
        return desc != null ? desc : "Unknown configuration";
    }

    @Override
    public String getDbDescriptorLabel()
    {
        final DatabaseConfig dbConfig = getDbConfig();
        if (dbConfig == null)
        {
            return null;
        }
        return dbConfig.getDescriptorLabel();
    }

    public String getAppServer()
    {
        switch (Container.get())
        {
            case Container.TOMCAT:
                return "Apache Tomcat";

            case Container.ORION:
                return "Orion";

            case Container.WEBLOGIC:
                return "IBM WebLogic";

            case Container.JRUN:
                return "JRUN";

            case Container.RESIN:
                return "RESIN" + Container.get();

            case Container.HPAS:
                return "HPAS";

            case Container.UNKNOWN:
            default:
                return "Unknown";
        }
    }

    public String getUptime(final ResourceBundle resourceBundle)
    {
        final ServletContext servletContext = ServletActionContext.getServletContext();
        if (servletContext == null)
        {
            return "N/A";
        }

        final Long startupTime = ((Long) servletContext.getAttribute(ContextKeys.STARTUP_TIME));
        if (startupTime == null)
        {
            return "N/A";
        }

        final long currentTime = System.currentTimeMillis();
        return DateUtils.dateDifference(startupTime, currentTime, 4, resourceBundle);
    }

    public long getTotalMemory()
    {
        final long totalMemory = rt.maxMemory();
        return totalMemory / MEGABYTE;
    }

    public long getFreeMemory()
    {
        // rt.freeMemory only returns the memory free in teh current allocation.
        // There fore the total amount free is that plus how much more is available for allocation -
        // i.e. rt.max - rt.total
        final long freeMemory = (rt.maxMemory() - rt.totalMemory()) + rt.freeMemory();
        return freeMemory / MEGABYTE;
    }

    @Override
    public long getFreeAllocatedMemory()
    {
        return rt.freeMemory() / MEGABYTE;
    }

    @Override
    public long getUnAllocatedMemory()
    {
        return (rt.maxMemory() - rt.totalMemory()) / MEGABYTE;
    }

    @Override
    public long getAllocatedMemory()
    {
        return rt.maxMemory() / MEGABYTE;
    }

    public long getUsedMemory()
    {
        return getTotalMemory() - getFreeMemory();
    }

    public List<MemoryInformation> getMemoryPoolInformation()
    {
        // This rather silly copying of the list is to work around an intermittent bug in some IBM JDK's that causes the
        // MemoryInformationBean.toString() implementation to blow up when least expected.
        // See http://jira.atlassian.com/browse/JRA-19389
        List<MemoryInformation> list = runtimeInformation.getMemoryPoolInformation();
        List<MemoryInformation> validList = new ArrayList<MemoryInformation>();
        for (MemoryInformation memoryInfo : list)
        {
            try
            {
                log.debug("Checking memory pool info is ok for: " + memoryInfo.getName());
                // The following will fail if we have the error described above.
                memoryInfo.toString();
                // That worked OK, so add to the list.
                validList.add(memoryInfo);
            }
            catch (RuntimeException e)
            {
                log.warn("Memory pool info returned by the java runtime is invalid for pool - " + memoryInfo.getName());
                log.debug(e.getMessage(), e);
            }
        }
        return validList;
    }

    public long getTotalPermGenMemory()
    {
        return runtimeInformation.getTotalPermGenMemory() / MEGABYTE;
    }

    public long getFreePermGenMemory()
    {
        final long freeMemory = runtimeInformation.getTotalPermGenMemory() - runtimeInformation.getTotalPermGenMemoryUsed();
        return freeMemory / MEGABYTE;
    }

    public long getUsedPermGenMemory()
    {
        return runtimeInformation.getTotalPermGenMemoryUsed() / MEGABYTE;
    }

    public long getTotalNonHeapMemory()
    {
        return runtimeInformation.getTotalNonHeapMemory() / MEGABYTE;
    }

    public long getFreeNonHeapMemory()
    {
        final long freeMemory = runtimeInformation.getTotalNonHeapMemory() - runtimeInformation.getTotalNonHeapMemoryUsed();
        return freeMemory / MEGABYTE;
    }

    public long getUsedNonHeapMemory()
    {
        return runtimeInformation.getTotalNonHeapMemoryUsed() / MEGABYTE;
    }

    public String getJvmInputArguments()
    {
        return runtimeInformation.getJvmInputArguments();
    }

    public SystemInfoUtils.DatabaseMetaData getDatabaseMetaData()
            throws GenericEntityException, JiraException, SQLException
    {
        Connection connection = null;
        try
        {

            connection = connectionFactory.getConnection();
            if (connection == null)
            {
                throw new JiraException("Could not get database connection");
            }

            final java.sql.DatabaseMetaData metaData = connection.getMetaData();
            return new MaskedUrlDatabaseMetaData(metaData.getDatabaseProductVersion(), metaData.getDriverName(), metaData.getDriverVersion(),
                    metaData.getURL());
        }
        catch (final SQLException e)
        {
            log.error(e, e);
            throw e;
        }
        finally
        {
            silentlyClose(connection);
        }
    }

    private static void silentlyClose(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                // Ohh well
            }
        }
    }

    public String getInstallationType()
    {
        final ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo(this.getClass());
        return releaseInfo.getInfo();
    }

    DatabaseConfig getDbConfig()
    {
        return ComponentAccessor.getComponent(DatabaseConfigurationManager.class).getDatabaseConfiguration();
    }

    protected static final class MaskedUrlDatabaseMetaData implements DatabaseMetaData
    {
        private final String productVersion;
        private final String driverName;
        private final String driverVersion;
        private final String maskedUrl;

        public static String maskDatabaseUrl(final String url)
        {
            if (url == null)
            {
                return null;
            }
            final Pattern passwordRegex = Pattern.compile("password=[^&;]*", Pattern.CASE_INSENSITIVE);
            return passwordRegex.matcher(url).replaceAll("password=****");
        }

        public MaskedUrlDatabaseMetaData(final String productVersion, final String driverName, final String driverVersion, final String url)
        {
            this.productVersion = productVersion;
            this.driverName = driverName;
            this.driverVersion = driverVersion;
            maskedUrl = maskDatabaseUrl(url);
        }

        public String getDatabaseProductVersion()
        {
            return productVersion;
        }

        public String getDriverName()
        {
            return driverName;
        }

        public String getDriverVersion()
        {
            return driverVersion;
        }

        public String getMaskedURL()
        {
            return maskedUrl;
        }

    }

}