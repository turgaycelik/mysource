/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.system.VersionNumber;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;

import javax.servlet.ServletContext;

/**
 * http://jira.atlassian.com/browse/JRA-10145
 * <p/>
 * OutOfMemoryError in Tomcat if system parameter not set
 *
 * @since v3.11.1
 */
class JRA10145Check implements SystemEnvironmentCheck
{
    // this is static as all Strings constants effectively are anyway
    private static final String LIMIT_BUFFER = "org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER";

    final VersionNumber v5 = new VersionNumber("5.0");
    final VersionNumber v5_5 = new VersionNumber("5.5");
    final VersionNumber v5_5_15 = new VersionNumber("5.5.15");
    final VersionNumber v6 = new VersionNumber("6.0");

    private final JiraProperties jiraSystemProperties;

    public JRA10145Check(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    public I18nMessage getWarningMessage()
    {
        final ServletContext context = ServletContextProvider.getServletContext();
        String serverInfo = context.getServerInfo();
        if (serverInfo.indexOf("Tomcat") >= 0)
        {
            VersionNumber versionNumber = new VersionNumber(serverInfo.substring(serverInfo.indexOf("/") + 1));
            if (versionNumber.isLessThan(v5))
            {
                return null;
            }
            if (versionNumber.isLessThan(v5_5_15))
            {
                I18nMessage warning = new I18nMessage("admin.warning.tomcat.version");
                warning.addParameter(serverInfo);
                warning.addParameter(LIMIT_BUFFER);
                warning.setLink(new VersionLink().getMoreInfo(versionNumber));
                return warning;
            }
            else if (!jiraSystemProperties.getBoolean(LIMIT_BUFFER))
            {
                I18nMessage warning = new I18nMessage("admin.warning.tomcat.limitbuffer");
                warning.addParameter(serverInfo);
                warning.addParameter(LIMIT_BUFFER);
                warning.setLink(new VersionLink().getMoreInfo(versionNumber));
                return warning;
            }
        }
        return null;
    }

    private final class VersionLink
    {
        private final String MORE_INFO = ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.tomcat");

        String getMoreInfo(VersionNumber version)
        {
            if (version.isLessThan(v5))
            {
                throw new IllegalStateException("We do not have information on this problem for version 4, only version 5 and above");
            }
            if (version.isLessThan(v5_5))
            {
                return MORE_INFO + "tomcat50.html";
            }
            if (version.isLessThan(v6))
            {
                return MORE_INFO + "tomcat55.html";
            }
            return MORE_INFO + "tomcat60.html";
        }
    }
}