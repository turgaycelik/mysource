package com.atlassian.jira.util.system;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jdk.utilities.JvmProperties;
import com.atlassian.jdk.utilities.exception.InvalidVersionException;
import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.cache.HashRegistryCache;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.configurableobjects.ConfigurableObjectUtil;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationInfo;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationService;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationUtil;
import com.atlassian.jira.service.JiraService;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.startup.PluginComparator;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.log.JiraLogLocator;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.modzdetector.Modifications;
import com.atlassian.modzdetector.ModzRegistryException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import webwork.action.ActionContext;

import static com.atlassian.jira.component.ComponentAccessor.getComponentOfType;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Implementation for the ExtendedSystemInfoUtils interface.
 *
 * @since v3.13
 */
public class ExtendedSystemInfoUtilsImpl implements ExtendedSystemInfoUtils
{
    private static final Logger log = Logger.getLogger(ExtendedSystemInfoUtilsImpl.class);
    private static final List<String> defaultProps;
    private static final List<Pattern> securityBlackListedProps;
    private static final int MILLISECONDS_IN_MINUTE = 60000;

    static
    {
        final List<String> props = new ArrayList<String>();
        props.add("user.dir");
        props.add("java.version");
        props.add("java.vendor");
        props.add("java.vm.specification.version");
        props.add("java.vm.specification.vendor");
        props.add("java.vm.version");
        props.add("java.runtime.name");
        props.add("java.vm.name");
        props.add("user.name");
        props.add("user.timezone");
        props.add("file.encoding");
        props.add("os.name");
        props.add("os.version");
        props.add("os.arch");
        props.add("os.arch");
        defaultProps = Collections.unmodifiableList(props);
    }

    static
    {
        final List<String> props = new ArrayList<String>();
        props.add("License.*");
        props.add("jira\\.sid\\.key");
        props.add("org\\.apache\\.shindig\\.common\\.crypto\\.BlobCrypter\\:key");
        props.add("applinks\\..*");

        final List<Pattern> rules = new ArrayList<Pattern>();
        for (String prop : props)
        {
            Pattern pattern = Pattern.compile(prop);
            rules.add(pattern);
        }
        securityBlackListedProps = Collections.unmodifiableList(rules);
    }

    private final DateFormat dateFormatter = new SimpleDateFormat("EEEEE, dd MMM yyyy");
    private final DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss Z");

    private final SystemInfoUtils systemInfoUtils;
    private final ServiceManager serviceManager;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final TrustedApplicationService trustedAppService;
    private final OfBizDelegator ofBizDelegator;
    private final I18nHelper i18nHelper;
    private final UserUtil userUtil;
    private final HashRegistryCache registry;
    private final LocaleManager localeManager;
    private final JiraLicenseService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final UpgradeManager upgradeManager;
    private final IssueIndexManager indexManager;
    private final JiraProperties jiraSystemProperties;
    private final ClusterManager clusterManager;

    protected ExtendedSystemInfoUtilsImpl(final SystemInfoUtils systemInfoUtils, final ServiceManager serviceManager, final PluginAccessor pluginAccessor, final ApplicationProperties applicationProperties, final TrustedApplicationService trustedAppService, final OfBizDelegator ofBizDelegator, final I18nHelper i18nHelper, final UserUtil userUtil, final HashRegistryCache registry, final LocaleManager localeManager, final JiraLicenseService jiraLicenseService, final BuildUtilsInfo buildUtilsInfo, final UpgradeManager upgradeManager, final IssueIndexManager indexManager, final JiraProperties jiraSystemProperties, final ClusterManager clusterManager)
    {
        this.systemInfoUtils = systemInfoUtils;
        this.serviceManager = serviceManager;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.trustedAppService = trustedAppService;
        this.ofBizDelegator = ofBizDelegator;
        this.i18nHelper = i18nHelper;
        this.userUtil = userUtil;
        this.registry = registry;
        this.localeManager = localeManager;
        this.indexManager = indexManager;
        this.jiraSystemProperties = jiraSystemProperties;
        this.clusterManager = clusterManager;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.upgradeManager = notNull("upgradeManager", upgradeManager);
    }

