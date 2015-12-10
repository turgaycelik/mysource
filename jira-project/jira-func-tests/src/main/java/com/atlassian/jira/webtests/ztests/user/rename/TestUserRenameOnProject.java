package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.table.HtmlTable;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PROJECTS})
public class TestUserRenameOnProject extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        administration.restoreData("user_rename.xml");

        //    KEY       USERNAME    NAME
        //    bb	    betty	    Betty Boop
        //    ID10001	bb	        Bob Belcher
        //    cc	    cat	        Crazy Cat
        //    ID10101	cc	        Candy Chaos
    }

    public void testJqlFunctionsFindRenamedProjectLeads()
    {
        backdoor.project().setProjectLead(10000, "cat");
        renameUser("cat", "crazy");
        renameUser("bb", "cat");
        // Check recycled user does not inherit their forebear's projects
        navigation.gotoResource("../../../rest/api/2/search?jql=project%20in%20projectsLeadByUser(cat)");
        tester.assertTextPresent("\"total\":0");

        // Check renamed user is still recognised as leading the same projects
        navigation.gotoResource("../../../rest/api/2/search?jql=project%20in%20projectsLeadByUser(crazy)");
        tester.assertTextPresent("\"total\":4");
    }

    public void testRenamedDefaultProjectLeadAsDefaultAssignee()
    {
        // Set Bob as the lead by name
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10000");
        FormParameterUtil leadEditForm = getLeadEditForm();
        leadEditForm.setFormElement("lead","bb");
        leadEditForm.submitForm();
        // Check Bob is the assignee for a new issue
        String dummyIssueId = backdoor.issues().createIssue("COW","Chew on some grass").key();
        navigation.gotoPage(String.format("browse/%s", dummyIssueId));
        assertions.assertNodeByIdHasText("assignee-val","Bob Belcher");

        // Set Betty as the lead by name
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10000");
        leadEditForm = getLeadEditForm();
        leadEditForm.setFormElement("lead","betty");
        leadEditForm.submitForm();
        // Check Betty is the assignee for a new issue
        dummyIssueId = backdoor.issues().createIssue("COW","Chew on some more grass").key();
        navigation.gotoPage(String.format("browse/%s", dummyIssueId));
        assertions.assertNodeByIdHasText("assignee-val","Betty Boop");
    }

    public void testUserRenameOnCreateProjectScreens()
    {
        tester.gotoPage("secure/admin/AddProject!default.jspa");
        FormParameterUtil newProjectForm = new FormParameterUtil(tester, "add-project", "Add");
        newProjectForm.setFormElement("name","Feline");
        newProjectForm.setFormElement("key","CAT");
        newProjectForm.addOptionToHtmlSelect("lead", new String[]{"betty", "Betty Boop"});
        newProjectForm.setFormElement("lead","betty");
        newProjectForm.submitForm();

        //On roles page
        tester.gotoPage("plugins/servlet/project-config/CAT/roles");
        assertions.assertNodeByIdHasText("projectLead_betty","Betty Boop");

        //On summary page
        tester.gotoPage("plugins/servlet/project-config/CAT/summary");
        assertions.assertNodeByIdHasText("projectLead_betty","Betty Boop");

        //On project list page
        navigation.gotoAdminSection("view_projects");
        HtmlTable projectsSummaryTable = page.getHtmlTable("project-list");
        assertEquals("Betty Boop", projectsSummaryTable.getRow(2).getCellForHeading("Project Lead"));

        //Check the new value is preserved when we go back to the edit screen
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10100");
        assertOptionTextSelected("lead","Betty Boop");

        renameUser("betty","bboop");

        //Check that Betty is preserved across all the screens

        //On roles page
        tester.gotoPage("plugins/servlet/project-config/CAT/roles");
        assertions.assertNodeByIdHasText("projectLead_bboop","Betty Boop");

        //On summary page
        tester.gotoPage("plugins/servlet/project-config/CAT/summary");
        assertions.assertNodeByIdHasText("projectLead_bboop","Betty Boop");

        //On project list page
        navigation.gotoAdminSection("view_projects");
        projectsSummaryTable = page.getHtmlTable("project-list");
        assertEquals("Betty Boop", projectsSummaryTable.getRow(2).getCellForHeading("Project Lead"));

        //Check the new value is preserved when we go back to the edit screen
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10100");
        assertOptionTextSelected("lead","Betty Boop");
    }

    public void testUserRenameOnUpdateProjectScreens()
    {
        navigation.gotoAdminSection("view_projects");
        HtmlTable projectsSummaryTable = page.getHtmlTable("project-list");
        assertEquals("Adam Ant", projectsSummaryTable.getRow(1).getCellForHeading("Project Lead"));

        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10000");
        assertOptionTextSelected("lead", "Adam Ant");
        FormParameterUtil leadEditForm = getLeadEditForm();
        leadEditForm.setFormElement("lead", "bb");
        leadEditForm.submitForm();

        //On roles page
        tester.gotoPage("plugins/servlet/project-config/COW/roles");
        assertions.assertNodeByIdHasText("projectLead_bb","Bob Belcher");

        //On summary page
        tester.gotoPage("plugins/servlet/project-config/COW/summary");
        assertions.assertNodeByIdHasText("projectLead_bb","Bob Belcher");

        //On project list page
        navigation.gotoAdminSection("view_projects");
        projectsSummaryTable = page.getHtmlTable("project-list");
        assertEquals("Bob Belcher", projectsSummaryTable.getRow(1).getCellForHeading("Project Lead"));

        //Check the new value is preserved when we go back to the edit screen
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10000");
        assertOptionTextSelected("lead", "Bob Belcher");

        leadEditForm = getLeadEditForm();
        leadEditForm.setFormElement("lead", "betty");
        leadEditForm.submitForm();

        //On roles page
        tester.gotoPage("plugins/servlet/project-config/COW/roles");
        assertions.assertNodeByIdHasText("projectLead_betty","Betty Boop");

        //On summary page
        tester.gotoPage("plugins/servlet/project-config/COW/summary");
        assertions.assertNodeByIdHasText("projectLead_betty","Betty Boop");

        //On project list page
        navigation.gotoAdminSection("view_projects");
        projectsSummaryTable = page.getHtmlTable("project-list");
        assertEquals("Betty Boop", projectsSummaryTable.getRow(1).getCellForHeading("Project Lead"));

        //Check the new value is preserved when we go back to the edit screen
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10000");
        assertOptionTextSelected("lead","Betty Boop");
    }

    public void testRenamedProjectLeadCanNotBeDeleted()
    {
        administration.restoreData("user_rename_doggy_components.xml");

        final String bettysProjectName = "Canine";

        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("betty"));
        text.assertTextPresent(deleteUserPage.getProjectLink(), bettysProjectName);

        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("bb"));
        text.assertTextNotPresent(deleteUserPage. getProjectLink(), bettysProjectName); // The user currently known as 'bb' does not have an associated project

        renameUser("bb","belchyman");
        renameUser("betty","bb");

        deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("bb"));
        text.assertTextPresent(deleteUserPage.getProjectLink(), bettysProjectName); // The user formerly known as 'betty' should be known as 'bb' now.
    }

    private void assertOptionTextSelected(String selectId, String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, String.format("//select[@id='%s']/option[@selected='selected']", selectId));
        assertTrue(locator.exists());
        text.assertTextPresent(locator, textToTest);
    }

    private void assertNodeHasText(final String xpath, final String textToTest)
    {
        final XPathLocator locator = new XPathLocator(tester, xpath);
        assertTrue(locator.exists());
        text.assertTextPresent(locator, textToTest);
    }

    private FormParameterUtil getLeadEditForm()
    {
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "project-edit-lead-and-default-assignee","Update");
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{ADMIN_USERNAME, "Adam Ant"});
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{"betty", "Betty Boop"});
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{"bb", "Bob Belcher"});
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{"cat", "Crazy Cat"});
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{"cc", "Candy Chaos"});
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{ADMIN_USERNAME});
        return formParameterUtil;
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}