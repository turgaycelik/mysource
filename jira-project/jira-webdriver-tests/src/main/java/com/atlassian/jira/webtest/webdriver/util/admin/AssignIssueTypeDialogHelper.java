package com.atlassian.jira.webtest.webdriver.util.admin;

import com.atlassian.jira.pageobjects.dialogs.admin.AbstractAssignIssueTypesDialog;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.pageobjects.dialogs.admin.AbstractAssignIssueTypesDialog.CheckBoxState.CHECKED;
import static com.atlassian.jira.pageobjects.dialogs.admin.AbstractAssignIssueTypesDialog.CheckBoxState.INDETERMINATE;
import static com.atlassian.jira.pageobjects.dialogs.admin.AbstractAssignIssueTypesDialog.CheckBoxState.UNCHECKED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
* @since v6.0
*/
public abstract class AssignIssueTypeDialogHelper<T>
{
    private Map<String, T> issueTypeMapping = Maps.newLinkedHashMap();
    private ListMultimap<T, String> dataMapping = ArrayListMultimap.create();
    private AbstractAssignIssueTypesDialog<?> dialog;
    private T excludeData;
    private boolean back;

    public AssignIssueTypeDialogHelper()
    {
    }

    public AssignIssueTypeDialogHelper<T> dialog(AbstractAssignIssueTypesDialog<?> dialog)
    {
        this.dialog = dialog;
        return this;
    }

    public AssignIssueTypeDialogHelper<T> addMapping(String issueType, T data)
    {
        issueTypeMapping.put(issueType, data);
        dataMapping.put(data, issueType);
        return this;
    }

    public AssignIssueTypeDialogHelper exclude(T data)
    {
        excludeData = data;
        return this;
    }

    public AssignIssueTypeDialogHelper withBack()
    {
        back = true;
        return this;
    }

    public AssignIssueTypeDialogHelper<T> withoutBack()
    {
        back = false;
        return this;
    }

    public AssignIssueTypeDialogHelper<T> assertDialog()
    {
        assertEquals(back, dialog.hasBackButton());

        List<String> expectedTypes = getAllIssueTypes();
        if (excludeData != null)
        {
            expectedTypes.removeAll(dataMapping.get(excludeData));
        }

        assertEquals(expectedTypes, dialog.getIssueTypeNames());

        //Make sure the workflows are reported correctly in the dialog.
        for (AbstractAssignIssueTypesDialog.IssueTypeEntry entry : dialog.getIssueTypes())
        {
            final String expectedData = asDisplayed(issueTypeMapping.get(entry.getIssueType()));
            assertEquals(expectedData, entry.getAssignedWorkflow());
        }
        assertSelectAll(expectedTypes);
        assertRemoveWorkflowReported();
        return this;
    }

    private void assertSelectAll(List<String> allTypes)
    {
        Set<String> allWorkflows = dialog.getWorkflows();

        //Should be unchecked.
        assertEquals(UNCHECKED, dialog.getSelectAllState());
        assertTrue(dialog.getSelectedIssueTypeNames().isEmpty());
        assertTrue(dialog.getWarningWorkflows().isEmpty());

        //Should select call.
        dialog.clickSelectAll();
        assertEquals(CHECKED, dialog.getSelectAllState());
        assertEquals(allTypes, dialog.getSelectedIssueTypeNames());
        assertEquals(allWorkflows, dialog.getWarningWorkflows());

        //Select the first issue type.
        AbstractAssignIssueTypesDialog.IssueTypeEntry entry = dialog.getIssueTypes().get(0);
        List<String> selected = Lists.newArrayList(allTypes);
        selected.remove(entry.getIssueType());
        entry.deselect();

        assertEquals(INDETERMINATE, dialog.getSelectAllState());
        assertEquals(selected, dialog.getSelectedIssueTypeNames());

        //Clicking on INDETERMINATE should now select all.
        dialog.clickSelectAll();

        assertEquals(CHECKED, dialog.getSelectAllState());
        assertEquals(allTypes, dialog.getSelectedIssueTypeNames());
        assertEquals(allWorkflows, dialog.getWarningWorkflows());

        //Checking on CHECKED should select none.
        dialog.clickSelectAll();
        assertEquals(UNCHECKED, dialog.getSelectAllState());
        assertTrue(dialog.getSelectedIssueTypeNames().isEmpty());
        assertTrue(dialog.getWarningWorkflows().isEmpty());

        //Select the last issue type and make sure we end up in INDETERMINATE STATE.
        entry = dialog.getIssueTypes().get(dialog.getIssueTypes().size() - 1);
        selected = Collections.singletonList(entry.getIssueType());
        entry.select();

        assertEquals(INDETERMINATE, dialog.getSelectAllState());
        assertEquals(selected, dialog.getSelectedIssueTypeNames());

        entry.deselect();

        assertEquals(UNCHECKED, dialog.getSelectAllState());
        assertTrue(dialog.getSelectedIssueTypeNames().isEmpty());
    }

