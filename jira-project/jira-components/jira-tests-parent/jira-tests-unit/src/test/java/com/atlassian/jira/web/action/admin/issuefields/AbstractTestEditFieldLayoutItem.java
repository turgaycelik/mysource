/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionSupport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public abstract class AbstractTestEditFieldLayoutItem
{
    @Mock
    @AvailableInContainer
    protected FieldManager fieldManager;

    @Mock
    @AvailableInContainer
    protected FieldLayoutManager fieldLayoutManager;

    @Mock
    @AvailableInContainer
    protected ManagedConfigurationItemService managedConfigurationItemService;

    @AvailableInContainer
    protected RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    protected I18nHelper i18nHelper;
    protected GenericValue fieldLayout;
    protected FieldLayout fl;
    protected OrderableField orderableField;
    protected EditableDefaultFieldLayout efl;
    protected FieldLayoutItem fli;

    public AbstractTestEditFieldLayoutItem()
    {
    }

    @Before
    public void setUp() throws Exception
    {
        i18nHelper = mock(I18nHelper.class);

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
        when(fieldManager.getFieldLayoutManager()).thenReturn(fieldLayoutManager);
    }

    public abstract AbstractEditFieldLayoutItem getEfli();

    public abstract void setNewEfli();

    @Test
    public void testGetSetDescription()
    {
        getEfli().setDescription("test descirption");
        assertEquals("test descirption", getEfli().getDescription());
    }

    @Test
    public void testDoDefaultBadPosition() throws Exception
    {
        int position = 100;
        getEfli().setPosition(new Integer(position));

        when(i18nHelper.getText("admin.errors.fieldlayout.field.does.not.exist","'100'")).thenReturn("position error");
        String result = getEfli().doDefault();
        assertEquals(ActionSupport.ERROR, result);
        assertEquals(1, getEfli().getErrorMessages().size());
        assertEquals("position error", getEfli().getErrorMessages().iterator().next());
    }

    @Test
    public void testDoExecuteBadPosition() throws Exception
    {
        int position = 100;
        getEfli().setPosition(new Integer(position));
        when(i18nHelper.getText("admin.errors.fieldlayout.field.does.not.exist", "'100'")).thenReturn("position error");

        String result = getEfli().execute();
        assertEquals(ActionSupport.INPUT, result);
        assertEquals(1, getEfli().getErrorMessages().size());
        assertEquals("position error", getEfli().getErrorMessages().iterator().next());
    }

    protected abstract Long getFieldLayoutId();

    @Test
    public void testDoDefault() throws Exception
    {
        // Set Up a field with a description

        String testDescription = "test description";


        OrderableField orderableField = mock(OrderableField.class);
        when(orderableField.getName()).thenReturn("mockname");
        EditableFieldLayout fieldLayout = mock(EditableFieldLayout.class);
        when(fieldLayout.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fli.getRawFieldDescription()).thenReturn(testDescription);
        when(fli.getOrderableField()).thenReturn(orderableField);

        if (getFieldLayoutId() != null)
        {
           long fieldLayoutId = getFieldLayoutId();
            when(fl.getId()).thenReturn(fieldLayoutId);
            when(fieldLayoutManager.getEditableFieldLayout(fieldLayoutId)).thenReturn(efl);
        }

        getEfli().setPosition(0);

        String result = getEfli().doDefault();
        assertEquals(ActionSupport.INPUT, result);
        assertEquals(testDescription, getEfli().getDescription());
        assertEquals("mockname", getEfli().getFieldName());
    }

    @Test
    public abstract void testDoExecute() throws Exception;

    protected abstract void createFieldLayoutItem(String description);
}
