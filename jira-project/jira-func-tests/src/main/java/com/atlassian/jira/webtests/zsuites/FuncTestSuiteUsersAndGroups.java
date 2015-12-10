package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestAdministerUserLink;
import com.atlassian.jira.webtests.ztests.admin.TestGroupBrowser;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditUserGroups;
import com.atlassian.jira.webtests.ztests.bulk.TestEditNestedGroups;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestConcurrentAttributeUpdates;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestCrowdAuthenticationResource;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestCrowdGroupsResource;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestCrowdSearchResource;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestCrowdServerConfiguration;
import com.atlassian.jira.webtests.ztests.crowd.embedded.TestCrowdUsersResource;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestSingleLevelGroupByReport;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithGroupsAndRoles;
import com.atlassian.jira.webtests.ztests.issue.assign.TestAssignToCurrentUserFunction;
import com.atlassian.jira.webtests.ztests.issue.assign.TestAssignUserProgress;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnMove;
import com.atlassian.jira.webtests.ztests.misc.TestForgotLoginDetails;
import com.atlassian.jira.webtests.ztests.misc.TestXSSInFullName;
import com.atlassian.jira.webtests.ztests.user.TestAddUser;
import com.atlassian.jira.webtests.ztests.user.TestAutoWatches;
import com.atlassian.jira.webtests.ztests.user.TestDeleteGroup;
import com.atlassian.jira.webtests.ztests.user.TestDeleteUserAndPermissions;
import com.atlassian.jira.webtests.ztests.user.TestDeleteUserSharedEntities;
import com.atlassian.jira.webtests.ztests.user.TestEditUserDetails;
import com.atlassian.jira.webtests.ztests.user.TestEditUserGroups;
import com.atlassian.jira.webtests.ztests.user.TestEditUserProjectRoles;
import com.atlassian.jira.webtests.ztests.user.TestExternalUserManagement;
import com.atlassian.jira.webtests.ztests.user.TestGlobalUserPreferences;
import com.atlassian.jira.webtests.ztests.user.TestGroupSelectorPermissions;
import com.atlassian.jira.webtests.ztests.user.TestGroupToRoleMappingTool;
import com.atlassian.jira.webtests.ztests.user.TestNonExistentUsers;
import com.atlassian.jira.webtests.ztests.user.TestShareUserDefault;
import com.atlassian.jira.webtests.ztests.user.TestUserDefaults;
import com.atlassian.jira.webtests.ztests.user.TestUserFormat;
import com.atlassian.jira.webtests.ztests.user.TestUserGroupPicker;
import com.atlassian.jira.webtests.ztests.user.TestUserHistory;
import com.atlassian.jira.webtests.ztests.user.TestUserHover;
import com.atlassian.jira.webtests.ztests.user.TestUserManagement;
import com.atlassian.jira.webtests.ztests.user.TestUserNameIsEncoded;
import com.atlassian.jira.webtests.ztests.user.TestUserNavigationBarWebFragment;
import com.atlassian.jira.webtests.ztests.user.TestUserProfile;
import com.atlassian.jira.webtests.ztests.user.TestUserProperties;
import com.atlassian.jira.webtests.ztests.user.TestUserRememberMeCookies;
import com.atlassian.jira.webtests.ztests.user.TestUserSessions;
import com.atlassian.jira.webtests.ztests.user.TestUserVotes;
import com.atlassian.jira.webtests.ztests.user.TestUserWatches;
import com.atlassian.jira.webtests.ztests.user.TestViewGroup;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnChangeHistory;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnComments;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnComponent;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnDashboards;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnFilters;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnIssuePrintableView;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnIssueXmlView;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnIssues;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnPermissions;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnProfiles;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnProject;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnProjectRoles;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnSearch;
import com.atlassian.jira.webtests.ztests.user.rename.TestUserRenameOnWorkflow;

import junit.framework.Test;

/**
 * A suite of test related to Users and Groups
 *
 * @since v4.0
 */
public class FuncTestSuiteUsersAndGroups extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteUsersAndGroups();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteUsersAndGroups()
    {
        addTest(TestUserNavigationBarWebFragment.class);
        addTest(TestExternalUserManagement.class);
        addTest(TestEditUserProjectRoles.class);
        addTest(TestBulkEditUserGroups.class);
        addTest(TestUserManagement.class);
        addTest(TestUserProfile.class);
        addTest(TestUserHover.class);
        addTest(TestUserWatches.class);
        addTest(TestAutoWatches.class);
        addTest(TestUserVotes.class);
        addTest(TestAssignUserProgress.class);
        addTest(TestAdministerUserLink.class);
        addTest(TestDeleteUserSharedEntities.class);
        addTest(TestUserGroupPicker.class);
        addTest(TestDeleteUserAndPermissions.class);
        addTest(TestGlobalUserPreferences.class);
        addTest(TestEditUserDetails.class);
        addTest(TestPromptUserForSecurityLevelOnMove.class);
        addTest(TestAssignToCurrentUserFunction.class);
        addTest(TestShareUserDefault.class);
        addTest(TestUserFormat.class);
        addTest(TestNonExistentUsers.class);
        addTest(TestUserRenameOnIssues.class);
        addTest(TestUserRenameOnWorkflow.class);
        addTest(TestUserRenameOnComments.class);
        addTest(TestUserRenameOnChangeHistory.class);
        addTest(TestUserRenameOnIssuePrintableView.class);
        addTest(TestUserRenameOnIssueXmlView.class);
        addTest(TestUserRenameOnPermissions.class);
        addTest(TestUserRenameOnProfiles.class);
        addTest(TestUserRenameOnProjectRoles.class);
        addTest(TestUserRenameOnProject.class);
        addTest(TestUserRenameOnDashboards.class);
        addTest(TestUserRenameOnFilters.class);
        addTest(TestUserRenameOnSearch.class);
        addTest(TestUserRenameOnComponent.class);
        addTest(TestDeleteGroup.class);
        addTest(TestGroupToRoleMappingTool.class);
        addTest(TestSingleLevelGroupByReport.class);
        addTest(TestEditNestedGroups.class);
        addTest(TestIssueSecurityWithGroupsAndRoles.class);
        addTest(TestGroupSelectorPermissions.class);
        addTest(TestXSSInFullName.class);
        addTest(TestAddUser.class);
        addTest(TestUserProperties.class);
        addTest(TestUserNameIsEncoded.class);
        addTest(TestEditUserGroups.class);
        addTest(TestGroupBrowser.class);
        addTest(TestViewGroup.class);
        addTest(TestForgotLoginDetails.class);
        addTest(TestUserDefaults.class);
        addTest(TestUserSessions.class);
        addTest(TestUserRememberMeCookies.class);
        addTest(TestUserHistory.class);

        addTest(TestConcurrentAttributeUpdates.class);
        addTest(TestCrowdAuthenticationResource.class);
        addTest(TestCrowdGroupsResource.class);
        addTest(TestCrowdSearchResource.class);
        addTest(TestCrowdServerConfiguration.class);
        addTest(TestCrowdUsersResource.class);
    }
}
