package com.atlassian.jira.issue.fields.config;

import java.util.Map;
import java.util.Set;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TestFieldConfigSchemeImpl
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private OfBizDelegator delegator = new MockOfBizDelegator();

    @Mock
    @AvailableInContainer
    private ConstantsManager constantsManager;

    @Mock
    private FieldConfigContextPersister mockContextPersister;
    @Mock
    private JiraContextNode contextNode;

    private FieldConfigScheme fieldConfigScheme;

    @Before
    public void setUp() throws Exception
    {
        final Map<String, FieldConfig> configMap = MapBuilder.build("1", null);
        when(mockContextPersister.getAllContextsForConfigScheme(Matchers.<FieldConfigScheme>any()))
                .thenReturn(Lists.<JiraContextNode>newArrayList(contextNode));
        fieldConfigScheme = new FieldConfigScheme.Builder()
                .setName("test scheme")
                .setDescription("scheme description")
                .setFieldConfigContextPersister(mockContextPersister)
                .setConfigs(configMap)
                .toFieldConfigScheme();
    }

    @Test
    public void testGetAssociatedIssueTypes()
    {
        final GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", ImmutableMap.of("id", "1", "name", "Bug", "description", "A Bug"));
        when(constantsManager.getIssueType(issueTypeGV.getString("id"))).thenReturn(issueTypeGV);

        final Set<GenericValue> issueTypes = fieldConfigScheme.getAssociatedIssueTypes();

        assertThat("getAssociatedIssueTypes should return one issue", issueTypes, org.hamcrest.Matchers.containsInAnyOrder(issueTypeGV));
    }
}
