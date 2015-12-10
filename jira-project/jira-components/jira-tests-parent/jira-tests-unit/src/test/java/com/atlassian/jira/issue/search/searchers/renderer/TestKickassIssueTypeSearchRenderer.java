package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.MockSubTaskManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
@RunWith (ListeningMockitoRunner.class)
public class TestKickassIssueTypeSearchRenderer
{
    private class TestableIssueTypeSearchRenderer
            extends IssueTypeSearchRenderer
    {
        public TestableIssueTypeSearchRenderer(
                final ApplicationProperties applicationProperties,
                final ConstantsManager constantsManager,
                final IssueTypeSchemeManager issueTypeSchemeManager,
                final OptionSetManager optionSetManager,
                final PermissionManager permissionManager,
                final SimpleFieldSearchConstants searchConstants,
                final String searcherNameKey,
                final SubTaskManager subTaskManager,
                final VelocityTemplatingEngine templatingEngine,
                final VelocityRequestContextFactory velocityRequestContextFactory)
        {
            super(applicationProperties, constantsManager,
                    issueTypeSchemeManager, optionSetManager, permissionManager,
                    searchConstants, searcherNameKey, subTaskManager,
                    templatingEngine, velocityRequestContextFactory);
        }

        @Override
        protected I18nHelper getI18n(final User user)
        {
            return i18nHelper;
        }

        @Override
        protected Collection<Option> getOptionsInSearchContext(
                SearchContext searchContext, User user)
        {
            return validOptions;
        }
    }

    @Mock private ConstantsManager constantsManager;
    private I18nHelper i18nHelper;
    @Mock private IssueTypeSchemeManager issueTypeSchemeManager;
    private SimpleFieldSearchConstants searchConstants;
    @Mock private SearchContext searchContext;
    private IssueTypeSearchRenderer searchRenderer;
    private SubTaskManager subTaskManager;
    private Collection<Option> validOptions;
    private Map<String, Object> velocityParameters;

    @Before
    public void setUp()
    {
        i18nHelper = new MockI18nHelper();
        searchConstants = SystemSearchConstants.forIssueType();
        subTaskManager = new MockSubTaskManager();
        velocityParameters = new HashMap<String, Object>();

        setIssueTypes();

        searchRenderer = new TestableIssueTypeSearchRenderer(null,
                constantsManager, issueTypeSchemeManager, null, null,
                searchConstants, null, subTaskManager, null, null);
    }

    /**
     * The "All Sub-Task Issue Types" option should become invalid if there are
     * no sub-tasks in the search context.
     */
    @Test
    public void testAllSubtasksNoSubtasks()
    {
        FieldValuesHolder fieldValuesHolder = createFieldValuesHolder(
                ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES);

        setIssueTypes(new MockIssueType("bug", "Bug"));
        searchRenderer.addEditParameters(fieldValuesHolder, searchContext, null,
                velocityParameters);

        List<Option> invalidOptions = (List<Option>)velocityParameters.get("invalidOptions");
        assertEquals(1, invalidOptions.size());
        assertEquals(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES,
                invalidOptions.get(0).getId());
    }

    @Test
    public void testEditInvalid()
    {
        setAllIssueTypes(
                new MockIssueType("bug", "Bug"),
                new MockIssueType("story", "Story")
        );

        setVisibleIssueTypes(new MockIssueType("bug", "Bug"));
        searchRenderer.addEditParameters(createFieldValuesHolder("story"),
                searchContext, null, velocityParameters);

        List<Option> invalidOptions = (List<Option>)velocityParameters.get("invalidOptions");
        assertEquals(1, invalidOptions.size());
        assertEquals("story", invalidOptions.get(0).getId());
    }

    /**
     * The "All Sub-Task Issue Types" option shouldn't be shown if there are no
     * sub-task issue types in the search context (in JIRA or in the projects).
     */
    @Test
    public void testEditNoSubtasks()
    {
        setAllIssueTypes(
                new MockIssueType("task", "Task"),
                new MockIssueType("subtask", "Sub-Task", true)
        );

        setVisibleIssueTypes(new MockIssueType("task", "Task"));
        searchRenderer.addEditParameters(createFieldValuesHolder(),
                searchContext, null, velocityParameters);

        List<Option> specialOptions = getSpecialOptions();
        assertEquals(1, specialOptions.size());
        assertEquals(ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                specialOptions.get(0).getId());

        assertEquals(0, getSubtaskOptions().size());
    }

    @Test
    public void testEditSubtasksDisabled()
    {
        setIssueTypes(
                new MockIssueType("bug", "Bug"),
                new MockIssueType("task", "Task"),
                new MockIssueType("subtask", "Sub-Task", true)
        );

        subTaskManager.disableSubTasks();
        searchRenderer.addEditParameters(createFieldValuesHolder(),
                searchContext, null, velocityParameters);

        List<Option> specialOptions = getSpecialOptions();
        assertEquals(1, specialOptions.size());
        assertEquals(ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                specialOptions.get(0).getId());

        List<Option> standardOptions = getStandardOptions();
        assertEquals(2, standardOptions.size());
        assertEquals("bug", standardOptions.get(0).getId());
        assertEquals("task", standardOptions.get(1).getId());

        assertEquals(0, getSubtaskOptions().size());
    }

