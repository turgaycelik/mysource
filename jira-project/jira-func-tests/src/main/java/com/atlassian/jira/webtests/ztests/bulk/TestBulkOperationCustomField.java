package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.FIELDS })
public class TestBulkOperationCustomField extends JIRAWebTest
{
    private static final String TABLE_EDITFIELDS_ID = "screen-tab-1-editfields";
    private static final String CHANGE_CF_FOR_PROJECT_A = "Change CF for project A";
    private static final String CHANGE_CF_FOR_PROJECT_B = "Change CF for project B";
    private static final String FIELD_NOT_AVAILABLE = "The field is not available for all issues with the same configuration.";
    private static final String TABLE_UNAVAILABLE_ACTIONS_ID = "unavailableActionsTable";

    public TestBulkOperationCustomField(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestBulkOperationCustomField.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    /**
     * Tests the availability of customfields with different contexts in bulk edit
     */
    public void testBulkEdit()
    {
        displayAllIssues();
        bulkChangeIncludeAllPages();
        selectAllIssuesToEdit();
        submit("Next");
        checkCheckbox(FIELD_OPERATION, RADIO_OPERATION_EDIT);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_EDIT);
        submit("Next");

        assertTextNotInElement(TABLE_UNAVAILABLE_ACTIONS_ID, CHANGE_CF_FOR_PROJECT_A);
        assertTextNotInElement(TABLE_UNAVAILABLE_ACTIONS_ID, CHANGE_CF_FOR_PROJECT_B);
        assertTextNotInElement(TABLE_UNAVAILABLE_ACTIONS_ID, FIELD_NOT_AVAILABLE);
    }

    private void selectAllIssuesToEdit()
    {
        tester.setWorkingForm("bulkedit");
        WebForm form = tester.getDialog().getForm();
        String[] parameterNames = form.getParameterNames();
        for (String name : parameterNames)
        {
            if (name.startsWith("bulkedit_"))
            {
                checkCheckbox(name);
            }
        }
    }

    /**
     * Tests the availability of customfields with different contexts in bulk workflow transition
     */
    public void testBulkTransition()
    {
        displayAllIssues();
        bulkChangeIncludeAllPages();
        selectAllIssuesToEdit();
        submit("Next");
        checkCheckbox(FIELD_OPERATION, RADIO_OPERATION_WORKFLOW);
        assertRadioOptionSelected(FIELD_OPERATION, RADIO_OPERATION_WORKFLOW);
        submit("Next");

        checkCheckbox("wftransition", "classic default workflow_5_5"); //resolve issue
        submit("Next");

        try
        {
            WebTable editFieldsTable = getDialog().getResponse().getTableWithID(TABLE_EDITFIELDS_ID);
            assertTextInTable(TABLE_EDITFIELDS_ID, CHANGE_CF_FOR_PROJECT_A);
            assertTextInTable(TABLE_EDITFIELDS_ID, CHANGE_CF_FOR_PROJECT_B);
            assertTextInTable(TABLE_EDITFIELDS_ID, FIELD_NOT_AVAILABLE);
            for (int i = 0; i < editFieldsTable.getRowCount(); i++)
            {
                //skip the first column (checkbox and 'N/A')
                String field = editFieldsTable.getCellAsText(i, 1).trim();
                if (field.equals(CHANGE_CF_FOR_PROJECT_A))
                {
                    assertTrue(editFieldsTable.getCellAsText(i, 2).trim().indexOf(FIELD_NOT_AVAILABLE) != -1);
                }
                else if (field.equals(CHANGE_CF_FOR_PROJECT_B))
                {
                    assertTrue(editFieldsTable.getCellAsText(i, 2).trim().indexOf(FIELD_NOT_AVAILABLE) != -1);
                }
            }
        }
        catch (SAXException e)
        {
        }
    }
}
