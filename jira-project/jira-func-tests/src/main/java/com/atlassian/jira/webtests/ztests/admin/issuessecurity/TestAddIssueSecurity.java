package com.atlassian.jira.webtests.ztests.admin.issuessecurity;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.IssueSecurityLevel;
import com.atlassian.jira.functest.framework.admin.IssueSecurityLevel.IssueSecurity;
import com.atlassian.jira.functest.framework.admin.IssueSecuritySchemes;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.HttpUnitDialog;

/**
 * Test web functionality for adding/removing issue securities
 * to/from security levels.
 *
 * @since v4.2
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestAddIssueSecurity extends FuncTestCase
{
    private static final String TEST_SCHEME_NAME = "test-scheme";
    private static final String TEST_SECURITY_LEVEL_NAME = "test-level";
    private static final String ADD_SECURITY_LINK_ID = "add_" + TEST_SECURITY_LEVEL_NAME;

    private static final String SCHEME_ID_REGEX = "schemeId=(\\d)+";
    private static final String LEVEL_ID_REGEX = "security=(\\d)+";

    private static final String ISSUE_SECURITY_PAGE_INDICATOR = "reporter_id";
    private static final String ISSUE_SECURITY_TYPE_RADIO_NAME = "type";
    private static final String ISSUE_SECURITY_TEST_GROUP = "issueSecurityGroup";

    @Override
    public void setUpTest()
    {
        administration.issueSecuritySchemes().newScheme(TEST_SCHEME_NAME, "blah").newLevel(TEST_SECURITY_LEVEL_NAME,
                "blah");
        backdoor.usersAndGroups().addGroup(ISSUE_SECURITY_TEST_GROUP);
        addUserIfNotExists("user1");
        addUserIfNotExists("user2");
    }

    private void addUserIfNotExists(String username)
    {
        if (!administration.usersAndGroups().userExists(username))
        {
            backdoor.usersAndGroups().addUser(username);
        }
    }

    @Override
    public void tearDownTest()
    {
        administration.issueSecuritySchemes().deleteScheme(TEST_SCHEME_NAME);
        administration.usersAndGroups().deleteGroup(ISSUE_SECURITY_TEST_GROUP);
        backdoor.usersAndGroups().deleteUser("user1");
        backdoor.usersAndGroups().deleteUser("user2");
    }

    public void testAttemptAddWithoutIssueSecurityScheme() throws Exception
    {
        goToTestScheme();
        String url = getLinkUrl(ADD_SECURITY_LINK_ID);
        log("Add security link URL=" + url);
        navigation.gotoResource(removeSchemeId(url));
        chooseIssueSecurityType(IssueSecurity.REPORTER.typeId());
        tester.submit();
        assertIsOnAddIssueSecurityPage();
        assertIsError("You must select a scheme to add the issue security to");
    }

    public void testAttemptAddWithoutIssueSecurityLevel() throws Exception
    {
        goToTestScheme();
        String url = getLinkUrl(ADD_SECURITY_LINK_ID);
        log("Add security link URL=" + url);
        navigation.gotoResource(removeLevelId(url));
        chooseIssueSecurityType(IssueSecurity.REPORTER.typeId());
        tester.submit();
        assertIsOnAddIssueSecurityPage();
        assertIsError("You must select an issue security level for this issue security.");
    }

    public void testAttemptAddGivenNoIssueSecurityTypeSelected() throws Exception
    {
        goToAddSecurityIssue();
        tester.setWorkingForm("jiraform");
        tester.submit();
        assertIsOnAddIssueSecurityPage();
        assertIsError("You must select a type for this issue security.");
    }

    public void testAddParameterlessIssueSecurity()
    {
        goToTestLevel().addIssueSecurity(IssueSecurity.REPORTER).addIssueSecurity(IssueSecurity.CURRENT_ASIGNEE)
                .addIssueSecurity(IssueSecurity.PROJECT_LEAD);
        goToAddSecurityIssue();
        assertCannotAddIssueSecurity(IssueSecurity.REPORTER);
        assertCannotAddIssueSecurity(IssueSecurity.CURRENT_ASIGNEE);
        assertCannotAddIssueSecurity(IssueSecurity.PROJECT_LEAD);
    }

    public void testAddParameterizedIssueSecurity()
    {
        goToTestLevel().addIssueSecurity(IssueSecurity.GROUP, ISSUE_SECURITY_TEST_GROUP)
                .addIssueSecurity(IssueSecurity.USER, "user1")
                .addIssueSecurity(IssueSecurity.USER, "user2");
        goToAddSecurityIssue();
        assertCannotAddIssueSecurity(IssueSecurity.GROUP, ISSUE_SECURITY_TEST_GROUP);
        assertCannotAddIssueSecurity(IssueSecurity.USER, "user1");
        assertCannotAddIssueSecurity(IssueSecurity.USER, "user2");
    }

    private IssueSecuritySchemes.IssueSecurityScheme goToTestScheme()
    {
        return administration.issueSecuritySchemes().getScheme(TEST_SCHEME_NAME);
    }

    private IssueSecurityLevel goToTestLevel()
    {
        return goToTestScheme().getLevel(TEST_SECURITY_LEVEL_NAME);
    }

    private void goToAddSecurityIssue()
    {
        goToTestScheme();
        tester.clickLink(ADD_SECURITY_LINK_ID);
    }

    private String getLinkUrl(String linkId) throws Exception
    {
        WebLink link = tester.getDialog().getResponse().getLinkWithID(linkId);
        assertNotNull(link);
        assertNotNull(link.getURLString());
        return link.getURLString();
    }

    private String removeSchemeId(String url)
    {
        assertTrue(url.matches(".*" + SCHEME_ID_REGEX + ".*"));
        return url.replaceAll(SCHEME_ID_REGEX, "");
    }

    private String removeLevelId(String url)
    {
        assertTrue(url.matches(".*" + LEVEL_ID_REGEX + ".*"));
        return url.replaceAll(LEVEL_ID_REGEX, "");
    }


    private void chooseIssueSecurityType(String type)
    {
        assertIsOnAddIssueSecurityPage();
        getDialog().setFormParameter(ISSUE_SECURITY_TYPE_RADIO_NAME, type);
        tester.assertRadioOptionSelected(ISSUE_SECURITY_TYPE_RADIO_NAME, type);
    }

    private void assertCannotAddIssueSecurity(IssueSecurity is)
    {
        chooseIssueSecurityType(is.typeId());
        tester.submit();
        assertIsOnAddIssueSecurityPage();
        assertIsError("This issue security already exists");
    }

    private void assertCannotAddIssueSecurity(IssueSecurity is, String param)
    {
        goToTestLevel().addIssueSecurity(is, param);
        assertIsOnAddIssueSecurityPage();
        assertIsError("This issue security already exists");
    }

    private void assertIsOnAddIssueSecurityPage()
    {
        tester.assertElementPresent(ISSUE_SECURITY_PAGE_INDICATOR);
    }

    private void assertIsError(String msg)
    {
        assertions.getJiraFormAssertions().assertFormErrMsg(msg);
    }

    private HttpUnitDialog getDialog()
    {
        return tester.getDialog();
    }
}
