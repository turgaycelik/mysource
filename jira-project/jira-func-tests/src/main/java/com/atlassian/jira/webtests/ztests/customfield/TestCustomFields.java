package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebLink;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collection;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.functest.framework.suite.Category.CUSTOM_FIELDS;
import static com.atlassian.jira.functest.framework.suite.Category.FIELDS;
import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.TIME_ZONES;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ( { FUNC_TEST, CUSTOM_FIELDS, FIELDS })
public class TestCustomFields extends JIRAWebTest
{
    private static final String CUSTOM_FIELD_NAME_ONE = "Assigned to Section";
    private static final String CUSTOM_FIELD_NAME_TWO = "Affects Project";
    private static final String CUSTOM_FIELD_NAME_THREE = "Time created";
    private static final String CUSTOM_FIELD_NAME_CASCADING_SELECT = "cascading select field name";
    private static final String CUSTOM_FIELD_NAME_MULTI_CHECKBOX = "multicheckboxes field name";
    private static final String CUSTOM_FIELD_NAME_MULTI_SELECT = "multiselect field name";
    private static final String CUSTOM_FIELD_NAME_PROJECT_PICKER = "project picker field name";
    private static final String CUSTOM_FIELD_NAME_TEXT_FIELD = "text field field name";
    private static final String CUSTOM_FIELD_NAME_DATE_TIME = "date time field name";
    private static final String CUSTOM_FIELD_NAME_USER_PICKER = "user picker field name";
    private static final String CUSTOM_FIELD_NAME_MULTI_USER_PICKER = "multi user picker field name";
    private static final String CUSTOM_FIELD_NAME_DATE_PICKER = "date picker field name";
    private static final String CUSTOM_FIELD_NAME_FREE_TEXT = "free text field name";
    private static final String CUSTOM_FIELD_NAME_NUMBER = "number field name";
    private static final String CUSTOM_FIELD_NAME_RADIO_BUTTONS = "radio buttons field name";
    private static final String CUSTOM_FIELD_NAME_SELECT_LIST = "select list field name";
    private static final String CUSTOM_FIELD_NAME_URL_FIELD = "url field name";
    private static final String CUSTOM_FIELD_NAME_VERSION_PICKER = "version picker field name";
    private static final String PARAM_NAME_SELECTED_OPTION_VALUE = "&selectedValue=";
    private static final String GLOBAL_SCOPE = "global";
    private static final String PROJECT_SCOPE = "project";

    private static final String[] customFieldNames = new String[] { CUSTOM_FIELD_NAME_CASCADING_SELECT, CUSTOM_FIELD_NAME_MULTI_CHECKBOX, CUSTOM_FIELD_NAME_MULTI_SELECT,
            CUSTOM_FIELD_NAME_PROJECT_PICKER, CUSTOM_FIELD_NAME_TEXT_FIELD, CUSTOM_FIELD_NAME_DATE_TIME,
            CUSTOM_FIELD_NAME_USER_PICKER, CUSTOM_FIELD_NAME_DATE_PICKER, CUSTOM_FIELD_NAME_FREE_TEXT,
            CUSTOM_FIELD_NAME_NUMBER, CUSTOM_FIELD_NAME_RADIO_BUTTONS, CUSTOM_FIELD_NAME_SELECT_LIST,
            CUSTOM_FIELD_NAME_URL_FIELD, CUSTOM_FIELD_NAME_VERSION_PICKER };

    private static final String freeTextEntry = "This is the default text area value\n with newlines possible\n and stuff.";
    private static final String numberEntry = "43";
    private static final String textEntry = "Text Field - This is the default text";
    private static final String urlEntry = "http://www.testhis.com";
    private static final String userEntry = "custom_field_user";
    private static final String version_oneDotOne = "1.1";
    private static final String version_oneDotTwo = "1.2";
    private static final String summary = "This is the summary of this issue.";

    private String issueKey1;
    private String issueKey2;
    private String issueKey3;
    private String fieldId_global;
    private String fieldId_issue;
    private String fieldId_project;
    private String cascadingSelectId;
    private String datePickerId;
    private String userPickerId;
    private String multiUserPickerId;
    private String versionPickerId;
    private String freeTextId;
    private String multiSelectId;
    private String multiCheckboxId;
    private String projectPickerId;
    private String radioButtonId;
    private String selectListId;
    private String textFieldId;
    private String urlId;
    private String dateTimeId;
    private String numberId;
    private String version_oneDotOneId;
    private String version_oneDotTwoId;
    private String project_Homosap_Id;
    private String project_Neo_Id;

    public TestCustomFields(String name)
    {
        super(name);
    }

    /**
     * This restores data to a blank instance + 3 issues with webwork plugin disabled and no custom fields. There's a
     * version 1.1 and 1.2 in project HSP
     */
    private void restoreDataForTest()
    {
        restoreData("TestCustomFields.xml");

        issueKey1 = "HSP-1";
        issueKey2 = "HSP-2";
        issueKey3 = "NDT-1";

        project_Homosap_Id = "10000";
        project_Neo_Id = "10010";

        version_oneDotOneId = "10010";
        version_oneDotTwoId = "10011";
    }

    public void testReturnUrl()
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            restoreBlankInstance();

            // Click Link 'administration' (id='admin_link').
            navigation.gotoAdmin();
            // Click Link 'Custom Fields' (id='view_custom_fields').
            tester.clickLink("view_custom_fields");
            // Click Link 'Add Custom Field' (id='add_custom_fields').
            tester.clickLink("add_custom_fields");
            tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect");

            pressCancel();

