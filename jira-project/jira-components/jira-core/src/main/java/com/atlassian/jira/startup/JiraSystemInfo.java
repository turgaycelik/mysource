package com.atlassian.jira.startup;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.configurableobjects.ConfigurableObjectUtil;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationInfo;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationManager;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.jira.service.JiraService;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.log.JiraLogLocator;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.util.system.ReleaseInfo;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.util.system.SystemInfoUtilsImpl;
import com.atlassian.jira.util.system.patch.AppliedPatchInfo;
import com.atlassian.jira.util.system.patch.AppliedPatches;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.config.DatasourceInfo;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This will obtain JIRA system information and place it in the specified {@link com.atlassian.jira.startup.FormattedLogMsg}
 * <p/>
 * This is used at JIRA startup time and is VERY aware of when certain methods can be called and when they cant.  During
 * startup we only access certain JIRA code very carefully.
 *
 * @since v3.13
 */
public class JiraSystemInfo
{
    private static final Logger log = Logger.getLogger(JiraSystemInfo.class);

    private static final String STRANGELY_UNKNOWN = "unknown??";
    private static final long MEGABYTE = 1048576;

    private static final List<String> PATH_RELATED_KEYS;
    private static final Set<String> IGNORE_THESE_KEYS;

    static
    {
        final CollectionBuilder<String> pathRelatedKeys = CollectionBuilder.newBuilder();
        pathRelatedKeys.add("sun.boot.class.path");
        pathRelatedKeys.add("com.ibm.oti.vm.bootstrap.library.path");
        pathRelatedKeys.add("java.library.path");
        pathRelatedKeys.add("java.endorsed.dirs");
        pathRelatedKeys.add("java.ext.dirs");
        pathRelatedKeys.add("java.class.path");
        PATH_RELATED_KEYS = pathRelatedKeys.asList();

        final CollectionBuilder<String> ignoreTheseKeys = CollectionBuilder.newBuilder();
        ignoreTheseKeys.addAll(PATH_RELATED_KEYS);
        ignoreTheseKeys.add("line.separator");
        ignoreTheseKeys.add("path.separator");
        ignoreTheseKeys.add("file.separator");
        IGNORE_THESE_KEYS = ignoreTheseKeys.asSet();
    }

    private final FormattedLogMsg logMsg;
    private final BuildUtilsInfo buildUtilsInfo;
    private final OfBizConnectionFactory connectionFactory;
    private final JiraProperties jiraSystemProperties;

    public JiraSystemInfo(final FormattedLogMsg logMsg, final BuildUtilsInfo buildUtilsInfo)
    {
        this.logMsg = notNull("logMsg", logMsg);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.connectionFactory = notNull("connectionFactory", DefaultOfBizConnectionFactory.getInstance());
        this.jiraSystemProperties = JiraSystemProperties.getInstance();
    }

