package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import com.opensymphony.util.TextUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionWithFields extends JIRAWebTest
{
    private static final String ISSUE_TO_CONVERT = "CIST-1";
    private static final String ISSUE_PARENT_TASK = "CIST-2";

    private FieldTestData fieldTestData;
    private final FieldData field13;
    private final FieldData field23;

    public TestIssueToSubTaskConversionWithFields(String name)
    {
        super(name);
        fieldTestData = new FieldTestData();
        fieldTestData.add(new FieldData("Field_01", false, false, "10000"));
        fieldTestData.add(new FieldData("Field_02", true, true, "10001", "")
                .setEnterValue("this is a normal field with entered value as it exists now"));
        fieldTestData.add(new FieldData("Field_03", true, true, "10002", "")
                .setEnterValue("this is a required field with entered value as it exists now"));
        fieldTestData.add(new FieldData("Field_04", false, false, "10003"));
        fieldTestData.add(new FieldData("Field_05", false, false, "10004"));
        fieldTestData.add(new FieldData("Field_06", false, false, "10005"));
        fieldTestData.add(new FieldData("Field_07", true, true, "10006", "")
                .setEnterValue("this is a required field with entered value as is not longer hidden"));
        fieldTestData.add(new FieldData("Field_08", false, true, "10007",
                "this is a normal field with value that does not exist in sub-task", ""));
        fieldTestData.add(new FieldData("Field_09", false, true, "10008",
                "this is a normal field with value that is hidden in sub-task", ""));
        fieldTestData.add(new FieldData("Field_10", false, false, "10009",
                "this is a normal field with value that stays a normal field in sub-task"));
        fieldTestData.add(new FieldData("Field_11", true, true, "10010",
                "this is a normal field with value that stays a normal field with *changed* renderer in sub-task",
                "this is a normal field with value that stays a normal field with <b>changed</b> renderer in sub-task")
                .setRendererChange(true));
        fieldTestData.add(new FieldData("Field_12", false, false, "10011",
                "this is a normal field with value that is a required field in sub-task"));
        fieldTestData.add(field13 = new FieldData("Field_13", true, true, "10012",
                "this is a normal field with value that is a *required* field with changed renderer in sub-task",
                "this is a normal field with value that is a <b>required</b> field with changed renderer in sub-task")
                .setRendererChange(true));
        fieldTestData.add(new FieldData("Field_14", false, false, "10013"));
        fieldTestData.add(new FieldData("Field_15", false, false, "10014"));
        fieldTestData.add(new FieldData("Field_16", false, false, "10015"));
        fieldTestData.add(new FieldData("Field_17", true, true, "10016", "")
                .setEnterValue("this is a required field with entered value as it is required now"));
        fieldTestData.add(new FieldData("Field_18", false, true, "10017",
                "this is a required field with value that does not exist in sub-task", ""));
        fieldTestData.add(new FieldData("Field_19", false, true, "10018",
                "this is a required field with value that is hidden in subtask", ""));
        fieldTestData.add(new FieldData("Field_20", false, false, "10019",
                "this is a required field with value that is a normal field in sub-task"));
        fieldTestData.add(new FieldData("Field_21", true, true, "10020",
                "this is a required field with *value* that is a normal field with changed renderer in sub-task",
                "this is a required field with <b>value</b> that is a normal field with changed renderer in sub-task")
                .setRendererChange(true));
        fieldTestData.add(new FieldData("Field_22", false, false, "10021",
                "this is a required field with value that stays a required field in sub-task"));
        fieldTestData.add(field23 = new FieldData("Field_23", true, true, "10022",
                "this is a required field with value that stays a required field with changed renderer in *sub-task*",
                "this is a required field with value that stays a required field with changed renderer in <b>sub-task</b>")
                .setRendererChange(true));
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubTaskConversionWithFields.xml");
    }

    public void testIssueToSubTaskConversion() throws IOException, TransformerException, ParserConfigurationException, SAXException
    {
        gotoIssue(ISSUE_TO_CONVERT);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_TO_CONVERT);

        setFormElement("parentIssueKey", ISSUE_PARENT_TASK);
        selectOption("issuetype", "Mini-task");
        // go next
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert 3rd screen was skipped
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 3);
        // assert presence of fields
        fieldTestData.assertFieldsOnFieldUpdateScreen();

        // set new values
        fieldTestData.setNewValues();
        // and go next
        submit("Next >>");

        // assert 4nd screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 4);
        // assert presence of fields
        fieldTestData.assertFieldsOnConfirmationScreen();

        // go next
        submit("Finish");

        assertSuccessfulConversion();

    }

    public void testIssueToSubTaskConversionWithErrorsCausedByNotEnteringRequiredData() throws IOException, TransformerException, ParserConfigurationException, SAXException
    {
        gotoIssue(ISSUE_TO_CONVERT);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_TO_CONVERT);
        // assert error is not present
        assertTextNotPresent("Parent issue key not specified.");

        // try go next
        submit("Next >>");

        // assert we are still on 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_TO_CONVERT);
        // assert error is reported
        assertTextPresent("Parent issue key not specified.");

        setFormElement("parentIssueKey", ISSUE_PARENT_TASK);
        selectOption("issuetype", "Mini-task");

        // go next
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert 3rd screen was skipped
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 3);
        // assert presence of fields
        fieldTestData.assertFieldsOnFieldUpdateScreen();

        // try to go next
        submit("Next >>");

        // assert we are still on 3rd screen
        assertTextPresent("Step 3 of 4");
        // assert errors are reported
        assertTextPresent("Field_03 is required.");
        assertTextPresent("Field_07 is required.");
        assertTextPresent("Field_17 is required.");

        // set new values
        fieldTestData.setNewValues();
        // and go next
        submit("Next >>");

        // assert 4nd screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 4);
        // assert presence of fields
        fieldTestData.assertFieldsOnConfirmationScreen();

        // go next
        submit("Finish");

        assertSuccessfulConversion();
    }

    public void testIssueToSubTaskConversionWithErrorsCausedByClearingRequiredData() throws IOException, TransformerException, ParserConfigurationException, SAXException
    {
        gotoIssue(ISSUE_TO_CONVERT);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_TO_CONVERT);

        setFormElement("parentIssueKey", ISSUE_PARENT_TASK);
        selectOption("issuetype", "Mini-task");

        // go next
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert 3rd screen was skipped
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 3);
        // assert presence of fields
        fieldTestData.assertFieldsOnFieldUpdateScreen();

        // clear required fields that have data carried over
        setFormElement("customfield_" + field13.id, "");
        setFormElement("customfield_" + field23.id, "");

        // try to go next
        submit("Next >>");

        // assert we are still on 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 3);
        // assert errors are reported
        assertTextPresent("Field_13 is required.");
        assertTextPresent("Field_23 is required.");

        // set new values
        fieldTestData.setNewValues();
        // set previously cleared required fields
        setFormElement("customfield_" + field13.id, field13.value);
        setFormElement("customfield_" + field23.id, field23.value);
        // and go next
        submit("Next >>");

        // assert 4nd screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT, 4);
        // assert presence of fields
        fieldTestData.assertFieldsOnConfirmationScreen();

        // go next
        submit("Finish");

        assertSuccessfulConversion();
    }

    private void assertSuccessfulConversion() throws TransformerException, IOException, ParserConfigurationException, SAXException
    {
        text.assertTextPresent(new IdLocator(tester, "key-val"), ISSUE_TO_CONVERT);
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "Green Sky");
        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), ISSUE_PARENT_TASK + " Super Mother");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Mini-task");
        // assert presence of fields on sub-task
        fieldTestData.assertFieldsOnSubtask();
        // assert data in change log
        assertChangeLog(ISSUE_TO_CONVERT, ISSUE_PARENT_TASK, "Bug", "Mini-task", "changehistory_10010");
        fieldTestData.assertChangeLogContainsAllChangedFields("changehistory_10010");
        fieldTestData.assertPresentInXMLIssueView();
    }

    private void assertChangeLog(String issue, String parent, String issueType, String subTaskType, String changeLogId)
    {
        gotoIssue(issue);
        if (getDialog().isLinkPresentWithText(CHANGE_HISTORY))
        {
            clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        }

        boolean isParentFound = false;
        boolean isSubTaskTypeFound = false;

        final WebTable table = getDialog().getWebTableBySummaryOrId(changeLogId);
        for (int row = 0; row < table.getRowCount(); row++) // skip first row (heading of change log)
        {
            final String first = table.getCellAsText(row, 0).trim();
            if (!isSubTaskTypeFound && "Issue Type".equals(first))
            {
                isSubTaskTypeFound = true;
                assertTrue(table.getCellAsText(row, 1).trim().startsWith(issueType));
                assertTrue(table.getCellAsText(row, 2).trim().startsWith(subTaskType));
            }
            if (!isParentFound && "Parent".equals(first))
            {
                isParentFound = true;
                assertEquals("", table.getCellAsText(row, 1).trim());
                assertTrue(table.getCellAsText(row, 2).trim().startsWith(parent));
            }
        }
        assertTrue("Type present in the change log", isSubTaskTypeFound);
        assertTrue("Parent present in the change log", isParentFound);
    }

    private class FieldTestData
    {
        protected List<FieldData> fieldData = new ArrayList<FieldData>(23);

        protected void add(FieldData data)
        {
            fieldData.add(data);
        }

        public void assertFieldsOnFieldUpdateScreen()
        {
            for (final FieldData data : fieldData)
            {
                if (data.isOnFieldUpdateScreen())
                {
                    assertTextPresent(data.name);
                    assertFormElementEquals(getFieldName(data), data.value);
                }
                else
                {
                    assertTextNotPresent(data.name);
                    assertFormElementNotPresent(getFieldName(data));
                }
            }
        }

        public void assertFieldsOnConfirmationScreen()
        {
            final Map<String, String> oldValues = new HashMap<String, String>();
            final Map<String, String> newValues = new HashMap<String, String>();

            final WebTable table = getDialog().getWebTableBySummaryOrId("convert_confirm_table");

            final int rowCount = table.getRowCount();
            for (int row = 2; row < rowCount; row++) // skip first two rows
            {
                final String fieldName = table.getCellAsText(row, 0).trim();
                oldValues.put(fieldName, table.getCellAsText(row, 1).trim());
                newValues.put(fieldName, table.getCellAsText(row, 2).trim()); // this removes HTML tags!!!
            }

            for (final FieldData data : fieldData)
            {
                if (data.isOnConfirmationScreen())
                {
                    assertTrue("Field: " + data.name, oldValues.containsKey(data.name));
                    assertEquals("Old value for: " + data.name, data.value, oldValues.get(data.name));
                    final String newValue = data.enterValue == null ? (data.newValue == null ? "" : data.newValue) : data.enterValue;
                    assertEquals("New value for: " + data.name, stripHtmlTags(newValue), newValues.get(data.name));
                }
                else
                {
                    assertTextNotPresent("Field: " + data.name);
                }
            }
        }

        public void assertChangeLogContainsAllChangedFields(String changeHistoryId)
        {
            final Map<String, String> oldValues = new HashMap<String, String>();
            final Map<String, String> newValues = new HashMap<String, String>();

            final WebTable table = getDialog().getWebTableBySummaryOrId(changeHistoryId);
            for (int row = 0; row < table.getRowCount(); row++)
            {
                final String fieldName = table.getCellAsText(row, 0).trim();
                if (fieldName.startsWith("Field_"))
                {
                    oldValues.put(fieldName, table.getCellAsText(row, 1).trim());
                    newValues.put(fieldName, table.getCellAsText(row, 2).trim()); // this removes HTML tags!!!
                }
            }

            for (final FieldData data : fieldData)
            {
                if (data.isInChangeLog())
                {
                    final String expectedValue = data.enterValue == null ? data.newValue : data.enterValue;
                    assertEquals("Old value of " + data.name + " not present in change log", data.value, oldValues.get(data.name));
                    assertEquals("New value of " + data.name + " not present in change log", expectedValue, newValues.get(data.name));
                }
            }
        }

        public void assertPresentInXMLIssueView() throws IOException, ParserConfigurationException, SAXException, TransformerException
        {
            gotoPage("/si/jira.issueviews:issue-xml/" + ISSUE_TO_CONVERT +"/" + ISSUE_TO_CONVERT + ".xml?jira.issue.searchlocation=index");

            String responseText = getDialog().getResponse().getText();
            log(responseText);
            org.w3c.dom.Document doc = XMLUnit.buildControlDocument(responseText);

            // Check parent, type and status is correct
            String xpathExpression = "//item[parent= &quot;" + ISSUE_PARENT_TASK + "&quot; ] ";
            log("Searching for existence of xpath " + xpathExpression);
            XMLAssert.assertXpathExists(xpathExpression, doc);

            xpathExpression = "//item[type= &quot;Mini-task&quot; ] ";
            log("Searching for existence of xpath " + xpathExpression);
            XMLAssert.assertXpathExists(xpathExpression, doc);

            for (final FieldData data : fieldData)
            {
                if (data.isInChangeLog())
                {
                    final String expectedValue = data.enterValue == null ? data.newValue : data.enterValue;
                    xpathExpression = "//item/customfields/customfield[@id= &quot;customfield_" + data.id + "&quot; ]/customfieldvalues[customfieldvalue= &quot;" + data.value + "&quot; ] ";
                    log("Searching for non existence of xpath " + xpathExpression);
                    XMLAssert.assertXpathNotExists(xpathExpression, doc);
                    if (TextUtils.stringSet(expectedValue))
                    {
                        xpathExpression = "//item/customfields/customfield[@id= &quot;customfield_" + data.id + "&quot; ]/customfieldvalues[customfieldvalue= &quot;" + expectedValue + "&quot; ] ";
                        log("Searching for existence of xpath " + xpathExpression);
                        XMLAssert.assertXpathExists(xpathExpression, doc);
                    }
                }
            }

        }

        public void assertFieldsOnSubtask()
        {
            for (final FieldData data : fieldData)
            {
                if (data.enterValue != null || data.newValue != null && !"".equals(data.newValue))
                {
                    assertTextPresent(data.name);
                }
            }
        }

        private String stripHtmlTags(String html)
        {
            return html.replaceAll("<[^>]*>", "");
        }

        public void setNewValues()
        {
            for (final FieldData data : fieldData)
            {
                if (data.hasNewValue())
                {
                    setFormElement(getFieldName(data), data.enterValue);
                }
            }
        }

        private String getFieldName(FieldData fieldData)
        {
            return "customfield_" + fieldData.id;
        }
    }

    private static class FieldData
    {
        protected final String name;
        protected final String id;
        protected final String value;
        protected final String newValue;
        private final boolean isFieldUpdate;
        private final boolean isOnConfirmation;
        protected String enterValue;
        private boolean isRendererChange;

        public FieldData(String name, boolean isFieldUpdate, boolean isOnConfirmation, String id, String value, String newValue)
        {
            this.name = name;
            this.isFieldUpdate = isFieldUpdate;
            this.isOnConfirmation = isOnConfirmation;
            this.id = id;
            this.value = value == null ? "" : value;
            this.newValue = newValue == null ? "" : newValue;
        }

        public FieldData(String name, boolean isFieldUpdate, boolean isOnConfirmation, String id, String value)
        {
            this(name, isFieldUpdate, isOnConfirmation, id, value, value);
        }

        public FieldData(String name, boolean isFieldUpdate, boolean isOnConfirmation, String id)
        {
            this(name, isFieldUpdate, isOnConfirmation, id, null);
        }

        public boolean isInChangeLog()
        {
            return !isRendererChange && (!value.equals(newValue) || enterValue != null);
        }

        public boolean isOnConfirmationScreen()
        {
            return isOnConfirmation;
        }

        public boolean isOnFieldUpdateScreen()
        {
            return isFieldUpdate;
        }

        public boolean hasNewValue()
        {
            return enterValue != null;
        }

        public FieldData setRendererChange(boolean value)
        {
            this.isRendererChange = value;
            return this;
        }

        public FieldData setEnterValue(String value)
        {
            this.enterValue = value;
            return this;
        }

        public String toString()
        {
            return id + " - " + name;
        }
    }

}
