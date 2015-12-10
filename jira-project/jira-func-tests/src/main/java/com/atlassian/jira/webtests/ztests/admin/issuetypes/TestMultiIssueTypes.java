package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES, Category.ISSUE_TYPES })
public class TestMultiIssueTypes extends FuncTestCase
{
    private static final String AUTO_CREATED_SCHEME_PREFIX = "New issue type scheme for project ";
    private static final String CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR = "#content > header.aui-page-header h1";
    private static final String VIEW_ISSUE_PAGE_ISSUE_TYPE_LABEL_ID = "type-val";

    public TestMultiIssueTypes()
    {
        super();
    }

    public void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void tearDownTest()
    {
        administration.restoreBlankInstance();
    }

    public void testMultiIssueTypes() throws Exception
    {
        _testCreateNewIssueTypeScheme();
        _testSameAsProject();
        _testChooseScheme();
    }

    private void _testChooseScheme()
    {
        log("Testing choosing scheme from a list & that all available projects share the same config");

        Long mkyProjectId = backdoor.project().getProjectId(PROJECT_MONKEY_KEY);
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + mkyProjectId);

        tester.checkCheckbox("createType", "createScheme");
        tester.selectOption("selectedOptions", "Task");
        tester.submit();

        assertThat(backdoor.project().getSchemes(mkyProjectId).issueTypeScheme.name, equalTo(AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY));


        Long hspProjectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + hspProjectId);

        tester.selectOption("schemeId", AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY);
        tester.submit();

        assertThat(backdoor.project().getSchemes(hspProjectId).issueTypeScheme.name, equalTo(AUTO_CREATED_SCHEME_PREFIX + PROJECT_MONKEY));

        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Task");
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");
    }

    private void _testSameAsProject()
    {
        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        log("Choose issue type scheme same as another project");

        tester.checkCheckbox("createType", "chooseProject");
        tester.selectOption("sameAsProjectId", PROJECT_MONKEY);
        tester.submit();
        text.assertTextPresent(locator.page(), "Default Issue Type Scheme");

        // Test issue creation should pass
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Task");
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");
    }

    private void _testCreateNewIssueTypeScheme()
    {
        log("Create a new issue type scheme");

        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        // Select options for the new scheme
        tester.checkCheckbox("createType", "createScheme");
        form.selectOptionsByDisplayName("selectedOptions",new String[]{"Bug","Improvement"});
        //selectMultiOption("selectedOptions", "Bug");
        //selectMultiOption("selectedOptions", "Improvement");
        tester.submit();

        assertThat(backdoor.project().getSchemes(projectId).issueTypeScheme.name, equalTo(AUTO_CREATED_SCHEME_PREFIX + PROJECT_HOMOSAP));

        // Test issue creation should fail
        if (new IdLocator(tester,"leave_admin").hasNodes())
        {
            tester.clickLink("leave_admin");
        }
        tester.clickLink("create_link");
        tester.selectOption("pid", PROJECT_HOMOSAP);
        tester.selectOption("issuetype", "Task");
        tester.submit();
        text.assertTextPresent(locator.page(), "The issue type selected is invalid");
        tester.selectOption("issuetype", "Bug");
        tester.submit();
        text.assertTextPresent(locator.css(CREATE_ISSUE_FORM_HEADER_CSS_LOCATOR), "Create Issue");
    }

    // Ensure that BULK CHANGE global permission is not required for issue type scheme association
    public void testMultipleIssueTypeSchemesWithoutBulkChangePermission()
    {
        administration.restoreData("TestIssueTypesSchemes.xml");
        administration.removeGlobalPermission(BULK_CHANGE, Groups.USERS);

        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLink("associate_10011");
        tester.selectOption("projects", "homosapien");
        tester.submit("Associate");
        tester.assertTextPresent("homosapien");
        tester.assertTextPresent("Bug");
        tester.assertTextPresent("1");
        tester.submit("nextBtn");
        tester.submit("nextBtn");
        tester.submit("nextBtn");
        tester.assertTextPresent("homosapien");
        tester.assertTextPresent("New Feature");
        tester.submit("nextBtn");

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(locator.id(VIEW_ISSUE_PAGE_ISSUE_TYPE_LABEL_ID), "New Feature");
    }
}
