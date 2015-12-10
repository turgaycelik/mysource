package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestCreateIssueViaDirectLink extends JIRAWebTest
{
    private static final String PROJECT_STD_NAME = "standardProject";
    private static final String PROJECT_ANY_NAME = "anyone";
    private static final String PROJECT_DEV_NAME = "developerProject";

    private static final String ERROR_MSG_NO_PERM_AND_NOT_LOGGED_IN = "You are not logged in, and do not have the permissions required to create an issue in this project as a guest.";
    private static final String ERROR_MSG_INVALID_PID = "You have not selected a valid project to create an issue in.";
    private static final String ERROR_MSG_NO_PERMISSION_TO_CREATE_ISSUE = "You do not have permission to create issues in this project.";

    private static final String SUMMARY_PREFIX = "test create issue via direct link";

    private static final String CUSTOM_FIELD_TEXTAREA_ID = "10000";
    private static final String CUSTOM_FIELD_DATEPICKER_ID = "10001";
    private static final String CUSTOM_FIELD_MULTICHECKBOX_ID = "10002";
    private static final String CUSTOM_FIELD_USERPICKER_ID = "10003";
    private static final String CUSTOM_FIELD_FLOAT_ID = "10004";

    private static final String CUSTOM_FIELD_TEXTAREA = "textarea";
    private static final String CUSTOM_FIELD_DATEPICKER = "datepicker";
    private static final String CUSTOM_FIELD_MULTICHECKBOX = "multicheckbox";
    private static final String CUSTOM_FIELD_USERPICKER = "userpicker";
    private static final String CUSTOM_FIELD_FLOAT = "numberfield";

    private static Map pidToName;
    public static final Map issueIdToName;
    private static final String LOGIN = "log in";

    static
    {
        issueIdToName = new HashMap();
        issueIdToName.put(ISSUE_BUG, ISSUE_TYPE_BUG);
        issueIdToName.put(ISSUE_NEWFEATURE, ISSUE_TYPE_NEWFEATURE);
        issueIdToName.put(ISSUE_TASK, ISSUE_TYPE_TASK);
        issueIdToName.put(ISSUE_IMPROVEMENT, ISSUE_TYPE_IMPROVEMENT);
    }

    public TestCreateIssueViaDirectLink(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestCreateIssueViaDirectLink.xml");
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");

        //set project ids
        pidToName = new HashMap();
        pidToName.put("10000", PROJECT_STD_NAME);
        pidToName.put("10001", PROJECT_DEV_NAME);
        pidToName.put("10002", PROJECT_ANY_NAME);
    }

    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        super.tearDown();
    }

    public void testCreateIssueViaDirectLink()
    {
        //logged in, valid params - successful
        directIssueCreationLoggedInValidParams();

        //logged in, no pid set - Invalid
        directIssueCreationLoggedInNoPid();

        //logged in, invalid pid set - Invalid
        directIssueCreationLoggedInInvalidPid();

        //logged in, no permission to create in project - Invalid
        directIssueCreationLoggedInNoPerm();

        //not logged in, valid params, no permission to create in project as guest (so login and create) - successful
        directIssueCreationNotLoggedInNoGuestPerm();

        //not logged in, invalid pid set - Invalid
        directIssueCreationNotLoggedInInvalidPid();

        //not logged in, no pid set - Invalid
        directIssueCreationNotLoggedInNoPid();

        //not logged in, valid params, public project - Successful
        directIssueCreationNotLoggedInGuestPerm();
    }

    private void directIssueCreationNotLoggedInGuestPerm()
    {
        logout();
        Map outputParams = createIssueViaDirectLink("10002", ISSUE_NEWFEATURE, SUMMARY_PREFIX + 1, true);
        submit("Create");
        assertCreatedIssueDetails(outputParams);
    }

    private void directIssueCreationNotLoggedInNoPid()
    {
        logout();
        createIssueViaDirectLink(null, ISSUE_BUG, SUMMARY_PREFIX + 2);
        assertTextPresent(ERROR_MSG_NO_PERM_AND_NOT_LOGGED_IN);
    }

    private void directIssueCreationNotLoggedInInvalidPid()
    {
        logout();
        createIssueViaDirectLink("invalid", ISSUE_NEWFEATURE, SUMMARY_PREFIX + 3);
        assertTextPresent(ERROR_MSG_NO_PERM_AND_NOT_LOGGED_IN);
    }

    private void directIssueCreationNotLoggedInNoGuestPerm()
    {
        logout();
        Map outputParams = createIssueViaDirectLink("10001", ISSUE_IMPROVEMENT, SUMMARY_PREFIX + 4);
        assertTextPresent("You are not logged in, and do not have the permissions required to create an issue in this project as a guest");
        assertLinkPresentWithText(LOGIN);
        clickLinkWithText(LOGIN);
        setFormElement("os_username", ADMIN_USERNAME);
        checkCheckbox("os_password", ADMIN_PASSWORD);
        setWorkingForm("login-form");
        submit();
        assertCreateIssueDetailsForm(getInputParamsForAssertion("10001", ISSUE_IMPROVEMENT, SUMMARY_PREFIX + 4));
        submit("Create");
        assertCreatedIssueDetails(outputParams);
    }

    private void directIssueCreationLoggedInNoPerm()
    {
        login(BOB_USERNAME, BOB_PASSWORD);
        createIssueViaDirectLink("10001", ISSUE_TASK, SUMMARY_PREFIX + 5);
        assertTextPresent(ERROR_MSG_NO_PERMISSION_TO_CREATE_ISSUE);
    }

    private void directIssueCreationLoggedInInvalidPid()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        createIssueViaDirectLink("invalid", ISSUE_NEWFEATURE, SUMMARY_PREFIX + 6);
        assertTextPresent(ERROR_MSG_INVALID_PID);
    }

    private void directIssueCreationLoggedInNoPid()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        createIssueViaDirectLink(null, ISSUE_IMPROVEMENT, SUMMARY_PREFIX + 7);
        assertTextPresent(ERROR_MSG_INVALID_PID);
    }

    private void directIssueCreationLoggedInValidParams()
    {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        Map outputParams = createIssueViaDirectLink("10000", ISSUE_BUG, SUMMARY_PREFIX + 8, true);
        submit("Create");
        assertCreatedIssueDetails(outputParams);
    }

    private Map createIssueViaDirectLink(String pid, String issuetypeId, String summary)
    {
        return createIssueViaDirectLink(pid, issuetypeId, summary, false);
    }

    /**
     * Populate the param map with constant values except the pid, issuetype and summary
     */
    private Map createIssueViaDirectLink(String pid, String issuetypeId, String summary, boolean assertParams)
    {
        Map inputParams = getInputParams(pid, issuetypeId, summary);
        Map inputParamsForAssertion = getInputParamsForAssertion(pid, issuetypeId, summary);
        createIssueViaDirectLink(inputParams, inputParamsForAssertion, assertParams);
        return getOutputParams(pid, summary, issuetypeId);
    }

    private Map getInputParams(String pid, String issuetypeId, String summary)
    {
        //changes here will be needed in getOutputParams to match
        Map inputParams = new HashMap();
        inputParams.put("pid", pid);
        inputParams.put("issuetype", issuetypeId);
        inputParams.put("summary", summary);
        inputParams.put("reporter", ADMIN_USERNAME);
        inputParams.put("assignee", ADMIN_USERNAME);
        inputParams.put("description", "description");
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_TEXTAREA_ID, "this is a textarea");
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_DATEPICKER_ID, "6/Dec/05");
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_MULTICHECKBOX_ID, "10000"); //can only select one checkbox cause its using a map
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_USERPICKER_ID, BOB_USERNAME);
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_FLOAT_ID, "123456789");
        return inputParams;
    }

    private Map getInputParamsForAssertion(String pid, String issuetypeId, String summary)
    {
        //changes here will be needed in getOutputParams to match
        Map inputParams = new HashMap();
        inputParams.put("pid", pid);
        inputParams.put("issuetype", issuetypeId);
        inputParams.put("summary", summary);
        inputParams.put("reporter", ADMIN_USERNAME);
        inputParams.put("assignee", ADMIN_USERNAME);
        inputParams.put("description", "\ndescription"); // new line solves JRA-11257
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_TEXTAREA_ID, "\nthis is a textarea"); // new line solves JRA-11257
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_DATEPICKER_ID, "6/Dec/05");
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_MULTICHECKBOX_ID, "10000"); //can only select one checkbox cause its using a map
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_USERPICKER_ID, BOB_USERNAME);
        inputParams.put(CUSTOM_FIELD_PREFIX + CUSTOM_FIELD_FLOAT_ID, "123456789");
        return inputParams;
    }

    private Map getOutputParams(String pid, String summary, String issuetypeId)
    {
        //these outputParams are used to check they are displayed properly in the view issue page (once issue is created)
        Map outputParams = new HashMap();
        outputParams.put(pidToName.get(pid), summary);
        outputParams.put("Type", issueIdToName.get(issuetypeId));
        outputParams.put("Reporter", ADMIN_USERNAME);
        outputParams.put("Description", "description");
        outputParams.put(CUSTOM_FIELD_TEXTAREA, "this is a textarea");
        outputParams.put(CUSTOM_FIELD_DATEPICKER, "06/Dec/05");
        outputParams.put(CUSTOM_FIELD_MULTICHECKBOX, "1");
        outputParams.put(CUSTOM_FIELD_USERPICKER, BOB_FULLNAME);
        outputParams.put(CUSTOM_FIELD_FLOAT, "123,456,789");
        return outputParams;
    }

    /**
     * Goes to the create issue details screen via direct link.<br>
     * The link is constructed with the key value paris in the inputParams.<br>
     * assertParams is to assert the field (key) has the correct value
     */
    private void createIssueViaDirectLink(Map inputParams, Map inputParamsForAssertion, boolean assertParams)
    {
        StringBuilder linkToCreateIssue = new StringBuilder(getEnvironmentData().getContext() + "/secure/CreateIssueDetails!init.jspa?");
        Iterator iter = inputParams.entrySet().iterator();
        int count = 1;
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            if (count != 1)
            {
                linkToCreateIssue.append("&");
            }
            linkToCreateIssue.append(entry.getKey()).append("=").append(entry.getValue());
            count++;
        }

        final String url = linkToCreateIssue.toString();
        gotoPage(url);

        if (assertParams)
        {
            assertCreateIssueDetailsForm(inputParamsForAssertion);
        }
    }

    /**
     * assert the field values are entered as expected from the inputParams map.<br>
     * (must be done within the create issue details screen)
     */
    private void assertCreateIssueDetailsForm(Map inputParams)
    {
        Iterator iter = inputParams.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String field = (String) entry.getKey();
            String value = (String) entry.getValue();

            if ("pid".equals(field))
            {
                assertTextPresentBeforeText("Project", (String) pidToName.get(value));
            }
            else if ("issuetype".equals(field))
            {
                assertTextPresentBeforeText("Issue Type", (String) issueIdToName.get(value));
            }
            else
            {
                assertFormElementEquals(field, value);
            }
        }
    }

    /**
     * assert the field values are set accoring to the param given (must be done within view issue page)
     */
    private void assertCreatedIssueDetails(Map outputParams)
    {
        assertTextPresent("Details");
        Iterator iterator = outputParams.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            assertTextPresentBeforeText((String) entry.getKey(), (String) entry.getValue());
        }
    }
}