    private Map<String, AbstractAssignIssueTypesDialog.IssueTypeEntry> assertRemoveWorkflowReported()
    {
        //Set the issue types such that one workflow is reported as being deleted.
        Map<T, Integer> countMap = Maps.newHashMap();
        Map<String, AbstractAssignIssueTypesDialog.IssueTypeEntry> typesMap = Maps.newHashMap();
        for (AbstractAssignIssueTypesDialog.IssueTypeEntry entry : dialog.getIssueTypes())
        {
            typesMap.put(entry.getIssueType(), entry);
        }

        int selectedCount = 0;
        final int maxSelected = typesMap.size();

        for (Map.Entry<T, Collection<String>> entry : dataMapping.asMap().entrySet())
        {
            final T data = entry.getKey();
            final List<String> issueTypes = (List<String>) entry.getValue();
            if (data.equals(excludeData))
            {
                continue;
            }

            countMap.put(data, issueTypes.size());
            assertFalse(dialog.getFinishEnabled());
            assertEquals(UNCHECKED, dialog.getSelectAllState());

            for (Iterator<String> issueTypeIterator = issueTypes.iterator(); issueTypeIterator.hasNext(); )
            {
                String issueType = issueTypeIterator.next();
                typesMap.get(issueType).select();
                selectedCount++;

                assertTrue(dialog.getFinishEnabled());
                if (selectedCount < maxSelected)
                {
                    assertEquals(INDETERMINATE, dialog.getSelectAllState());
                }
                else
                {
                    assertEquals(CHECKED, dialog.getSelectAllState());
                }

                final boolean removeWorkflow = !issueTypeIterator.hasNext();
                assertEquals(removeWorkflow ? Collections.singleton(asDisplayed(data)) : Collections.emptySet(),
                        dialog.getWarningWorkflows());
            }

            for (Iterator<String> iterator = Lists.reverse(issueTypes).iterator(); iterator.hasNext(); )
            {
                String issueType = iterator.next();
                typesMap.get(issueType).deselect();
                selectedCount--;

                assertEquals(Collections.emptySet(), dialog.getWarningWorkflows());
                if (iterator.hasNext())
                {
                    assertTrue(dialog.getFinishEnabled());
                    assertEquals(INDETERMINATE, dialog.getSelectAllState());
                }
                else
                {
                    assertFalse(dialog.getFinishEnabled());
                    assertEquals(UNCHECKED, dialog.getSelectAllState());
                }
            }
        }

        //Set all issue types to ensure that multiple projects are reported as removed.
        Set<String> deleteWorkflows = Sets.newHashSet();
        for (String issueType : dialog.getIssueTypeNames())
        {
            typesMap.get(issueType).select();
            selectedCount++;
            assertTrue(dialog.getFinishEnabled());
            if (selectedCount < maxSelected)
            {
                assertEquals(INDETERMINATE, dialog.getSelectAllState());
            }
            else
            {
                assertEquals(CHECKED, dialog.getSelectAllState());
            }
            final T data = issueTypeMapping.get(issueType);
            Integer count = countMap.get(data);
            //Has no mapping.
            if (count != null)
            {
                count = count - 1;
                if (count == 0)
                {
                    countMap.remove(data);
                    deleteWorkflows.add(asDisplayed(data));
                }
                else
                {
                    countMap.put(data, count);
                }
            }
            assertEquals(deleteWorkflows, dialog.getWarningWorkflows());
        }

        return typesMap;
    }

    abstract protected String asDisplayed(T data);
    abstract protected List<String> getAllIssueTypes();
}