    /**
     * This only gets the most basic environment information to avoid bring up the JIRA world before the raw database
     * checks are done.
     * <p/>
     * It MUST BE CAREFUL not to access an JIRA code that will bring up the world
     *
     * @param context - a ServletContext that the app is running in.  This may be nulll
     */
    public void obtainBasicInfo(final ServletContext context)
    {
        final SystemInfoUtils systemInfoUtils = new SystemInfoUtilsImpl();
        final ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo(ReleaseInfo.class);

        logMsg.outputHeader("Environment");

        logMsg.outputProperty("JIRA Build", buildUtilsInfo.getBuildInformation());
        logMsg.outputProperty("Build Date", String.valueOf(buildUtilsInfo.getCurrentBuildDate()));
        logMsg.outputProperty("JIRA Installation Type", releaseInfo.getInfo());

        if (context != null)
        {
            logMsg.outputProperty("Application Server",
                    context.getServerInfo() + " - Servlet API " + context.getMajorVersion() + "." + context.getMinorVersion());
        }
        logMsg.outputProperty("Java Version", jiraSystemProperties.getProperty("java.version", STRANGELY_UNKNOWN)
                + " - " + jiraSystemProperties.getProperty("java.vendor", STRANGELY_UNKNOWN));
        logMsg.outputProperty("Current Working Directory", jiraSystemProperties.getProperty("user.dir", STRANGELY_UNKNOWN));

        final Runtime rt = Runtime.getRuntime();
        final long maxMemory = rt.maxMemory() / MEGABYTE;
        final long totalMemory = rt.totalMemory() / MEGABYTE;
        final long freeMemory = rt.freeMemory() / MEGABYTE;
        final long usedMemory = totalMemory - freeMemory;

        logMsg.outputProperty("Maximum Allowable Memory", maxMemory + "MB");
        logMsg.outputProperty("Total Memory", totalMemory + "MB");
        logMsg.outputProperty("Free Memory", freeMemory + "MB");
        logMsg.outputProperty("Used Memory", usedMemory + "MB");

        for (final MemoryInformation memory : systemInfoUtils.getMemoryPoolInformation())
        {
            logMsg.outputProperty("Memory Pool: " + memory.getName(), memory.toString());
        }
        logMsg.outputProperty("JVM Input Arguments", systemInfoUtils.getJvmInputArguments());

        // do we have any patches
        Set<AppliedPatchInfo> appliedPatches = AppliedPatches.getAppliedPatches();
        if (appliedPatches.size() > 0)
        {
            logMsg.outputHeader("Applied Patches");
            for (AppliedPatchInfo appliedPatch : appliedPatches)
            {
                logMsg.outputProperty(appliedPatch.getIssueKey(), appliedPatch.getDescription());
            }
        }
        logMsg.outputProperty("Java Compatibility Information", "JIRA version = " + buildUtilsInfo.getVersion()
                + ", Java Version = " + jiraSystemProperties.getProperty("java.version", STRANGELY_UNKNOWN));
    }

    /**
     * Gets basic Java System Properties.  These are safe to access very early on and won't bring up an unintended JIRA
     * code.
     */
    public void obtainSystemProperties()
    {
        final Properties sysProps = jiraSystemProperties.getProperties();

        @SuppressWarnings ("unchecked")
        // cannot use a generified constructor
        final Map<String, String> properties = new TreeMap(sysProps);
        properties.keySet().removeAll(IGNORE_THESE_KEYS);
        logMsg.outputHeader("Java System Properties");
        for (final Map.Entry<String, String> entry : properties.entrySet())
        {
            logMsg.outputProperty(entry.getKey(), entry.getValue(), ",");
        }
    }

    public void obtainSystemPathProperties()
    {
        final Properties sysProps = jiraSystemProperties.getProperties();
        logMsg.outputHeader("Java Class Paths");
        for (final String key : PATH_RELATED_KEYS)
        {
            final String value = sysProps.getProperty(key, null);
            if (value != null)
            {
                logMsg.outputProperty(key, value, File.pathSeparator);
                logMsg.add("");
            }
        }
    }

    public void obtainUserDirectoyInfo()
    {
        if (JiraUtils.isSetup())
        {
            CrowdDirectoryService crowdDirectoryService = ComponentAccessor.getComponent(CrowdDirectoryService.class);
            logMsg.outputHeader("User Directories (Ordered)");
            List<Directory> directories = crowdDirectoryService.findAllDirectories();
            for (Directory directory : directories)
            {
                logMsg.outputProperty(directory.getType().name(), directory.getName());
                logMsg.outputProperty("Implementing Class", directory.getImplementationClass(), 2);
                logMsg.outputProperty("Allowed Operations", StringUtils.join(directory.getAllowedOperations(), ","), ",", 2);
                logMsg.outputProperty("Encryption Type", directory.getEncryptionType(), 2);
                logMsg.outputProperty("Active", String.valueOf(directory.isActive()), 2);
                logMsg.outputProperty("Attributes", "", 2);
                Map<String, String> attributes = directory.getAttributes();
                ArrayList<String> keys = new ArrayList<String>(attributes.keySet());
                Collections.sort(keys);
                for (String key : keys)
                {
                    String value = key.toLowerCase().contains("password") ? "xxxxxx" : attributes.get(key);
                    logMsg.outputProperty(key, value, 3);
                }
            }
        }
    }

