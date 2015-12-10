package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

@Ignore ("I have no idea but since the whole test is commented out at least this way it will show up in the ignored list - rsmart")
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestEditIssueForEnterprise extends JIRAWebTest
{
    public TestEditIssueForEnterprise(String name)
    {
        super(name);
    }

    public void testEditIssueForEnterprise()
    {


//        editIssueWithIssueSecurity();
//        editIssueWithoutIssueSecurity();
//        editIssueWithSecurityRequired();
//        editIssueIssueSecurityViolation();
//        editIssueWithFieldLayoutSchemeHidden();
//        editIssueWithFieldLayoutSchemeRequired();
    }
//    /**
//     * Tests if the user is able to alter the security level of an issue
//     */
//    public void editIssueWithIssueSecurity()
//    {
////        associateSecuritySchemeToProject(PROJECT_HOMOSAP, SECURITY_SCHEME_NAME);
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        log("Edit Issue; Test ability to change Security Level");
//
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-9");
//        clickLink("edit-issue");
//
//        selectOption("security", SECURITY_LEVEL_TWO_NAME);
//        submit();
//        assertTextPresent(PROJECT_HOMOSAP_KEY + "-9");
//        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
//        removeGroupPermission(SET_ISSUE_SECURITY,JIRA_ADMIN);
////        associateSecuritySchemeToProject(PROJECT_HOMOSAP, "None");
//    }
//
//    /**
//     * Tests if the 'Security Level' Link is available with an project WITHOUT an associated 'Issue Security Scheme'
//     */
//    public void editIssueWithoutIssueSecurity()
//    {
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        log("Edit Issue: Test availability of 'Security Level' field");
//        gotoIssue(PROJECT_MONKEY_KEY + "-1");
//        clickLink("edit-issue");
//
//        assertFormElementNotPresent("security");
//        removeGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//    }

//    /**
//     * Tests for error handling with 'Security Level' required
//     */
//    public void editIssueWithSecurityRequired()
//    {
////        associateSecuritySchemeToProject(PROJECT_HOMOSAP, SECURITY_SCHEME_NAME);
//        setSecurityLevelToRequried();
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//
//        log("Edit Issue: Test the ability to update an issue with 'Security Level' required");
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
//        clickLink("edit-issue");
//
//        submit("Update");
//
//        assertTextPresent("Use this form to edit the fields of this issue.");
//        assertTextPresent("Security Level is required - please select an actual security level.");
//
//        removeGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        resetFields();
////        associateSecuritySchemeToProject(PROJECT_HOMOSAP, "None");
//    }

//    /**
//     * Tests if a user can breach Issue Security()
//     */
//    public void editIssueIssueSecurityViolation()
//    {
//        log("Edit Issue: Test the availabilty of an issue for which a user is not permitted to view.");
//        logout();
//        login(USERNAME_BOB, PASSWORD_BOB);
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-9");
//        assertTextPresent("PERMISSION VIOLATION");
//        assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
//        logout();
//        login(ADMIN_USERNAME, ADMIN_PASSWORD);
//    }

    /**
     * Tests the functionality of Field Layout Schemes for 'Create Issue' using 'Hidden' fields
     */
    public void editIssueWithFieldLayoutSchemeHidden()
    {
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        // Set fields to be hidden
        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME, AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME, FIX_VERSIONS_FIELD_ID);

        log("Edit Issue: Test the updating of an issue using hidden fields");
        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
        clickLink("edit-issue");

        assertFormElementNotPresent("components");
        assertFormElementNotPresent("versions");
        assertFormElementNotPresent("fixVersions");

        // Reset fields to be optional
        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME, AFFECTS_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
    }

    /**
     * Tests the functionality of Field Layout Schemes for 'Create Issue' using 'Required' fields
     */
    public void editIssueWithFieldLayoutSchemeRequired()
    {
        log("Edit Issue: Attempt to edit an issue with issue field layout");
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME, COMPONENTS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME, AFFECTS_VERSIONS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME, FIX_VERSIONS_FIELD_ID);

        gotoIssue(PROJECT_HOMOSAP_KEY + "-2");
        clickLink("edit-issue");

        assertTextPresent("Edit Issue");
        submit("Update");

        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        setOptionalFieldsOnEnterprise(FIELD_SCHEME_NAME, COMPONENTS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_SCHEME_NAME, AFFECTS_VERSIONS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_SCHEME_NAME, FIX_VERSIONS_FIELD_ID);
    }
}
