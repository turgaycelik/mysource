package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.web.util.HelpUtil;
import org.apache.log4j.Logger;

/**
 * Checks for particular JVM Versions related to JRA-9198, JRA-15681
 *
 * @since v4.0
 */
public class JVMCheck implements SystemEnvironmentCheck
{
    private static final Logger logger = Logger.getLogger(SystemEnvironmentChecklist.class.getName());
    private final JiraProperties jiraSystemProperties;
    private JvmVersionUtil jvmVersionUtil;

    public JVMCheck(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
        jvmVersionUtil = new JvmVersionUtil();
    }

    public I18nMessage getWarningMessage()
    {
        final String jvmVendor = jiraSystemProperties.getProperty("java.vm.vendor");

        // Currently we only have checks for Sun JDK's
        if ("Sun Microsystems Inc.".equalsIgnoreCase(jvmVendor))
        {
            return checkSunJVMVersion();
        }

        return null;
    }

    private I18nMessage checkSunJVMVersion()
    {
        String javaVersion = jiraSystemProperties.getProperty("java.version");
        if (javaVersion != null && javaVersion.startsWith(JvmVersionUtil.JAVA_VERSION_6))
        {
            int jvmMinorVersion = jvmVersionUtil.getMinorVersion(javaVersion);

            if (jvmMinorVersion == -1)
            {
                logger.warn("Failed to determine JVM minor version. java.version='" + javaVersion + "'");
            }
            else if (jvmMinorVersion < 18)
            {
                I18nMessage warning = new I18nMessage("admin.warning.jvmversion6");
                warning.addParameter(javaVersion);
                final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("requirements");
                warning.setLink(helpPath.getUrl());
                return warning;
            }
        }
        return null;
    }
}
