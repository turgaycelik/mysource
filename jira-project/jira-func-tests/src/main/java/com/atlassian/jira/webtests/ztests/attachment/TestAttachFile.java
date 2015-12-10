package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;

/**
 * Functional test for attaching files.
 */
@WebTest({Category.FUNC_TEST, Category.ATTACHMENTS })
public class TestAttachFile extends FuncTestCase
{
    private static final String LOGIN = "log in";
    private static final String SIGNUP = "sign up";
    private static final String ANONYMOUS_USERS_GROUP_NAME = "";
    private static final String JIRA_USERS_GROUP_NAME = "jira-users";
    private static final int CREATE_ATTACHMENTS_PERMISSION_ID = 19;

    public void setUpTest()
    {
        super.setUpTest();

        //add full anonymous permissions
        administration.restoreData("TestFullAnonymousPermissions.xml");
        administration.attachments().enable();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void tearDownTest()
    {
        //disable js in case tests fail without disabling it
        HttpUnitOptions.setScriptingEnabled(false);
        super.tearDownTest();
    }

    /**
     * Tests that the attachment page is able to be accessed when logged in (using default permission scheme)
     */
    public void testAttachFileLoggedIn()
    {
        disallowAnonymousAttachmentCreation();
        navigation.issue().gotoIssue("MKY-2");
        tester.clickLink("attach-file");
        tester.assertElementPresent("attach-file-submit");
    }

    public void testAttachNoFileError()
    {
        navigation.issue().gotoIssue("MKY-2");
        tester.clickLink("attach-file");
        text.assertTextNotPresent(locator.page(), "Errors");
        text.assertTextNotPresent(locator.page(), "Please indicate the file you wish to upload");
        tester.setWorkingForm("attach-file");
        tester.submit();
        text.assertTextPresent(locator.page(), "Please indicate the file you wish to upload");
    }

    /**
     * Tests that the attachment link is missing from the issue and the URL redirects to the security breach page if
     * anonymous access is disabled and the user is not logged in
     */
    public void testAttachFileLoggedOutNoAnonymousPermission()
    {
        disallowAnonymousAttachmentCreation();
        navigation.logout();

        //attempt to create attachment via UI
        navigation.issue().gotoIssue("MKY-2");
        tester.assertLinkNotPresent("attach-file");

        //attempt to access URL directly (working id)
        tester.gotoPage("/secure/AttachFile!default.jspa?id=10011");
        text.assertTextPresent(locator.page(), "You do not have permission to create attachments for this issue.");
        text.assertTextPresent(locator.page(), "It seems that you have tried to perform an operation which you are not permitted to perform.");
        tester.assertLinkPresentWithText(LOGIN);
        tester.assertLinkPresentWithText(SIGNUP);
        tester.assertElementNotPresent("attach-file-submit");

        //attempt to access URL directly (non existent id)
        tester.gotoPage("/secure/AttachFile!default.jspa?id=99999");
        text.assertTextNotPresent(locator.page(), "You do not have permission to create attachments for this issue.");
        text.assertTextNotPresent(locator.page(), "It seems that you have tried to perform an operation which you are not permitted to perform.");
        tester.assertElementNotPresent("attach-file-submit");
    }

    /**
     * Tests that the user can attach files anonymously if the anonymous 'create attachment' permission is enabled
     */
    public void testAttachFileLoggedOutWithAnonymousPermission()
    {
        navigation.logout();
        navigation.issue().gotoIssue("MKY-2");
        tester.clickLink("attach-file");
        tester.gotoPage("/secure/AttachFile!default.jspa?id=10011");
        tester.assertElementPresent("attach-file-submit");
        text.assertTextNotPresent(locator.page(), "You are not logged in, and do not have the permissions required to attach a file on the selected issue as a guest.");

        //attempt to access URL directly (non existant id)
        tester.gotoPage("/secure/AttachFile!default.jspa?id=99999");
        text.assertTextNotPresent(locator.page(), "You do not have permission to create attachments for this issue.");
        text.assertTextNotPresent(locator.page(), "You are not logged in, and do not have the permissions required to attach a file on the selected issue as a guest.");
        tester.assertElementNotPresent("attach-file-submit");
    }

    /**
     * Tests that a user who is logged in but does not have create attachment permission cannot attach files
     */
    public void testAttachFileLoggedInWithoutPermission()
    {
        disallowAnyoneAttachmentCreation();

        //attempt to create attachment via UI
        navigation.issue().gotoIssue("MKY-2");
        tester.assertLinkNotPresent("attach-file");

        //attempt to access URL directly (working id)
        tester.gotoPage("/secure/AttachFile!default.jspa?id=10011");
        text.assertTextPresent(locator.page(), "You do not have permission to create attachments for this issue.");
        tester.assertElementNotPresent("attach-file-submit");

        //attempt to access URL directly (non existant id)
        tester.gotoPage("/secure/AttachFile!default.jspa?id=99999");
        text.assertTextNotPresent(locator.page(), "You do not have permission to create attachments for this issue.");
        text.assertTextNotPresent(locator.page(), "You are not logged in, and do not have the permissions required to attach a file on the selected issue as a guest.");
        tester.assertElementNotPresent("attach-file-submit");
    }

    /**
     * Remove anonymous permission for creating attachments
     */
    private void disallowAnonymousAttachmentCreation()
    {
        administration.permissionSchemes().defaultScheme().
                removePermission(CREATE_ATTACHMENTS_PERMISSION_ID, ANONYMOUS_USERS_GROUP_NAME);
    }

    private void disallowUsersAttachmentCreation()
    {
        administration.permissionSchemes().defaultScheme().
                removePermission(CREATE_ATTACHMENTS_PERMISSION_ID, JIRA_USERS_GROUP_NAME);
    }

    /**
     * Remove all permission for creating attachments
     */
    private void disallowAnyoneAttachmentCreation()
    {
        disallowAnonymousAttachmentCreation();
        disallowUsersAttachmentCreation();
    }
}
