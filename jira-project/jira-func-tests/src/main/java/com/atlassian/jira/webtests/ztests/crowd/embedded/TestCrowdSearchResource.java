package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.crowd.acceptance.tests.rest.service.SearchResourceTest;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Acceptance tests for the Crowd search resource.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestCrowdSearchResource extends SearchResourceTest implements EnvironmentAware
{
    public TestCrowdSearchResource(String name)
    {
        super(name, new CrowdEmbeddedServer().usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        setRestServer(new CrowdEmbeddedServer(environmentData).usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void testGetUserNames_Aliases()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetUserNames_AliasesIgnoreUsernameWhenAliasExists()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetUserNames_AliasesAll()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetGroupNames()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetGroupNames_StartIndex()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetGroupNames_ActiveRestriction()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetGroups()
    {
        // DISABLED because the functionality is not supported in JIRA
    }
}
