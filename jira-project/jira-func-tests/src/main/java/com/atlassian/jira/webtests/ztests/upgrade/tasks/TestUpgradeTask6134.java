package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Responsible for testing project roles.
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS, Category.PROJECTS, Category.ROLES })
public class TestUpgradeTask6134 extends FuncTestCase
{
    private static final String EXPECTED_ACTORS =
            "[Actor[id=10020,type=atlassian-user-role-actor,name=Fred,displayName=Fred Normal]," +
            "Actor[id=10021,type=atlassian-group-role-actor,name=jira-Developers,displayName=jira-Developers]," +
            "Actor[id=10022,type=atlassian-user-role-actor,name=devO,displayName=Totes Devo]]";

    public void testProjectRolesCorrectForMixedCaseUsersAndGroups() throws IOException, SAXException
    {
        administration.restoreData("TestUpgradeTask6134.xml");
        final ProjectRole role = new ProjectRoleClient(environmentData).get("HSP", "test role");
        assertEquals(EXPECTED_ACTORS, toString(role.actors));

        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("view_test role");
        text.assertTextSequence(locator.table("issuesecurityschemes").getText(),
                "Default Issue Security Scheme", "homosapien", "3 (View)");
    }

    private static String toString(List<ProjectRole.Actor> actors)
    {
        if (actors.isEmpty())
        {
            return "[]";
        }

        final List<ProjectRole.Actor> copy = Lists.newArrayList(actors);
        Collections.sort(copy, OrderById.INSTANCE);
        final StringBuilder sb = new StringBuilder(256).append('[');
        for (ProjectRole.Actor actor : copy)
        {
            sb.append("Actor[id=").append(actor.id)
                    .append(",type=").append(actor.type)
                    .append(",name=").append(actor.name)
                    .append(",displayName=").append(actor.displayName)
                    .append("],");
        }
        sb.setCharAt(sb.length()-1, ']');
        return sb.toString();
    }

    private static class OrderById implements Comparator<ProjectRole.Actor>
    {
        final static OrderById INSTANCE = new OrderById();

        @Override
        public int compare(ProjectRole.Actor o1, ProjectRole.Actor o2)
        {
            return o1.id.compareTo(o2.id);
        }
    }
}


