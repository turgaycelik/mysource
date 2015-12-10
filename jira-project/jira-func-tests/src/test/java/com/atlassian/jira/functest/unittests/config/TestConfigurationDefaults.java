package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.AttachmentDirectoryChecker;
import com.atlassian.jira.functest.config.BackupChecker;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.ConfigurationDefaults;
import com.atlassian.jira.functest.config.IndexDirectoryChecker;
import com.atlassian.jira.functest.config.ServiceChecker;
import com.atlassian.jira.functest.config.crowd.CrowdApplicationCheck;
import com.atlassian.jira.functest.config.dashboard.DashboardConfigurationCheck;
import com.atlassian.jira.functest.config.mail.MailChecker;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test {@link com.atlassian.jira.functest.config.ConfigurationDefaults}.
 *
 * @since v4.1
 */
public class TestConfigurationDefaults extends TestCase
{
    public void testCreateDefaultConfigurationChecks() throws Exception
    {
        Collection<Class<? extends ConfigurationCheck>> list = new ArrayList<Class<? extends ConfigurationCheck>>();
        for (ConfigurationCheck check : ConfigurationDefaults.createDefaultConfigurationChecks())
        {
            list.add(check.getClass());
        }
        assertTrue(list.containsAll(Arrays.asList(MailChecker.class, BackupChecker.class, IndexDirectoryChecker.class,
                AttachmentDirectoryChecker.class, DashboardConfigurationCheck.class, ServiceChecker.class,
                CrowdApplicationCheck.class)));
    }
}