            tester.assertTitleEquals("Custom Fields - jWebTest JIRA installation");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }

    }

    private void pressCancel()
    {
        assertTrue("Scripting must be enabled in the HttpUnit for cancel to work correctly", HttpUnitOptions.isScriptingEnabled());
        tester.setWorkingForm("jiraform");
        tester.clickLink("cancelButton");
    }

    public void testCustomFields()
    {
        restoreDataForTest();

        customFieldsAddField();
        customFieldsDeleteField();
        customFieldsAddCustomFieldOption();
        customFieldsdelCustomFieldOption();
        customFieldCreateIssueWithCustomFields();
        customFieldCreateIssueWithFieldScope();
        customFieldsEditIssueWithCustomFields();
        customFieldEditIssueWithFieldScope();
        customFieldsMoveIssueWithCustomFieldsforProject();
        customFieldWithFieldScreenSchemes();

        customFieldsMoveIssueWithCustomFieldsforIssueType();
        customFieldCreateSubTaskWithCustomFields();
    }

    ////    **************  Test All Custom Field Test  **************

    // 1. Add one of each custom field (expect read-only custom fields) and add to default screen
    // 2. Create an issue - ensure default values selected
    // 3. View issue - ensure selected values displayed
    // 4. Edit Issue - ensure selected values displayed
    // 5. View Issue - ensure selected values displayed
    // 6. View Issue in Issue Navigator with all custom field columns present - ensure selected values displayed
    // 7. Remove project, version and all custom fields

    public void testAllCustomFields()
    {
        restoreBlankInstance();
        restoreDataForTest();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        setupAllCustomFields();

        createAndValidateIssue();

        viewAndValidateIssue(false);

        editAndValidateIssue();

        viewAndValidateIssue(true);

        viewIssueInNavigator();
    }

    // JRA-16005: XSS bug
    public void testNumberCustomField()
    {
        restoreBlankInstance();
        setupNumberField("");


        String xsrfToken = page.getXsrfToken();

        gotoPage("/secure/CreateIssueDetails.jspa?pid=10000&issuetype=2&atl_token=" + xsrfToken);

        tester.setFormElement("customfield_10000", "\"><script>alert(document.cookie);</script>");
        tester.submit("Create");

        //ensure the warning is encoded.
        tester.assertTextPresent("&#39;&quot;&gt;&lt;script&gt;alert(document.cookie);&lt;/script&gt;&#39; is an invalid number");
        //ensure the field value is in the input text (note that webunit decodes it which is why it's not html escaped here)
        // if the value wasn't encoded properly however, then the value would show up empty here!
        assertFormElementWithNameHasValue("issue-create", "customfield_10000", "\"><script>alert(document.cookie);</script>");
    }

    public void testUserCustomFieldWithInvalidDefaultValue()
    {
        restoreBlankInstance();
        userPickerId = addCustomField("userpicker", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_USER_PICKER, "user picker field", null, null, null);

        configureDefaultCustomFieldValue(userPickerId, "user_does_not_exist");
        assertions.getTextAssertions().assertTextPresentHtmlEncoded("User 'user_does_not_exist' was not found in the system");
    }

    public void testMultiUserCustomFieldWithInvalidDefaultValue()
    {
        restoreBlankInstance();
        multiUserPickerId = addCustomField("multiuserpicker", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_MULTI_USER_PICKER, "multi user picker field", null, null, null);

        configureDefaultCustomFieldValue(multiUserPickerId, "user_does_not_exist");
        assertTextPresent("Could not find usernames: user_does_not_exist");
    }

    // Setup each custom field available (except for read-only fields)
    // Default Values set for all fields
    private void setupAllCustomFields()
    {
        // Cascading Select
        setupCascadingSelect();

        // Date Time
        setupDateTime("24/Aug/2005 6:00 AM");

        // Multi-Select
        Collection options = new ArrayList();
        options.add("MultiSelect One");
        options.add("MultiSelect Two");
        options.add("MultiSelect Three");
        setupMultiSelect(options);

        // Project Picker
        setupProjectPicker(project_Homosap_Id);

        // Text Field

        setupTextField(textEntry);

        // User Picker
        setupUserPicker(userEntry);

        // Date Picker
        setupDatePicker("23/Aug/2005");

        // Free Text
        setupFreeText(freeTextEntry);

        // Multi-Checkbox
        options.clear();
        options.add("MultiCheckBox One");
        options.add("MultiCheckBox Two");
        options.add("MultiCheckBox Three");
        setupMultiCheckboxes(options);

        // Number Field
        setupNumberField(numberEntry);

        // Radio Buttons
        options.clear();
        options.add("Radio One");
        options.add("Radio Two");
        options.add("Radio Three");
        setupRadioButtons(options);

        // Select List
        options.clear();
        options.add("Select List One");
        options.add("Select List Two");
        options.add("Select List Three");
        setupSelectList(options);

        // URL Field
        setupURLField(urlEntry);

        // Version Picker
        setupVersionPicker(version_oneDotOneId);
    }

    private void viewIssueInNavigator()
    {
        addColumnToIssueNavigator(customFieldNames);
        navigation.issueNavigator().createSearch("project = " + project_Homosap_Id);
        viewAndValidateIssue(true);
        restoreColumnDefaults();
    }

    // Create an issue and validate custom field values
    private void createAndValidateIssue()
    {
        // Create first step of issue and ensure that all values are available
        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Bug");
        assertTextPresent("CreateIssueDetails.jspa");

        // Check that default values are present and select other values
        validateAndSetCustomFieldValues(false);
    }

    private void validateAndSetCustomFieldValues(boolean edit)
    {
        assertTextPresent(CUSTOM_FIELD_NAME_CASCADING_SELECT);
        assertTextPresent(CUSTOM_FIELD_NAME_DATE_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_DATE_TIME);
        assertTextPresent(CUSTOM_FIELD_NAME_FREE_TEXT);
        assertTextPresent(CUSTOM_FIELD_NAME_MULTI_CHECKBOX);
        assertTextPresent(CUSTOM_FIELD_NAME_MULTI_SELECT);
        assertTextPresent(CUSTOM_FIELD_NAME_NUMBER);
        assertTextPresent(CUSTOM_FIELD_NAME_PROJECT_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_RADIO_BUTTONS);
        assertTextPresent(CUSTOM_FIELD_NAME_SELECT_LIST);
        assertTextPresent(CUSTOM_FIELD_NAME_TEXT_FIELD);
        assertTextPresent(CUSTOM_FIELD_NAME_URL_FIELD);
        assertTextPresent(CUSTOM_FIELD_NAME_USER_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_VERSION_PICKER);

        if (edit)
        {
            setWorkingForm("issue-edit");
            selectOption(CUSTOM_FIELD_PREFIX + cascadingSelectId, "Cascade One");
            selectOption(CUSTOM_FIELD_PREFIX + cascadingSelectId + ":1", "Cascade B");
            assertOptionEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId, "Cascade One");
            assertOptionEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId + ":1", "Cascade B");
