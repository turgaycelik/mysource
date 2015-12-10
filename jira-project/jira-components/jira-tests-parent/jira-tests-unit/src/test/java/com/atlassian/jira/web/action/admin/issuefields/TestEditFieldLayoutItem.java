/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Lists;
import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionSupport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestEditFieldLayoutItem extends AbstractTestEditFieldLayoutItem
{
    EditDefaultFieldLayoutItem efli;

    public TestEditFieldLayoutItem()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        fieldLayout = mock(GenericValue.class);
        when(fieldLayoutManager.getEditableFieldLayout(1L)).thenReturn(mock(EditableFieldLayout.class));
        when(fieldLayout.getLong("id")).thenReturn(1L);

        orderableField = mock(OrderableField.class);
        fli = mock(FieldLayoutItem.class);
        fl = mock(FieldLayout.class);
        efl = mock(EditableDefaultFieldLayout.class);
        when(fli.getOrderableField()).thenReturn(orderableField);
        when(efl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fl.getFieldLayoutItem(orderableField)).thenReturn(fli);
        when(fl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fieldLayoutManager.getEditableDefaultFieldLayout()).thenReturn(efl);
        when(fieldLayoutManager.getEditableFieldLayout(1L)).thenReturn(efl);
    }

    public AbstractEditFieldLayoutItem getEfli()
    {
        if (efli == null)
        {
            setNewEfli();
        }
        return efli;
    }

    public void setNewEfli()
    {
        efli = new EditDefaultFieldLayoutItem(managedConfigurationItemService, fieldLayoutManager, fieldManager)
        {
            protected I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }

        };
    }

    protected Long getFieldLayoutId()
    {
        return null;
    }

    @Test
    public void testDoExecute() throws Exception
    {
        // Setup expected redirect on success
        MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ViewIssueFields.jspa");
        String testDescription = "test description";

        getEfli().setPosition(new Integer(0));
        getEfli().setDescription(testDescription);

        String result = getEfli().execute();
        assertEquals(ActionSupport.NONE, result);

        Mockito.verify(efl).setDescription(fli,testDescription);

        // Ensure the redirect was received
        mockHttpServletResponse.verify();
    }

    protected void createFieldLayoutItem(String description)
    {
        GenericValue fieldLayout = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Layout", "type", FieldLayoutManager.TYPE_DEFAULT));
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", fieldLayout.getLong("id"), "description", description, "fieldidentifier", IssueFieldConstants.ISSUE_TYPE, "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
    }
}