    public ExtendedSystemInfoUtilsImpl(final I18nHelper i18nHelper)
    {
        this(new SystemInfoUtilsImpl(), getComponentOfType(ServiceManager.class), getComponentOfType(PluginAccessor.class),
                ComponentAccessor.getApplicationProperties(), getComponentOfType(TrustedApplicationService.class),
                ComponentAccessor.getOfBizDelegator(), i18nHelper, ComponentAccessor.getUserUtil(), getComponentOfType(HashRegistryCache.class),
                getComponentOfType(LocaleManager.class), getComponentOfType(JiraLicenseService.class),
                getComponentOfType(BuildUtilsInfo.class), getComponentOfType(UpgradeManager.class), getComponentOfType(IssueIndexManager.class),
                getComponentOfType(JiraProperties.class), getComponentOfType(ClusterManager.class));
    }

    public SystemInfoUtils getSystemInfoUtils()
    {
        return systemInfoUtils;
    }

    public Map<String, String> getProps()
    {
        return getProps(false);
    }

    public Map<String, String> getProps(final boolean showSensitiveInfo)
    {
        final MapBuilder<String, String> props = MapBuilder.newBuilder();
        final Properties sysProps = jiraSystemProperties.getProperties();

        props.add(getText("admin.systeminfo.system.date"), dateFormatter.format(new Date()));
        props.add(getText("admin.systeminfo.system.time"), timeFormatter.format(new Date()));
        props.add(getText("admin.systeminfo.system.cwd"), sysProps.getProperty("user.dir"));

        props.add(getText("admin.systeminfo.java.version"), sysProps.getProperty("java.version"));
        props.add(getText("admin.systeminfo.java.vendor"), sysProps.getProperty("java.vendor"));
        props.add(getText("admin.systeminfo.jvm.version"), sysProps.getProperty("java.vm.specification.version"));
        props.add(getText("admin.systeminfo.jvm.vendor"), sysProps.getProperty("java.vm.specification.vendor"));
        props.add(getText("admin.systeminfo.jvm.implementation.version"), sysProps.getProperty("java.vm.version"));
        props.add(getText("admin.systeminfo.java.runtime"), sysProps.getProperty("java.runtime.name"));
        props.add(getText("admin.systeminfo.java.vm"), sysProps.getProperty("java.vm.name"));

        props.add(getText("admin.systeminfo.user.name"), sysProps.getProperty("user.name"));
        props.add(getText("admin.systeminfo.user.timezone"), sysProps.getProperty("user.timezone"));
        props.add(getText("admin.systeminfo.user.locale"), (Locale.getDefault() == null ? "null" : Locale.getDefault().getDisplayName()));
        props.add(getText("admin.systeminfo.system.encoding"), sysProps.getProperty("file.encoding"));

        props.add(getText("admin.systeminfo.operating.system"), sysProps.getProperty("os.name") + " " + sysProps.getProperty("os.version"));
        props.add(getText("admin.systeminfo.os.architecture"), sysProps.getProperty("os.arch"));

        String serverInfo = "";
        try
        {
            serverInfo = (ActionContext.getServletContext().getServerInfo());
        }
        catch (final Exception e)
        {
            //do nothing - we may not have an action context here.
        }

        props.add(getText("admin.systeminfo.application.server.container"), serverInfo);
        props.add(getText("admin.systeminfo.database.type"), systemInfoUtils.getDatabaseType());
        props.add(getText("admin.systeminfo.database.jndi.address"), systemInfoUtils.getDbDescriptorValue());

        try
        {
            final SystemInfoUtils.DatabaseMetaData databaseMetaData = systemInfoUtils.getDatabaseMetaData();
            if (showSensitiveInfo)
            {
                props.add(getText("admin.systeminfo.database.url"), databaseMetaData.getMaskedURL());
            }
            else
            {
                props.add(getText("admin.systeminfo.database.url"), getText("admin.systeminfo.hidden.field"));
            }
            props.add(getText("admin.systeminfo.database.version"), databaseMetaData.getDatabaseProductVersion());
            props.add(getText("admin.systeminfo.database.driver"), databaseMetaData.getDriverName() + " " + databaseMetaData.getDriverVersion());
        }
        catch (final Exception e)
        {
            props.add(getText("admin.systeminfo.database.accesserror"), e.toString());
            log.error(e, e);
        }

        try
        {
            props.add(
                    getText("admin.generalconfiguration.external.user.management"),
                    applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT) ? getText("admin.common.words.on") : getText("admin.common.words.off"));
        }
        catch (final Exception e)
        {
            log.error("Error getting the " + APKeys.JIRA_OPTION_USER_EXTERNALMGT + " application property: " + e, e);
            props.add(getText("admin.generalconfiguration.external.user.management"), "ERROR");
        }

