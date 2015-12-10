/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opensymphony.workflow.WorkflowException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestUpdateIssueFieldFunction
{
    @Rule
    public RuleChain mockItAll = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private FieldManager fieldManager;

    @Mock
    @AvailableInContainer
    private FieldLayoutManager fieldLayoutManager;


    @Mock
    private MutableIssue issue;

    @Mock
    private OrderableField orderableField;

    @Mock
    private FieldLayoutItem fieldLayoutItem;

    @Mock
    private FieldLayout fieldLayout;

    private UpdateIssueFieldFunction updateIssueFieldFunction;

    private Map<String, Object> input;

    private User user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("testuser");

        updateIssueFieldFunction = new UpdateIssueFieldFunction();

        input = MapBuilder.<String, Object>newBuilder().add("issue", issue).toHashMap();
    }

    @Test
    public void testUpdateIssueFieldFunctionUpdateStatus() throws Exception
    {
        // given:
        final Map<String, Object> args = ImmutableMap.<String, Object>of(
                "field.name", IssueFieldConstants.STATUS,
                "field.value", "2",
                "username", user.getName());

        // when:
        updateIssueFieldFunction.execute(input, args, null);

        // then:
        verify(issue).setStatusId("2");
    }

    @Test
    public void testUpdateIssueFieldFunctionUpdateTimeSpent() throws GenericEntityException, WorkflowException
    {
        // given:
        final Long value = 20L;
        final Map<String, Object> args = ImmutableMap.<String, Object>of(
                "field.name", IssueFieldConstants.TIME_SPENT,
                "field.value", value.toString(),
                "username", user.getName());
        final Long oldValue = 10L;

        when(issue.getTimeSpent()).thenReturn(oldValue);

        // when:
        updateIssueFieldFunction.execute(input, args, null);

        // then:
        verify(issue).setTimeSpent(value);

        final List<?> changeItems = (List<?>) input.get("changeItems");
        assertEquals(1, changeItems.size());

        final ChangeItemBean cib = (ChangeItemBean) Iterables.getFirst(changeItems, null);
        assertEquals(ChangeItemBean.STATIC_FIELD, cib.getFieldType());
        assertEquals(IssueFieldConstants.TIME_SPENT, cib.getField());
        assertEquals(oldValue.toString(), cib.getFrom());
        assertEquals(oldValue.toString(), cib.getFromString());
        assertEquals(value.toString(), cib.getTo());
        assertEquals(value.toString(), cib.getToString());
    }

    @Test
    public void testUpdateIssueFieldFunctionUpdateTimetracking() throws Exception
    {
        updateField(IssueFieldConstants.TIMETRACKING, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE, "1440", "86400");
    }

    @Test
    public void testUpdateIssueFieldFunctionUpdateField() throws Exception
    {
        final String fieldValue = "test summary";
        final String fieldId = IssueFieldConstants.SUMMARY;
        when(fieldManager.isOrderableField(fieldId)).thenReturn(true);

        updateField(fieldId, fieldId, fieldValue, fieldValue);
    }

    private void updateField(final String fieldId, final String fieldIdArg, final String fieldValue, final String fieldValueArg)
            throws Exception
    {
        final Project project = new MockProject(1);
        final IssueType issueType = new MockIssueType("bug", "Bug");

        final Map<String, Object> args = ImmutableMap.<String, Object>of(
                "field.name", fieldIdArg,
                "field.value", fieldValueArg,
                "username", user.getName());


        when(fieldManager.getOrderableField(fieldId)).thenReturn(orderableField);
        when(fieldLayoutManager.getFieldLayout(project, "bug")).thenReturn(fieldLayout);
        when(fieldLayout.getFieldLayoutItem(orderableField)).thenReturn(fieldLayoutItem);

        when(orderableField.getId()).thenReturn(fieldId);

        final ModifiedValue modifiedValue = new ModifiedValue(null, fieldValue);
        final Map modifiedFields = MapBuilder.<String, Object>newBuilder(fieldId, modifiedValue).toHashMap();

        when(issue.getProjectObject()).thenReturn(project);
        when(issue.getModifiedFields()).thenReturn(modifiedFields);
        when(issue.getIssueTypeObject()).thenReturn(issueType);

        // when:
        updateIssueFieldFunction.execute(input, args, null);

        // then:
        verify(orderableField).populateFromParams(eq(Collections.<String, Object>emptyMap()), anyMap());
        verify(orderableField).updateIssue(fieldLayoutItem, issue, Collections.emptyMap());
    }
}