//            assertFormElementEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId, "10000");
//            assertFormElementEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId + ":1", "10004");

            setFormElement(CUSTOM_FIELD_PREFIX + datePickerId, "22/Aug/05");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + datePickerId, "22/Aug/05");

            setFormElement(CUSTOM_FIELD_PREFIX + dateTimeId, "25/Aug/05 7:00 AM");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + dateTimeId, "25/Aug/05 7:00 AM");

            setFormElement(CUSTOM_FIELD_PREFIX + freeTextId, freeTextEntry + " edited.");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + freeTextId, freeTextEntry + " edited.");

            setFormElement(CUSTOM_FIELD_PREFIX + multiCheckboxId, "10017");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + multiCheckboxId, "10017");

            setFormElement(CUSTOM_FIELD_PREFIX + multiSelectId, "10013");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + multiSelectId, "10013");

            setFormElement(CUSTOM_FIELD_PREFIX + numberId, numberEntry + "3");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + numberId, numberEntry + "3");

            setFormElement(CUSTOM_FIELD_PREFIX + projectPickerId, project_Neo_Id);
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + projectPickerId, project_Neo_Id);

            setFormElement(CUSTOM_FIELD_PREFIX + radioButtonId, "10020");
            assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + radioButtonId, "10020");

            selectOption(CUSTOM_FIELD_PREFIX + selectListId, "Select List Three");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + selectListId, "10023");

            setFormElement(CUSTOM_FIELD_PREFIX + textFieldId, textEntry + " edited.");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + textFieldId, textEntry + " edited.");

            setFormElement(CUSTOM_FIELD_PREFIX + userPickerId, ADMIN_USERNAME);
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + userPickerId, ADMIN_USERNAME);

            selectOption(CUSTOM_FIELD_PREFIX + versionPickerId, version_oneDotTwo);
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + versionPickerId, version_oneDotTwoId);

            setFormElement(CUSTOM_FIELD_PREFIX + urlId, urlEntry + ".au");
            assertFormElementEquals(CUSTOM_FIELD_PREFIX + urlId, urlEntry + ".au");
        }
        else
        {
            assertOptionEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId, "Cascade Three");
            assertOptionEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId + ":1", "Cascade Beta");
