/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.util.system.check;

import com.atlassian.jira.util.system.VersionNumber;
import com.atlassian.jira.web.ServletContextProvider;

import javax.servlet.ServletContext;

/**
 * http://jira.atlassian.com/browse/JRA-20617
 * <p/>
 * JVM deadlock if run on Tomcat 6.0.24
 *
 * @since v4.1
 */
class JRA20617Check implements SystemEnvironmentCheck
{
    final VersionNumber v6024 = new VersionNumber("6.0.24");

    public I18nMessage getWarningMessage()
    {
        final ServletContext context = ServletContextProvider.getServletContext();
        String serverInfo = context.getServerInfo();
        if (serverInfo.indexOf("Tomcat") >= 0)
        {
            VersionNumber versionNumber = new VersionNumber(serverInfo.substring(serverInfo.indexOf("/") + 1));
            if (versionNumber.equals(v6024))
            {
                I18nMessage warning = new I18nMessage("admin.warning.tomcat.6024.version");
                warning.addParameter(serverInfo);
                return warning;
            }
        }
        return null;
    }


}