    /**
     * Obtains database configuration information.  This should be called after the database has been checked for sanity
     * and hence we can safely do some entityengine.xml and database connection test. But this is before the database is
     * auto-created and hence the support team can get valuable configuration information before a real DB cockup is
     * encountered.
     */
    public void obtainDatabaseConfigurationInfo()
    {
        logMsg.outputHeader("Database Configuration");
        final URL entityEngineURL = ClassLoaderUtils.getResource("entityengine.xml", getClass());
        logMsg.outputProperty("Loading entityengine.xml from", entityEngineURL.toString());

        final DatasourceInfo datasourceInfo = connectionFactory.getDatasourceInfo();
        if (datasourceInfo != null)
        {
            logMsg.outputProperty("Entity model field type name", datasourceInfo.getFieldTypeName());
            logMsg.outputProperty("Entity model schema name", datasourceInfo.getSchemaName());
        }

        Connection connection = null;
        try
        {
            connection = connectionFactory.getConnection();
            final DatabaseMetaData metaData = connection.getMetaData();
            final SystemInfoUtils jiraSysInfo = new SystemInfoUtilsImpl();

            logMsg.outputProperty("Database Version", metaData.getDatabaseProductName() + " - " + metaData.getDatabaseProductVersion());
            logMsg.outputProperty("Database Driver", metaData.getDriverName() + " - " + metaData.getDriverVersion());
            logMsg.outputProperty("Database URL", maskURL(metaData.getURL()));
            logMsg.outputProperty(jiraSysInfo.getDbDescriptorLabel(), jiraSysInfo.getDbDescriptorValue());
        }
        catch (final SQLException e)
        {
            // dont worry about this exception here. Code later one will barf on the same problem and do more appropriate actions.
            // We are just trying to get startup information for support purposes for now.
            log.debug(e);
        }
        finally
        {
            silentlyClose(connection);
        }

    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainJiraAppProperties()
    {
        final ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo(ReleaseInfo.class);

        final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        /** We want to log this info in English. */
        final ExtendedSystemInfoUtils extendedSystemInfoUtils = new ExtendedSystemInfoUtilsImpl(new I18nBean(Locale.ENGLISH));
        final JiraLicenseService jiraLicenseService = ComponentAccessor.getComponentOfType(JiraLicenseService.class);

        final String defaultLocal = applicationProperties.getDefaultLocale().getDisplayName();
        final String externalUserManagment = (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT) ? "ON" : "OFF");
        final String partnerName = buildUtilsInfo.getBuildPartnerName();
        String baseUrl = applicationProperties.getDefaultBackedString(APKeys.JIRA_BASEURL);
        if (StringUtils.isBlank(baseUrl))
        {
            baseUrl = "not set";
        }

        logMsg.outputHeader("Core Application Properties");
        logMsg.outputProperty("Version", buildUtilsInfo.getVersion());
        logMsg.outputProperty("Build #", buildUtilsInfo.getCurrentBuildNumber());
        logMsg.outputProperty("Build Date", String.valueOf(buildUtilsInfo.getCurrentBuildDate()));
        logMsg.outputProperty("Installation Type", releaseInfo.getInfo());
        if (StringUtils.isNotEmpty(partnerName))
        {
            logMsg.outputProperty("Atlassian Partner", partnerName);
        }
        logMsg.outputProperty("Server ID", jiraLicenseService.getServerId());
        logMsg.outputProperty("Base URL", baseUrl);
        logMsg.outputProperty("Default Language", defaultLocal);
        logMsg.outputProperty("External User Management", externalUserManagment);

        logMsg.outputHeader("Application Properties");
        Map<String, String> allProperties = extendedSystemInfoUtils.getApplicationPropertiesFormatted("\n");
        for (Map.Entry<String, String> entry : allProperties.entrySet())
        {
            logMsg.outputProperty(entry.getKey(), entry.getValue());
        }

        obtainLicenseInfo(jiraLicenseService);
    }