    @Test
    public void testEditSubtasksEnabled()
    {
        setIssueTypes(
                new MockIssueType("bug", "Bug"),
                new MockIssueType("task", "Task"),
                new MockIssueType("subtask", "Sub-Task", true)
        );

        searchRenderer.addEditParameters(createFieldValuesHolder(),
                searchContext, null, velocityParameters);

        List<Option> specialOptions = getSpecialOptions();
        assertEquals(2, getSpecialOptions().size());
        assertEquals(ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                specialOptions.get(0).getId());
        assertEquals(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES,
                specialOptions.get(1).getId());

        List<Option> standardOptions = getStandardOptions();
        assertEquals(2, standardOptions.size());
        assertEquals("bug", standardOptions.get(0).getId());
        assertEquals("task", standardOptions.get(1).getId());

        List<Option> subtaskOptions = getSubtaskOptions();
        assertEquals(1, subtaskOptions.size());
        assertEquals("subtask", subtaskOptions.get(0).getId());
    }

    @Test
    public void testView()
    {
        setIssueTypes(
                new MockIssueType("bug", "Bug"),
                new MockIssueType("story", "Story")
        );

        FieldValuesHolder fieldValuesHolder = createFieldValuesHolder(
                ConstantsManager.ALL_STANDARD_ISSUE_TYPES,
                ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES,
                "bug",
                "story"
        );

        searchRenderer.addViewParameters(fieldValuesHolder, searchContext, null,
                velocityParameters);

        List selectedIssueTypes = (List)velocityParameters.get("selectedIssueTypes");
        assertEquals(4, selectedIssueTypes.size());
    }

    @Test
    public void testViewInvalid()
    {
        setAllIssueTypes(
                new MockIssueType("task", "Task"),
                new MockIssueType("subtask", "Sub-Task", true)
        );

        FieldValuesHolder fieldValuesHolder = createFieldValuesHolder(
                ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES, "subtask");

        setVisibleIssueTypes(new MockIssueType("task", "Task"));
        searchRenderer.addViewParameters(fieldValuesHolder, searchContext, null,
                velocityParameters);

        List<String> invalidIssueTypes = (List<String>)velocityParameters.get("invalidIssueTypes");
        assertEquals(2, invalidIssueTypes.size());
        assertTrue(invalidIssueTypes.contains("Sub-Task"));

        List<String> selectedIssueTypes = (List<String>)velocityParameters.get("selectedIssueTypes");
        assertEquals(2, selectedIssueTypes.size());
    }

    /**
     * "All Sub-Task Issue Types" is invalid if subtasks are disabled.
     */
    @Test
    public void testViewSubtasksDisabled()
    {
        setIssueTypes(
                new MockIssueType("task", "Task"),
                new MockIssueType("subtask", "Sub-Task", true)
        );

        FieldValuesHolder fieldValuesHolder = createFieldValuesHolder(
                ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES);

        subTaskManager.disableSubTasks();
        searchRenderer.addViewParameters(fieldValuesHolder, searchContext, null,
                velocityParameters);

        List<String> invalidIssueTypes = (List<String>)velocityParameters.get("invalidIssueTypes");
        assertEquals(1, invalidIssueTypes.size());
        assertEquals("common.filters.allsubtaskissuetypes",
                invalidIssueTypes.get(0));
    }

    private FieldValuesHolder createFieldValuesHolder(String... values)
    {
        FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put(searchConstants.getUrlParameter(), values);
        return fieldValuesHolder;
    }

    private List<Option> getSpecialOptions()
    {
        return (List<Option>)velocityParameters.get("specialOptions");
    }

    private List<Option> getStandardOptions()
    {
        return (List<Option>)velocityParameters.get("standardOptions");
    }

    private List<Option> getSubtaskOptions()
    {
        return (List<Option>)velocityParameters.get("subtaskOptions");
    }

    private void setAllIssueTypes(final IssueType... issueTypes)
    {
        when(constantsManager.getAllIssueTypeObjects())
                .thenReturn(Arrays.asList(issueTypes));

        // When convertToConstantObjects is called, use the given issue types.
        when(constantsManager.convertToConstantObjects(eq("IssueType"),
                anyCollection())).thenAnswer(new Answer<List<IssueConstant>>()
        {
            @Override
            public List<IssueConstant> answer(InvocationOnMock invocation)
            {
                Collection ids = (Collection)invocation.getArguments()[1];
                List<IssueConstant> result = new ArrayList<IssueConstant>();

                for (Object id : ids)
                {
                    for (IssueType issueType : issueTypes)
                    {
                        if (issueType.getId().equals(id))
                        {
                            result.add(issueType);
                        }
                    }
                }

                return result;
            }
        });
    }

    private void setIssueTypes(IssueType... issueTypes)
    {
        setAllIssueTypes(issueTypes);
        setVisibleIssueTypes(issueTypes);
    }

    private void setVisibleIssueTypes(IssueType... issueTypes)
    {
        validOptions = Collections2.transform(
                Arrays.asList(issueTypes), new Function<IssueType, Option>()
        {
            @Override
            public Option apply(IssueType input)
            {
                return new IssueConstantOption(input);
            }
        });
    }
}