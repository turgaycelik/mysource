package com.atlassian.jira.util.system.check;

/**
 * A utility class for retreiving the various parts of a Java version
 *
 * @since v4.0
 */
public class JvmVersionUtil
{
    public final static String JAVA_VERSION_6 = "1.6.0";
    public static final String JAVA_VERSION_5 = "1.5.0";

    /**
     * @param javaVersion the full Java version string
     * @return the Major version of the Java string, e.g. "1.6.0_10_b3" will return 6 or -1 if the version can not be read
     */
    public int getMajorVersion(final String javaVersion)
    {
        final String[] parts = javaVersion.split("\\.");
        return parts.length >= 2 ? parse(parts[1]) : -1;
    }

    /**
     * @param javaVersion the full Java version string
     * @return the Minor version of the Java string, e.g. "1.6.0_10_b3" will return 10 or -1 if the version can not be read
     */
    public int getMinorVersion(final String javaVersion)
    {
        final int jvmMinorVersion;
        if (javaVersion.indexOf("_") == -1)
        {
            jvmMinorVersion = 0;
        }
        else
        {
            //if the java version is > 6.0 the version stamp will contain a "_" for the minor version (e.g. 1.6.0_10)
            //thus to determine the minor version we have to add 1 to the length of the JAVA_VERSION_6 string.
            final String[] parts = javaVersion.split("[-_]");
            jvmMinorVersion = parts.length >= 2 ? parse(parts[1]) : -1;
        }
        return jvmMinorVersion;
    }

    /**
     * @param javaVersion the full Java version string
     * @return the build number of the Java string, e.g. "1.6.0_10_b3" will return 3 or -1 if the version can not be read
     */
    public int getBuildNumber(final String javaVersion)
    {
        final int build;
        if (javaVersion.indexOf("-") == -1)
        {
            build = 0;
        }
        else
        {
            //if the java version is > 6.0 the version stamp will contain a "_" for the minor version (e.g. 1.6.0_10)
            //thus to determine the minor version we have to add 1 to the length of the JAVA_VERSION_6 string.
            final String[] parts = javaVersion.split("[-_]");
            build = parts.length >= 3 ? parse(parts[2].substring(1)) : -1;
        }
        return build;
    }

    private int parse(final String str)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }
}
