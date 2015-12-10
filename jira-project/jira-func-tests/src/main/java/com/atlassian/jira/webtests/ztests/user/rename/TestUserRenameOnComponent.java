package com.atlassian.jira.webtests.ztests.user.rename;

/**
 * @since v6.0
 */

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PROJECTS})
public class TestUserRenameOnComponent extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("user_rename_doggy_components.xml");

        //    KEY       USERNAME    NAME
        //    bb        betty       Betty Boop
        //    ID10001   bb          Bob Belcher
        //    cc        cat         Crazy Cat
        //    ID10101   cc          Candy Chaos

        //    COMPONENT ID  COMPONENT NAME      LEAD_KEY    LEAD_DISPLAY_NAME
        //    10100         Lean & Mean         ID10101     Candy Chaos
        //    10000         Small & Ratty       ID10001     Bob Belcher
        //    10001         Big & Wussy         bb          Betty Boop
        //    10102         Wiry & Skittish     cc          Crazy Cat
    }

    private static final String LEAN_MEAN_ID = "10100";
    private static final String SMALL_RATTY_ID = "10000";
    private static final String BIG_WUSSY_ID = "10001";
    private static final String WIRY_SKITTISH_ID = "10002";

    public void testJqlFunctionsFindRenamedComponentLeads()
    {
        Map<String,String[]> componentParamMap = new HashMap<String, String[]>();
        componentParamMap.put("components", new String[] { WIRY_SKITTISH_ID });
        final String wirySkittishIssueKey = navigation.issue().createIssue("Canine", "Task", "Run in circles on the spot", componentParamMap);
        renameUser("cat", "crazy");
        renameUser("bb", "cat");

        // Check recycled user does not inherit their forbear's components
        navigation.gotoResource("../../../rest/api/2/search?jql=component%20in%20componentsLeadByUser(\"cat\")");
        tester.assertTextPresent("\"total\":0");

        // Check renamed user still keeps their components
        navigation.gotoResource("../../../rest/api/2/search?jql=component%20in%20componentsLeadByUser(\"crazy\")");
        tester.assertTextPresent("\"total\":1");
        tester.assertTextPresent(wirySkittishIssueKey);

    }

    public void testRenamedComponentLeadAssignedWhenDefault()
    {
        navigation.issue().goToCreateIssueForm("Canine",null);
        Map<String,String[]> componentParamMap = new HashMap<String, String[]>();
        componentParamMap.put("components", new String[] { SMALL_RATTY_ID });
        final String smallRattyIssueKey = navigation.issue().createIssue("Canine", "Task", "Yip in an annoying fashion", componentParamMap);
        navigation.issue().gotoIssue(smallRattyIssueKey);
        text.assertTextPresent(locator.id("assignee-val"),"Bob Belcher");

        navigation.issue().goToCreateIssueForm("Canine",null);
        componentParamMap = new HashMap<String, String[]>();
        componentParamMap.put("components", new String[] { WIRY_SKITTISH_ID });
        final String wirySkittishIssueKey = navigation.issue().createIssue("Canine", "Task", "Run in circles on the spot", componentParamMap);
        navigation.issue().gotoIssue(wirySkittishIssueKey);
        text.assertTextPresent(locator.id("assignee-val"),"Crazy Cat");

        navigation.issue().goToCreateIssueForm("Canine",null);
        componentParamMap = new HashMap<String, String[]>();
        componentParamMap.put("components", new String[] { LEAN_MEAN_ID });
        final String leanMeanIssueKey = navigation.issue().createIssue("Canine", "Task", "Bite off the postie's arm", componentParamMap);
        navigation.issue().gotoIssue(leanMeanIssueKey);
        text.assertTextPresent(locator.id("assignee-val"),"Candy Chaos");

    }

    public void testRenamedComponentLeadsDisplayedCorrectly()
    {
        assertComponentLeads();

        renameUser("bb","bob");
        renameUser("cat","bb");

        assertComponentLeads("betty", "bob", "bb", "cc");
    }


    public void testRenamedComponentLeadElicitsDeletionWarning()
    {
        final String bettysComponent = "Big & Wussy";
        final String bbsComponent = "Small & Ratty";

        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("bb"));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage("bb")));
        text.assertTextPresent(deleteUserPage.getComponentLink(), bbsComponent);


        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("betty"));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage("betty")));
        text.assertTextPresent(deleteUserPage.getComponentLink(), bettysComponent);

        renameUser("bb","belchyman");
        renameUser("betty","bb");

        // The users were switched around, so their components should be switched around as well

        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("bb"));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage("bb")));
        text.assertTextPresent(deleteUserPage.getComponentLink(), bettysComponent);
        text.assertTextNotPresent(deleteUserPage.getComponentLink(), bbsComponent);

        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("belchyman"));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage("belchyman")));
        text.assertTextPresent(deleteUserPage.getComponentLink(), bbsComponent);
        text.assertTextNotPresent(deleteUserPage.getComponentLink(), bettysComponent);
    }

    public void testReferencesToDeletedRenamedComponentLeadRemoved()
    {
        // Delete a recycled user
        navigation.issue().deleteIssue("COW-3");
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("bb"));
        deleteUserPage.clickDeleteUser();

        //Browse projects components panel
        navigation.gotoPage("browse/DOG?selectedTab=com.atlassian.jira.jira-projects-plugin%3Acomponents-panel");
        HtmlTable componentsTable = page.getHtmlTable("components_panel");
        assertEquals("",componentsTable.getRow(3).getCellAsText(2));

        // Delete a renamed user
        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, "returnUrl=UserBrowser.jspa&name="+"cc");
        deleteUserPage.clickDeleteUser();

        //Browse projects components panel
        navigation.gotoPage("browse/DOG?selectedTab=com.atlassian.jira.jira-projects-plugin%3Acomponents-panel");
        componentsTable = page.getHtmlTable("components_panel");
        assertEquals("",componentsTable.getRow(2).getCellAsText(2));

    }

    private void assertComponentLeads()
    {
        assertComponentLeads("betty", "bb", "cat", "cc");
    }

    private void assertComponentLeads(String bettyUserName, String bobUserName, String crazyUserName, String candyUserName)
    {
        //Check the correct user is displayed as owner in each screen (based on their key)

        //Browse projects components panel
        navigation.gotoPage("browse/DOG?selectedTab=com.atlassian.jira.jira-projects-plugin%3Acomponents-panel");
        assertComponentAndLeadInBrowseSummaryTable(1, "Big & Wussy", "Betty Boop");
        assertComponentAndLeadInBrowseSummaryTable(2, "Lean & Mean", "Candy Chaos");
        assertComponentAndLeadInBrowseSummaryTable(3, "Small & Ratty", "Bob Belcher");
        assertComponentAndLeadInBrowseSummaryTable(4, "Wiry & Skittish", "Crazy Cat");

        //Project summary tab components panel
        navigation.gotoPage("plugins/servlet/project-config/DOG/summary");
        assertComponentAndLeadInSummaryList(1, "Big & Wussy", "Betty Boop");
        assertComponentAndLeadInSummaryList(2, "Lean & Mean", "Candy Chaos");
        assertComponentAndLeadInSummaryList(3, "Small & Ratty", "Bob Belcher");
        assertComponentAndLeadInSummaryList(4, "Wiry & Skittish", "Crazy Cat");

        //Individual view component pages
        navigation.gotoPage("browse/DOG/component/" + LEAN_MEAN_ID);
        tester.assertTextInElement("component_summary_" + candyUserName, "Candy Chaos");
        navigation.gotoPage("browse/DOG/component/" + SMALL_RATTY_ID);
        tester.assertTextInElement("component_summary_" + bobUserName, "Bob Belcher");
        navigation.gotoPage("browse/DOG/component/" + BIG_WUSSY_ID);
        tester.assertTextInElement("component_summary_" + bettyUserName, "Betty Boop");
        navigation.gotoPage("browse/DOG/component/" + WIRY_SKITTISH_ID);
        tester.assertTextInElement("component_summary_" + crazyUserName, "Crazy Cat");
    }

    private void assertComponentAndLeadInBrowseSummaryTable(int position, String expectedComponent, String expectedLead)
    {
        HtmlTable componentsTable = page.getHtmlTable("components_panel");
        assertEquals(expectedComponent,componentsTable.getRow(position).getCellAsText(1));
        assertEquals(expectedLead, componentsTable.getRow(position).getCellAsText(2));
    }

    private void assertComponentAndLeadInSummaryList(int position, String expectedComponent, String expectedLead)
    {
        final XPathLocator componentLocator = new XPathLocator(tester, String.format("//ul[@id='project-config-summary-components-list']/li[%d]/span[1]", position));
        final XPathLocator nameLocator = new XPathLocator(tester, String.format("//ul[@id='project-config-summary-components-list']/li[%d]/span[2]", position));
        assertTrue(componentLocator.exists());
        text.assertTextPresent(componentLocator, expectedComponent);
        assertTrue(nameLocator.exists());
        text.assertTextPresent(nameLocator,expectedLead);
    }

    private void renameUser(String from, String to)
    {
        // Rename bb to bob
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}
