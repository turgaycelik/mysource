package com.atlassian.jira.functest.unittests.config.crowd;

import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplication;
import com.atlassian.jira.functest.config.crowd.CrowdApplicationCheck;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since v4.3
 */
public class TestCrowdApplicationCheck
{
    private static final String CHECK_APPLICATION_TYPE = "crowdapplicationtype";

    @Test
    public void testCheckWithGoodApplications()
    {
        CheckResultBuilder expectedBuilder = new CheckResultBuilder();
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L).setApplicationType("jack");
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(expectedBuilder.buildResult(), actualResult);
    }

    @Test
    public void testCheckWithBadApplications() throws Exception
    {
        CheckResultBuilder expectedBuilder = new CheckResultBuilder();
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(3L).setName("Name");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2, application3));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.allOptions());

        expectedBuilder.error("Crowd application '<unknown>' exists without type.", CHECK_APPLICATION_TYPE);
        expectedBuilder.error("Crowd application 'Name' exists without type.", CHECK_APPLICATION_TYPE);

        Assert.assertEquals(expectedBuilder.buildResult(), actualResult);
    }

    @Test
    public void testCheckWithBadApplicationsButDisabled() throws Exception
    {
        CheckResultBuilder expectedBuilder = new CheckResultBuilder();
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(3L).setName("Name");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2, application3));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        ConfigurationCheck.Result actualResult = check.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_APPLICATION_TYPE));
        
        Assert.assertEquals(expectedBuilder.buildResult(), actualResult);
    }

    @Test
    public void testFixWithGoodApplications()
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L).setApplicationType("jack");
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.allOptions());

        Assert.assertEquals(Arrays.asList(application1, application2), config.getCrowdApplications());
    }

    @Test
    public void testFixWithBadApplications() throws Exception
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(3L).setName("Name");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2, application3));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.allOptions());

        ConfigCrowdApplication expectedApplication1 = new ConfigCrowdApplication().setId(1L).setApplicationType("CROWD");
        ConfigCrowdApplication expectedApplication2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");
        ConfigCrowdApplication expectedApplication3 = new ConfigCrowdApplication().setId(3L).setName("Name").setApplicationType("CROWD");

        Assert.assertEquals(Arrays.asList(expectedApplication1, expectedApplication2, expectedApplication3), config.getCrowdApplications());
    }

    @Test
    public void testFixWithBadApplicationsButDisabled() throws Exception
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(1L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(2L).setApplicationType("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(3L).setName("Name");

        JiraConfig config = new JiraConfig();
        config.setCrowdApplications(Arrays.asList(application1, application2, application3));

        CrowdApplicationCheck check = new CrowdApplicationCheck();
        check.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_APPLICATION_TYPE));

        Assert.assertEquals(Arrays.asList(application1, application2, application3), config.getCrowdApplications());
    }

}
