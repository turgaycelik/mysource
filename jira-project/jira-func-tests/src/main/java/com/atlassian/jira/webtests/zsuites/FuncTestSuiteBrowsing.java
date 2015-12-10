package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestAdminMenuWebFragment;
import com.atlassian.jira.webtests.ztests.admin.TestEventTypes;
import com.atlassian.jira.webtests.ztests.admin.TestGeneralConfiguration;
import com.atlassian.jira.webtests.ztests.admin.TestGlobalPermissions;
import com.atlassian.jira.webtests.ztests.admin.TestIntegrityChecker;
import com.atlassian.jira.webtests.ztests.admin.TestModz;
import com.atlassian.jira.webtests.ztests.admin.TestServerId;
import com.atlassian.jira.webtests.ztests.admin.TestServices;
import com.atlassian.jira.webtests.ztests.admin.TestSystemInfoPage;
import com.atlassian.jira.webtests.ztests.admin.TestTimeTrackingAdmin;
import com.atlassian.jira.webtests.ztests.admin.index.TestIndexAdmin;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypeDeleteFieldConfig;
import com.atlassian.jira.webtests.ztests.admin.statuses.TestStatuses;
import com.atlassian.jira.webtests.ztests.admin.trustedapps.TestTrustedApplicationClientVersion0;
import com.atlassian.jira.webtests.ztests.admin.trustedapps.TestTrustedApplicationClientVersion1;
import com.atlassian.jira.webtests.ztests.admin.trustedapps.TestTrustedApplications;
import com.atlassian.jira.webtests.ztests.attachment.TestAttachFile;
import com.atlassian.jira.webtests.ztests.attachment.TestAttachmentsListSorting;
import com.atlassian.jira.webtests.ztests.attachment.TestEditAttachmentSettings;
import com.atlassian.jira.webtests.ztests.attachment.TestImageAttachmentsGallerySorting;
import com.atlassian.jira.webtests.ztests.customfield.TestCustomFieldMultiUserPicker;
import com.atlassian.jira.webtests.ztests.customfield.TestMultiGroupSelector;
import com.atlassian.jira.webtests.ztests.email.TestEmailSubscription;
import com.atlassian.jira.webtests.ztests.email.TestMailServer;
import com.atlassian.jira.webtests.ztests.email.TestSharedEmailSubscription;
import com.atlassian.jira.webtests.ztests.favourite.TestAdjustFavourite;
import com.atlassian.jira.webtests.ztests.filter.TestFilterPageNavigation;
import com.atlassian.jira.webtests.ztests.issue.TestEditIssueForEnterprise;
import com.atlassian.jira.webtests.ztests.issue.TestVoters;
import com.atlassian.jira.webtests.ztests.issue.TestVotersWhenVotePermissionSetToUserPickerCustomField;
import com.atlassian.jira.webtests.ztests.issue.TestWatchers;
import com.atlassian.jira.webtests.ztests.issue.assign.TestAssignToMe;
import com.atlassian.jira.webtests.ztests.issue.history.TestRecentIssueHistory;
import com.atlassian.jira.webtests.ztests.issue.security.TestIssueSecurityLevel;
import com.atlassian.jira.webtests.ztests.issue.security.TestIssueSecurityLevelOnlyCheckedForBrowseProjectPermission;
import com.atlassian.jira.webtests.ztests.misc.Test500Page;
import com.atlassian.jira.webtests.ztests.misc.TestAnnouncementBanner;
import com.atlassian.jira.webtests.ztests.misc.TestBasic;
import com.atlassian.jira.webtests.ztests.misc.TestCustomCreateButtonName;
import com.atlassian.jira.webtests.ztests.misc.TestDateInputValidationOnCreateIssue;
import com.atlassian.jira.webtests.ztests.misc.TestDefaultJiraDataFromInstall;
import com.atlassian.jira.webtests.ztests.misc.TestJohnsonFiltersWhileNotSetup;
import com.atlassian.jira.webtests.ztests.misc.TestLoginActions;
import com.atlassian.jira.webtests.ztests.misc.TestMyJiraHome;
import com.atlassian.jira.webtests.ztests.misc.TestNoSessionOnRoot;
import com.atlassian.jira.webtests.ztests.misc.TestReleaseNotes;
import com.atlassian.jira.webtests.ztests.misc.TestRememberMeCookie;
import com.atlassian.jira.webtests.ztests.misc.TestSessionIdInUrl;
import com.atlassian.jira.webtests.ztests.misc.TestSetup;
import com.atlassian.jira.webtests.ztests.misc.TestShowConstantsHelp;
import com.atlassian.jira.webtests.ztests.misc.TestSignup;
import com.atlassian.jira.webtests.ztests.misc.TestUnsupportedBrowser;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorEncoding;
import com.atlassian.jira.webtests.ztests.project.TestNotificationOptions;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingAggregates;
import com.atlassian.jira.webtests.ztests.user.TestGroupSelector;
import com.atlassian.jira.webtests.ztests.user.TestMultiUserPicker;
import com.atlassian.jira.webtests.ztests.user.TestUserBrowser;
import com.atlassian.jira.webtests.ztests.user.TestViewProfile;
import com.atlassian.jira.webtests.ztests.workflow.TestExcludeResolutionOnTransitions;
import junit.framework.Test;