    private void obtainLicenseInfo(final JiraLicenseService jiraLicenseService)
    {
        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
        logMsg.outputHeader("License Details");
        logMsg.outputProperty("License Set", String.valueOf(licenseDetails.isLicenseSet()));
        if (licenseDetails.isLicenseSet())
        {
            logMsg.outputProperty("Entitled To Support", String.valueOf(licenseDetails.isEntitledToSupport()));
            logMsg.outputProperty("Evaluation", String.valueOf(licenseDetails.isEvaluation()));
            logMsg.outputProperty("Description", licenseDetails.getDescription());
            logMsg.outputProperty("SEN", licenseDetails.getSupportEntitlementNumber());
            logMsg.outputProperty("Organisation", licenseDetails.getOrganisation());
            logMsg.outputProperty("Partner", licenseDetails.getPartnerName());
            logMsg.outputProperty("Maximum Number Of Users", licenseDetails.isUnlimitedNumberOfUsers() ? "Unlimited" : String.valueOf(licenseDetails.getMaximumNumberOfUsers()));
        }
        logMsg.add("");
    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainDatabaseStatistics()
    {
        final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        final long issueCount = delegator.getCount("Issue");
        final long projectCount = delegator.getCount("Project");
        final long customFieldCount = delegator.getCount("CustomField");
        final long workflowCount = delegator.getCount("Workflow");
        final long userCount = delegator.getCount("User");
        final long groupCount = delegator.getCount("Group");
        final long attachmentCount = delegator.getCount(AttachmentConstants.ATTACHMENT_ENTITY_NAME);
        final int commentCount = obtainCommentCountFromIndex();

        logMsg.outputHeader("Database Statistics");
        logMsg.outputProperty("Issues", String.valueOf(issueCount));
        logMsg.outputProperty("Projects", String.valueOf(projectCount));
        logMsg.outputProperty("Custom Fields", String.valueOf(customFieldCount));
        logMsg.outputProperty("Workflows", String.valueOf(workflowCount));
        logMsg.outputProperty("Users", String.valueOf(userCount));
        logMsg.outputProperty("Groups", String.valueOf(groupCount));
        logMsg.outputProperty("Attachments", String.valueOf(attachmentCount));
        logMsg.outputProperty("Comments", String.valueOf(commentCount));
    }

    private int obtainCommentCountFromIndex()
    {
        final IssueIndexManager indexManager = ComponentAccessor.getComponentOfType(IssueIndexManager.class);
        try
        {
            return indexManager.getCommentSearcher().getIndexReader().numDocs();
        }
        catch (SearchUnavailableException ignored)
        {
            // this will happen during Setup when the lucene indexes are temporarily in a weird state.
            return 0;
        }
    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainUpgradeHistory()
    {
        final UpgradeManager upgradeManager = ComponentAccessor.getComponentOfType(UpgradeManager.class);
        final List<UpgradeHistoryItem> history = upgradeManager.getUpgradeHistory();

        logMsg.outputHeader("Upgrade History");

        if (history.isEmpty())
        {
            logMsg.add("No upgrade information is available for this instance.", 1);
            return;
        }

        for (UpgradeHistoryItem upgradeHistoryItem : history)
        {
            final String targetVersion = String.format("%s%s%s", upgradeHistoryItem.getTargetVersion(), "#", upgradeHistoryItem.getTargetBuildNumber());
            final String originalVersion;
            if (upgradeHistoryItem.getOriginalVersion() != null)
            {
                originalVersion = String.format("%s%s%s", upgradeHistoryItem.getOriginalVersion(), "#", upgradeHistoryItem.getOriginalBuildNumber());
            }
            else
            {
                originalVersion = null;
            }
            final String date;
            if (upgradeHistoryItem.getTimePerformed() != null)
            {
                date = String.valueOf(upgradeHistoryItem.getTimePerformed());
            }
            else
            {
                date = "Unknown";
            }
            logMsg.add(targetVersion, 1);
            logMsg.outputProperty("Time Performed", date, 2);
            if (originalVersion != null)
            {
                logMsg.outputProperty("Original Version", originalVersion, 2);
            }
            logMsg.add("");
        }
    }

    public void obtainFilePaths()
    {
        obtainFilePaths(ComponentAccessor.getComponent(JiraHome.class));
    }

    /**
     * Only call AFTER JIRA is fully up!
     *
     * @param jiraHome the JIRA home
     */
    public void obtainFilePaths(final JiraHome jiraHome)
    {
        final String indexPath = ComponentAccessor.getIndexPathManager().getIndexRootPath();
        final String attachmentPath = ComponentAccessor.getAttachmentPathManager().getAttachmentPath();

        logMsg.outputHeader("File Paths");
        String jiraHomePath;
        try
        {
            jiraHomePath = jiraHome.getHomePath();
        }
        catch (IllegalStateException e)
        {
            jiraHomePath = "";
        }
        logMsg.outputProperty("JIRA Home", jiraHomePath);
        String jiraLocalHomePath;
        try
        {
            jiraLocalHomePath = jiraHome.getLocalHomePath();
        }
        catch (IllegalStateException e)
        {
            jiraLocalHomePath = "";
        }
        logMsg.outputProperty("JIRA Local Home", jiraLocalHomePath);
        logMsg.outputProperty("Location of atlassian-jira.log", getLogPath(jiraHome));
        logMsg.outputProperty("Index Path", indexPath);
        logMsg.outputProperty("Attachment Path", attachmentPath);
    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainPlugins()
    {
        final PluginInfoProvider pluginInfoProvider = ComponentAccessor.getComponentOfType(PluginInfoProvider.class);

        logMsg.add(pluginInfoProvider.getUserPlugins().prettyPrint());
        logMsg.add(pluginInfoProvider.getSystemPlugins(false).prettyPrint());
    }

    /**
     * A simple class that allows easier represenation of Listener information, along with some simple sorting. FindBugs
     * made me put the hashCode/equals on it!
     */
    private static class ListenerRepresentation implements Comparable<ListenerRepresentation>
    {
        private final String name;
        private final String clazz;
        private final GenericValue gv;

        ListenerRepresentation(final GenericValue gv)
        {
            name = gv.getString("name");
            clazz = gv.getString("clazz");
            this.gv = gv;
        }

        public int compareTo(final ListenerRepresentation that)
        {
            int rc = name.compareTo(that.name);
            if (rc == 0)
            {
                rc = clazz.compareTo(that.clazz);
            }
            return rc;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ListenerRepresentation))
            {
                return false;
            }

            final ListenerRepresentation that = (ListenerRepresentation) o;

            if (name != null ? !name.equals(that.name) : that.name != null)
            {
                return false;
            }
            if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = (name != null ? name.hashCode() : 0);
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            return result;
        }
    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainListeners()
    {
        final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        final List<GenericValue> listenerGVs = delegator.findAll("ListenerConfig");
        final List<ListenerRepresentation> listeners = new ArrayList<ListenerRepresentation>();
        for (final GenericValue gv : listenerGVs)
        {
            final String name = gv.getString("name");
            if (!name.startsWith("com.atlassian.jira.event,listeners"))
            {
                listeners.add(new ListenerRepresentation(gv));
            }
        }
        logMsg.outputHeader("Listeners");
        logMsg.outputProperty("Instance Count", String.valueOf(listeners.size()));
        logMsg.add("");

        for (final ListenerRepresentation rep : listeners)
        {
            logMsg.outputProperty(rep.name, rep.clazz);
            logPropertySet(rep.gv, 2);
        }
    }

    private void logPropertySet(final GenericValue gv, final int indentLevel)
    {
        final PropertySet propertySet = OFBizPropertyUtils.getCachingPropertySet(gv);
        if (propertySet != null)
        {
            @SuppressWarnings("unchecked")
            final Collection<String> properties = propertySet.getKeys();
            for (final String key : properties)
            {
                logMsg.outputProperty(key, String.valueOf(propertySet.getAsActualType(key)), indentLevel);
            }
        }
    }

    /**
     * Only call AFTER JIRA is fully up!
     */
    public void obtainServices()
    {
        final ServiceManager serviceManager = ComponentAccessor.getComponentOfType(ServiceManager.class);
        final Collection<JiraServiceContainer> services = new TreeSet<JiraServiceContainer>(JiraService.NAME_COMPARATOR);
        services.addAll(serviceManager.getServices());

        logMsg.outputHeader("Services");
        logMsg.outputProperty("Instance Count", String.valueOf(services.size()));
        logMsg.add("");

        for (final JiraServiceContainer service : services)
        {
            printServiceInfo(service);
        }
    }

    private void printServiceInfo(JiraServiceContainer service)
    {
        logMsg.outputProperty(service.getName(), service.getServiceClass());
        logMsg.outputProperty("Service Delay", String.valueOf(service.getDelay()) + "ms", 2);
        try
        {
            final long lastRun = service.getLastRun();
            if (lastRun > 0)
            {
                final DateFormat df = DateFormat.getInstance();
                final Date dt = new Date(lastRun);
                logMsg.outputProperty("Last Run", df.format(dt));
            }
            final Map<String, String> properties = ConfigurableObjectUtil.getPropertyMap(service);
            for (final Map.Entry<String, String> entry : properties.entrySet())
            {
                logMsg.outputProperty(entry.getKey(), entry.getValue(), 2);
            }
        }
        catch (final Exception e)
        {
            logMsg.outputProperty("Exception getting Service information", e.toString());
        }
    }

    /**
     * Only call AFTER JIRA is fully up!
     * <p/>
     * NOTE : Only PRIVILEGED CODE should make this call.  This code goes to the Trusted Apps Manager directly.  It does
     * this because at JIRA start time, there is no user.  So be careful when calling this method and don't propagate
     * more permissions that are required.
     */
    public void obtainTrustedApps()
    {
        final TrustedApplicationManager trustedAppManager = ComponentAccessor.getComponentOfType(TrustedApplicationManager.class);

        final Collection<TrustedApplicationInfo> trustedApplications = new TreeSet<TrustedApplicationInfo>(TrustedApplicationService.NAME_COMPARATOR);
        //
        // PRIVILEGED CALL HERE
        trustedApplications.addAll(trustedAppManager.getAll());

        logMsg.outputHeader("Trusted Applications");
        logMsg.outputProperty("Instance Count", String.valueOf(trustedApplications.size()));
        logMsg.add("");

        for (final Object element : trustedApplications)
        {
            final TrustedApplicationInfo applicationInfo = (TrustedApplicationInfo) element;
            logMsg.outputProperty("Application Name", applicationInfo.getName());
            logMsg.outputProperty("Matching URLs", applicationInfo.getUrlMatch(), 2);
            logMsg.outputProperty("Matching IP", applicationInfo.getIpMatch(), 2);
        }
    }

    /*====================================================*/

    private void silentlyClose(final Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (final SQLException e)
            {
                // ignore silently
            }
        }
    }

    private String maskURL(final String url)
    {
        if (url == null)
        {
            return null;
        }
        final Pattern passwordRegex = Pattern.compile("password=[^&;]*", Pattern.CASE_INSENSITIVE);
        return passwordRegex.matcher(url).replaceAll("password=****");
    }

    private String getLogPath(final JiraHome jiraHome)
    {
        final JiraLogLocator locator = new JiraLogLocator(jiraHome);
        final File logFile = locator.findJiraLogFile();
        if (logFile != null)
        {
            return logFile.getAbsolutePath();
        }
        else
        {
            return STRANGELY_UNKNOWN;
        }
    }
}
