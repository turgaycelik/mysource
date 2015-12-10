package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.GroupAndUserPickerClient;
import com.atlassian.jira.testkit.client.restclient.GroupAndUserSuggestions;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestGroupAndUserPickerResource extends RestFuncTest
{
    private GroupAndUserPickerClient groupAndUserPickerClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        groupAndUserPickerClient = new GroupAndUserPickerClient(getEnvironmentData());
    }

    @Override
    protected void tearDownTest()
    {
        super.tearDownTest();
        groupAndUserPickerClient = null;
    }


    public void testMatches()
    {
        administration.restoreData("TestGroupPickerResource.xml");
        GroupAndUserSuggestions suggestions = groupAndUserPickerClient.get("z");
        assertEquals(20, suggestions.groups.groups.size());
        assertEquals("Showing 20 of 21 matching groups", suggestions.groups.header);
        assertEquals(0, suggestions.users.users.size());
        assertEquals("Showing 0 of 0 matching users", suggestions.users.header);
        suggestions = groupAndUserPickerClient.get("a");
        assertEquals(3, suggestions.groups.groups.size());
        assertEquals("Showing 3 of 3 matching groups", suggestions.groups.header);
        assertEquals(1, suggestions.users.users.size());
        assertEquals("Showing 1 of 1 matching users", suggestions.users.header);
        assertEquals("admin", suggestions.users.users.get(0).name);
        assertEquals("admin", suggestions.users.users.get(0).key);
    }
}
