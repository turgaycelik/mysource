package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.BackupChecker;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.ServiceChecker;
import com.atlassian.jira.functest.config.mail.MailChecker;
import com.atlassian.jira.functest.config.service.ConfigService;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.ServiceChecker}.
 *
 * @since v4.3
 */
public class TestServiceChecker
{
    private static final String CHECK_SERVICE = "service";
    private static final List<String> IGNORED_SERVICES;
    private static final String BAD_SERVICE_CLAZZ = "clazz";
    private static final String RANDOM_NAME = "Random Name";

    static
    {
        IGNORED_SERVICES = ImmutableList.<String>builder()
                .addAll(MailChecker.SERVICES)
                .add(BackupChecker.BACKUP_SERVICE)
                .add("MailQueueService")
                .add("JiraPluginSchedulerService").build();
    }

    @Test
    public void testCheckNoServicesConfig() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        JiraConfig config = new JiraConfig();

        config.setServices(new ArrayList<ConfigService>());

        final ServiceChecker checker = new ServiceChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(builder.buildResult(), result);
    }

    @Test
    public void testCheckIgnoredServices()
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        JiraConfig config = new JiraConfig();
        config.setServices(createIgnoredServices());

        final ServiceChecker checker = new ServiceChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(builder.buildResult(), result);
    }

    @Test
    public void testCheckWithBadService() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();
        builder.error("Service 'Random Name' exits.", CHECK_SERVICE);

        final JiraConfig config = new JiraConfig();
        final ConfigService service = new ConfigService().setClazz("class").setName(RANDOM_NAME);

        config.setServices(CollectionBuilder.newBuilder(service).asArrayList());

        final ServiceChecker checker = new ServiceChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(builder.buildResult(), result);
    }

    @Test
    public void testCheckWithChecksDisabled() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        final JiraConfig config = new JiraConfig();
        final ConfigService service = new ConfigService().setClazz(BAD_SERVICE_CLAZZ).setName(RANDOM_NAME);

        config.setServices(CollectionBuilder.newBuilder(service).asArrayList());

        final ServiceChecker checker = new ServiceChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_SERVICE));

        Assert.assertEquals(builder.buildResult(), result);
    }

    @Test
    public void testFixRemoveService() throws Exception
    {
        final JiraConfig config = new JiraConfig();
        final ConfigService service = new ConfigService().setClazz(BAD_SERVICE_CLAZZ).setName(RANDOM_NAME);
        final ConfigService service2 = new ConfigService().setClazz(BackupChecker.BACKUP_SERVICE).setName(RANDOM_NAME);

        config.setServices(CollectionBuilder.newBuilder(service, service2).asArrayList());

        final ServiceChecker checker = new ServiceChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(Collections.singletonList(service2), config.getServices());
    }

    @Test
    public void testFixNoop() throws Exception
    {
        final JiraConfig config = new JiraConfig();

        config.setServices(createIgnoredServices());

        final ServiceChecker checker = new ServiceChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(createIgnoredServices(), config.getServices());
    }

    @Test
    public void testFixDisabled() throws Exception
    {
        final JiraConfig config = new JiraConfig();
        final ConfigService service = new ConfigService().setClazz(BAD_SERVICE_CLAZZ).setName(RANDOM_NAME);

        config.setServices(CollectionBuilder.newBuilder(service).asArrayList());

        final ServiceChecker checker = new ServiceChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_SERVICE));

        ConfigService expectedService = new ConfigService().setClazz(BAD_SERVICE_CLAZZ).setName(RANDOM_NAME);

        Assert.assertEquals(Collections.singletonList(expectedService), config.getServices());
    }

    private static List<ConfigService> createIgnoredServices()
    {
        CollectionBuilder<ConfigService> badServices = CollectionBuilder.newBuilder();
        for (String ignoredService : IGNORED_SERVICES)
        {
            badServices.add(new ConfigService().setClazz(ignoredService).setName("Name: " + ignoredService));
        }
        return badServices.asMutableList();
    }
}
