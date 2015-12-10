package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;

import org.apache.log4j.Logger;

/**
 * Check that the JVM version is above 1.5.0-18_b02 because of the
 * JVM bug {@link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6754146}
 *
 * @since v4.0
 */
public class JRA18659Check implements SystemEnvironmentCheck
{
    private static final Logger logger = Logger.getLogger(SystemEnvironmentChecklist.class.getName());
    private final JvmVersionUtil jvmVersionUtil;
    private final JiraProperties jiraSystemProperties;

    public JRA18659Check(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
        jvmVersionUtil = new JvmVersionUtil();
    }

    public I18nMessage getWarningMessage()
    {
        String jvmVersion = jiraSystemProperties.getProperty("java.vm.version");
        if (!checkJRA_18659(jvmVersion))
        {
            final I18nMessage message = new I18nMessage("admin.warning.jra_18659");
            message.addParameter(jvmVersion);
            return message;
        }
        return null;
    }

    private boolean checkJRA_18659(String jvmVersion)
    {
        if (jvmVersion.startsWith(JvmVersionUtil.JAVA_VERSION_5))
        {
            final int minorVersion = jvmVersionUtil.getMinorVersion(jvmVersion);
            final int buildNumber = jvmVersionUtil.getBuildNumber(jvmVersion);
            if (minorVersion == -1)
            {
                logger.warn("Failed to determine JVM minor version. java.version='" + jvmVersion + "'");
            }
            else if (minorVersion < 18)
            {
                return false;
            }
            else if (minorVersion == 18)
            {
                if (buildNumber == -1)
                {
                    logger.warn("Failed to determine JVM minor version. java.version='" + jvmVersion + "'");
                }
                else if (buildNumber < 3)
                {
                    return false;
                }
            }
        }
        return true;
    }
}
