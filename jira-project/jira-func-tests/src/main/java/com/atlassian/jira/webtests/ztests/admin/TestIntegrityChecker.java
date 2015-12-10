package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Ensure that all the correct integrity checks are shown in the UI.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestIntegrityChecker extends FuncTestCase
{
    private static final String INTEGRITY_CHECKBOX_TEXT_LOCATOR = "//table[@class='jiraform maxWidth']//td";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    /**
     * Assert all integrity checks can be accessed via the UI.
     */
    public void testAllIntegrityChecksPresent()
    {
        navigation.gotoAdmin();
        tester.clickLink("integrity_checker");
        tester.assertTextPresent("Select one or more integrity checks from the list below to check for out of date information in the database.");
        assertIntegrityOptionDescriptionPresent("Select All");
        assertIntegrityOptionDescriptionPresent("Check Issue Relations");
        assertIntegrityOptionDescriptionPresent("Check Issue for Relation 'ParentProject'");
        assertIntegrityOptionDescriptionPresent("Check Issue for Relation 'RelatedOSWorkflowEntry'");
        assertIntegrityOptionDescriptionPresent("Check that all Issue Links are associated with valid issues");
        assertIntegrityOptionDescriptionPresent("Check Search Request");
        assertIntegrityOptionDescriptionPresent("Check search request references a valid project");
        assertIntegrityOptionDescriptionPresent("Check for Duplicate Permissions");
        assertIntegrityOptionDescriptionPresent("Check the permissions are not duplicated");
        assertIntegrityOptionDescriptionPresent("Check Workflow Integrity");
        assertIntegrityOptionDescriptionPresent("Check workflow entry states are correct");
        assertIntegrityOptionDescriptionPresent("Check workflow current step entries");
        assertIntegrityOptionDescriptionPresent("Check JIRA issues with null status");
        assertIntegrityOptionDescriptionPresent("Check Field Layout Scheme Integrity");
        assertIntegrityOptionDescriptionPresent("Check field layout schemes for references to deleted custom fields");
        assertIntegrityOptionDescriptionPresent("Check for invalid filter subscriptions");
        assertIntegrityOptionDescriptionPresent("Check FilterSubscriptions for references to non-existent scheduled job");
        assertIntegrityOptionDescriptionPresent("Check FilterSubscriptions for references to non-existent SearchRequests");
    }

    private void assertIntegrityOptionDescriptionPresent(String actual)
    {
        assertions.assertNodeHasText(INTEGRITY_CHECKBOX_TEXT_LOCATOR, actual);
    }
}