        if (isJvmJava5OrGreater())
        {
            props.add(getText("admin.systeminfo.jvm.input.arguments"), systemInfoUtils.getJvmInputArguments());
        }
        else
        {
            props.add(getText("admin.systeminfo.jvm.input.arguments"), getText("admin.systeminfo.not.possible"));
        }
        if (showSensitiveInfo)
        {
            String modifiedFilesDescription = getText("admin.systeminfo.modz.missing");
            String removedFilesDescription = getText("admin.systeminfo.modz.missing");

            // in the interest of keeping the page load relatively quick (at least
            // for subsequent loads) we only do the hashing and detection once and
            // then cache the results
            try
            {
                final Modifications modifications = registry.getModifications();
                if (!modifications.modifiedFiles.isEmpty())
                {
                    modifiedFilesDescription = StringUtils.join(modifications.modifiedFiles, ", ");
                }
                else
                {
                    modifiedFilesDescription = getText("admin.systeminfo.modz.nomodifications");
                }
                if (!modifications.removedFiles.isEmpty())
                {
                    removedFilesDescription = StringUtils.join(modifications.removedFiles, ", ");
                }
                else
                {
                    removedFilesDescription = getText("admin.systeminfo.modz.noremovals");
                }
            }
            catch (final ModzRegistryException e)
            {
                // no need to handle the exception. just leaving modifications
                // unset will result in us doing the right thing: displaying a
                // vaguely informative error message and then trying to re-detect
                // changes the next time the page is loaded.
                log.error(e);
            }
            catch (final RuntimeException e)
            {
                log.error(e);
            }

            final String installType = "[" + getText("admin.systeminfo.installation.type") + ": " + systemInfoUtils.getInstallationType() + "] ";
            props.add(getText("admin.systeminfo.modz.modified"), installType + modifiedFilesDescription);
            props.add(getText("admin.systeminfo.modz.removed"), installType + removedFilesDescription);
        }

        props.add(getText("admin.systeminfo.clustered"), clusterManager.isClustered() ? getText("admin.common.words.on") : getText("admin.common.words.off"));
        if (clusterManager.isClustered())
        {
            props.add(getText("admin.systeminfo.clustered.node.id"), clusterManager.getNodeId());
        }