//            assertFormElementEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId, "10002");
//            assertFormElementEquals(CUSTOM_FIELD_PREFIX + cascadingSelectId + ":1", "10010");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + datePickerId, "23/Aug/05");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + dateTimeId, "24/Aug/05 6:00 AM");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + freeTextId, "\n" + freeTextEntry); //new line solves JRA-11257

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + multiCheckboxId, "10016");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + multiSelectId, "10014");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + numberId, numberEntry);

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + projectPickerId, project_Homosap_Id);

            assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + radioButtonId, "10019");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + selectListId, "10022");

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + textFieldId, textEntry);

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + urlId, urlEntry);

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + userPickerId, userEntry);

            assertFormElementEquals(CUSTOM_FIELD_PREFIX + versionPickerId, version_oneDotOneId);
        }

        setFormElement("summary", summary);
        submit();
    }

    // Validate that the correct values are shown when viewing the issue
    private void viewAndValidateIssue(boolean edited)
    {
        assertTextPresent(CUSTOM_FIELD_NAME_CASCADING_SELECT);
        assertTextPresent(CUSTOM_FIELD_NAME_DATE_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_DATE_TIME);
        assertTextPresent(CUSTOM_FIELD_NAME_FREE_TEXT);
        assertTextPresent(CUSTOM_FIELD_NAME_MULTI_CHECKBOX);
        assertTextPresent(CUSTOM_FIELD_NAME_MULTI_SELECT);
        assertTextPresent(CUSTOM_FIELD_NAME_NUMBER);
        assertTextPresent(CUSTOM_FIELD_NAME_PROJECT_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_RADIO_BUTTONS);
        assertTextPresent(CUSTOM_FIELD_NAME_SELECT_LIST);
        assertTextPresent(CUSTOM_FIELD_NAME_TEXT_FIELD);
        assertTextPresent(CUSTOM_FIELD_NAME_URL_FIELD);
        assertTextPresent(CUSTOM_FIELD_NAME_USER_PICKER);
        assertTextPresent(CUSTOM_FIELD_NAME_VERSION_PICKER);

        if (edited)
        {
            assertTextPresent("Cascade One");
            assertTextPresent("Cascade B");
            assertTextPresent("22/Aug/05");
            assertTextPresent("25/Aug/05 7:00 AM");
            assertTextPresent(textEntry);
            assertTextPresent("MultiCheckBox Three");
            assertTextPresent("MultiSelect Two");
            assertTextPresent(numberEntry);
            assertTextPresent(PROJECT_HOMOSAP);
            assertTextPresent("Radio Three");
            assertTextPresent("Select List Three");
            assertTextPresent(textEntry);
            assertTextPresent(urlEntry);
            assertTextPresent(ADMIN_USERNAME);
            assertTextPresent(version_oneDotTwo);
        }
        else
        {
            assertTextPresent("Cascade Three");
            assertTextPresent("Cascade Beta");
            assertTextPresent("23/Aug/05");
            assertTextPresent("24/Aug/05 6:00 AM");
            assertTextPresent(textEntry);
            assertTextPresent("MultiCheckBox Two");
            assertTextPresent("MultiSelect Three");
            assertTextPresent(numberEntry);
            assertTextPresent(PROJECT_HOMOSAP);
            assertTextPresent("Radio Two");
            assertTextPresent("Select List Two");
            assertTextPresent(textEntry);
            assertTextPresent(urlEntry);
            assertTextPresent(userEntry);
            assertTextPresent(version_oneDotOne);
        }
    }

    // Ensure all custom field values are initially correct on edit display
    private void editAndValidateIssue()
    {
        clickLink("edit-issue");
        validateAndSetCustomFieldValues(true);
    }

    private void setupVersionPicker(String version)
    {
        versionPickerId = addCustomField("version", PROJECT_SCOPE, CUSTOM_FIELD_NAME_VERSION_PICKER, "version picker field", null, PROJECT_HOMOSAP, null);
        configureDefaultCustomFieldValue(versionPickerId, version);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_VERSION_PICKER);
    }

    private void setupURLField(String url)
    {
        urlId = addCustomField("url", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_URL_FIELD, "url field", null, null, null);
        configureDefaultCustomFieldValue(urlId, url);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_URL_FIELD);
    }

    private void setupSelectList(Collection options)
    {
        selectListId = addCustomField("select", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_SELECT_LIST, "select list field", null, null, null);
        for (final Object option1 : options)
        {
            String option = (String) option1;
            configureCustomFieldOption(selectListId, option);
        }

        configureDefaultCustomFieldValue(selectListId, "10022");

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_SELECT_LIST);
    }

    private void setupRadioButtons(Collection options)
    {
        radioButtonId = addCustomField("radiobuttons", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_RADIO_BUTTONS, "radio buttons field", null, null, null);
        for (final Object option1 : options)
        {
            String option = (String) option1;
            configureCustomFieldOption(radioButtonId, option);
        }

        configureDefaultCustomFieldValue(radioButtonId, "10019");

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_RADIO_BUTTONS);
    }

    private void setupNumberField(String defaultValue)
    {
        numberId = addCustomField("float", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_NUMBER, "number field", null, null, "Number Searcher");
        if (StringUtils.isNotEmpty(defaultValue))
        {
            configureDefaultCustomFieldValue(numberId, defaultValue);
        }

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_NUMBER);
    }

    private void setupMultiCheckboxes(Collection options)
    {
        multiCheckboxId = addCustomField("multicheckboxes", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_MULTI_CHECKBOX, "multicheckbox field", null, null, null);
        for (final Object option1 : options)
        {
            String option = (String) option1;
            configureCustomFieldOption(multiCheckboxId, option);
        }

        configureDefaultCheckBoxCustomFieldValue(multiCheckboxId, "2");

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_MULTI_CHECKBOX);
    }

    private void setupFreeText(String text)
    {
        freeTextId = addCustomField("textarea", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_FREE_TEXT, "free text field", null, null, null);
        configureDefaultCustomFieldValue(freeTextId, text);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_FREE_TEXT);
    }

    private void setupDatePicker(String date)
    {
        datePickerId = addCustomField("datepicker", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_DATE_PICKER, "date picker field", null, null, null);
        configureDefaultCustomFieldValue(datePickerId, date);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_DATE_PICKER);
    }

    private void setupUserPicker(String user)
    {
        userPickerId = addCustomField("userpicker", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_USER_PICKER, "user picker field", null, null, null);
        configureDefaultCustomFieldValue(userPickerId, user);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_USER_PICKER);
    }

    private void setupTextField(String text)
    {
        textFieldId = addCustomField("textfield", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_TEXT_FIELD, "text field field", null, null, null);
        configureDefaultCustomFieldValue(textFieldId, text);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_TEXT_FIELD);
    }

    private void setupProjectPicker(String projectId)
    {
        projectPickerId = addCustomField("project", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_PROJECT_PICKER, "project picker field", null, null, null);
        configureDefaultCustomFieldValue(projectPickerId, projectId);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_PROJECT_PICKER);
    }

    private void setupMultiSelect(Collection options)
    {
        // Multi Select
        multiSelectId = addCustomField("multiselect", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_MULTI_SELECT, "multiselect field", null, null, null);
        for (final Object option1 : options)
        {
            String option = (String) option1;
            configureCustomFieldOption(multiSelectId, option);
        }

        configureDefaultCustomFieldValue(multiSelectId, "10014");

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_MULTI_SELECT);
    }

    private void setupDateTime(String dateTime)
    {
        // Date Time
        dateTimeId = addCustomField("datetime", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_DATE_TIME, "date time custom field", null, null, null);
        configureDefaultCustomFieldValue(dateTimeId, dateTime);

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_DATE_TIME);
    }

    private void setupCascadingSelect()
    {
        // Cascading Select
        cascadingSelectId = addCustomField("cascadingselect", GLOBAL_SCOPE, CUSTOM_FIELD_NAME_CASCADING_SELECT, "cascading select field", null, null, null);
        configureCustomFieldOption(cascadingSelectId, "Cascade One");
        configureCustomFieldOption(cascadingSelectId, "Cascade Two");
        configureCustomFieldOption(cascadingSelectId, "Cascade Three");
        clickLinkWithText("Cascade One");
        setFormElement("addValue", "Cascade A");
        submit("Add");
        setFormElement("addValue", "Cascade B");
        submit("Add");
        setFormElement("addValue", "Cascade C");
        submit("Add");
        clickLinkWithText("View Custom Field Configuration");
        clickLinkWithText("Edit Options");
        clickLinkWithText("Cascade Two");
        setFormElement("addValue", "Cascade I");
        submit("Add");
        setFormElement("addValue", "Cascade II");
        submit("Add");
        setFormElement("addValue", "Cascade III");
        submit("Add");
        clickLinkWithText("View Custom Field Configuration");
        clickLinkWithText("Edit Options");
        clickLinkWithText("Cascade Three");
        setFormElement("addValue", "Cascade Alhpa");
        submit("Add");
        setFormElement("addValue", "Cascade Beta");
        submit("Add");
        setFormElement("addValue", "Cascade Gamma");
        submit("Add");

        configureDefaultMultiCustomFieldValue(cascadingSelectId, "Cascade Three", "Cascade Beta");

        // Add to Default Screen
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_CASCADING_SELECT);
    }

    public void testAddingACustomFieldAndEditingTheConfiguration()
    {
        restoreDataForTest();
        gotoAdmin();
        clickLink("view_custom_fields");
        // Add a Version Picker custom field
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:multiversion");
        submit("nextBtn");

        // Add specific information for the custom field
        setFormElement("fieldName", "Version Picker Custom Field");
        // Set the 'context'
        // Assign the CF to the Bug type
        selectOption("issuetypes", "Bug");
        // Assign it to the homosapien project
        checkCheckbox("global", "false");
        selectOption("projects", "homosapien");
        submit("nextBtn");

        // Associate with all screens
        checkCheckbox("associatedScreens", "1");
        checkCheckbox("associatedScreens", "2");
        checkCheckbox("associatedScreens", "3");
        submit("Update");

        // Configure and set the default value for the field
        clickLink("config_customfield_10000");
        clickLinkWithText("Edit Default Value");
        selectOption("customfield_10000", "New Version 1");
        submit();

        // Re-edit the configuration
        clickLink("edit_10010");
        selectOption("issuetypes", "Bug");
        selectOption("issuetypes", "Improvement");
        submit();
    }

    ////    **************  End Test All Custom Field Test  **************
    public void testCustomFieldsOrdering()
    {
        restoreBlankInstance();
        log("Testing Ordering of options for multicheckboxes field");
        String fieldId = addCustomField("multicheckboxes", "global", CUSTOM_FIELD_NAME_MULTI_CHECKBOX, "multicheckboxes field for ordering tests", null, null, null);
        startCustomFieldsOrderingTest(fieldId, null);

        log("Testing Ordering of options for cascading select field");
        fieldId = addCustomField("cascadingselect", "global", CUSTOM_FIELD_NAME_CASCADING_SELECT, "cascading select field for ordering tests", null, null, null);
        String parentOption = startCustomFieldsOrderingTest(fieldId, null);

        log("Testing Ordering of options for a child option of the cascading select field");
        startCustomFieldsOrderingTest(fieldId, parentOption);
    }

    public String startCustomFieldsOrderingTest(String fieldId, String parentOption)
    {
        //add 'numberOfOptions' amount of option values to this custom field
        int numberOfOptions = 6;
        String optionValue[] = new String[numberOfOptions];
        String optionId[] = new String[numberOfOptions];
        int i;
        for (i = 1; i < numberOfOptions; i++)
        {
            optionValue[i] = "Value_" + i;
            clickOnAdminPanel("admin.issuefields", "view_custom_fields");
            clickLink("config_" + CUSTOM_FIELD_PREFIX + fieldId);
            clickLinkWithText("Edit Options");
            assertTextPresent("Edit Options for Custom Field");
            if (parentOption != null)
            {
                clickLinkWithText(parentOption);
            }
            setFormElement("addValue", optionValue[i]);
            submit("Add");
            assertTextPresent(optionValue[i]);

        }
        // Gather all the delete links
        i = 1;
        try
        {
            WebLink[] links = getDialog().getResponse().getLinks();
            for (WebLink link : links)
            {
                if (link.getID().startsWith("del_"))
                {
                    optionId[i] = link.getID().substring(4, 9);
                    i++;
                }
            }
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }

        resetInAscendingOrdering(optionId, "Option");
        checkOrderingUsingArrows(optionValue, optionId);

        return checkOrderingUsingMoveToPos(optionValue, optionId, "Option");
    }

    /* -------- Custom Fields tests -------- */

    public void customFieldsAddField()
    {
        log("Test adding a custom field");
        fieldId_global = addCustomField("radiobuttons", "global", CUSTOM_FIELD_NAME_ONE, "custom field 1", null, null, null);
        clickOnAdminPanel("admin.issuefields", "view_custom_fields");
        assertTextPresent(CUSTOM_FIELD_NAME_ONE);

        // Add custom field to the default screen
        // The default screen has id of 1
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_ONE);
    }

    public void customFieldsDeleteField()
    {
        log("Test deleting a custom field");
        deleteCustomField(fieldId_global);
        clickOnAdminPanel("admin.issuefields", "view_custom_fields");
        assertTextNotPresent(CUSTOM_FIELD_NAME_ONE);

        fieldId_global = addCustomField("radiobuttons", "global", CUSTOM_FIELD_NAME_ONE, "custom field 1", null, null, null);
        log("Field: " + fieldId_global);
        assertTextPresent(CUSTOM_FIELD_NAME_ONE);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_ONE);

        fieldId_issue = addCustomField("project", "issuetype", CUSTOM_FIELD_NAME_TWO, "custom field 2", "Bug", null, null);
        log("Field: " + fieldId_issue);
        assertTextPresent(CUSTOM_FIELD_NAME_TWO);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_TWO);

        fieldId_project = addCustomField("datetime", "project", CUSTOM_FIELD_NAME_THREE, "custom field 3", null, PROJECT_HOMOSAP, null);
        log("Field: " + fieldId_project);
        assertTextPresent(CUSTOM_FIELD_NAME_THREE);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_THREE);
    }

    public void customFieldsAddCustomFieldOption()
    {
        log("Test adding a custom field option");
        addCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId_global, "Blue");
        assertTextPresent("Blue");
    }

    public void customFieldsdelCustomFieldOption()
    {
        log("Test deleting a custom field option");
        delCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId_global, "10000");
        assertTextPresent("There are currently no options available for this select list");

        addCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId_global, "Blue");
        addCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId_global, "Red");
        addCustomFieldOption(CUSTOM_FIELD_PREFIX + fieldId_global, "White");
    }

    public void customFieldCreateIssueWithCustomFields()
    {
        log("Create an issue with custom fields");
        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Bug");

        setFormElement("summary", "issue with custom fields");
        selectOption("priority", "Minor");
        getDialog().setFormParameter(CUSTOM_FIELD_PREFIX + fieldId_global, "10001");
        assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + fieldId_global, "10001");
        selectOption(CUSTOM_FIELD_PREFIX + fieldId_issue, PROJECT_HOMOSAP);
        setFormElement(CUSTOM_FIELD_PREFIX + fieldId_project, "27/Jan/05 6:00 am");

        submit("Create");

        assertTextPresent("Details");
        assertTextPresent(CUSTOM_FIELD_NAME_ONE);
        assertTextPresent(CUSTOM_FIELD_NAME_TWO);
        assertTextPresent(CUSTOM_FIELD_NAME_THREE);
        clickLink("delete-issue");
        submit("Delete");
    }

    public void customFieldCreateIssueWithFieldScope()
    {
        log("Test the availibility of custom fields using field scope for creating an issue");
        getNavigation().issue().goToCreateIssueForm(PROJECT_NEO, "Bug");

        // project scope
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Improvement");

        // issue type scope
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
    }

    public void customFieldsEditIssueWithCustomFields()
    {
        log("Edit Issue: " + issueKey1 + " with custom fields");
        gotoIssue(issueKey1);
        clickLink("edit-issue");

        getDialog().setFormParameter(CUSTOM_FIELD_PREFIX + fieldId_global, "10001");
        assertRadioOptionSelected(CUSTOM_FIELD_PREFIX + fieldId_global, "10001");
        selectOption(CUSTOM_FIELD_PREFIX + fieldId_issue, PROJECT_HOMOSAP);
        setFormElement(CUSTOM_FIELD_PREFIX + fieldId_project, "27/Jan/05 6:00 am");

        submit("Update");

        assertTextPresent("Details");
        assertTextPresent(CUSTOM_FIELD_NAME_ONE);
        assertTextPresent(CUSTOM_FIELD_NAME_TWO);
        assertTextPresent(CUSTOM_FIELD_NAME_THREE);
    }

    public void customFieldEditIssueWithFieldScope()
    {
        log("Test the availibility of custom fields using field scope for updating an issue");

        gotoIssue(issueKey3);
        clickLink("edit-issue");
        // project scope
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        gotoIssue(issueKey2);
        clickLink("edit-issue");
        // issue type scope
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
    }

    public void customFieldsMoveIssueWithCustomFieldsforProject()
    {
        log("Test the availibility of custom fields using field scope for moving an issue to a different project");

        // towards project
        gotoIssue(issueKey3);
        clickLink("move-issue");

        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Update the fields of the issue to relate to the new project.");

        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        // Global fields should only appear in the move if the field is required in the target project and does not have a current value
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_global);
        // Issue type custom field should only appear if issue type is being changed or if the field has no current value for the issue
        // and is required in teh destination field configuration
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        // away from project
        gotoIssue(issueKey2);
        clickLink("move-issue");

        selectOption("pid", PROJECT_NEO);
        selectOption("issuetype", "Bug");
        submit();

        assertTextPresent("Update the fields of the issue to relate to the new project.");

        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);
        // Project custom field should only appear if project is being changed or if the field has no current value for the issue
        // and is required in teh destination field configuration
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        // Global fields should only appear in the move if the field is required in the target project and does not have a current value
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_global);
    }

    public void customFieldsMoveIssueWithCustomFieldsforIssueType()
    {
        log("Test the availibility of custom fields using field scope for moving an issue to a different issue type");

        gotoIssue(issueKey3);
        clickLink("move-issue");

        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Update the fields of the issue to relate to the new project.");

        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_global);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
    }

    public void customFieldCreateSubTaskWithCustomFields()
    {
        activateSubTasks();

        gotoIssue(issueKey1);
        clickLink("create-subtask");

        assertTextPresent("Create Sub-Task");

        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        gotoIssue(issueKey3);
        clickLink("create-subtask");

        assertTextPresent("Create Sub-Task");

        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        deactivateSubTasks();
    }

    public void customFieldWithFieldScreenSchemes()
    {
        log("Test the availabilty of custom fields using the field screen schemes");

        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Bug");
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_global);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] { CUSTOM_FIELD_NAME_ONE });
        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] { CUSTOM_FIELD_NAME_TWO });
        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] { CUSTOM_FIELD_NAME_THREE });

        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, "Bug");
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_global);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_project);
        assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + fieldId_issue);

        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_ONE);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_TWO);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME_THREE);
    }

    public void testCustomFieldUserCFPermissions()
    {
        _testViewIssueMultiUserCFPermissionScheme();
        _testViewIssueMultiUserCFIssueLevelSecurity();
    }

    public void testVersionCustomFieldPromptsForValuesInMove()
    {
        restoreData("TestVersionCustomFields.xml");
        gotoIssue("HSP-1");
        clickLink("move-issue");
        selectOption("pid", PROJECT_MONKEY);
        submit();
        assertTextPresent("multi version picker cf");
        selectOption("customfield_10000", "New Version 3");
        assertTextPresent("single version picker cf");
        selectOption("customfield_10001", "New Version 3");
        submit();
        submit("Move");
        gotoIssue("MKY-1");
        assertTextPresent("New Version 3");
        assertTextNotPresent("New Version 2");
        assertTextNotPresent("New Version 1");
    }

    public void testVersionCustomFieldPromptsForValuesInBulkMove()
    {
        restoreData("TestVersionCustomFields.xml");
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeSelectIssue("HSP-1");
        chooseOperationBulkMove();
        selectOption("10010_1_pid", PROJECT_MONKEY);
        navigation.clickOnNext();
        assertTextPresent("multi version picker cf");
        selectOption("customfield_10000_10010", "New Version 3");
        selectOption("customfield_10000_10011", "New Version 3");
        assertTextPresent("single version picker cf");
        selectOption("customfield_10001_10011", "New Version 3");
        navigation.clickOnNext();
        navigation.clickOnNext();
        gotoIssue("MKY-1");
        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10000"), "New Version 3");
        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10001"), "New Version 3");
        assertTextNotPresent("New Version 2");
        assertTextNotPresent("New Version 1");
    }

    private void _testViewIssueMultiUserCFIssueLevelSecurity()
    {
        restoreData("multiuser_cf_issue_perm.xml");
        _testViewIssuePerm();
    }

    private void _testViewIssueMultiUserCFPermissionScheme()
    {
        restoreData("multiuser_cf_perm_scheme.xml");
        _testViewIssuePerm();
    }

    private void _testViewIssuePerm()
    {
        try
        {
            logout();
            login(BOB_USERNAME, BOB_PASSWORD);

            // make sure that we can see the issue since we have permission at the user cf level
            gotoIssue("HSP-1");
            assertTextPresent("This is a test issue");
            logout();
            login(ADMIN_USERNAME, ADMIN_PASSWORD);

            // make sure that we can see the issue through the issue navigator
            showIssues("project=HSP AND issuetype=bug");
            assertTextPresent("This is a test issue");

            // remove bob from the multiuser cf
            editIssueWithCustomFields("HSP-1", "10001", "", CUSTOM_FIELD_TYPE_USERPICKER);
            logout();
            login(BOB_USERNAME, BOB_PASSWORD);
            gotoIssue("HSP-1");
            assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform");

            // make sure that we can not see the issue through the issue navigator
            // make sure that we can see the issue through the issue navigator
            showIssues("project=HSP AND issuetype=bug");
            assertTextNotPresent("This is a test issue");
        }
        finally
        {
            logout();
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    /**
     * Editing issue types caused a custom field context configuration to lose its association with the issue type
     * because the (mutable) GenericValue was used as a key in the configs map (cache). Subsequent calls to see if the
     * exact same issue type key returned a field config failed because the key was then in the wrong hash bucket. This
     * resulted in the custom field not being shown on the view issue screen. Test test checks to see if this problem
     * occurs.
     * <p/>
     * NOTE that this test can produce false negatives (incorrect test passes) due to the necessity of causing the jdk
     * implementation to use a different bucket for the new issue type. Of course this doesn't happen in the fixed
     * implementation of the production code so there should be no false positives (incorrect failures)
     */
    public void testEditIssueTypeDoesNotCauseCustomFieldToDisappear()
    {
        restoreBlankInstance();
        // create an issue type
        String issueTypeName = "todo";
        String issueTypeId = addIssueType(issueTypeName, "a todo item");

        // now create a custom field associated to the issue type
        String customFieldName = "TodoTextField";
        String todoTextFieldId = addCustomField("textfield", GLOBAL_SCOPE, customFieldName, "field for testing todo issue type", issueTypeName, null, null);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, customFieldName);

        // now create an issue and confirm the custom field is shown
        getNavigation().issue().goToCreateIssueForm("monkey", issueTypeName);
        setFormElement("summary", "testing todo");
        setFormElement("customfield_10000", "contents of the todo field");
        submit("Create");
        assertTextPresent("contents of the todo field");

        // now edit the issue type hoping its new hashcode changes which bucket it goes in
        // this update has proven sufficiently bucket-shifting
        editIssueType(issueTypeId, issueTypeName, "a todo item");

        gotoIssue("MKY-1"); // this should be the issue key

        // check we can still see our custom field
        assertTextPresent("contents of the todo field");
        assertTextPresent("TodoTextField:");
    }

    public void testEditSelectWithMultipleValuesFixesField()
    {
        restoreData("TestSelectCustomFieldBroken.xml");

        gotoIssue("HSP-1");

// This situation no longer breaks the UI. The first value shows.
//        assertTextNotPresent("sweet");
//        assertTextNotPresent("dude");
//        assertTextNotPresent("select cf");
//
        clickLinkWithText("HSP-1");
        clickLink("edit-issue");
        selectOption("customfield_10010", "dude");
        submit("Update");

        assertTextPresent("dude");
        assertTextPresent("select cf");
    }

    // This test verifies JRA-12868 has been fixed. We make sure that even though the custom fields data is
    // invalid that the user can fix/edit the field in the UI.
    public void testEditUserCFWithDeletedUsersValuesSelected()
    {
        restoreData("TestUserCustomFieldBroken.xml");

        gotoIssue("HSP-1");

        assertTextNotPresent(FRED_FULLNAME);
        assertTextPresent(FRED_USERNAME);
        assertTextPresent("user cf");

        clickLinkWithText("HSP-1");
        clickLink("edit-issue");
        setFormElement("customfield_10000", ADMIN_USERNAME);
        submit("Update");

        assertTextPresent(ADMIN_FULLNAME);
        assertTextPresent("user cf");
    }

    /**
     * Test that JRA-10461 has been fixed.  If issue types are deleted and a custom field is configured to use them then
     * the "edit configuration page" should be able to handle this.
     */
    public void testMissingIssueTypesForCustomField()
    {
        restoreBlankInstance();

        // add some issue types that will later be deleted
        String issueTypeId = addIssueType("A Thorn", "A Thorn In My Side");

        // config a custom field to use this issue type only
        addCustomField("version", PROJECT_SCOPE, "versionP1", "versionP1", "A Thorn", PROJECT_HOMOSAP, null);
        addCustomFieldWithMultipleIssueTypes("version", PROJECT_SCOPE, "versionP2", "versionP2", new String[] { "A Thorn", "Bug" }, PROJECT_HOMOSAP, null);

        // now delete that issue type
        gotoPage("secure/admin/DeleteIssueType!default.jspa?id=" + issueTypeId);
        submit("Delete");

        // and try to edit the custom field again.  It should succeed
        clickLink("view_custom_fields");
        assertTextSequence(new String[] { "versionP1", "Not configured for any context" });
        assertTextSequence(new String[] { "versionP2", "Issue type", "Project" });
        assertTextPresentBeforeText("Not configured for any context", "versionP2");

        // and try to edit the custom field again.  It should succeed
        clickLink("view_custom_fields");
        clickLink("config_customfield_10001");
        clickLink("edit_10011");
        submit("Modify");

        assertTextPresent("Default Configuration Scheme for versionP2");
    }

    @WebTest(TIME_ZONES)
    public void testDateTimeCustomFieldShouldRespectUserTimeZone() throws Exception
    {
        final String HSP_1 = "HSP-1";
        final String SYDNEY_TZ = "Australia/Sydney";
        final String ROME_TZ = "Europe/Rome";

        administration.restoreData("TestCustomFields.xml");
        administration.generalConfiguration().setDefaultUserTimeZone(SYDNEY_TZ);

        // set up: create the custom field
        final String CF_NAME = "tzAwareDateTime";
        final String CF_ID = addCustomField(CUSTOM_FIELD_TYPE_DATETIME, CF_NAME);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CF_NAME);

        final String DATE_1_SYDNEY = "10/Jan/99 12:00 PM";
        final String DATE_1_ROME = "10/Jan/99 2:00 AM";

        // 1. set DATE_1 from Sydney
        setCustomFieldValue(HSP_1, CF_ID, DATE_1_SYDNEY);

        // date should still show in Sydney time zone
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_1_SYDNEY));

        // 2. now fly over to Rome
        navigation.userProfile().changeUserTimeZone(ROME_TZ);

        // 3. read DATE_1 from Rome
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_1_ROME));

        final String DATE_2_ROME = "05/Jan/99 1:00 AM";
        final String DATE_2_SYDNEY = "05/Jan/99 11:00 AM";

        // 4. now enter a new date from Rome
        setCustomFieldValue(HSP_1, CF_ID, DATE_2_ROME);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_2_ROME));

        // 5. fly back to Sydney and make sure the date is good
        navigation.userProfile().changeUserTimeZone(SYDNEY_TZ);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_2_SYDNEY));
    }

    @WebTest(TIME_ZONES)
    public void testDateCustomFieldShouldBeDisplayedInSystemTimeZone() throws Exception
    {
        final String HSP_1 = "HSP-1";
        final String ROME_TZ = "Europe/Rome";

        administration.restoreData("TestCustomFields.xml");

        // set up: create the date custom field
        final String CF_NAME = "dateOnly";
        final String CF_ID = addCustomField(CUSTOM_FIELD_TYPE_DATEPICKER, CF_NAME);
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CF_NAME);

        final String DATE_STRING_SYDNEY = "10/Jan/99";

        // set the date and check that it was saved correctly
        setCustomFieldValue(HSP_1, CF_ID, DATE_STRING_SYDNEY);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_STRING_SYDNEY));

        // changing the default user time zone should *not* affect how dates are displayed
        administration.generalConfiguration().setDefaultUserTimeZone(ROME_TZ);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_STRING_SYDNEY));

        // or how they are saved
        setCustomFieldValue(HSP_1, CF_ID, DATE_STRING_SYDNEY);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_STRING_SYDNEY));

        // changing a user's time zone should still not affect how dates are displayed for that user
        navigation.userProfile().changeUserTimeZone(ROME_TZ);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_STRING_SYDNEY));

        // or how they are saved
        setCustomFieldValue(HSP_1, CF_ID, DATE_STRING_SYDNEY);
        assertThat(customField(HSP_1, CF_ID).getText(), equalTo(DATE_STRING_SYDNEY));
    }

    protected void setCustomFieldValue(String issueKey, String cfId, String dateString)
    {
        navigation.issue().gotoEditIssue(issueKey);
        tester.setFormElement(String.format("customfield_%s", cfId), dateString);
        tester.submit();
    }

    protected CssLocator customField(String issueKey, String cfId)
    {
        navigation.issue().gotoIssue(issueKey);
        return locator.css(String.format("#customfield_%s-val", cfId));
    }


}
