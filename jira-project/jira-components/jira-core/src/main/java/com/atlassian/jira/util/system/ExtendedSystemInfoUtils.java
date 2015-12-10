package com.atlassian.jira.util.system;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jdk.utilities.runtimeinformation.MemoryInformation;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.security.auth.trustedapps.TrustedApplicationInfo;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;
import com.atlassian.plugin.Plugin;

import org.ofbiz.core.entity.GenericValue;

/**
 * Contains methods for obtaining various collections of system information in a localised format.
 * In contrast to SystemInfoUtils, these methods are more dependant on components being correctly initialised, thus
 * they should only be used once the application is in a stable state.
 *
 * @since v3.13
 */
public interface ExtendedSystemInfoUtils
{
    /**
     * @return the SystemInfoUtils object
     */
    SystemInfoUtils getSystemInfoUtils();

    /**
     * Default is to hide sensitive system information (for the 500 page, etc.)
     *
     * @return an ordered map of system properties
     */
    Map<String, String> getProps();

    /**
     * @param showSensitiveInfo Whether to include sensitive system information (e.g. database URL)
     * @return an ordered map of system properties
     */
    Map<String, String> getProps(boolean showSensitiveInfo);

    /**
     * The common configuration properties that apply to this instance of JIRA
     *
     * @return a Map of property names to
     */
    Map<String, String> getCommonConfigProperties();

    /**
     * @return memory statistics for the JVM
     */
    Map<String, String> getJvmStats();

    /**
     * @return memory pool statistics for the JVM
     */
    List<MemoryInformation> getMemoryPoolInformation();
    /**
     * @return information about the current build of JIRA
     */
    Map<String, String> getBuildStats();

    /**
     * @return information about the upgrade history of JIRA
     * @since v4.1
     */
    List<UpgradeHistoryItem> getUpgradeHistory();

    /**
     * For each application property, display the key and its value. If the value is a path, then break the paths at
     * semi colons ';' (For non-windows operatin systems, breaks on colon ':' as well). The break is accomplished by
     * replacing the ';' or ':' with the suffix string argument
     *
     * @param suffix the string to add to the suffix of ';' and/or ':'
     * @return Map of system property keys to its value
     */
    Map<String, String> getApplicationPropertiesFormatted(String suffix);

    /**
     * For each system properties, display the key and its value. If the value is a path, then break the paths at semi
     * colons ';' (For non-windows operatin systems, breaks on colon ':' as well). The break is accomplished by replacing
     * the ';' or ':' with the suffix string argument
     *
     * @param suffix the string to add to the suffix of ';' and/or ':'
     * @return Map of system property keys to its value
     */
    Map<String, String> getSystemPropertiesFormatted(String suffix);

    /**
     * @return the default language set in the application's properties - rendered in the current user's locale
     */
    String getDefaultLanguage();

    /**
     * @return the base URL of the application
     */
    String getBaseUrl();

    /**
     * @return usage statistics for the application (user counts, issue counts, etc)
     */
    Map<String, String> getUsageStats();

    /**
     * Checks whether the default locale has been set or not.
     *
     * @return true if default locale is set, false if using system locale
     */
    boolean isUsingSystemLocale();

    /**
     * @return the path to the entityengine.xml file
     */
    // TODO - candidate for SystemInfoUtils ?
    String getEntityEngineXmlPath();

    /**
     * @return the path to the atlassian-jira.log file
     */
    // TODO - candidate for SystemInfoUtils ?
    String getLogPath();

    /**
     * @return the path to the application's index file
     */
    String getIndexLocation();

    /**
     * @return the path to the attachments directory
     */
    String getAttachmentsLocation();

    /**
     * @return the path to the backup directory
     */
    String getBackupLocation();

    /**
     * @return a collection of raw listener GVs
     */
    // TODO - don't use GVs?
    Collection<GenericValue> getListeners();

    /**
     * @return a collection of JiraServiceContainers for each service
     */
    // TODO - don't use JiraServiceContainer?
    Collection<JiraServiceContainer> getServices();

    /**
     * @param serviceContainer the service object to obtain properties for
     * @return a map of properties relating to the service configuration
     */
    Map<String, String> getServicePropertyMap(JiraServiceContainer serviceContainer);

    /**
     * @param ms milliseconds
     * @return the number of minutes represented by the specified number of milliseconds
     */
    // TODO - candidate for SystemInfoUtils ?
    long getMillisecondsToMinutes(long ms);

    /**
     * @return a collection of plugins
     */
    Collection<Plugin> getPlugins();

    /**
     * @param plugin the plugin
     * @return true if the plugin is currently enabled; false otherwise
     */
    boolean isPluginEnabled(Plugin plugin);

    /**
     * @param jiraServiceContext the JIRA service context
     * @return a set of TrustedApplicationInfo objects
     */
    Set<TrustedApplicationInfo> getTrustedApplications(JiraServiceContext jiraServiceContext);

    /**
     * @param info the trusted application
     * @return the set of IP matches defined for this trusted app
     */
    Set<String> getIPMatches(TrustedApplicationInfo info);

    /**
     * @param info the trusted application
     * @return the set of URL matches defined for this trusted app
     */
    Set<String> getUrlMatches(TrustedApplicationInfo info);

    /**
     * @return details of the current license
     */
    Map<String, String> getLicenseInfo();

    /**
     * @return true if using a 1.5 or higher JVM, false otherwise.
     */
    boolean isJvmJava5OrGreater();

    /**
     *
     * @return true if using a JVM that has PermGen space, false if no PermGen available
     * (typically JVM's after Java 1.7, 1.8 now use Metaspace).
     */
    boolean isJvmWithPermGen();

    /**
     * @return the current configured location of shared JIRA Home
     */
    String getJiraHomeLocation();

    /**
     * @return the current configured location of local JIRA Home
     */
    String getJiraLocalHomeLocation();

    /**
     * @return true if the instance is clustered.
     */
    boolean isClustered();

    /**
     * @return A map of cluster nodes mapped to their live status, i.e. are they heartbeating.
     */
    Map<Node, Boolean> getClusterNodeInformation();


}
