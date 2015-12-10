/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestWorkflowStateCheck
{
    @Mock
    private OfBizDelegator ofBizDelegator;
    private WorkflowStateCheck wfCheck;
    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());

        // Create three issue or which two do no have a project.
        List<GenericValue> issues = new ArrayList<GenericValue>();
        issues.add(new MockGenericValue("Issue", FieldMap.build("workflowId", new Long(1001), "key", "ABC-1")));
        issues.add(new MockGenericValue("Issue", FieldMap.build("workflowId", new Long(1002), "key", "ABC-2")));
        issues.add(new MockGenericValue("Issue", FieldMap.build("workflowId", new Long(1003), "key", "ABC-3")));

        when(ofBizDelegator.findListIteratorByCondition("Issue", null, null, Arrays.asList("workflowId", "project", "number"), null, null)).thenReturn(new MockOfBizListIterator(issues));

        List<GenericValue> workFlowEntries = new ArrayList<GenericValue>();
        workFlowEntries.add(new MockGenericValue("OSWorkflowEntry", FieldMap.build("id", new Long(1001), "state", new Integer(1))));
        workFlowEntries.add(new MockGenericValue("OSWorkflowEntry", FieldMap.build("id", new Long(1002), "state", new Integer(0))));
        workFlowEntries.add(new MockGenericValue("OSWorkflowEntry", FieldMap.build("id", new Long(1003))));

        when(ofBizDelegator.findByAnd("OSWorkflowEntry", FieldMap.build("id", new Long(1001)))).thenReturn(workFlowEntries);

        wfCheck = new WorkflowStateCheck(ofBizDelegator, 1);

    }

    @Test
    public void testPreview() throws IntegrityException
    {
        List<CheckAmendment> amendments = wfCheck.preview();
        assertEquals(2, amendments.size());
        assertEquals("ABC-1 - The workflow entry with id 1002 has an incorrect state.", amendments.get(0).getMessage());
    }

    @Test
    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by removing the issues with no project
        List<CheckAmendment> amendments = wfCheck.correct();
        assertEquals(2, amendments.size());
        assertEquals("ABC-1 - The workflow entry with id 1002 has been reset to the ACTIVATED state.", amendments.get(0).getMessage());

        // This should return no amendments as they have just been corrected.
        amendments = wfCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        this.wfCheck = null;
        this.ofBizDelegator = null;

    }
}