        return props.toListOrderedMap();
    }

    public Map<String, String> getApplicationPropertiesFormatted(final String suffix)
    {
        Map<String,Object> properties = applicationProperties.asMap();
        final Map<String, String> props = new TreeMap<String, String>();
        for (String propertyName : properties.keySet())
        {
            boolean blacklist = false;
            for (Pattern rule : securityBlackListedProps)
            {
                if (rule.matcher(propertyName).matches())
                {
                    blacklist = true;
                    break;
                }
            }
            if (!blacklist)
            {
                props.put(propertyName, String.valueOf(properties.get(propertyName)));
            }
        }
        final Map<String, String> propsMap = new LinkedHashMap<String, String>();
        propsMap.putAll(props);
        return propsMap;
    }



    public Map<String, String> getSystemPropertiesFormatted(final String suffix)
    {
        final Properties sysProps = jiraSystemProperties.getProperties();

        @SuppressWarnings ("unchecked")
        final Enumeration<String> propNames = (Enumeration<String>) sysProps.propertyNames();
        final boolean isWindows = sysProps.getProperty("os.name").toLowerCase(Locale.getDefault()).startsWith("windows");
        final Map<String, String> props = new TreeMap<String, String>();
        final Map<String, String> pathProps = new TreeMap<String, String>();
        while (propNames.hasMoreElements())
        {
            final String propertyName = propNames.nextElement();
            if (!defaultProps.contains(propertyName))
            {
                if (propertyName.endsWith(".path"))
                {
                    String htmlValue = sysProps.getProperty(propertyName);
                    if (!isWindows)//for non-windows operating systems split on colons as well
                    {
                        htmlValue = breakSeperators(htmlValue, ":", suffix);
                    }
                    //as this entry spans multiple lines, put it at the end of the map
                    pathProps.put(propertyName, breakSeperators(htmlValue, ";", suffix));
                }
                else
                {
                    props.put(propertyName, sysProps.getProperty(propertyName));
                }
            }
        }

        final Map<String, String> propsMap = new LinkedHashMap<String, String>();
        propsMap.putAll(props);
        //put all the properties to be added later to the end
        propsMap.putAll(pathProps);
        return propsMap;
    }

    public Map<String, String> getLicenseInfo()
    {
        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();

        final Map<String, String> licenseInfo = new LinkedHashMap<String, String>();
        licenseInfo.put(getText("admin.license.date.purchased"), licenseDetails.getPurchaseDate(getOutlookDate()));
        licenseInfo.put(getText("admin.license.type"), licenseDetails.getDescription());
        if (!licenseDetails.isUnlimitedNumberOfUsers())
        {
            final int userLimit = licenseDetails.getMaximumNumberOfUsers();
            licenseInfo.put(getText("admin.license.user.limit"), userLimit + " (" + getText("admin.license.active.user.count",
                    String.valueOf(userUtil.getActiveUserCount())) + ")");
        }
        licenseInfo.put(getText("admin.license.maintenance.period.end.date"), licenseDetails.getMaintenanceEndString(getOutlookDate()));
        licenseInfo.put(getText("admin.license.maintenance.status"), licenseDetails.getBriefMaintenanceStatusMessage(i18nHelper));
        licenseInfo.put(getText("admin.license.sen"), StringUtils.isNotBlank(licenseDetails.getSupportEntitlementNumber()) ? licenseDetails.getSupportEntitlementNumber() : getText("common.concepts.not.applicable"));
        return licenseInfo;
    }

    private String breakSeperators(final String input, final String seperator, final String suffix)
    {
        return input.replaceAll(seperator, seperator + suffix);
    }

    public Map<String, String> getJvmStats()
    {
        final MapBuilder<String, String> jvmStats = MapBuilder.newBuilder();
        jvmStats.add(getText("admin.systeminfo.total.memory"), systemInfoUtils.getTotalMemory() + " MB");
        jvmStats.add(getText("admin.systeminfo.free.memory"), systemInfoUtils.getFreeMemory() + " MB");
        jvmStats.add(getText("admin.systeminfo.used.memory"), systemInfoUtils.getUsedMemory() + " MB");
        if(isJvmWithPermGen())
        {
            jvmStats.add(getText("admin.systeminfo.total.perm.gen.memory"), systemInfoUtils.getTotalPermGenMemory() + " MB");
            jvmStats.add(getText("admin.systeminfo.free.perm.gen.memory"), systemInfoUtils.getFreePermGenMemory() + " MB");
            jvmStats.add(getText("admin.systeminfo.used.perm.gen.memory"), systemInfoUtils.getUsedPermGenMemory() + " MB");
        }
        return jvmStats.toListOrderedMap();
    }

    public Map<String, String> getCommonConfigProperties()
    {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        map.put(getText("admin.config.allow.attachments"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)));
        map.put(getText("admin.config.allow.voting"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)));
        map.put(getText("admin.config.allow.issue.watching"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING)));
        map.put(getText("admin.config.allow.unassigned"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)));
        map.put(getText("admin.config.allow.subtasks"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS)));
        map.put(getText("admin.config.allow.issue.linking"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)));
        map.put(getText("admin.config.timetracking.enabled"), String.valueOf(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)));
        map.put(getText("admin.config.timetracking.hours.per.day"), applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY));
        map.put(getText("admin.config.timetracking.days.per.week"), applicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK));

        return map;
    }

    public List<MemoryInformation> getMemoryPoolInformation()
    {
        return systemInfoUtils.getMemoryPoolInformation();
    }

    public Map<String, String> getBuildStats()
    {
        final MapBuilder<String, String> buildstats = MapBuilder.newBuilder();
        buildstats.add(getText("admin.systeminfo.uptime"), systemInfoUtils.getUptime(i18nHelper.getDefaultResourceBundle()));
        buildstats.add(getText("admin.systeminfo.version"), buildUtilsInfo.getVersion());
        buildstats.add(getText("admin.systeminfo.build.number"), buildUtilsInfo.getCurrentBuildNumber());
        buildstats.add(getText("admin.systeminfo.build.date"), String.valueOf(buildUtilsInfo.getCurrentBuildDate()));
        buildstats.add(getText("admin.systeminfo.build.revision"), String.valueOf(buildUtilsInfo.getCommitId()));
        buildstats.add(getText("admin.license.partner.name"), buildUtilsInfo.getBuildPartnerName());
        buildstats.add(getText("admin.systeminfo.installation.type"), systemInfoUtils.getInstallationType());
        buildstats.add(getText("admin.server.id"), jiraLicenseService.getServerId());

        // if there is an upgrade history - display it
        final List<UpgradeHistoryItem> historyItems = upgradeManager.getUpgradeHistory();
        if (!historyItems.isEmpty())
        {
            final UpgradeHistoryItem lastUpgrade = historyItems.get(0);
            final StringBuilder sb = new StringBuilder(getOutlookDate().formatDMYHMS(lastUpgrade.getTimePerformed()));

            // if we have a previous version, also display which version was last used
            if (!StringUtils.isBlank(lastUpgrade.getOriginalVersion()) && !StringUtils.isBlank(lastUpgrade.getOriginalBuildNumber()))
            {
                final String versionPriorToUpgrade = lastUpgrade.getOriginalVersion();
                final String numberPriorToUpgrade = lastUpgrade.getOriginalBuildNumber();
                sb.append(String.format(" (v%s%s%s)", versionPriorToUpgrade, "#", numberPriorToUpgrade));
            }

            buildstats.add(getText("admin.systeminfo.last.upgrade"), sb.toString());
        }

        return buildstats.toListOrderedMap();
    }

    public List<UpgradeHistoryItem> getUpgradeHistory()
    {
        return upgradeManager.getUpgradeHistory();
    }

    public String getDefaultLanguage()
    {
        return applicationProperties.getDefaultLocale().getDisplayName(i18nHelper.getLocale());
    }

    public String getBaseUrl()
    {
        return applicationProperties.getDefaultBackedString(APKeys.JIRA_BASEURL);
    }

    public boolean isUsingSystemLocale()
    {
        return localeManager.getLocale(applicationProperties.getDefaultBackedString(APKeys.JIRA_I18N_DEFAULT_LOCALE)) == null;
    }

    public Map<String, String> getUsageStats()
    {
        final MapBuilder<String, String> usageStats = MapBuilder.newBuilder();
        try
        {
            final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();

            usageStats.add(getText("admin.systeminfo.issues"), Long.toString(delegator.getCount("Issue")));
            usageStats.add(getText("admin.systeminfo.projects"), Long.toString(delegator.getCount("Project")));
            usageStats.add(getText("admin.systeminfo.custom.fields"), Long.toString(delegator.getCount("CustomField")));
            usageStats.add(getText("admin.systeminfo.workflows"), Long.toString(delegator.getCount("Workflow")));
            usageStats.add(getText("admin.systeminfo.attachments"), Long.toString(delegator.getCount(AttachmentConstants.ATTACHMENT_ENTITY_NAME)));
            usageStats.add(getText("admin.systeminfo.comments"), Long.toString((long) indexManager.getCommentSearcher().getIndexReader().numDocs()));

            boolean externalUserManagment = false;
            try
            {
                externalUserManagment = applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
            }
            catch (final Exception e)
            {
                log.error("Error reading " + APKeys.JIRA_OPTION_USER_EXTERNALMGT + " application property: " + e, e);
            }

            if (externalUserManagment)
            {
                usageStats.add(getText("admin.systeminfo.users"),
                        delegator.getCount("User") + " " + getText("admin.generalconfiguration.external.user.management.statistics"));
                usageStats.add(getText("admin.systeminfo.groups"),
                        delegator.getCount("Group") + " " + getText("admin.generalconfiguration.external.user.management.statistics"));

            }
            else
            {
                usageStats.add(getText("admin.systeminfo.users"), Long.toString(delegator.getCount("User")));
                usageStats.add(getText("admin.systeminfo.groups"), Long.toString(delegator.getCount("Group")));
            }
        }
        catch (final Exception e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error while retrieving usage statistics", e);
            }
            //not much we can do
            return null;
        }
        return usageStats.toListOrderedMap();
    }

    public String getEntityEngineXmlPath()
    {
        try
        {
            return ClassLoaderUtils.getResource(EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME, getClass()).toExternalForm();
        }
        catch (final Exception e)
        {
            log.error("Could not load " + EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME + " path " + e.getMessage(), e);
            return "Could not load " + EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME + "  path.  Exception " + e.getMessage();
        }
    }

    public String getLogPath()
    {
        final JiraLogLocator locator = getComponentOfType(JiraLogLocator.class);
        final File logFile = locator.findJiraLogFile();
        final String path;
        if (logFile != null)
        {
            path = logFile.getAbsolutePath();
        }
        else
        {
            path = "Could not find " + JiraLogLocator.AJL_FILE_NAME + ".";
            log.debug(path);
        }
        return path;
    }

    public String getIndexLocation()
    {
        return ComponentAccessor.getIndexPathManager().getIndexRootPath();
    }

    public String getAttachmentsLocation()
    {
        return ComponentAccessor.getAttachmentPathManager().getAttachmentPath();
    }

    public String getBackupLocation()
    {
        final JiraHome jiraHome = getComponentOfType(JiraHome.class);
        try
        {
            return jiraHome.getExportDirectory().getPath();
        }
        catch (final IllegalStateException e)
        {
            return "";
        }
    }

    public String getJiraHomeLocation()
    {
        final JiraHome jiraHome = getComponentOfType(JiraHome.class);
        String path;
        try
        {
            path = jiraHome.getHomePath();
        }
        catch (final IllegalStateException e)
        {
            path = "";
        }
        return path;
    }

    public String getJiraLocalHomeLocation()
    {
        final JiraHome jiraHome = getComponentOfType(JiraHome.class);
        String path;
        try
        {
            path = jiraHome.getLocalHomePath();
        }
        catch (final IllegalStateException e)
        {
            path = "";
        }
        return path;
    }

    public Collection<GenericValue> getListeners()
    {
        final Collection<GenericValue> listeners = new TreeSet<GenericValue>(new Comparator<GenericValue>()
        {
            public int compare(final GenericValue o1, final GenericValue o2)
            {
                final String name1 = getName(o1);
                final String name2 = getName(o2);
                return name1.compareTo(name2);
            }

            private String getName(final GenericValue o)
            {
                return o.getString("name");
            }

        });
        @SuppressWarnings ("unchecked")
        final List<GenericValue> allListeners = ofBizDelegator.findAll("ListenerConfig");
        listeners.addAll(allListeners);
        return listeners;
    }

    public Collection<JiraServiceContainer> getServices()
    {
        final Collection<JiraServiceContainer> services = new TreeSet<JiraServiceContainer>(JiraService.NAME_COMPARATOR);
        services.addAll(serviceManager.getServices());
        return services;
    }

    public Map<String, String> getServicePropertyMap(final JiraServiceContainer serviceContainer)
    {
        try
        {
            return ConfigurableObjectUtil.getPropertyMap(serviceContainer);
        }
        catch (final RuntimeException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public long getMillisecondsToMinutes(final long ms)
    {
        return ms / MILLISECONDS_IN_MINUTE;
    }

    public Collection<Plugin> getPlugins()
    {
        final SortedSet<Plugin> plugins = new TreeSet<Plugin>(new PluginComparator());
        for(Plugin plugin : pluginAccessor.getPlugins()) {
            if (!plugins.add(plugin)) {
                throw new IllegalStateException("Multiple plugins with the same key and version:" + plugin.getKey() + " " + plugin.getPluginsVersion());
            }
        }
        return Collections.unmodifiableSet(plugins);
    }

    public boolean isPluginEnabled(final Plugin plugin)
    {
        return pluginAccessor.isPluginEnabled(plugin.getKey());
    }

    public Set<TrustedApplicationInfo> getTrustedApplications(final JiraServiceContext jiraServiceContext)
    {
        final Set<TrustedApplicationInfo> trustedApplications = new TreeSet<TrustedApplicationInfo>(TrustedApplicationService.NAME_COMPARATOR);
        trustedApplications.addAll(trustedAppService.getAll(jiraServiceContext));
        return trustedApplications;
    }

    public Set<String> getIPMatches(final TrustedApplicationInfo info)
    {
        return TrustedApplicationUtil.getLines(info.getIpMatch());
    }

    public Set<String> getUrlMatches(final TrustedApplicationInfo info)
    {
        return TrustedApplicationUtil.getLines(info.getUrlMatch());
    }

    public boolean isJvmJava5OrGreater()
    {
        try
        {
            return JvmProperties.isJvmVersion(1.5F);
        }
        catch (final InvalidVersionException e)
        {
            return false;
        }
    }

    @Override
    public boolean isJvmWithPermGen()
    {
        return systemInfoUtils.getTotalPermGenMemory() > 0;
    }

    private OutlookDate getOutlookDate()
    {
        return ComponentAccessor.getComponent(OutlookDateManager.class).getOutlookDate(i18nHelper.getLocale());
    }

    private String getText(final String key)
    {
        return i18nHelper.getText(key);
    }

    private String getText(final String key, final String value1)
    {
        return i18nHelper.getText(key, value1);
    }

    @Override
    public boolean isClustered()
    {
        return clusterManager.isClustered();
    }

    @Override
    public Map<Node, Boolean> getClusterNodeInformation()
    {
        Map<Node, Boolean> nodeMap = new HashMap<Node, Boolean>();
        if (clusterManager.isClustered())
        {
            Collection<Node> allNodes = clusterManager.getAllNodes();
            Collection<Node> liveNodes = clusterManager.findLiveNodes();
            for (Node node : allNodes)
            {
                boolean isLive = false;
                for (Node liveNode : liveNodes)
                {
                    if (StringUtils.equals(liveNode.getNodeId(), node.getNodeId()))
                    {
                        isLive = true;
                    }
                }
                nodeMap.put(node, isLive);
            }
        }
        return ImmutableMap.copyOf(nodeMap);
    }
}
