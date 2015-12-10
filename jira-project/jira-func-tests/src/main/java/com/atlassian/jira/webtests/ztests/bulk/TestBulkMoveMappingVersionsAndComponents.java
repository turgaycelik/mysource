package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.ViewIssueAssertions;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * JRA-8248 JRADEV-190
 *
 * Bulk Move has been "improved" to allow explicit mapping of Version and Component values between projects. These tests
 * try to cover all the cases for the system and custom fields.
 *
 * @since v4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkMoveMappingVersionsAndComponents extends FuncTestCase
{
    private static final String BULK_EDIT_KEY = "10000_1_";
    private static final String UNKNOWN = "Unknown";

    // Bulk Moving issues from HSP and MKY to just MKY. The existing MKY issues should not be touched and not have their
    // values mapped at all. The HSP issues should be mapped appropriately, with defaults set.
    public void testBulkMoveDistinctValuesMapped() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 1", "New Version 2");
        navigation.issue().setFixVersions(hsp1, "New Version 1", "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 1", "New Component 2");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 1", "New Version 5");

        final String hsp2 = navigation.issue().createIssue("homosapien", "Bug", "second issue");
        navigation.issue().setAffectsVersions(hsp2, "New Version 1", "New Version 3");
        navigation.issue().setFixVersions(hsp2, "New Version 1", "New Version 4");
        navigation.issue().setComponents(hsp2, "New Component 1", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp2, "customfield_10003", "New Version 2");

        final String mky1 = navigation.issue().createIssue("monkey", "Bug", "third issue");
        navigation.issue().setAffectsVersions(mky1, "New Version 3");
        navigation.issue().setFixVersions(mky1, "New Version 5");
        navigation.issue().setComponents(mky1, "New Component 4");
        navigation.issue().setIssueMultiSelectField(mky1, "customfield_10003", "New Version 6");

        // set up the bulk move - HSPs and MKYs are going to MKY
        bulkMoveAllIssuesToProject(TestBulkMoveIssues.PROJECT_MONKEY);

        // verify that the default selected options are the ones with the same name (where appropriate)

        // Fix Versions: New Version 1, New Version 3, New Version 4
        assertDefaultOption("fixVersions", "10000", UNKNOWN);
        assertDefaultOption("fixVersions", "10003", "New Version 3");
        assertDefaultOption("fixVersions", "10004", UNKNOWN);
        // Affects Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("versions", "10000", UNKNOWN);
        assertDefaultOption("versions", "10001", UNKNOWN);
        assertDefaultOption("versions", "10003", "New Version 3");
        // Components: New Component 1, New Component 2, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", UNKNOWN);
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 1, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", UNKNOWN);
        assertDefaultOption("customfield_10003", "10001", UNKNOWN);
        assertDefaultOption("customfield_10003", "10005", "New Version 5");

        // choose some values for the unknowns
        tester.selectOption("fixVersions_10000", "New Version 5");
        tester.selectOption("fixVersions_10004", "New Version 6");
        tester.selectOption("versions_10000", "New Version 5");
        tester.selectOption("versions_10001", "New Version 6");
        tester.selectOption("components_10000", "New Component 4");
        tester.selectOption("components_10001", "New Component 4");
        tester.selectOption("customfield_10003_10000", "New Version 5");
        tester.selectOption("customfield_10003_10001", "New Version 6");

        // complete the wizard
        completeBulkMoveWizard();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // MKY-1 should be unchanged
        assertIssueValuesAfterMove("MKY-1",
                "New Version 3",
                "New Version 5",
                "New Component 4",
                "New Version 6");

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "MKY");
        assertIssueValuesAfterMove(firstIssueKey,
                "New Version 5, New Version 6",
                "New Version 3, New Version 5",
                "New Component 4",
                "New Version 5");

        // second issue changed
        final String secondIssueKey = oldway_consider_porting.getIssueKeyWithSummary("second issue", "MKY");
        assertIssueValuesAfterMove(secondIssueKey,
                "New Version 3, New Version 5",
                "New Version 5, New Version 6",
                "New Component 3, New Component 4",
                "New Version 6");
    }

    // JRA-21669 - user picker custom fields should not attempt to do any mapping. Added a required user picker field
    // in the target project for the bulk move to ensure that no mapping is attempted.
    public void testBulkMoveWithUserCFType() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponentsAndUsers.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 1", "New Version 2");
        navigation.issue().setFixVersions(hsp1, "New Version 1", "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 1", "New Component 2");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 1", "New Version 5");
        // set one issue with a user picker value already entered
        navigation.issue().setFreeTextCustomField(hsp1, "customfield_10010", ADMIN_USERNAME);

        final String hsp2 = navigation.issue().createIssue("homosapien", "Bug", "second issue");
        navigation.issue().setAffectsVersions(hsp2, "New Version 1", "New Version 3");
        navigation.issue().setFixVersions(hsp2, "New Version 1", "New Version 4");
        navigation.issue().setComponents(hsp2, "New Component 1", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp2, "customfield_10003", "New Version 2");
        // second issue does not have a user picker value entered - it will gain one through the bulk move

        // set up the bulk move - HSPs and MKYs are going to MKY
        bulkMoveAllIssuesToProject(TestBulkMoveIssues.PROJECT_MONKEY);

        // verify that the default selected options are the ones with the same name (where appropriate)

        // Fix Versions: New Version 1, New Version 3, New Version 4
        assertDefaultOption("fixVersions", "10000", UNKNOWN);
        assertDefaultOption("fixVersions", "10003", "New Version 3");
        assertDefaultOption("fixVersions", "10004", UNKNOWN);
        // Affects Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("versions", "10000", UNKNOWN);
        assertDefaultOption("versions", "10001", UNKNOWN);
        assertDefaultOption("versions", "10003", "New Version 3");
        // Components: New Component 1, New Component 2, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", UNKNOWN);
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 1, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", UNKNOWN);
        assertDefaultOption("customfield_10003", "10001", UNKNOWN);
        assertDefaultOption("customfield_10003", "10005", "New Version 5");

        // choose some values for the unknowns
        tester.selectOption("fixVersions_10000", "New Version 5");
        tester.selectOption("fixVersions_10004", "New Version 6");
        tester.selectOption("versions_10000", "New Version 5");
        tester.selectOption("versions_10001", "New Version 6");
        tester.selectOption("components_10000", "New Component 4");
        tester.selectOption("components_10001", "New Component 4");
        tester.selectOption("customfield_10003_10000", "New Version 5");
        tester.selectOption("customfield_10003_10001", "New Version 6");

        // user picker
        tester.setFormElement("customfield_10010", ADMIN_USERNAME);

        // complete the wizard
        completeBulkMoveWizard();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "MKY");
        assertIssueValuesAfterMoveWithUserPicker(firstIssueKey,
                "New Version 5, New Version 6",
                "New Version 3, New Version 5",
                "New Component 4",
                ADMIN_FULLNAME,
                "New Version 5");

        // second issue changed
        final String secondIssueKey = oldway_consider_porting.getIssueKeyWithSummary("second issue", "MKY");
        assertIssueValuesAfterMoveWithUserPicker(secondIssueKey,
                "New Version 3, New Version 5",
                "New Version 5, New Version 6",
                "New Component 3, New Component 4",
                ADMIN_FULLNAME,
                "New Version 6");
    }

    // Bulk Moving issues from HSP to MKY. The two HSP issues will be mapped in different contexts, and we will apply
    // different mappings for each context. Both mappings should be applied, and not just the latest one. After the first
    // mapping is chosen, the second context should default to values chosen in the first context.
    public void testBulkMoveTwoContextsDifferentMappings() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setFixVersions(hsp1, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 1", "New Component 2", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 1", "New Version 2", "New Version 5");

        final String hsp2 = navigation.issue().createIssue("homosapien", "New Feature", "second issue");
        navigation.issue().setAffectsVersions(hsp2, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setFixVersions(hsp2, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setComponents(hsp2, "New Component 1", "New Component 2", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp2, "customfield_10003", "New Version 1", "New Version 2", "New Version 5");

        // set up the bulk move - HSP Bug goes to MKY Bug, HSP New Feature goes to MKY New Feature
        navigation.issueNavigator().createSearch("project = HSP");
        oldway_consider_porting.bulkChangeIncludeAllPages();
        oldway_consider_porting.bulkChangeChooseIssuesAll();
        oldway_consider_porting.chooseOperationBulkMove();
        isStepSelectProjectIssueType();
        tester.selectOption("10000_1_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        tester.selectOption("10000_2_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        navigation.clickOnNext();
        isStepSetFields();

        // BUG: verify that the default selected options are the ones with the same name (where appropriate)

        // Fix Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("fixVersions", "10000", UNKNOWN);
        assertDefaultOption("fixVersions", "10001", UNKNOWN);
        assertDefaultOption("fixVersions", "10003", "New Version 3");
        // Affects Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("versions", "10000", UNKNOWN);
        assertDefaultOption("versions", "10001", UNKNOWN);
        assertDefaultOption("versions", "10003", "New Version 3");
        // Components: New Component 1, New Component 2, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", UNKNOWN);
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 1, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", UNKNOWN);
        assertDefaultOption("customfield_10003", "10001", UNKNOWN);
        assertDefaultOption("customfield_10003", "10005", "New Version 5");

        // choose some values for the unknowns
        tester.selectOption("fixVersions_10000", "New Version 5");
        tester.selectOption("fixVersions_10001", "New Version 5");
        tester.selectOption("fixVersions_10003", "New Version 6");
        tester.selectOption("versions_10000", "New Version 5");
        tester.selectOption("versions_10001", "New Version 6");
        tester.selectOption("versions_10003", UNKNOWN);
        tester.selectOption("components_10001", "New Component 4");
        tester.selectOption("customfield_10003_10000", "New Version 5");
        tester.selectOption("customfield_10003_10001", "New Version 5");
        tester.selectOption("customfield_10003_10005", "New Version 6");

        navigation.clickOnNext();

        // NEW FEATURE: verify that the second context will have defaults based on what was previously selected and also name matching

        // Fix Versions: New Version 5, New Version 5, New Version 6
        assertDefaultOption("fixVersions", "10000", "New Version 5");
        assertDefaultOption("fixVersions", "10001", "New Version 5");
        assertDefaultOption("fixVersions", "10003", "New Version 6");
        // Affects Versions: New Version 5, New Version 6, New Version 3
        assertDefaultOption("versions", "10000", "New Version 5");
        assertDefaultOption("versions", "10001", "New Version 6");
        assertDefaultOption("versions", "10003", UNKNOWN);
        // Components: New Component 1, New Component 2, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", "New Component 4");
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 1, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", "New Version 5");
        assertDefaultOption("customfield_10003", "10001", "New Version 5");
        assertDefaultOption("customfield_10003", "10005", "New Version 6");

        // choose some values for the unknowns
        tester.selectOption("fixVersions_10000", UNKNOWN);
        tester.selectOption("versions_10000", UNKNOWN);
        tester.selectOption("customfield_10003_10001", "New Version 3");

        navigation.clickOnNext();

        // check the confirmation screen for correct mappings
        oldway_consider_porting.isStepConfirmation();

        // BUG
        assertConfirmationMapping("10000_1_", "fixVersions",
                "New Version 1", "New Version 5",
                "New Version 2", "New Version 5",
                "New Version 3", "New Version 6");

        assertConfirmationMapping("10000_1_", "versions",
                "New Version 1", "New Version 5",
                "New Version 2", "New Version 6",
                "New Version 3", UNKNOWN);

        assertConfirmationMapping("10000_1_", "components",
                "New Component 1", UNKNOWN,
                "New Component 2", "New Component 4",
                "New Component 3", "New Component 3");

        assertConfirmationMapping("10000_1_", "customfield_10003",
                "New Version 1", "New Version 5",
                "New Version 2", "New Version 5",
                "New Version 5", "New Version 6");

        // NEW FEATURE
        assertConfirmationMapping("10000_2_", "fixVersions",
                "New Version 1", UNKNOWN,
                "New Version 2", "New Version 5",
                "New Version 3", "New Version 6");

        assertConfirmationMapping("10000_2_", "versions",
                "New Version 1", UNKNOWN,
                "New Version 2", "New Version 6",
                "New Version 3", UNKNOWN);

        assertConfirmationMapping("10000_2_", "components",
                "New Component 1", UNKNOWN,
                "New Component 2", "New Component 4",
                "New Component 3", "New Component 3");

        assertConfirmationMapping("10000_2_", "customfield_10003",
                "New Version 1", "New Version 5",
                "New Version 2", "New Version 3",
                "New Version 5", "New Version 6");


        // complete the wizard
        navigation.clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "MKY");
        assertIssueValuesAfterMove(firstIssueKey,
                "New Version 5, New Version 6",
                "New Version 5, New Version 6",
                "New Component 3, New Component 4",
                "New Version 5", "New Version 6");

        // second issue changed
        final String secondIssueKey = oldway_consider_porting.getIssueKeyWithSummary("second issue", "MKY");
        assertIssueValuesAfterMove(secondIssueKey,
                "New Version 6",
                "New Version 5, New Version 6",
                "New Component 3, New Component 4",
                "New Version 5", "New Version 3", "New Version 6");
    }

    // Bulk Moving issues from HSP to MKY. The three HSP issues will be mapped in 3 different contexts, and we will apply
    // different mappings for each context. Values chosen in the first context should be defaulted for second context, and
    // the same for second going to third. However, if second context does not contain a value that was used in first context,
    // it should still be present in the third. I.e. the mappings accumulate as you proceed through the contexts.
    public void testBulkMoveThreeContextsDifferentMappings() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setFixVersions(hsp1, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 1", "New Component 2", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 1", "New Version 2", "New Version 5");

        final String hsp2 = navigation.issue().createIssue("homosapien", "New Feature", "second issue");
        navigation.issue().setAffectsVersions(hsp2, "New Version 1");
        navigation.issue().setFixVersions(hsp2, "New Version 1");
        navigation.issue().setComponents(hsp2, "New Component 1");
        navigation.issue().setIssueMultiSelectField(hsp2, "customfield_10003", "New Version 1");

        final String hsp3 = navigation.issue().createIssue("homosapien", "Task", "third issue");
        navigation.issue().setAffectsVersions(hsp3, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setFixVersions(hsp3, "New Version 1", "New Version 2", "New Version 3");
        navigation.issue().setComponents(hsp3, "New Component 1", "New Component 2", "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp3, "customfield_10003", "New Version 1", "New Version 2", "New Version 5");

        // set up the bulk move - HSP Bug goes to MKY Bug, HSP New Feature goes to MKY New Feature
        navigation.issueNavigator().createSearch("project = HSP");
        oldway_consider_porting.bulkChangeIncludeAllPages();
        oldway_consider_porting.bulkChangeChooseIssuesAll();
        oldway_consider_porting.chooseOperationBulkMove();
        isStepSelectProjectIssueType();
        tester.selectOption("10000_1_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        tester.selectOption("10000_2_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        tester.selectOption("10000_3_pid", TestBulkMoveIssues.PROJECT_MONKEY);
        navigation.clickOnNext();
        isStepSetFields();

        // BUG: verify that the default selected options are the ones with the same name (where appropriate)

        // Fix Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("fixVersions", "10000", UNKNOWN);
        assertDefaultOption("fixVersions", "10001", UNKNOWN);
        assertDefaultOption("fixVersions", "10003", "New Version 3");
        // Affects Versions: New Version 1, New Version 2, New Version 3
        assertDefaultOption("versions", "10000", UNKNOWN);
        assertDefaultOption("versions", "10001", UNKNOWN);
        assertDefaultOption("versions", "10003", "New Version 3");
        // Components: New Component 1, New Component 2, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", UNKNOWN);
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 1, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", UNKNOWN);
        assertDefaultOption("customfield_10003", "10001", UNKNOWN);
        assertDefaultOption("customfield_10003", "10005", "New Version 5");

        tester.selectOption("fixVersions_10000", "New Version 5");
        tester.selectOption("fixVersions_10001", "New Version 6");
        tester.selectOption("versions_10000", "New Version 5");
        tester.selectOption("versions_10001", "New Version 6");
        tester.selectOption("versions_10003", UNKNOWN);
        tester.selectOption("components_10001", "New Component 4");
        tester.selectOption("customfield_10003_10000", "New Version 5");
        tester.selectOption("customfield_10003_10001", "New Version 6");

        navigation.clickOnNext();

        // NEW FEATURE: verify that the second context will have defaults based on what was previously selected and also name matching

        // Fix Versions: New Version 5, New Version 5, New Version 6
        assertDefaultOption("fixVersions", "10000", "New Version 5");
        // Affects Versions: New Version 5, New Version 6, New Version 3
        assertDefaultOption("versions", "10000", "New Version 5");
        // Components: Unknown
        assertDefaultOption("components", "10000", UNKNOWN);
        // Multi VP: New Version 5, New Version 2, New Version 5
        assertDefaultOption("customfield_10003", "10000", "New Version 5");

        // change one value and continue to third context
        tester.selectOption("fixVersions_10000", UNKNOWN);
        navigation.clickOnNext();

        // TASK: verify that the default selected options are the ones with the same name (where appropriate)

        // Fix Versions: Unknown, New Version 6, New Version 3
        assertDefaultOption("fixVersions", "10000", UNKNOWN);
        assertDefaultOption("fixVersions", "10001", "New Version 6");
        assertDefaultOption("fixVersions", "10003", "New Version 3");
        // Affects Versions: New Version 5, New Version 6, New Version 3
        assertDefaultOption("versions", "10000", "New Version 5");
        assertDefaultOption("versions", "10001", "New Version 6");
        assertDefaultOption("versions", "10003", UNKNOWN);
        // Components: Unknown, New Component 4, New Component 3
        assertDefaultOption("components", "10000", UNKNOWN);
        assertDefaultOption("components", "10001", "New Component 4");
        assertDefaultOption("components", "10002", "New Component 3");
        // Multi VP: New Version 5, New Version 6, New Version 5
        assertDefaultOption("customfield_10003", "10000", "New Version 5");
        assertDefaultOption("customfield_10003", "10001", "New Version 6");
        assertDefaultOption("customfield_10003", "10005", "New Version 5");
    }

    private void assertConfirmationMapping(final String uniqueContextId, final String fieldId, final String... mappingValues)
    {
        text.assertTextSequence(new IdLocator(tester, uniqueContextId + fieldId), mappingValues);
    }

    // Bulk Moving issues from HSP and MKY to NDT. Issues in both have values set to the same name, so we will have to
    // map to values of the same name but different projects. Also, neither value exists in the target project.
    public void testBulkMoveIssuesFromTwoProjects() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // add a version to neanderthal so that we have something to map to
        administration.project().addVersion("NDT", "XYZ", null, null);
        administration.project().addComponent("NDT", "ABC", null, null);

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 3");
        navigation.issue().setFixVersions(hsp1, "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 3");

        final String mky1 = navigation.issue().createIssue("monkey", "Bug", "second issue");
        navigation.issue().setAffectsVersions(mky1, "New Version 3");
        navigation.issue().setFixVersions(mky1, "New Version 3");
        navigation.issue().setComponents(mky1, "New Component 3");
        navigation.issue().setIssueMultiSelectField(mky1, "customfield_10003", "New Version 3");

        // set up the bulk move - HSPs and MKYs are going to NDT
        bulkMoveAllIssuesToProject("neanderthal");

        // verify that all defaults are unknown since target project has no mappable versions/components
        // also verify that there is a distinction between the same-named values across projects
        // Fix Versions
        assertDefaultOption("fixVersions", "10002", "New Version 3", "monkey", UNKNOWN);
        assertDefaultOption("fixVersions", "10003", "New Version 3", "homosapien", UNKNOWN);
        // Affects Versions
        assertDefaultOption("versions", "10002", "New Version 3", "monkey", UNKNOWN);
        assertDefaultOption("versions", "10003", "New Version 3", "homosapien", UNKNOWN);
        // Components
        assertDefaultOption("components", "10010", "New Component 3", "monkey", UNKNOWN);
        assertDefaultOption("components", "10002", "New Component 3", "homosapien", UNKNOWN);
        // Multi VP
        assertDefaultOption("customfield_10003", "10002", "New Version 3", "monkey", UNKNOWN);
        assertDefaultOption("customfield_10003", "10003", "New Version 3", "homosapien", UNKNOWN);

        // all the monkeys get a value, all the homosapiens remain UNKNOWN
        tester.selectOption("fixVersions_10002", "XYZ");
        tester.selectOption("versions_10002", "XYZ");
        tester.selectOption("components_10010", "ABC");
        tester.selectOption("customfield_10003_10002", "XYZ");

        // complete the wizard
        completeBulkMoveWizard();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "NDT");
        assertIssueValuesAfterMove(firstIssueKey,
                "None",
                "None",
                "None",
                (String[])null);

        // second issue changed
        final String secondIssueKey = oldway_consider_porting.getIssueKeyWithSummary("second issue", "NDT");
        assertIssueValuesAfterMove(secondIssueKey,
                "XYZ",
                "XYZ",
                "ABC",
                "XYZ");
    }

    // Bulk Moving issues from HSP and MKY to NDT. HSP has no values set, MKY does. We require values to be set in the target project
    public void testBulkMoveIssuesFromTwoProjectsDestinationRequired() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // add a version to neanderthal so that we have something to map to
        administration.project().addVersion("NDT", "XYZ", null, null);
        administration.project().addVersion("NDT", "XYZ1", null, null);
        administration.project().addComponent("NDT", "ABC", null, null);
        administration.project().addComponent("NDT", "ABC1", null, null);

        // set up some issues
        // HSP has no values
        navigation.issue().createIssue("homosapien", "Bug", "first issue");

        final String mky1 = navigation.issue().createIssue("monkey", "Bug", "second issue");
        navigation.issue().setAffectsVersions(mky1, "New Version 3");
        navigation.issue().setFixVersions(mky1, "New Version 3");
        navigation.issue().setComponents(mky1, "New Component 3");
        navigation.issue().setIssueMultiSelectField(mky1, "customfield_10003", "New Version 3");

        setFieldsToBeRequired();

        // set up the bulk move - HSPs and MKYs are going to NDT
        bulkMoveAllIssuesToProject("neanderthal");

        // verify that all defaults are ABC/XYZ since target project has no mappable versions/components and value is required
        // also verify that there is a distinction between the same-named values across projects
        // Fix Versions
        assertDefaultOption("fixVersions", "10002", "New Version 3", "monkey", "XYZ", true);
        assertDefaultOption("fixVersions", "-1", "No Version", null, "XYZ", true);
        // Affects Versions
        assertDefaultOption("versions", "10002", "New Version 3", "monkey", "XYZ", true);
        assertDefaultOption("versions", "-1", "No Version", null, "XYZ", true);
        // Components
        assertDefaultOption("components", "10010", "New Component 3", "monkey", "ABC", true);
        assertDefaultOption("components", "-1", "No Component", null, "ABC", true);
        // Multi VP
        assertDefaultOption("customfield_10003", "10002", "New Version 3", "monkey", "XYZ", true);
        assertDefaultOption("customfield_10003", "-1", "No Version", null, "XYZ", true);

        // all the monkeys get the second value, all the homosapiens remain first value
        tester.selectOption("fixVersions_10002", "XYZ1");
        tester.selectOption("versions_10002", "XYZ1");
        tester.selectOption("components_10010", "ABC1");
        tester.selectOption("customfield_10003_10002", "XYZ1");

        // complete the wizard
        completeBulkMoveWizard();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "NDT");
        assertIssueValuesAfterMove(firstIssueKey,
                "XYZ",
                "XYZ",
                "ABC",
                "XYZ");

        // second issue changed
        final String secondIssueKey = oldway_consider_porting.getIssueKeyWithSummary("second issue", "NDT");
        assertIssueValuesAfterMove(secondIssueKey,
                "XYZ1",
                "XYZ1",
                "ABC1",
                "XYZ1");
    }

    // Bulk Moving issues from HSP to NDT. NDT has no components/versions, so you can't map anything, and all fields
    // should be blanked out.
    public void testBulkMoveIssuesWithNoDestinationValues() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 3");
        navigation.issue().setFixVersions(hsp1, "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 3");

        // set up the bulk move - HSPs are going to NDT
        bulkMoveAllIssuesToProject("neanderthal");

        // verify that nothing can be set as there are no options in the destination project
        assertNoOption("fixVersions");
        // Affects Versions
        assertNoOption("versions");
        // Components
        assertNoOption("components");
        // Multi VP
        assertNoOption("customfield_10003");

        // complete the wizard
        completeBulkMoveWizard();

        waitAndReloadBulkOperationProgressPage();

        // check the values of the fields in the new issues

        // first issue changed
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "NDT");
        final ViewIssueAssertions viewIssueAssertions = assertions.getViewIssueAssertions();
        navigation.issue().viewIssue(firstIssueKey);
        viewIssueAssertions.assertComponentsAbsent();
        viewIssueAssertions.assertAffectsVersionAbsent();
        viewIssueAssertions.assertFixVersionAbsent();
    }

    // Bulk Moving issues from HSP to NDT. NDT has no components/versions, so you can't map anything, but target field config
    // requires fields, so we will see error messages
    public void testBulkMoveIssuesWithNoDestinationValuesButRequired() throws Exception
    {
        administration.restoreData("TestBulkMoveMappingVersionsAndComponents.xml");

        // set up some issues
        final String hsp1 = navigation.issue().createIssue("homosapien", "Bug", "first issue");
        navigation.issue().setAffectsVersions(hsp1, "New Version 3");
        navigation.issue().setFixVersions(hsp1, "New Version 3");
        navigation.issue().setComponents(hsp1, "New Component 3");
        navigation.issue().setIssueMultiSelectField(hsp1, "customfield_10003", "New Version 3");

        setFieldsToBeRequired();

        // set up the bulk move - HSPs are going to NDT
        bulkMoveAllIssuesToProject("neanderthal");

        // verify that nothing can be set as there are no options in the destination project
        assertNoOption("fixVersions");
        // Affects Versions
        assertNoOption("versions");
        // Components
        assertNoOption("components");
        // Multi VP
        assertNoOption("customfield_10003");

        // try click Next and verify the error messages
        navigation.clickOnNext();

        final String versionErrorMsg = "\"%s\" field is required and the project \"neanderthal\" does not have any versions.";
        final String componentErrorMsg = "\"%s\" field is required and the project \"neanderthal\" does not have any components.";
        text.assertTextPresent(new WebPageLocator(tester), String.format(versionErrorMsg, "Affects Version/s"));
        text.assertTextPresent(new WebPageLocator(tester), String.format(versionErrorMsg, "Fix Version/s"));
        text.assertTextPresent(new WebPageLocator(tester), String.format(componentErrorMsg, "Component/s"));
        text.assertTextPresent(new WebPageLocator(tester), "Multi VP is required.");

        // first issue should still be the same
        final String firstIssueKey = oldway_consider_porting.getIssueKeyWithSummary("first issue", "HSP");
        assertIssueValuesAfterMove(firstIssueKey,
                "New Version 3",
                "New Version 3",
                "New Component 3",
                "New Version 3");
    }

    private void setFieldsToBeRequired()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Affects Version/s");
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Fix Version/s");
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Component/s");
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Multi VP");
    }

    private void assertIssueValuesAfterMove(final String issueKey, final String affectsVersions, final String fixVersions, final String components, final String... multiVersionCFValues)
    {
        navigation.issue().viewIssue(issueKey);
        assertions.getViewIssueAssertions().assertAffectsVersions(affectsVersions);
        assertions.getViewIssueAssertions().assertFixVersions(fixVersions);
        assertions.getViewIssueAssertions().assertComponents(components);
        if (multiVersionCFValues == null)
        {
            text.assertTextNotPresent(new WebPageLocator(tester), "Multi VP");
        }
        else
        {
            for (final String multiVersionCFValue : multiVersionCFValues)
            {
                assertions.getViewIssueAssertions().assertCustomFieldValue("customfield_10003", multiVersionCFValue);
            }
        }
    }

    private void assertIssueValuesAfterMoveWithUserPicker(final String issueKey, final String affectsVersions, final String fixVersions, final String components, final String userPickerValues, final String... multiVersionCFValues)
    {
        navigation.issue().viewIssue(issueKey);
        assertions.getViewIssueAssertions().assertAffectsVersions(affectsVersions);
        assertions.getViewIssueAssertions().assertFixVersions(fixVersions);
        assertions.getViewIssueAssertions().assertComponents(components);
        assertions.getViewIssueAssertions().assertCustomFieldValue("customfield_10010", userPickerValues);
        if (multiVersionCFValues == null)
        {
            text.assertTextNotPresent(new WebPageLocator(tester), "Multi VP");
        }
        else
        {
            for (final String multiVersionCFValue : multiVersionCFValues)
            {
                assertions.getViewIssueAssertions().assertCustomFieldValue("customfield_10003", multiVersionCFValue);
            }
        }
    }

    private void bulkMoveAllIssuesToProject(final String projectName)
    {
        navigation.issueNavigator().displayAllIssues();
        oldway_consider_porting.bulkChangeIncludeAllPages();
        oldway_consider_porting.bulkChangeChooseIssuesAll();
        oldway_consider_porting.chooseOperationBulkMove();
        isStepSelectProjectIssueType();
        tester.checkCheckbox(TestBulkMoveIssues.SAME_FOR_ALL, BULK_EDIT_KEY);
        tester.selectOption(TestBulkMoveIssues.TARGET_PROJECT_ID, projectName);
        navigation.clickOnNext();
        isStepSetFields();
    }

    private void completeBulkMoveWizard()
    {
        navigation.clickOnNext();
        oldway_consider_porting.isStepConfirmation();
        navigation.clickOnNext();
    }

    private void isStepSelectProjectIssueType()
    {
        tester.assertTextPresent("Select Projects and Issue Types");
    }

    private void isStepSetFields()
    {
        tester.assertTextPresent("Update Fields for Target Project");
        log("Step Set Fields");
    }

    private void assertDefaultOption(final String fieldId, final String originalId, final String expected)
    {
        assertDefaultOption(fieldId, originalId, null, null, expected);
    }

    private void assertNoOption(final String fieldId)
    {
        text.assertTextPresent(new IdLocator(tester, fieldId + "_container"), "None");
    }

    private void assertDefaultOption(final String fieldId, final String originalId, final String originalName, final String originalProject, final String expected)
    {
        assertDefaultOption(fieldId, originalId, originalName, originalProject, expected, false);
    }

    private void assertDefaultOption(final String fieldId, final String originalId, final String originalName, final String originalProject, final String expected, final boolean isUnknownDisallowed)
    {
        final String uniqueId = fieldId + "_" + originalId;
        if (originalName != null && originalProject != null)
        {
            text.assertTextSequence(new IdLocator(tester, "rowFor_" + uniqueId), originalName, "[", "Project:", originalProject);
        }
        else if (originalName != null)
        {
            text.assertTextPresent(new IdLocator(tester, "rowFor_" + uniqueId), originalName);
        }
        assertEquals(expected, tester.getDialog().getSelectedOption(uniqueId));

        final String[] options = tester.getDialog().getOptionsFor(uniqueId);
        boolean unknownFound = false;
        for (String option : options)
        {
            if (UNKNOWN.equals(option.trim()))
            {
                if (isUnknownDisallowed)
                {
                    fail("Found Unknown option when it shouldn't have been present for input '" + uniqueId + "'");
                }
                else
                {
                    unknownFound = true;
                }
            }
        }

        if (!isUnknownDisallowed && !unknownFound)
        {
            fail("Did not find Unknown option when it should have been present for input '" + uniqueId + "'");
        }
    }
}
