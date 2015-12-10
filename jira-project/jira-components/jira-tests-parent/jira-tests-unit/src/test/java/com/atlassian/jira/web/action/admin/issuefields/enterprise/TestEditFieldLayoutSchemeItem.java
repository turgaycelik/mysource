/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.issuefields.AbstractEditFieldLayoutItem;
import com.atlassian.jira.web.action.admin.issuefields.AbstractTestEditFieldLayoutItem;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import webwork.action.ActionSupport;

import static org.junit.Assert.assertEquals;

@RunWith(ListeningMockitoRunner.class)
public class TestEditFieldLayoutSchemeItem extends AbstractTestEditFieldLayoutItem
{
    private EditFieldLayoutItem editFieldLayoutSchemeItem;

    public TestEditFieldLayoutSchemeItem()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    public AbstractEditFieldLayoutItem getEfli()
    {
        if (editFieldLayoutSchemeItem == null)
        {
            setNewEfli();
        }
        return editFieldLayoutSchemeItem;
    }

    public void setNewEfli()
    {
        editFieldLayoutSchemeItem = new EditFieldLayoutItem(fieldLayoutManager, fieldManager, managedConfigurationItemService)
        {
            @Override
            protected <T> T getComponentInstanceOfType(Class<T> clazz)
            {
                return null;
            }

            @Override
            protected I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }

        };
        editFieldLayoutSchemeItem.setId(fieldLayout.getLong("id"));
    }

    protected Long getFieldLayoutId()
    {
        return fieldLayout.getLong("id");
    }

    @Test
    public void testDoExecute() throws Exception
    {
        // Setup expected redirect on success
        MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ConfigureFieldLayout.jspa?id=" + getFieldLayoutId());
        String testDescription = "test description";

        getEfli().setPosition(new Integer(0));
        getEfli().setDescription(testDescription);

        String result = getEfli().execute();
        assertEquals(ActionSupport.NONE, result);

        Mockito.verify(efl).setDescription(fli,testDescription);

        // Ensure the redirect was received
        mockHttpServletResponse.verify();
    }

    protected void createFieldLayoutItem(String testDescription)
    {
        UtilsForTests.getTestEntity("FieldLayoutItem", EasyMap.build("fieldlayout", getFieldLayoutId(), "description", testDescription, "fieldidentifier", IssueFieldConstants.ISSUE_TYPE, "ishidden", Boolean.FALSE.toString(), "isrequired", Boolean.TRUE.toString()));
    }
}
