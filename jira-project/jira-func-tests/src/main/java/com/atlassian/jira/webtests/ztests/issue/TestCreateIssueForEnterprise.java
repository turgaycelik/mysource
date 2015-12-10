package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestCreateIssueForEnterprise extends JIRAWebTest
{
    // Tests 'Create Issue' functions that are Enterprise Edition specific
    public TestCreateIssueForEnterprise(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        getBackdoor().usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
    }

    private void ensureProjectsExist()
    {
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project '" + PROJECT_HOMOSAP + "' exists");
            if (!(componentExists(COMPONENT_NAME_THREE, PROJECT_HOMOSAP)))
            {
                addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
            }
        }
        else
        {
            administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
            addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
        }
    }

    public void testCreateIssueForEnterprise()
    {
        ensureProjectsExist();

        createIssueWithComponentLeadAssignee();
        createIssueWithProjectLeadAssignee();
        createIssueWithUnassignedComponentLeadAssignee();
        deleteAllIssuesInAllPages();
    }

    /**
     * HSP-6: Creating issue with default component assignee set at 'Component Lead'
     */
    public String createIssueWithComponentLeadAssignee()
    {
        // Creating an issue with a component lead assignee

        // Ensure user has the 'Assignable User' permission
        getBackdoor().usersAndGroups().addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);
        setComponentLead(PROJECT_HOMOSAP, BOB_USERNAME, BOB_FULLNAME, COMPONENT_NAME_ONE);
        setComponentAssigneeOptions(PROJECT_HOMOSAP, COMPONENT_NAME_ONE, "1");
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 6", "Minor", new String[]{COMPONENT_NAME_ONE}, null, null, null, "test environment 6", "test description 6 for test create issue (enterprise)", null, null, null);
        assertTextPresent("test 6");
        assertTextPresent("Minor");
        assertTextPresent("Bug");
        assertTextPresent(BOB_FULLNAME);
        clearComponentLead(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
        getBackdoor().usersAndGroups().removeUserFromGroup(BOB_USERNAME, Groups.DEVELOPERS);
        return issueKey;
    }

    /**
     * HSP-7: Creating issue with default component assignee set 'Project Lead'
     */
    public String createIssueWithProjectLeadAssignee()
    {
        // Creating an issue with a Project Lead assignee

        // Ensure user has the 'Assignable User' permission
        addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);
        setProjectLead(PROJECT_HOMOSAP, BOB_USERNAME);
        setComponentAssigneeOptions(PROJECT_HOMOSAP, COMPONENT_NAME_ONE, "2");
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 7", "Minor", new String[]{COMPONENT_NAME_ONE}, null, null, "- Automatic -", "test environment 7", "test description 7 for test create issue (enterprise)", null, null, null);
        assertTextPresent("test 7");
        assertTextPresent("Bug");
        assertTextPresent("Minor");
        assertTextPresent(BOB_FULLNAME);
        setProjectLead(PROJECT_HOMOSAP,ADMIN_USERNAME);
        removeUserFromGroup(BOB_USERNAME, Groups.DEVELOPERS);
        return issueKey;
    }

    /**
     * HSP-8: Creating issue with default component assignee set 'Unassigned'
     */
    public String createIssueWithUnassignedComponentLeadAssignee()
    {
        // Creating an issue with 'Unassigned' as component lead assignee
        setUnassignedIssuesOption(true);
        setComponentAssigneeOptions(PROJECT_HOMOSAP, COMPONENT_NAME_ONE, "3");
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 8", "Minor", new String[]{COMPONENT_NAME_ONE}, null, null, "- Automatic -", "test environment 8", "test description 8 for test create issue (enterprise)", null, null, null);
        assertTextPresent("test 8");
        assertTextPresent("Bug");
        assertTextPresent("Minor");
        assertTextPresentBeforeText("Assignee:", "Unassigned");
        // Ensure issue is assigned
        assignIssue(issueKey, "Assigning issue to ADMIN", ADMIN_FULLNAME);
        setUnassignedIssuesOption(false);
        return issueKey;
    }
}