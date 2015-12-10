package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.crowd.acceptance.tests.rest.service.UsersResourceTest;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Acceptance tests for the Crowd users resource.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestCrowdUsersResource extends UsersResourceTest implements EnvironmentAware
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name the test name
     */
    public TestCrowdUsersResource(String name)
    {
        super(name, new CrowdEmbeddedServer().usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        setRestServer(new CrowdEmbeddedServer(environmentData).usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void testGetUserWithAttributes()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetNestedGroups()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetDirectGroups()
    {
        // DISABLED because the functionality is not supported in JIRA
    }
}
