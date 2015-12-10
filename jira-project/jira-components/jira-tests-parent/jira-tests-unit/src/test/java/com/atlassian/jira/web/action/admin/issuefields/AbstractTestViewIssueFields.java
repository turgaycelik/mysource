/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.EnvironmentSystemField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.SummaryField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldDescriptionHelper;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractTestViewIssueFields
{
    protected I18nHelper i18Helper;
    protected User mockUser;

    @Mock
    @AvailableInContainer
    protected ReindexMessageManager reindexMessageManager;

    @Mock
    @AvailableInContainer
    protected FieldLayoutSchemeHelper fieldLayoutSchemeHelper;

    @Mock
    @AvailableInContainer
    protected ManagedConfigurationItemService managedConfigurationItemService;

    @Mock
    @AvailableInContainer
    protected FieldManager fieldManager;

    @AvailableInContainer
    protected RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    public AbstractTestViewIssueFields()
    {
    }

    @Before
    public void setUp() throws Exception
    {
        JiraTestUtil.setupExpectedRedirect("ViewIssueFields.jspa");

        i18Helper = mock(I18nHelper.class);
        mockUser = mock(User.class);

        // note: this is really cheating, but the testing of actions is not my main concern, so we're fudging it.
        when(fieldLayoutSchemeHelper.doesChangingFieldLayoutRequireMessage(Mockito.any(User.class), Mockito.any(EditableFieldLayout.class)))
                .thenReturn(true);
    }

    protected void tearDown() throws Exception
    {
        Mockito.verify(fieldLayoutSchemeHelper);
        Mockito.verify(reindexMessageManager);
    }

    public abstract void setNewVif();

    public abstract AbstractConfigureFieldLayout getVif();

    public abstract void refreshVif();

    @Test
    public void testIsHideable()
    {
        //OrderableField hideableField = (OrderableField) ComponentAccessor.getFieldManager().getField(IssueFieldConstants.ENVIRONMENT);
        OrderableField hideableField = mock(EnvironmentSystemField.class);
        FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setFieldManager(fieldManager)
                .setFieldDescriptionHelper(mock(FieldDescriptionHelper.class))
                .setRendererType("")
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(false)
                .build();
        when(fieldManager.isHideableField(hideableField)).thenReturn(true);
        when(fieldManager.isRequirableField(hideableField)).thenReturn(true);
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertTrue(getVif().isRequirable(fieldLayoutItem));
        fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setFieldManager(fieldManager)
                .setFieldDescriptionHelper(mock(FieldDescriptionHelper.class))
                .setRendererType("")
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(true)
                .setRequired(false)
                .build();
        when(fieldManager.isHideableField(hideableField)).thenReturn(true);
        when(fieldManager.isRequirableField(hideableField)).thenReturn(false);
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertFalse(getVif().isRequirable(fieldLayoutItem));
        fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setFieldManager(fieldManager)
                .setFieldDescriptionHelper(mock(FieldDescriptionHelper.class))
                .setRendererType("")
                .setOrderableField(hideableField)
                .setFieldDescription("the environment field")
                .setHidden(false)
                .setRequired(true)
                .build();
        when(fieldManager.isHideableField(hideableField)).thenReturn(true);
        when(fieldManager.isRequirableField(hideableField)).thenReturn(true);
        assertTrue(getVif().isHideable(fieldLayoutItem));
        assertTrue(getVif().isRequirable(fieldLayoutItem));
        try
        {
            new FieldLayoutItemImpl.Builder()
                    .setFieldManager(fieldManager)
                    .setFieldDescriptionHelper(mock(FieldDescriptionHelper.class))
                    .setRendererType("")
                    .setOrderableField(mock(SummaryField.class))
                    .setFieldDescription("the summary field")
                    .setHidden(true)
                    .setRequired(false)
                    .build();
            fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testHide() throws Exception
    {
        FieldLayoutManager flm = mock(FieldLayoutManager.class);
        EditableDefaultFieldLayout edfl = mock(EditableDefaultFieldLayout.class);
        FieldLayoutItem fli = mock(FieldLayoutItem.class);
        OrderableField orderableField = mock(OrderableField.class);
        when(fieldManager.getFieldLayoutManager()).thenReturn(flm);
        when(flm.getEditableFieldLayout(0L)).thenReturn(edfl);
        when(flm.getEditableDefaultFieldLayout()).thenReturn(edfl);
        when(edfl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fli.getOrderableField()).thenReturn(orderableField);
        when(orderableField.getId()).thenReturn("1");
        when(fieldManager.isHideableField(orderableField)).thenReturn(true);
        getVif().setHide(new Integer(0));

        getVif().doHide();
        verify(edfl).hide(fli);
        verify(edfl,never()).makeRequired(fli);
        refreshVif();
    }

    @Test
    public void testHideNonHideable() throws Exception
    {
        FieldLayoutManager flm = mock(FieldLayoutManager.class);
        OrderableField orderableField = mock(OrderableField.class);
        EditableDefaultFieldLayout edfl = mock(EditableDefaultFieldLayout.class);
        FieldLayoutItem fli = mock(FieldLayoutItem.class);
        when(fieldManager.isHideableField(orderableField)).thenReturn(false);
        when(fieldManager.getFieldLayoutManager()).thenReturn(flm);
        when(flm.getEditableFieldLayout(0L)).thenReturn(edfl);
        when(flm.getEditableDefaultFieldLayout()).thenReturn(edfl);
        when(edfl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fli.getOrderableField()).thenReturn(orderableField);
        when(orderableField.getId()).thenReturn("1");
        when(orderableField.getName()).thenReturn("summary");
        when(i18Helper.getText("admin.errors.fieldlayout.cannot.hide.this.field", "'1'")).thenReturn("cant hide 1");
        int position = 0;

        getVif().setHide(new Integer(position));

        final String result = getVif().doHide();
        assertEquals(Action.ERROR, result);
        assertFalse(getVif().getErrorMessages().isEmpty());
        assertEquals(1, getVif().getErrorMessages().size());
        assertEquals("cant hide 1", getVif().getErrorMessages().iterator().next());
    }

    @Test
    public void testMakeRequired() throws Exception
    {
        FieldLayoutManager flm = mock(FieldLayoutManager.class);
        OrderableField orderableField = mock(OrderableField.class);
        EditableDefaultFieldLayout edfl = mock(EditableDefaultFieldLayout.class);
        FieldLayoutItem fli = mock(FieldLayoutItem.class);
        when(fieldManager.isRequirableField(orderableField)).thenReturn(true);
        when(fieldManager.getFieldLayoutManager()).thenReturn(flm);
        when(flm.getEditableFieldLayout(0L)).thenReturn(edfl);
        when(flm.getEditableDefaultFieldLayout()).thenReturn(edfl);
        when(edfl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fli.getOrderableField()).thenReturn(orderableField);
        when(orderableField.getId()).thenReturn("1");
        when(orderableField.getName()).thenReturn("summary");
        when(i18Helper.getText("admin.errors.fieldlayout.cannot.hide.this.field", "'1'")).thenReturn("cant hide 1");
        int position = 0;
        getVif().setRequire(new Integer(0));


        getVif().doRequire();
        refreshVif();

        verify(edfl,never()).hide(fli);
        verify(edfl).makeRequired(fli);
    }

    @Test
    public void testHideNonMandatoriable() throws Exception
    {
        FieldLayoutManager flm = mock(FieldLayoutManager.class);
        OrderableField orderableField = mock(OrderableField.class);
        EditableDefaultFieldLayout edfl = mock(EditableDefaultFieldLayout.class);
        FieldLayoutItem fli = mock(FieldLayoutItem.class);
        when(fieldManager.isRequirableField(orderableField)).thenReturn(false);
        when(fieldManager.getFieldLayoutManager()).thenReturn(flm);
        when(flm.getEditableFieldLayout(0L)).thenReturn(edfl);
        when(flm.getEditableDefaultFieldLayout()).thenReturn(edfl);
        when(edfl.getFieldLayoutItems()).thenReturn(Lists.newArrayList(fli));
        when(fli.getOrderableField()).thenReturn(orderableField);
        when(orderableField.getId()).thenReturn("1");
        when(orderableField.getName()).thenReturn("summary");
        when(orderableField.getNameKey()).thenReturn("summaryKey");
        when(i18Helper.getText("summaryKey")).thenReturn("summary");
        when(i18Helper.getText("admin.errors.fieldlayout.cannot.make.this.field.optional", "'summary'")).thenReturn("cant require 1");
        int position = 0;
        getVif().setRequire(new Integer(0));

        final String aResult = getVif().doRequire();
        assertEquals(Action.ERROR, aResult);
        assertFalse(getVif().getErrorMessages().isEmpty());
        assertEquals(1, getVif().getErrorMessages().size());
        assertEquals("cant require 1", getVif().getErrorMessages().iterator().next());
    }
}
