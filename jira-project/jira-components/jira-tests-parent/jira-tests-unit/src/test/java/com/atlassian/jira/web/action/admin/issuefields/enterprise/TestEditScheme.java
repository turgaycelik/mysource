/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestEditScheme
{
    private EditFieldLayoutScheme efls;
    @Mock @AvailableInContainer
    private FieldLayoutManager mockFieldLayoutManager;
    @Mock @AvailableInContainer
    private FieldLayoutScheme mockFieldLayoutScheme;
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock @AvailableInContainer
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        efls = new EditFieldLayoutScheme(mockFieldLayoutManager);
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());
    }

    @Test
    public void testGetsSets()
    {
        Long id = new Long(1);
        efls.setId(id);
        assertEquals(id, efls.getId());
        String name = "Test Name";
        efls.setFieldLayoutSchemeName(name);
        assertEquals(name, efls.getFieldLayoutSchemeName());
        String description = "Test Description";
        efls.setFieldLayoutSchemeDescription(description);
        assertEquals(description, efls.getFieldLayoutSchemeDescription());
    }

    @Test
    public void testDoDefaultNoId() throws Exception
    {
        assertEquals(Action.INPUT, efls.doDefault());
        checkSingleElementCollection(efls.getErrorMessages(), "Id is required.");
    }

    @Test
    public void testDoDefaultInvalidId() throws Exception
    {
        Long id = new Long(1);
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(eq(id))).thenReturn(null);
        efls.setId(id);
        assertEquals(Action.INPUT, efls.doDefault());
        checkSingleElementCollection(efls.getErrorMessages(), "Invalid id '" + id + "'.");
    }

    @Test
    public void testDoDefault() throws Exception
    {
        Long id = new Long(1);
        String name = "Test Name";
        when(mockFieldLayoutScheme.getName()).thenReturn(name);
        String description = "Test Description";
        when(mockFieldLayoutScheme.getDescription()).thenReturn(description);
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(eq(id))).thenReturn(mockFieldLayoutScheme);
        efls.setId(id);
        assertEquals(Action.INPUT, efls.doDefault());
        assertEquals(name, efls.getFieldLayoutSchemeName());
        assertEquals(description, efls.getFieldLayoutSchemeDescription());
    }

    @Test
    public void testDoValidation() throws Exception
    {
        assertEquals(Action.INPUT, efls.execute());
        assertEquals(1, efls.getErrors().size());
        assertEquals("You must enter a valid name.", efls.getErrors().get("fieldLayoutSchemeName"));
    }

    @Test
    public void testDoValidationNoId() throws Exception
    {
        efls.setFieldLayoutSchemeName("Some Name");
        assertEquals(Action.INPUT, efls.execute());
        checkSingleElementCollection(efls.getErrorMessages(), "Id is required.");
    }

    @Test
    public void testDoValidationInvalidId() throws Exception
    {
        when(mockFieldLayoutManager.getFieldLayoutSchemes()).thenReturn(Collections.EMPTY_LIST);
        Long id = new Long(1);
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(eq(id))).thenReturn(null);
        efls.setFieldLayoutSchemeName("Some Name");
        efls.setId(id);
        assertEquals(Action.INPUT, efls.execute());
        checkSingleElementCollection(efls.getErrorMessages(), "Invalid id '" + id + "'.");
    }

    @Test
    public void testDoValidationDuplicateName() throws Exception
    {
        String name = "some name";
        when(mockFieldLayoutScheme.getName()).thenReturn(name);
        // Return a different id to cause an error
        when(mockFieldLayoutScheme.getId()).thenReturn(new Long(2));
        Long id = new Long(1);
        FieldLayoutScheme fieldLayoutScheme = mockFieldLayoutScheme;
        when(mockFieldLayoutManager.getFieldLayoutSchemes()).thenReturn(EasyList.build(fieldLayoutScheme));
        efls.setFieldLayoutSchemeName(name);
        efls.setId(id);
        assertEquals(Action.INPUT, efls.execute());
        assertEquals(1, efls.getErrors().size());
        assertEquals("A field configuration scheme with this name already exists.", efls.getErrors().get("fieldLayoutSchemeName"));
    }

    @Test
    public void testDoExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");
        String name = "some name";
        String description = "Test Description";
        Long id = new Long(1);
        when(mockFieldLayoutScheme.getId()).thenReturn(id);
        FieldLayoutScheme fieldLayoutScheme = mockFieldLayoutScheme;
        when(mockFieldLayoutManager.getFieldLayoutSchemes()).thenReturn(EasyList.build(fieldLayoutScheme));
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(eq(id))).thenReturn(fieldLayoutScheme);
        efls.setFieldLayoutSchemeName(name);
        efls.setFieldLayoutSchemeDescription(description);
        efls.setId(id);
        assertEquals(Action.NONE, efls.execute());
        response.verify();
        verify(mockFieldLayoutScheme).setName(eq(name));
        verify(mockFieldLayoutScheme).setDescription(eq(description));
        verify(mockFieldLayoutScheme).store();
    }

    private void checkSingleElementCollection(Collection collection, Object expected)
    {
        assertEquals(1, collection.size());
        assertEquals(expected, collection.iterator().next());
    }
}
