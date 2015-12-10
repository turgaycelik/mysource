package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.crowd.acceptance.tests.rest.service.AuthenticationResourceTest;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Tests the Crowd REST API for the running in JIRA.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestCrowdAuthenticationResource extends AuthenticationResourceTest implements EnvironmentAware
{
    public TestCrowdAuthenticationResource(String name)
    {
        super(name, new CrowdEmbeddedServer().usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        setRestServer(new CrowdEmbeddedServer(environmentData).usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void testUserAuthentication_CommonUserUnauthorised()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testUserAuthentication_UnauthorisedGroupUser()
    {
        // DISABLED because the functionality is not supported in JIRA
    }
}
