package com.atlassian.jira.webtests.ztests.crowd.embedded;

import com.atlassian.crowd.acceptance.tests.rest.service.GroupsResourceTest;
import com.atlassian.crowd.integration.rest.entity.MembershipsEntity;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.client.WebResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tests the Crowd REST API for the running in JIRA.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestCrowdGroupsResource extends GroupsResourceTest implements EnvironmentAware
{
    public TestCrowdGroupsResource(String name)
    {
        super(name, new CrowdEmbeddedServer().usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        setRestServer(new CrowdEmbeddedServer(environmentData).usingXmlBackup(CrowdEmbeddedServer.XML_BACKUP));
    }

    @Override
    public void testStoreGroupAttributes()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testDeleteGroupAttribute()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testGetGroup_Expanded()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    public void testAddDirectChildGroup_NoNestedGroups()
    {
        // DISABLED because the functionality is not supported in JIRA
    }

    @Override
    /**
     * Copy and paste from superclass because we still have the 3 standard JIRA groups and the assertion compares all.
     */
    public void testGetMembershipsReturnsExactlyExpectedMemberships()
    {
        WebResource webResource = getRootWebResource("crowd", APPLICATION_PASSWORD).
                path(GROUPS_RESOURCE).path("membership");

        MembershipsEntity mems = webResource.get(MembershipsEntity.class);

        assertNotNull(mems.getList());

        Map<String, Set<String>> users = new HashMap<String, Set<String>>();
        Map<String, Set<String>> childGroups = new HashMap<String, Set<String>>();

        for (Membership e : mems.getList())
        {
            users.put(e.getGroupName(), e.getUserNames());
            childGroups.put(e.getGroupName(), e.getChildGroupNames());
        }

        Set<String> E = Collections.emptySet();

        Map<String, Set<String>> expectedUsers = new HashMap<String, Set<String>>();
        expectedUsers.put("animals", E);
        expectedUsers.put("badgers", ImmutableSet.of("admin", "eeeep"));
        expectedUsers.put("birds", E);
        expectedUsers.put("cats", E);
        expectedUsers.put("crowd-administrators", ImmutableSet.of("admin", "secondadmin"));
        expectedUsers.put("crowd-testers", ImmutableSet.of("penny"));
        expectedUsers.put("crowd-users", E);
        expectedUsers.put("dogs", E);
        // The jira groups
        expectedUsers.put("jira-users", ImmutableSet.<String>of("admin", "regularuser"));
        expectedUsers.put("jira-developers", ImmutableSet.of("admin"));
        expectedUsers.put("jira-administrators", ImmutableSet.of("admin"));

        Map<String, Set<String>> expectedChildGroups = new HashMap<String, Set<String>>();
        expectedChildGroups.put("animals", E);
        expectedChildGroups.put("badgers", E);
        expectedChildGroups.put("birds", E);
        expectedChildGroups.put("cats", E);
        expectedChildGroups.put("crowd-administrators", ImmutableSet.of("crowd-testers"));
        expectedChildGroups.put("crowd-testers", ImmutableSet.of("badgers"));
        expectedChildGroups.put("crowd-users", ImmutableSet.of("badgers"));
        expectedChildGroups.put("dogs", E);
        expectedChildGroups.put("jira-users", E);
        expectedChildGroups.put("jira-developers", E);
        expectedChildGroups.put("jira-administrators", E);

        assertEquals(expectedUsers, users);
        assertEquals(expectedChildGroups, childGroups);
    }
}
