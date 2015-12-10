package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.lang.Pair;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This class gives access to build-time properties at runtime. Its sources are the legacy {@link
 * com.atlassian.jira.util.BuildUtils} class and the <code>BuildUtilsInfo.properties</code> file.
 *
 * @since v4.0
 */
public class BuildUtilsInfoImpl implements BuildUtilsInfo
{
    /**
     * The name of the properties file that contains the build properties.
     */
    private static final String BUILD_PROPERTIES_FILENAME = "jira-build.properties";

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BuildUtilsInfoImpl.class);

    /**
     * The build properties.
     */
    private final Properties buildProperties;
    private final int applicationBuildNumber;

    /**
     * Creates a new BuildUtilsInfoImpl, loading the properties from the '{@value #BUILD_PROPERTIES_FILENAME}' file.
     * file.
     */
    public BuildUtilsInfoImpl()
    {
        applicationBuildNumber = Integer.parseInt(BuildUtils.getCurrentBuildNumber());
        buildProperties = loadProperties();
    }

    public String getVersion()
    {
        return BuildUtils.getVersion();
    }

    @Override
    public String getDocVersion()
    {
        return buildProperties.getProperty("jira.docs.version");
    }

    public int[] getVersionNumbers()
    {
        return parseVersion(getVersion()).first();
    }

    public String getCurrentBuildNumber()
    {
        return BuildUtils.getCurrentBuildNumber();
    }

    @Override
    public int getApplicationBuildNumber()
    {
        return applicationBuildNumber;
    }

    @Override
    public int getDatabaseBuildNumber()
    {
        final String patchedVersion = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_PATCHED_VERSION);
        if (patchedVersion == null)
        {
            return 0;
        }
        return Integer.parseInt(patchedVersion);
    }

    public String getMinimumUpgradableBuildNumber()
    {
        return BuildUtils.getMinimumUpgradableBuildNumber();
    }

    public Date getCurrentBuildDate()
    {
        return BuildUtils.getCurrentBuildDate();
    }

    public String getBuildPartnerName()
    {
        return BuildUtils.getBuildPartnerName();
    }

    public String getBuildInformation()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getVersion());
        sb.append("#");
        sb.append(getCurrentBuildNumber());

        // the revision may be blank when building the source release where the source wasn't checked out
        // from SCM!
        if (isNotBlank(getCommitId()))
        {
            sb.append("-sha1:").append(getCommitId());
        }
        if (isNotBlank(getBuildPartnerName()))
        {
            sb.append("-").append(getBuildPartnerName());
        }

        return sb.toString();
    }

    @Override
    @Deprecated
    public String getSvnRevision()
    {
        return getCommitId();
    }

    @Override
    public String getCommitId()
    {
        return BuildUtils.getCommitId();
    }

    public String getMinimumUpgradableVersion()
    {
        return BuildUtils.getMinimumUpgradableVersion();
    }

    public Collection<Locale> getUnavailableLocales()
    {
        return BuildUtils.getUnavailableLocales();
    }

    /**
     * Returns the version of Atlassian SAL that JIRA exports into OSGI-land.
     *
     * @return the version of Atlassian SAL that JIRA exports
     */
    @Override
    public String getSalVersion()
    {
        return buildProperties.getProperty("sal.version");
    }

    /**
     * Returns the version of AppLinks that JIRA ships with.
     *
     * @return the version of AppLinks that JIRA ships with
     */
    @Override
    public String getApplinksVersion()
    {
        return buildProperties.getProperty("applinks.version");
    }

    @Override
    public String getLuceneVersion()
    {
        return buildProperties.getProperty("lucene.version");
    }

    @Override
    public String getGuavaOsgiVersion()
    {
        return buildProperties.getProperty("guava.osgi.version");
    }

    @Override
    public String getBuildProperty(String key)
    {
        return buildProperties.getProperty(key);
    }

    @Override
    public boolean isBeta()
    {
        return parseVersion(getVersion()).second().toLowerCase().startsWith("-beta");
    }

    @Override
    public boolean isRc()
    {
        return parseVersion(getVersion()).second().toLowerCase().startsWith("-rc");
    }

    @Override
    public boolean isSnapshot()
    {
        return parseVersion(getVersion()).second().toUpperCase().startsWith("-SNAPSHOT");
    }

    @Override
    public boolean isMilestone()
    {
        return parseVersion(getVersion()).second().toLowerCase().startsWith("-m");
    }

    @Override
    public String toString()
    {
        return getBuildInformation();
    }

    /**
     * Loads the properties from the '{@value #BUILD_PROPERTIES_FILENAME}' file.
     *
     * @return a new Properties instance
     * @throws RuntimeException if there's a problem reading the file
     */
    private Properties loadProperties() throws RuntimeException
    {
        InputStream propsFile = BuildUtilsInfoImpl.class.getResourceAsStream("/" + BUILD_PROPERTIES_FILENAME);
        if (propsFile == null)
        {
            throw new IllegalStateException("File not found: " + BUILD_PROPERTIES_FILENAME);
        }

        Properties result = new Properties();
        try
        {
            result.load(propsFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                propsFile.close();
            }
            catch (IOException e)
            {
                logger.warn("Error closing {}", propsFile);
            }
        }

        return result;
    }

    /** package-scope for for testing */
    static Pair<int[], String> parseVersion(String version)
    {
        List<Integer> ints = new LinkedList<Integer>();
        Matcher m = Pattern.compile("([0-9]+)(\\.?)(.*)").matcher("");
        String rest = version;
        while (m.reset(rest).matches()) {
            String i = m.group(1);
            ints.add(Integer.parseInt(i));

            String dot = m.group(2);
            rest = m.group(3);
            if (!".".equals(dot)) {
                break; // no more numbers to be had
            }
        }

        while (ints.size() < 3) { // make sure there is always at least 3
            ints.add(0);
        }
        return Pair.of(Ints.toArray(ints), rest);
    }
}
