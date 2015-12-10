package com.atlassian.jira.util.system.check;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.web.util.HelpUtil;
import org.apache.log4j.Logger;

/**
 * This is a simple check to ensure that the 'mail.mime.decodeparameters' is set during starup.
 *
 * @since v4.0
 */
public class JRA12525Check implements SystemEnvironmentCheck
{

    public I18nMessage getWarningMessage()
    {
        try
        {
            if (!JiraSystemProperties.isDecodeMailParameters())
            {
                HelpUtil helpUtil = new HelpUtil();
                I18nMessage warning = new I18nMessage("admin.warning.mail.decorateparameter");
                warning.addParameter(SystemPropertyKeys.MAIL_DECODE_PARAMETERS);
                warning.setLink(helpUtil.getHelpPath("decodeparameters").getUrl());
                return warning;
            }
        }
        catch (Exception e)
        {
            final Logger log = Logger.getLogger(JRA12525Check.class);
            log.error("Error occured while checking e-mail properties: " + e.getMessage() + ". JIRA will continue starting.", e);
        }
        return null;
    }
}
