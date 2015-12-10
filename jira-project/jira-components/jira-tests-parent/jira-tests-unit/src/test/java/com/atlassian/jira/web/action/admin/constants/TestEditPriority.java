/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.action.admin.priorities.EditPriority;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestEditPriority
{
    private EditPriority editPriorityAction;
    private GenericValue constant;
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();
    @AvailableInContainer
    @Mock
    private PriorityManager priorityManager;
    @AvailableInContainer
    @Mock
    private ConstantsManager constantsManager;
    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    public TestEditPriority()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        // use priorities to test the abstract class
        editPriorityAction = new EditPriority(priorityManager);
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());
    }

    @Test
    public void testGetConstant() throws GenericEntityException
    {
        Assert.assertNull(editPriorityAction.getConstant());
        editPriorityAction.setId("1");
        Assert.assertNull(editPriorityAction.getConstant());

        final Priority priority = (Priority) makeConstant();
        when(constantsManager.getPriorityObject("1")).thenReturn(priority);

        Assert.assertEquals(constant, editPriorityAction.getConstant());
    }

    @Test
    public void testValidation() throws Exception
    {
        editPriorityAction.setId("1");

        String result = editPriorityAction.execute();

        Assert.assertEquals(Action.INPUT, result);
        Assert.assertEquals(1, editPriorityAction.getErrorMessages().size());
        Assert.assertEquals("Specified constant does not exist.", editPriorityAction.getErrorMessages().iterator().next());
        Assert.assertEquals(3, editPriorityAction.getErrors().size());
        Assert.assertEquals("You must specify a URL for the icon of the constant.", editPriorityAction.getErrors().get("iconurl"));
        Assert.assertEquals("You must specify a name.", editPriorityAction.getErrors().get("name"));
        Assert.assertEquals("You must specify a value for the priority colour.", editPriorityAction.getErrors().get("statusColor"));
    }

    @Test
    public void testDefault() throws Exception
    {
        final Priority priority = (Priority) makeConstant();
        when(constantsManager.getPriorityObject("1")).thenReturn(priority);
        editPriorityAction.setId("1");

        editPriorityAction.doDefault();
        Assert.assertEquals("TEST", editPriorityAction.getName());
        Assert.assertEquals("This is a test Constant", editPriorityAction.getDescription());
        Assert.assertEquals("C:\test", editPriorityAction.getIconurl());
    }

    @Test
    public void testExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPriorities.jspa");

        final Priority priority = (Priority) makeConstant();
        when(constantsManager.getPriorityObject("1")).thenReturn(priority);
        when(priorityManager.getPriority("1")).thenReturn(priority);
        editPriorityAction.setId("1");

        editPriorityAction.setName("MODIFIED");
        editPriorityAction.setDescription("Description Modified");
        editPriorityAction.setIconurl("c:\test");
        editPriorityAction.setStatusColor("#ff0000");

        String result = editPriorityAction.execute();
        Assert.assertEquals(Action.NONE, result);

        verify(priorityManager).editPriority(priority, "MODIFIED", "Description Modified", "c:\test", "#ff0000");

        response.verify();
    }

    private IssueConstant makeConstant() throws GenericEntityException
    {
        constant = new MockGenericValue("Priority",
                new FieldMap().add("id", "1").add("name", "TEST").add("description", "This is a test Constant").add("sequence", new Long(1)).add("iconurl", "C:\test"));
        return new PriorityImpl(constant, null, null, null);
    }
}
