package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.AdminTabs;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestAdminTabs extends FuncTestCase
{
    public void testCorrectTabsShown() throws SAXException
    {
        if (backdoor.darkFeatures().isGlobalEnabled("com.atlassian.jira.darkfeature.CommonHeader"))
        {
            log("The common header is enabled for this instance. Therefore, not running TestAdminTabs as the tabbed "
                    + "administration UI has been no longer exists in JIRA.");
            return;
        }
        administration.restoreBlankInstance();
        final AdminTabs tabs = administration.tabs();

        //test single level tabs
        navigation.gotoAdminSection("view_projects");
        assertEquals("Projects", tabs.getPageHeading());
        assertEquals(0, tabs.getNumberOfTabs());

        navigation.gotoAdminSection("view_categories");
        assertEquals("Project Categories", tabs.getPageHeading());
        assertEquals(0, tabs.getNumberOfTabs());

        //test no tabs
        navigation.gotoAdminSection("general_configuration");
        assertEquals("General Configuration", tabs.getPageHeading());
        assertEquals(0, tabs.getNumberOfTabs());

        //test multi level tabs
        navigation.gotoAdminSection("issue_types_section");
        List<AdminTabs.TabGroup> expected =
                createFlatTabs(new AdminTabs.Tab("Issue Types", true), new AdminTabs.Tab("Issue Type Schemes", false), new AdminTabs.Tab("Sub-Tasks", false));
        assertEquals("Issue Types", tabs.getPageHeading());
        assertEquals(expected, tabs.getCurrentTabs());
        tester.clickLink("subtasks_tab");
        expected = createFlatTabs(new AdminTabs.Tab("Issue Types", false), new AdminTabs.Tab("Issue Type Schemes", false), new AdminTabs.Tab("Sub-Tasks", true));
        assertEquals("Issue Types", tabs.getPageHeading());
        assertEquals(expected, tabs.getCurrentTabs());
        tester.clickLink("issue_type_schemes_tab");
        expected = createFlatTabs(new AdminTabs.Tab("Issue Types", false), new AdminTabs.Tab("Issue Type Schemes", true), new AdminTabs.Tab("Sub-Tasks", false));
        assertEquals("Issue Types", tabs.getPageHeading());
        assertEquals(expected, tabs.getCurrentTabs());


        navigation.gotoAdminSection("timetracking");
        expected = createFlatTabs(new AdminTabs.Tab("Time Tracking", true), new AdminTabs.Tab("Issue Linking", false));
        assertEquals("Issue Features", tabs.getPageHeading());
        assertEquals(expected, tabs.getCurrentTabs());

        //test ignite tabs
        navigation.gotoAdminSection("view_projects");
        tester.clickLink("view-project-10000");

        expected = CollectionBuilder.newBuilder(
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Summary", true)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Issue Types", false),
                        new AdminTabs.Tab("Workflows", false),
                        new AdminTabs.Tab("Screens", false),
                        new AdminTabs.Tab("Fields", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Versions", false),
                        new AdminTabs.Tab("Components", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Roles", false),
                        new AdminTabs.Tab("Permissions", false),
                        new AdminTabs.Tab("Issue Security", false),
                        new AdminTabs.Tab("Notifications", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Team Shortcuts", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Issue Collectors", false)
                ).asList())
        ).asList();
        assertEquals(expected, tabs.getCurrentTabs());

        tester.clickLink("view_project_issuesecurity_tab");
        expected = CollectionBuilder.newBuilder(
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Summary", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Issue Types", false),
                        new AdminTabs.Tab("Workflows", false),
                        new AdminTabs.Tab("Screens", false),
                        new AdminTabs.Tab("Fields", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Versions", false),
                        new AdminTabs.Tab("Components", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Roles", false),
                        new AdminTabs.Tab("Permissions", false),
                        new AdminTabs.Tab("Issue Security", true),
                        new AdminTabs.Tab("Notifications", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Team Shortcuts", false)
                ).asList()),
                new AdminTabs.TabGroup(CollectionBuilder.newBuilder(
                        new AdminTabs.Tab("Issue Collectors", false)
                ).asList())
        ).asList();
        assertEquals(expected, tabs.getCurrentTabs());
    }

    private List<AdminTabs.TabGroup> createFlatTabs(AdminTabs.Tab... tabs)
    {
        final List<AdminTabs.TabGroup> ret = new ArrayList<AdminTabs.TabGroup>();
        ret.add(new AdminTabs.TabGroup(Arrays.asList(tabs)));
        return ret;
    }
}
