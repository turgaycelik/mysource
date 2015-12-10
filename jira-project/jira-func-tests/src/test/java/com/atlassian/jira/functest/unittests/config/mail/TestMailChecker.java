package com.atlassian.jira.functest.unittests.config.mail;

import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.mail.ConfigMailServer;
import com.atlassian.jira.functest.config.mail.MailChecker;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.service.ConfigService;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test {@link MailChecker}.
 *
 * @since v4.1
 */
public class TestMailChecker extends TestCase
{
    private final static String CHECK_MAIL_SERVER = "mailserver";
    private final static String CHECK_MAIL_SERVICE = "mailservice";

    public void testCheckMailServer() throws Exception
    {
        List<ConfigMailServer> servers = new ArrayList<ConfigMailServer>();
        servers.add(new ConfigMailServer().setId(10L));
        servers.add(new ConfigMailServer().setId(11L).setServerName("blarg2").setUserName("bbain").setType(ConfigMailServer.Type.SMTP));

        JiraConfig config = new JiraConfig();
        config.setMailServers(servers);

        final MailChecker checker = new MailChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        CheckResultBuilder builds = new CheckResultBuilder();
        builds.error("Mail server '10' to '<unknown type>:<unknown server>' for user '<anonymous>' exists.", CHECK_MAIL_SERVER);
        builds.error("Mail server '11' to 'SMTP:blarg2' for user 'bbain' exists.", CHECK_MAIL_SERVER);

        assertEquals(builds.buildResult(), result);
    }

    public void testCheckMailServerCheckDisabled() throws Exception
    {
        List<ConfigMailServer> servers = new ArrayList<ConfigMailServer>();
        servers.add(new ConfigMailServer().setId(10L));
        servers.add(new ConfigMailServer().setId(11L).setServerName("blarg2").setUserName("bbain").setType(ConfigMailServer.Type.SMTP));

        JiraConfig config = new JiraConfig();
        config.setMailServers(servers);

        final MailChecker checker = new MailChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_MAIL_SERVER));

        CheckResultBuilder builds = new CheckResultBuilder();
        assertEquals(builds.buildResult(), result);
    }

    public void testCheckMailServices() throws Exception
    {
        List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(new ConfigService(15L, 10L, "ssslsls.MailFetcherService", "PopService", new ConfigPropertySet()));
        services.add(new ConfigService(16L, 10L, "ssslsls.Name", "name", new ConfigPropertySet()));
        services.add(new ConfigService(17L, 10L, "ssslsls.MailFetcherService", "ImapService", new ConfigPropertySet()));
        services.add(new ConfigService(18L, 10L, "ssslsls.FileService", "FileService", new ConfigPropertySet()));

        JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setMailServers(Collections.<ConfigMailServer>emptyList());

        final MailChecker checker = new MailChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        CheckResultBuilder builds = new CheckResultBuilder();
        builds.error("Mail service 'PopService' exists.", CHECK_MAIL_SERVICE);
        builds.error("Mail service 'ImapService' exists.", CHECK_MAIL_SERVICE);
        builds.error("Mail service 'FileService' exists.", CHECK_MAIL_SERVICE);

        assertEquals(builds.buildResult(), result);
    }

    public void testCheckMailServicesCheckDisabled() throws Exception
    {
        List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(new ConfigService(15L, 10L, "ssslsls.MailFetcherService", "PopService", new ConfigPropertySet()));
        services.add(new ConfigService(16L, 10L, "ssslsls.Name", "name", new ConfigPropertySet()));
        services.add(new ConfigService(17L, 10L, "ssslsls.MailFetcherService", "ImapService", new ConfigPropertySet()));
        services.add(new ConfigService(18L, 10L, "ssslsls.FileService", "FileService", new ConfigPropertySet()));

        JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setMailServers(Collections.<ConfigMailServer>emptyList());

        final MailChecker checker = new MailChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_MAIL_SERVICE));

        CheckResultBuilder builds = new CheckResultBuilder();
        assertEquals(builds.buildResult(), result);
    }

    public void testFixMailServer() throws Exception
    {
        List<ConfigMailServer> servers = new ArrayList<ConfigMailServer>();
        servers.add(new ConfigMailServer().setId(10L));
        servers.add(new ConfigMailServer().setId(11L).setServerName("blarg2").setUserName("bbain").setType(ConfigMailServer.Type.SMTP));

        JiraConfig config = new JiraConfig();
        config.setMailServers(servers);

        final MailChecker checker = new MailChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(config.getMailServers().isEmpty());
    }

    public void testFixMailServerCheckDisabled() throws Exception
    {
        List<ConfigMailServer> servers = new ArrayList<ConfigMailServer>();
        servers.add(new ConfigMailServer().setId(10L));
        servers.add(new ConfigMailServer().setId(11L).setServerName("blarg2").setUserName("bbain").setType(ConfigMailServer.Type.SMTP));

        JiraConfig config = new JiraConfig();
        config.setMailServers(new ArrayList<ConfigMailServer>(servers));

        final MailChecker checker = new MailChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_MAIL_SERVER));

        assertEquals(servers, config.getMailServers());
    }

    public void testFixMailServices() throws Exception
    {
        final List<ConfigService> services = new ArrayList<ConfigService>();
        final ConfigService keptService = new ConfigService(16L, 10L, "ssslsls.Name", "name", new ConfigPropertySet());

        services.add(keptService);
        services.add(new ConfigService(15L, 10L, "ssslsls.MailFetcherService", "PopService", new ConfigPropertySet()));
        services.add(new ConfigService(17L, 10L, "ssslsls.MailFetcherService", "ImapService", new ConfigPropertySet()));
        services.add(new ConfigService(18L, 10L, "ssslsls.FileService", "FileService", new ConfigPropertySet()));

        JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setMailServers(Collections.<ConfigMailServer>emptyList());

        final MailChecker checker = new MailChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(Collections.singletonList(keptService), config.getServices());
    }

    public void testFixMailServicesCheckDisabled() throws Exception
    {
        List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(new ConfigService(15L, 10L, "ssslsls.MailFetcherService", "PopService", new ConfigPropertySet()));
        services.add(new ConfigService(16L, 10L, "ssslsls.Name", "name", new ConfigPropertySet()));
        services.add(new ConfigService(17L, 10L, "ssslsls.MailFetcherService", "ImapService", new ConfigPropertySet()));
        services.add(new ConfigService(18L, 10L, "ssslsls.FileService", "FileService", new ConfigPropertySet()));

        JiraConfig config = new JiraConfig();
        config.setServices(new ArrayList<ConfigService>(services));
        config.setMailServers(Collections.<ConfigMailServer>emptyList());

        final MailChecker checker = new MailChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_MAIL_SERVICE));

        assertEquals(services, config.getServices());
    }
}