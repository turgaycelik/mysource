package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.AssertionsImpl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import net.sourceforge.jwebunit.WebTester;

@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.PROJECTS })
public class TestComponentValidation extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestBrowseProjectRoadmapAndChangeLogTab.xml");
        backdoor.usersAndGroups().addUser("dev", "dev", "Developer", "dev@example.com");
        backdoor.usersAndGroups().addUserToGroup("dev", "jira-developers");
    }

    public void testComponentValidationSwitchingProjectsUnderneath()
    {
        navigation.issue().viewIssue("LOTS-1");
        tester.clickLink("edit-issue");

        WebTester tester2 = getNewTester();

        Navigation navigation2 = new NavigationImpl(tester2, environmentData);
        Assertions assertions2 = new AssertionsImpl(tester2, environmentData, navigation2, locator);

        navigation2.login(ADMIN_USERNAME);
        navigation2.issue().viewIssue("LOTS-1");
        tester2.clickLink("move-issue");
        tester2.setFormElement("pid", "10051");
        tester2.submit("Next >>");
        tester2.submit("Next >>");
        tester2.submit("Move");
        assertions2.assertNodeExists("//a[@title='Component 1 ']");
        assertions2.getLinkAssertions().assertLinkAtNodeContains("//a[@title='Component 1 ']", "browse/RELEASED/component/10040");

        tester.submit("Update");

        assertions.assertNodeHasText("//*[@class='error']", "Components Component 1(10030), Component 3(10031) are not valid for project 'All Released'.");
        assertions.assertNodeHasText("//*[@id='key-val']", "RELEASED-1");
    }

    public void testComponentValidationNonExistantComponent()
    {
        navigation.login("dev");
        tester.gotoPage(page.addXsrfToken("/secure/EditIssue.jspa?id=10000&summary=LOTS-1&components=99&fixVersions=999&assignee=admin&reporter=admin&issuetype=1"));

        //this validation error now only shows up for non project-admins. Project admins are allowed to create non-existing components.
        assertions.assertNodeHasText("//*[@class='error']", "Component with id '99' does not exist.");
    }

    private WebTester getNewTester()
    {
        final WebTester tester2 = WebTesterFactory.createNewWebTester(environmentData);
        tester2.beginAt("/");
        return tester2;
    }

}
