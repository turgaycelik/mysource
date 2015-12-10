package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.Permissions;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkMoveIssues extends BulkChangeIssues
{
    protected static final String ERROR_MOVE_PERMISSION = "You do not have the permission to move one or more of the selected issues";

    private static final String HSP1 = "HSP-1";
    private static final String HSP2 = "HSP-2";
    private static final String HSP3 = "HSP-3";
    private static final String HSP4 = "HSP-4";

    private static final String MKY1 = "MKY-1";

    public static final String SAME_FOR_ALL = "sameAsBulkEditBean";
    private static final String MONKEY_PID_OPTION = "10000_1_pid";
    protected static final String TARGET_PROJECT_ID = MONKEY_PID_OPTION;

    private IssueClient issueClient = null;
    private Component hspComponentOne;
    private Component hspComponentTwo;
    private Component hspComponentThree;
    private Version hsp1dot0;
    private Version hsp2dot0;
    private Version hsp3dot0;
    private Version mky1dot0;
    private Version mky1dot1;
    private Version mky1dot2;
    private Component mkyWrench;
    private Component hspWrench;

    public TestBulkMoveIssues(final String name)
    {
        super(name);
    }

    @Override
    public void setUp()
    {
        super.setUp();
        backdoor.restoreBlankInstance();
        issueClient = new IssueClient(getEnvironmentData());

        hspComponentOne = createComponent("ONE", "component one", PROJECT_HOMOSAP_KEY);
        hspComponentTwo = createComponent("TWO", "component two", PROJECT_HOMOSAP_KEY);
        hspComponentThree = createComponent("THREE", "component three", PROJECT_HOMOSAP_KEY);

        hspWrench = createComponent("wrench", "lowercase", PROJECT_HOMOSAP_KEY);
        mkyWrench = createComponent("WRENCH", "uppercase", PROJECT_MONKEY_KEY);

        hsp1dot0 = createVersion("1.0", "one", PROJECT_HOMOSAP_KEY);
        hsp2dot0 = createVersion("2.0", "two", PROJECT_HOMOSAP_KEY);
        hsp3dot0 = createVersion("3.0", "three", PROJECT_HOMOSAP_KEY);

        mky1dot0 = createVersion("1.0", "first", PROJECT_MONKEY_KEY);
        mky1dot1 = createVersion("1.1", "second", PROJECT_MONKEY_KEY);
        mky1dot2 = createVersion("1.2", "third", PROJECT_MONKEY_KEY);
    }

    /*
     * test that error is shown when move is not permitted.
     * details unit tested in: com.atlassian.jira.bulkedit.operation.TestBulkMoveOperation
     */
    public void testErrorVisibleWhenOperationNotPermitted()
    {
        log("Bulk Move - move operation is not available without the move permission");

        //given:
        setupIssues(1);
        backdoor.permissionSchemes().removeGroupPermission(DEFAULT_PERM_SCHEME_ID, MOVE_ISSUE, Groups.DEVELOPERS);

        //when:
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues();

        //then:
        tester.assertTextPresent(ERROR_MOVE_PERMISSION);
    }

    public void testNavigatorCanShowMovedIssue()
    {
        log("Bulk Move - navigator can follow changed issue key.");

        //given:
        setupIssues(1);

        //when:
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(PROJECT_MONKEY)
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        navigation.issue().gotoIssue(HSP1);

        //then:
        assertions.getViewIssueAssertions().assertOnViewIssuePage(MKY1);
    }

    public void testBulkMoveMatchesComponentsOrVersionsWithChangedCase() throws Exception
    {
        log("Bulk Move - matching fields and remembering decisions for target context");

        // given:
        setupIssues(4);
        updateIssue(HSP1, hspWrench, hsp1dot0);
        updateIssue(HSP2, hspWrench, hsp2dot0);
        updateIssue(HSP3, hspWrench, hsp1dot0, ISSUE_TYPE_NEWFEATURE);
        updateIssue(HSP4, hspWrench, hsp3dot0, ISSUE_TYPE_NEWFEATURE);

        // when:
        navigation.issueNavigator().displayAllIssues();
        final BulkChangeWizard wizard = navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForEach(2, PROJECT_MONKEY);

        // then:
        log("Bulk Move - checking matched fields for bug type");
        // first issue type: bug:
        // component is not matched, version 2.0 isn't either, but version 1.0 is:
        tester.assertFormElementEquals(targetComponentFieldName(hspWrench), UNKNOWN_ID);
        tester.assertFormElementEquals(targetFixVersionName(hsp1dot0), mky1dot0.id.toString());
        tester.assertFormElementEquals(targetFixVersionName(hsp2dot0), UNKNOWN_ID);
        tester.assertFormElementNotPresent(targetFixVersionName(hsp3dot0));
        // ... but can be matched:
        wizard.setFieldValue(targetFixVersionName(hsp2dot0), mky1dot1.id.toString())
                .setFieldValue(targetComponentFieldName(hspWrench), mkyWrench.id.toString())
                .finaliseFields();

        log("Bulk Move - checking matched fields for new feature type");
        // second issue type: new feature:
        // component should be remembered, version 1.0 also matched, but version 2.0 should not be:
        tester.assertFormElementEquals(targetComponentFieldName(hspWrench), mkyWrench.id.toString());
        tester.assertFormElementEquals(targetFixVersionName(hsp1dot0), mky1dot0.id.toString());
        tester.assertFormElementNotPresent(targetFixVersionName(hsp2dot0));
        tester.assertFormElementEquals(targetFixVersionName(hsp3dot0), UNKNOWN_ID);
        // ... but can be matched:
        wizard.setFieldValue(targetFixVersionName(hsp3dot0), mky1dot1.id.toString())
                .finaliseFields();

        Assert.assertThat("all selections should be accepted.", wizard.getState(), Matchers.is(BulkChangeWizard.WizardState.CONFIRMATION));
    }

    // Bulk Moving issues from HSP and MKY to just MKY. The existing MKY issues should not be touched and not have their
    // values mapped at all. The HSP issue needs the assignee changed and we change it to Jon and select "retain".
    public void testBulkMoveWithAssigneeRetain() throws Exception
    {
        log("Bulk Move - checking retained assignee");

        // set up some issues
        try
        {
            // given:
            // setup project monkey so that FRED cannot be assigned to issues there, but JON can:
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            final String monkeyUsers = "monkey-users";
            backdoor.usersAndGroups().addGroup(monkeyUsers);

            final Long monkeyPermissionScheme = backdoor.permissionSchemes().copyDefaultScheme("test-scheme");
            backdoor.permissionSchemes().removeGroupPermission(monkeyPermissionScheme, Permissions.ASSIGNABLE_USER, "jira-developers");
            backdoor.permissionSchemes().addGroupPermission(monkeyPermissionScheme, Permissions.ASSIGNABLE_USER, monkeyUsers);

            final String jon = "Jon";
            backdoor.usersAndGroups().addUser(jon);
            backdoor.usersAndGroups().addUserToGroup(jon, monkeyUsers);
            backdoor.usersAndGroups().addUserToGroup(ADMIN_USERNAME, monkeyUsers);
            backdoor.usersAndGroups().addUserToGroup(FRED_USERNAME, "jira-developers");

            backdoor.project().setPermissionScheme(backdoor.project().getProjectId(PROJECT_MONKEY_KEY), monkeyPermissionScheme);

            backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "first issue", FRED_USERNAME); // HSP1
            backdoor.issues().createIssue(PROJECT_MONKEY_KEY, "second issue");

            log("Bulk Move - bulk moving with retain");
            // set up the bulk move - HSPs and MKYs are going to MKY
            navigation.issueNavigator().displayAllIssues();
            BulkChangeWizard wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                    .selectAllIssues()
                    .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                    .chooseTargetContextForAll(PROJECT_MONKEY);

            // choose Jon as the assignee, because Fred cannot be kept, and set the assignee to retain
            tester.selectOption("assignee", jon);
            tester.checkCheckbox("retain_assignee");

            // complete the wizard
            wizard.finaliseFields();
            Assert.assertThat("all selections should be accepted.", wizard.getState(), Matchers.is(BulkChangeWizard.WizardState.CONFIRMATION));
            wizard.complete();

            waitAndReloadBulkOperationProgressPage();

            // check the values of the fields in the new issues

            // MKY-1 should be unchanged
            navigation.issue().viewIssue(MKY1);
            assertions.getViewIssueAssertions().assertAssignee(ADMIN_FULLNAME);

            // first issue changed to Jon
            navigation.issue().viewIssue(backdoor.issueNavControl().getIssueKeyForSummary("first issue"));
            assertions.getViewIssueAssertions().assertAssignee(jon);

            log("Bulk Move - bulk moving without retain");
            // now, try with retain:
            backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "third issue", FRED_USERNAME); // HSP2
            navigation.issueNavigator().displayAllIssues();
            wizard = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                    .selectAllIssues()
                    .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                    .chooseTargetContextForAll(PROJECT_MONKEY);

            // choose Jon as the assignee and set the assignee to retain
            tester.selectOption("assignee", jon);

            // complete the wizard
            wizard.finaliseFields();
            Assert.assertThat("all selections should be accepted.", wizard.getState(), Matchers.is(BulkChangeWizard.WizardState.CONFIRMATION));
            wizard.complete();

            waitAndReloadBulkOperationProgressPage();

            // MKY-1 should be unchanged
            navigation.issue().viewIssue(MKY1);
            assertions.getViewIssueAssertions().assertAssignee(jon);

            // last inserted issue also assigned to jon
            navigation.issue().viewIssue(backdoor.issueNavControl().getIssueKeyForSummary("third issue"));
            assertions.getViewIssueAssertions().assertAssignee(jon);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
    }

    public void testMoveSTDComponentsAndVersionsRequiredFailure()
    {
        log("Bulk Move - STD - components and versions required - failure");
        setupIssues(2);
        backdoor.versions().delete(mky1dot0.id.toString());
        backdoor.versions().delete(mky1dot1.id.toString());
        backdoor.versions().delete(mky1dot2.id.toString());
        backdoor.components().delete(mkyWrench.id.toString());

        setRequiredFields();
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(PROJECT_MONKEY)
                .finaliseFields();
        assertErrorMsgFieldRequired(COMPONENTS_FIELD_ID, PROJECT_MONKEY, "components");
        assertErrorMsgFieldRequired(FIX_VERSIONS_FIELD_ID, PROJECT_MONKEY, "versions");
        assertErrorMsgFieldRequired(AFFECTS_VERSIONS_FIELD_ID, PROJECT_MONKEY, "versions");
    }

    public void testDontRetainRequiredComponentAndVersions()
    {
        log("Bulk Move - STD - No retain, components and versions Required, Select new values");

        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue that stays 1");
        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue that stays 2");
        backdoor.issues().createIssue(PROJECT_MONKEY_KEY, "issue to move");
        updateIssue(HSP1, hspComponentOne, hsp2dot0);
        updateIssue(HSP2, hspComponentTwo, hsp3dot0);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", HSP1, "component", hspComponentOne.name, "fixVersion", hsp2dot0.name), null, HSP1);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", HSP2, "component", hspComponentTwo.name, "fixVersion", hsp3dot0.name), null, HSP2);

        setRequiredFields();

        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(PROJECT_HOMOSAP)
                .setFieldValue(targetFieldName(FIELD_COMPONENTS, "-1"), hspComponentThree.id.toString())
                .setFieldValue(targetFieldName(FIELD_FIX_VERSIONS, "-1"), hsp1dot0.id.toString())
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        navigation.issue().gotoIssue(HSP1);
        assertions.getViewIssueAssertions().assertComponents(hspComponentOne.name);
        assertions.getViewIssueAssertions().assertFixVersions(hsp2dot0.name);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_ONE);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", HSP1, "component", hspComponentOne.name, "fixVersion", hsp2dot0.name, "version", VERSION_NAME_ONE), null, HSP1);


        navigation.issue().gotoIssue(HSP2);
        assertions.getViewIssueAssertions().assertComponents(hspComponentTwo.name);
        assertions.getViewIssueAssertions().assertFixVersions(hsp3dot0.name);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_ONE);
        assertIndexedFieldCorrect("//item", EasyMap.build("key", HSP2, "component", hspComponentTwo.name, "fixVersion", hsp3dot0.name, "version", VERSION_NAME_ONE), null, HSP2);

        //issue key changed...
        navigation.issue().gotoIssue(MKY1);
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponents(hspComponentThree.name);
        assertions.getViewIssueAssertions().assertFixVersions(hsp1dot0.name);
        assertions.getViewIssueAssertions().assertAffectsVersions(VERSION_NAME_ONE);
        //assert item was moved and the key has been updated in the index
        assertIndexedFieldCorrect("//item", EasyMap.build("key", HSP3, "component", hspComponentThree.name, "fixVersion", hsp1dot0.name, "version", VERSION_NAME_ONE), null, HSP3);

    }

    public void testDontRetainNotRequiredComponentAndVersions()
    {
        log("Bulk Move - STD - Dont Retain, components and versions Not Required, Select new values");

        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue to move 1");
        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue to move 2");
        backdoor.issues().createIssue(PROJECT_MONKEY_KEY, "issue that stays");
        updateIssue(HSP1, hspComponentOne, hsp2dot0);
        updateIssue(HSP2, hspComponentTwo, hsp3dot0);
        updateIssue(MKY1, mkyWrench, mky1dot1);

        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(PROJECT_MONKEY)
                .setFieldValue(targetComponentFieldName(hspComponentOne), mkyWrench.id.toString())
                .setFieldValue(targetFixVersionName(hsp2dot0), mky1dot2.id.toString())
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        // this issue was not touched by transformation
        navigation.issue().gotoIssue(MKY1);
        assertions.getViewIssueAssertions().assertComponents(mkyWrench.name);
        assertions.getViewIssueAssertions().assertFixVersions(mky1dot1.name);

        // this issue should have its versions/components changed:
        navigation.issue().gotoIssue(HSP1);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponents(mkyWrench.name);
        assertions.getViewIssueAssertions().assertFixVersions(mky1dot2.name);

        // this issue should have its versions/components cleared:
        navigation.issue().gotoIssue(HSP2);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponentsNone();
        assertions.getViewIssueAssertions().assertFixVersionsNone();
    }


    public void testDontRetainNotRequiredNotSelectedComponentAndVersions()
    {
        log("Bulk Move - STD - Dont Retain, components and versions Not Required, Dont Select new values");

        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue to move 1");
        backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, "issue to move 2");
        backdoor.issues().createIssue(PROJECT_MONKEY_KEY, "issue that stays");
        updateIssue(HSP1, hspComponentOne, hsp2dot0);
        updateIssue(HSP2, hspComponentTwo, hsp3dot0);

        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(PROJECT_MONKEY)
                .finaliseFields()
                .complete();

        waitAndReloadBulkOperationProgressPage();

        // this issue was not touched by transformation
        navigation.issue().gotoIssue(MKY1);
        assertions.getViewIssueAssertions().assertComponentsNone();
        assertions.getViewIssueAssertions().assertFixVersionsNone();

        // this issue should have its versions/components cleared:
        navigation.issue().gotoIssue(HSP1);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponentsNone();
        assertions.getViewIssueAssertions().assertFixVersionsNone();

        // this issue should have its versions/components cleared:
        navigation.issue().gotoIssue(HSP2);
        assertLinkNotPresentWithText(PROJECT_HOMOSAP);
        assertLinkPresentWithText(PROJECT_MONKEY);
        assertions.getViewIssueAssertions().assertComponentsNone();
        assertions.getViewIssueAssertions().assertFixVersionsNone();
    }

    private void setupIssues(final int howMany)
    {
        for (int i = 0; i < howMany; i++)
        {
            final String summary = Integer.toBinaryString(i);
            assertNotNull(backdoor.issues().createIssue(PROJECT_HOMOSAP_KEY, summary).id);
        }
    }

    private String targetComponentFieldName(final Component forComponent) {
        return targetFieldName(FIELD_COMPONENTS, forComponent.id.toString());
    }

    private String targetFixVersionName(final Version forVersion) {
        return targetFieldName(FIELD_FIX_VERSIONS, forVersion.id.toString());
    }

    private String targetFieldName(final String fieldTypeName, final String id) {
        return StringUtils.join(new String[] { fieldTypeName, id }, "_");
    }


    private Version createVersion(final String name, final String description, final String project)
    {
        return backdoor.versions().create(new Version().name(name).description(description).project(project));
    }

    private Component createComponent(final String name, final String description, final String projectKey)
    {
        return backdoor.components().create(new Component().name(name).description(description).project(projectKey));
    }

    private void updateIssue(final String issueKey, final Component component, final Version version)
    {
        updateIssue(issueKey, component, version, null);
    }

    private void updateIssue(final String issueKey, final Component component, final Version version, final String issueType)
    {
        final IssueFields fields = new IssueFields()
                .fixVersions(ResourceRef.withId(version.id.toString()))
                .components(ResourceRef.withId(component.id.toString()));
        if(issueType != null)
        {
            fields.issueType(ResourceRef.withName(issueType));
        }
        issueClient.update(issueKey, new IssueUpdateRequest().fields(fields));
    }
}
