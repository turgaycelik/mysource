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
public class TestVersionValidation extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestBrowseProjectRoadmapAndChangeLogTab.xml");
    }

    public void testVersionValidationSwitchingProjectsUnderneath()
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
        assertions2.assertNodeExists("//a[@title='Version 1 ']");
        assertions2.getLinkAssertions().assertLinkAtNodeContains("//a[@title='Version 1 ']", "browse/RELEASED/fixforversion/10072");

        tester.submit("Update");

        assertions.assertNodeHasText("//*[@class='error']", "Versions Version 1(10040), Version 2(10041), Version A(10042), Version <b>B</b>(10043), Version 3(10044), Version Version(10045), Version 6(10046), Version 8(10047), Version Nick(10048), This is getting silly(10060), V2(10059), Lets throw in a Date(10058), Still going(10057), Version Justus(10054), Version Brenden(10053) are not valid for project 'All Released'.");
        assertions.assertNodeHasText("//*[@id='key-val']", "RELEASED-1");
    }

    public void testVersionValidationNonExistantVersion()
    {
        tester.gotoPage(page.addXsrfToken("/secure/EditIssue.jspa?id=10000&summary=LOTS-1&components=99&fixVersions=999&assignee=admin&reporter=admin&issuetype=1"));

        assertions.assertNodeHasText("//*[@class='error']", "Version with id '999' does not exist.");
    }

    private WebTester getNewTester()
    {
        final WebTester tester2 = WebTesterFactory.createNewWebTester(environmentData);
        tester2.beginAt("/");
        return tester2;
    }

}
