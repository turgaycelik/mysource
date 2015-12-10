package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.GroupPickerClient;
import com.atlassian.jira.testkit.client.restclient.GroupSuggestion;
import com.atlassian.jira.testkit.client.restclient.GroupSuggestions;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Func test for GroupPickerResource
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestGroupPickerResource extends RestFuncTest
{
    private GroupPickerClient groupPickerClient;


    public void testGroupSuggestionsOrder()
    {
        administration.restoreBlankInstance();
        administration.usersAndGroups().addGroup("jara-users");
        administration.usersAndGroups().addGroup("zara-users");

        List<GroupSuggestion> expectedSuggestions = Lists.newArrayList(
                new GroupSuggestion().name("jara-users").html("jara-<b>users</b>"),
                new GroupSuggestion().name("jira-users").html("jira-<b>users</b>"),
                new GroupSuggestion().name("zara-users").html("zara-<b>users</b>")
        );

        GroupSuggestions suggestions = groupPickerClient.get("users");

        assertEquals("Showing 3 of 3 matching groups", suggestions.header);
        assertEquals(expectedSuggestions, suggestions.groups);
    }

    public void testGroupSuggestionsAreHtmlEncoded()
    {
        administration.restoreBlankInstance();
        administration.usersAndGroups().addGroup("<script>alert('wtf')</script>");

        List<GroupSuggestion> expectedSuggestions = Lists.newArrayList(
                new GroupSuggestion().name("<script>alert('wtf')</script>").html("&lt;script&gt;alert(&#39;<b>wtf</b>&#39;)&lt;/script&gt;")
        );

        GroupSuggestions suggestions = groupPickerClient.get("wtf");

        assertEquals("Showing 1 of 1 matching groups", suggestions.header);
        assertEquals(expectedSuggestions, suggestions.groups);
    }

    public void testGroupSuggestionsNoQueryString()
    {
        administration.restoreBlankInstance();

        List<GroupSuggestion> expectedSuggestions = Lists.newArrayList(
                new GroupSuggestion().name("jira-administrators").html("jira-administrators"),
                new GroupSuggestion().name("jira-developers").html("jira-developers"),
                new GroupSuggestion().name("jira-users").html("jira-users")
        );

        GroupSuggestions suggestions = groupPickerClient.get(null);

        assertEquals("Showing 3 of 3 matching groups", suggestions.header);
        assertEquals(expectedSuggestions, suggestions.groups);
    }

    public void testFindGroupsWithNoMatch()
    {
        administration.restoreBlankInstance();
        GroupSuggestions suggestions = groupPickerClient.get("lalala");

        assertEquals("Showing 0 of 0 matching groups", suggestions.header);
        assertEquals(Collections.<GroupSuggestion>emptyList(), suggestions.groups);
    }

    public void testFindGroupsExcessResults()
    {
        administration.restoreData("TestGroupPickerResource.xml");
        // What I would give for some way to change the application properties
        GroupSuggestions suggestions = groupPickerClient.get("z");

        List<GroupSuggestion> expectedSuggestions = Lists.newArrayList();
        for(int i = 0; i < 20; ++i)
        {
            String expectedName = "z" + String.format("%02d", i);
            String expectedHtml = "<b>z</b>" + String.format("%02d", i);
            expectedSuggestions.add(new GroupSuggestion().name(expectedName).html(expectedHtml));
        }

        assertEquals("Showing 20 of 21 matching groups", suggestions.header);
        assertEquals(expectedSuggestions, suggestions.groups);

    }


    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        groupPickerClient = new GroupPickerClient(getEnvironmentData());
    }

    @Override
    protected void tearDownTest()
    {
        super.tearDownTest();
        groupPickerClient = null;
    }
}
