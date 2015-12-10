package com.atlassian.jira.plugin.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.UserKeyService;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestUpdateIssueFieldFunctionPluginFactory
{
    @Rule public InitMockitoMocks initMocks = new InitMockitoMocks(this);

    @Mock private FieldManager fieldManager;
    @Mock private UserKeyService userKeyService;
    @Mock private FunctionDescriptor functionDescriptor;

    @Before
    public void setUp()
    {
        when(userKeyService.getKeyForUsername("admin")).thenReturn("admin");
        when(userKeyService.getUsernameForKey("admin")).thenReturn("admin");
    }

    @Test
    public void testGetVelocityParamsForViewAssigneeField() throws Exception
    {
        ImmutableMap<String, String> descriptors =
                ImmutableMap.of(UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_NAME, IssueFieldConstants.ASSIGNEE,
                                UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_VALUE, "admin");
        when(functionDescriptor.getArgs()).thenReturn(descriptors);

        when(fieldManager.getField(IssueFieldConstants.ASSIGNEE)).thenReturn(new TestAssigneeField());
        UpdateIssueFieldFunctionPluginFactory updateIssueFieldFunctionFactory = new UpdateIssueFieldFunctionPluginFactory(fieldManager, userKeyService);

        Map<String, Object> velocityParams = new HashMap<String, Object>();
        updateIssueFieldFunctionFactory.getVelocityParamsForView(velocityParams, functionDescriptor);

        assertThat((String) velocityParams.get(UpdateIssueFieldFunctionPluginFactory.PARAM_FIELD_VALUE), equalTo("admin"));
    }

    @Test
    public void testGetVelocityParamsForViewResolutionField() throws Exception
    {
        ImmutableMap<String, String> descriptors =
                ImmutableMap.of(UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_NAME, IssueFieldConstants.RESOLUTION,
                                UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_VALUE, "1");
        when(functionDescriptor.getArgs()).thenReturn(descriptors);

        when(fieldManager.getField(IssueFieldConstants.RESOLUTION)).thenReturn(new TestResolutionField());
        UpdateIssueFieldFunctionPluginFactory updateIssueFieldFunctionFactory = new UpdateIssueFieldFunctionPluginFactory(fieldManager, userKeyService);

        Map<String, Object> velocityParams = new HashMap<String, Object>();
        updateIssueFieldFunctionFactory.getVelocityParamsForView(velocityParams, functionDescriptor);

        assertThat((String) velocityParams.get(UpdateIssueFieldFunctionPluginFactory.PARAM_FIELD_VALUE), equalTo("Fixed"));
    }

    @Test
    public void testGetVelocityParamsForViewPriorityField() throws Exception
    {
        ImmutableMap<String, String> descriptors =
                ImmutableMap.of(UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_NAME, IssueFieldConstants.PRIORITY,
                                UpdateIssueFieldFunctionPluginFactory.TARGET_FIELD_VALUE, "3");
        when(functionDescriptor.getArgs()).thenReturn(descriptors);

        when(fieldManager.getField(IssueFieldConstants.PRIORITY)).thenReturn(new TestPriorityField());
        UpdateIssueFieldFunctionPluginFactory updateIssueFieldFunctionFactory = new UpdateIssueFieldFunctionPluginFactory(fieldManager, userKeyService);

        Map<String, Object> velocityParams = new HashMap<String, Object>();
        updateIssueFieldFunctionFactory.getVelocityParamsForView(velocityParams, functionDescriptor);

        assertThat((String) velocityParams.get(UpdateIssueFieldFunctionPluginFactory.PARAM_FIELD_VALUE), equalTo("Major"));
    }

    private static class TestAssigneeField extends MockOrderableField
    {
        public TestAssigneeField()
        {
            super(IssueFieldConstants.ASSIGNEE);
        }

        @Override
        public String getNameKey()
        {
            return "issue.field.assignee";
        }

        @Override
        public Object getValueFromParams(Map params) throws FieldValidationException
        {
            return params.get(getId());
        }
    }

    private static class TestResolutionField extends MockOrderableField
    {
        private final Map<String, MockGenericValue> mappings =
                ImmutableMap.of("1", new MockGenericValue(null, ImmutableMap.of("name", "Fixed")));

        public TestResolutionField()
        {
            super(IssueFieldConstants.RESOLUTION);
        }

        @Override
        public String getNameKey()
        {
            return "issue.field.resolution";
        }

        @Override
        public Object getValueFromParams(Map params) throws FieldValidationException
        {
            return mappings.get(params.get(getId()));
        }
    }

    private static class TestPriorityField extends MockOrderableField
    {
        private final Map<String, MockGenericValue> mappings =
                ImmutableMap.of("3", new MockGenericValue(null, ImmutableMap.of("name", "Major")));

        public TestPriorityField()
        {
            super(IssueFieldConstants.PRIORITY);
        }

        @Override
        public String getNameKey()
        {
            return "issue.field.priority";
        }

        @Override
        public Object getValueFromParams(Map params) throws FieldValidationException
        {
            return mappings.get(params.get(getId()));
        }
    }
}
