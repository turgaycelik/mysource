package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.LinkGroup;
import com.atlassian.jira.testkit.client.restclient.SimpleLink;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceOpsbar extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    public void testLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");

        final Issue issue = issueClient.get("HSP-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        final List<LinkGroup> opsBarGroups = opsbarGroup.getGroups();
        assertEquals(5, opsBarGroups.size());
        assertGroupContainsLinkIds(opsBarGroups.get(0), "edit-issue");
        assertGroupContainsLinkIds(opsBarGroups.get(1), "comment-issue");
        assertGroupContainsLinkIds(opsBarGroups.get(2), "assign-issue");
        //On Bamboo the reference plugin adds some issue operations and workflow transitions to the opsbar
        final LinkGroup transitions = opsBarGroups.get(3);
        if(isReferencePluginEnabled())
        {
            assertGroupContainsLinkIds(transitions, "reference-transition-item", "action_id_4");
        }
        else
        {
            assertGroupContainsLinkIds(transitions, "action_id_4", "action_id_5");
        }

        final List<LinkGroup> workflowGroups = transitions.getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertEquals(1, workflowGroups.get(0).getGroups().size());
        //On Bamboo the reference plugin adds some issue operations and workflow transitions to the opsbar
        if(isReferencePluginEnabled())
        {
            assertGroupContainsLinkIds(workflowGroups.get(0).getGroups().get(0), "action_id_5", "action_id_2");
        }
        else
        {
            assertGroupContainsLinkIds(workflowGroups.get(0).getGroups().get(0), "action_id_2");
        }

        assertAdminMenu(opsBarGroups.get(4));
        assertToolsGroup(linkGroups.get(1));
    }

    public void testCanEditWhenNotLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");
        navigation.logout();

        final Issue issue = issueClient.anonymous().get("ANONED-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        assertEquals(4, opsbarGroup.getGroups().size());
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(0), "edit-issue");
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(1), "edit-labels");
        assertNoLinks(opsbarGroup.getGroups().get(2));
        //Nothing in the admin group.
        assertNoLinks(opsbarGroup.getGroups().get(3));

        final List<LinkGroup> workflowGroups = opsbarGroup.getGroups().get(2).getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertNoGroups(workflowGroups.get(0));

        assertToolsGroup(linkGroups.get(1));
    }

    public void testCannotEditWhenNotLoggedIn() throws Exception
    {
        administration.restoreData("TestOpsBar.xml");
        navigation.logout();

        final Issue issue = issueClient.anonymous().get("ANON-1", Issue.Expand.operations);

        final List<LinkGroup> linkGroups = issue.operations.getLinkGroups();
        assertEquals(2, linkGroups.size());

        final LinkGroup opsbarGroup = linkGroups.get(0);
        assertEquals("view.issue.opsbar", opsbarGroup.getId());
        assertNoLinks(opsbarGroup);

        assertEquals(4, opsbarGroup.getGroups().size());
        assertGroupContainsLinkIds(opsbarGroup.getGroups().get(0), "ops-login-lnk");
        assertNoLinks(opsbarGroup.getGroups().get(1));
        assertNoLinks(opsbarGroup.getGroups().get(2));
        assertNoLinks(opsbarGroup.getGroups().get(3));

        final List<LinkGroup> workflowGroups = opsbarGroup.getGroups().get(2).getGroups();
        assertEquals(1, workflowGroups.size());
        assertEquals("opsbar-transitions_more", workflowGroups.get(0).getHeader().id);
        assertNoGroups(workflowGroups.get(0));

        assertToolsGroup(linkGroups.get(1));
    }

    private static void assertAdminMenu(final LinkGroup adminGroups)
    {
        //No top level links in admin group.
        assertNoLinks(adminGroups);
        assertEquals(1, adminGroups.getGroups().size());

        //This is where all the admin links are.
        final LinkGroup realAdminGroup = adminGroups.getGroups().get(0);
        assertGroupContainsLinkIds(realAdminGroup.getGroups().get(0), "com.atlassian.jira.jira-project-config-plugin:add-custom-field");
    }

    private boolean isReferencePluginEnabled()
    {
        return administration.plugins().referencePlugin().isInstalled() && administration.plugins().referencePlugin().isEnabled();
    }

    private static void assertToolsGroup(final LinkGroup toolsGroup)
    {
        assertEquals("jira.issue.tools", toolsGroup.getId());
        assertGroupContainsLinkLabels(getOnlyGroup(toolsGroup), "XML", "Word", "Printable");
    }

    private static LinkGroup getOnlyGroup(final LinkGroup group)
    {
        assertEquals(1, group.getGroups().size());
        return group.getGroups().get(0);
    }

    private static void assertGroupContainsLinkIds(final LinkGroup group, final String... linkIds)
    {
        List<String> ids = Lists.transform(group.getLinks(),new Function<SimpleLink, String>()
        {
            @Override
            public String apply(@Nullable final SimpleLink simpleLink)
            {
                return simpleLink.id;
            }
        });

        assertTrue(ids.containsAll(Arrays.asList(linkIds)));
    }

    private static void assertGroupContainsLinkLabels(final LinkGroup group, final String... linkLabels)
    {
        List<String> labels = Lists.transform(group.getLinks(),new Function<SimpleLink, String>()
        {
            @Override
            public String apply(@Nullable final SimpleLink simpleLink)
            {
                return simpleLink.label;
            }
        });

        assertTrue(labels.containsAll(Arrays.asList(linkLabels)));
    }

    private static void assertNoLinks(final LinkGroup group)
    {
        assertTrue(group.getLinks().isEmpty());
    }

    private static void assertNoGroups(final LinkGroup group)
    {
        assertTrue(group.getGroups().isEmpty());
    }
}