public class FuncTestSuiteBrowsing extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteBrowsing();

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

    public FuncTestSuiteBrowsing()
    {
        // must run first?
        addTest(TestDefaultJiraDataFromInstall.class);

        addTest(Test500Page.class);
        addTest(TestLoginActions.class);
        addTest(TestMyJiraHome.class);
        addTest(TestNoSessionOnRoot.class);
        addTest(TestUserBrowser.class);
        addTest(TestGroupSelector.class);
        addTest(TestMultiUserPicker.class);
        addTest(TestMultiGroupSelector.class);
        addTest(TestCustomFieldMultiUserPicker.class);
        addTest(TestIssueNavigatorEncoding.class);
        addTest(TestViewProfile.class);
        addTest(TestRememberMeCookie.class);
        addTest(TestSessionIdInUrl.class);
        addTest(TestSystemInfoPage.class);
        addTest(TestTimeTrackingAdmin.class);
        addTest(TestVoters.class);
        addTest(TestVotersWhenVotePermissionSetToUserPickerCustomField.class);
        addTest(TestWatchers.class);
        addTest(TestBasic.class);


        addTest(TestAdjustFavourite.class);
        addTest(TestAdminMenuWebFragment.class);
        addTest(TestAnnouncementBanner.class);
        addTest(TestAssignToMe.class);
        addTest(TestAttachFile.class);
        addTest(TestAttachmentsListSorting.class);
        addTest(TestImageAttachmentsGallerySorting.class);
        addTest(TestCustomCreateButtonName.class);


        addTest(TestEditAttachmentSettings.class);
        addTest(TestEmailSubscription.class);
        addTest(TestEventTypes.class);
        addTest(TestExcludeResolutionOnTransitions.class);
        addTest(TestGeneralConfiguration.class);
        addTest(TestGlobalPermissions.class);
        addTest(TestIndexAdmin.class);
        addTest(TestIntegrityChecker.class);

        // this test must run before JIRA is setup because it tests the
        // johnson event filter for this situation
        addTest(TestJohnsonFiltersWhileNotSetup.class);

        addTest(TestMailServer.class);

        addTest(TestNotificationOptions.class);
        addTest(TestRecentIssueHistory.class);
        addTest(TestStatuses.class);
        addTest(TestReleaseNotes.class);
        addTest(TestServices.class);
        addTest(TestSetup.class);
        addTest(TestSharedEmailSubscription.class);
        addTest(TestShowConstantsHelp.class);
        addTest(TestSignup.class);
        addTest(TestTimeTrackingAggregates.class);
        addTest(TestTrustedApplicationClientVersion0.class);
        addTest(TestTrustedApplicationClientVersion1.class);
        addTest(TestTrustedApplications.class);

        addTest(TestModz.class);
        addTest(TestIssueTypeDeleteFieldConfig.class);
        addTest(TestServerId.class);
        addTest(TestFilterPageNavigation.class);
        addTest(TestEditIssueForEnterprise.class);
        addTest(TestDateInputValidationOnCreateIssue.class);
        addTest(TestIssueSecurityLevel.class);
        addTest(TestIssueSecurityLevelOnlyCheckedForBrowseProjectPermission.class);
        addTest(TestUnsupportedBrowser.class);
    }
}