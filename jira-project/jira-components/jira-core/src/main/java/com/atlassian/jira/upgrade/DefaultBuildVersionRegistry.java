package com.atlassian.jira.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation which looks up the known versions from the file build-versions.properties.
 *
 * @since v4.1
 */
@InjectableComponent
public class DefaultBuildVersionRegistry implements BuildVersionRegistry
{
    private static final Logger log = Logger.getLogger(DefaultBuildVersionRegistry.class);
    private static final String BUILD_VERSIONS_PROPERTIES = "build-versions.properties";

    private final Map<String, String> buildVersionProperties;
    private final BuildUtilsInfo buildUtilsInfo;

    public DefaultBuildVersionRegistry(final BuildUtilsInfo buildUtilsInfo)
    {
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.buildVersionProperties = new TreeMap<String, String>(new BuildNumComparator());
        loadBuildVersions();
    }

    public BuildVersion getVersionForBuildNumber(final String targetBuildNumber)
    {
        Assertions.notBlank("targetBuildNumber", targetBuildNumber);
        final Integer targetBuildInt;
        try
        {
            targetBuildInt = Integer.valueOf(targetBuildNumber);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException(e);
        }

        // see if input build number is mapped exactly to a version
        if (buildVersionProperties.containsKey(targetBuildNumber))
        {
            final String version = buildVersionProperties.get(targetBuildNumber);
            return new BuildVersionImpl(targetBuildNumber, version);
        }

        // if not found immediately, then we must look for the first key which is greater than or equal to the input and return its version
        for (String buildNumberKeyString : buildVersionProperties.keySet())
        {
            final Integer buildNumberKey = Integer.valueOf(buildNumberKeyString);
            if (buildNumberKey >= targetBuildInt)
            {
                final String version = buildVersionProperties.get(buildNumberKeyString);
                return new BuildVersionImpl(buildNumberKeyString, version);
            }
        }

        // input build number is entirely unknown; assume that it is the latest version of JIRA.
        return new BuildVersionImpl(buildUtilsInfo.getCurrentBuildNumber(), buildUtilsInfo.getVersion());
    }

    @Override
    public BuildVersion getBuildNumberForVersion(@Nonnull String targetVersion)
    {
        for (Map.Entry<String, String> entry : buildVersionProperties.entrySet())
        {
            if (entry.getValue().equals(targetVersion))
            {
                return new BuildVersionImpl(entry.getKey(), entry.getValue());
            }
        }
        return new BuildVersionImpl(""+ Integer.MAX_VALUE, targetVersion);
    }

    private void loadBuildVersions()
    {
        final Properties props = new Properties();
        final InputStream in = ClassLoaderUtils.getResourceAsStream(BUILD_VERSIONS_PROPERTIES, DefaultBuildVersionRegistry.class);
        try
        {
            props.load(in);
            in.close();

            // cant rely on the ordering of the keys from the properties file - must sort them ourselves using TreeMap
            for (Object o : props.keySet())
            {
                final String key = (String) o;
                buildVersionProperties.put(key, props.getProperty(key));
            }
        }
        catch (IOException e)
        {
            log.error("Unable to load build versions properties from '" + BUILD_VERSIONS_PROPERTIES + "'.");
        }
    }
}

