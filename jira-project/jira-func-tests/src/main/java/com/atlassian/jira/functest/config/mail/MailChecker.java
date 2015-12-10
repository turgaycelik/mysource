package com.atlassian.jira.functest.config.mail;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.service.ConfigService;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Check to make sure that there are no mail services configured.
 *
 * @since v4.0
 */
public class MailChecker implements ConfigurationCheck
{
    /**
     * Check to make sure there are no mail servers.
     */
    public final static String CHECK_MAIL_SERVER = "mailserver";

    /**
     * Check to make sure there are no mail services.
     */
    public final static String CHECK_MAIL_SERVICE = "mailservice";

    /**
     * JIRA's mail services.
     */
    public final static List<String> SERVICES = Arrays.asList("MailFetcherService", "FileService");

    public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
    {
        CheckResultBuilder resultBuilder = new CheckResultBuilder();
        //<MailServer id="10000" name="SMTP" description="" from="me@example.com" prefix="{jira}" smtpPort="25" type="smtp" servername="smtp@example.com"/>
        if (options.checkEnabled(CHECK_MAIL_SERVER))
        {
            for (ConfigMailServer mailServer : config.getMailServers())
            {
                String name = mailServer.getName() == null ? mailServer.getId().toString() : mailServer.getName();
                String type = mailServer.getType() == null ? "<unknown type>" : mailServer.getType().name();
                String serverName = mailServer.getServerName() == null ? "<unknown server>" : mailServer.getServerName();
                String username = mailServer.getUserName() == null ? "<anonymous>" : mailServer.getUserName();

                resultBuilder.error(String.format("Mail server '%s' to '%s:%s' for user '%s' exists.",
                        name, type, serverName, username), CHECK_MAIL_SERVER);
            }
        }

        if (options.checkEnabled(CHECK_MAIL_SERVICE))
        {
            final List<ConfigService> configServiceList = config.getServices();
            for (ConfigService service : configServiceList)
            {
                for (String clazzName : SERVICES)
                {
                    if (service.getClazz().contains(clazzName))
                    {
                        String name = service.getName();
                        if (StringUtils.isBlank(name))
                        {
                            name = service.getId().toString();
                        }
                        resultBuilder.error(String.format("Mail service '%s' exists.", name), CHECK_MAIL_SERVICE);
                    }
                }
            }
        }

        return resultBuilder.buildResult();
    }

    public void fixConfiguration(final JiraConfig config, final CheckOptions options)
    {
        if (options.checkEnabled(CHECK_MAIL_SERVER))
        {
            config.getMailServers().clear();
        }

        if (options.checkEnabled(CHECK_MAIL_SERVICE))
        {
            final List<ConfigService> configServiceList = config.getServices();
            for (Iterator<ConfigService> serviceIterator = configServiceList.iterator(); serviceIterator.hasNext();)
            {
                ConfigService service = serviceIterator.next();
                for (String clazzName : SERVICES)
                {
                    if (service.getClazz().contains(clazzName))
                    {
                        serviceIterator.remove();
                    }
                }
            }
        }
    }
}
