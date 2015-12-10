package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.WorkflowContext;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.NoopI18nHelper.makeTranslation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestWorkflowUtil
{
    private static final String FIELD_NAME = "fName";

    @Rule public final MockitoContainer mockContainer = new MockitoContainer(this);

    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext
            = new MockSimpleAuthenticationContext(new MockUser("WAT"), Locale.ENGLISH, new NoopI18nHelper());

    @Mock private JiraWorkflow workflow;

    private ErrorCollection errorCollection = new SimpleErrorCollection();

    @Test
    public void shouldReturnFalseWhenNameIsNull()
    {
        final boolean check = WorkflowUtil.isAcceptableName(null, FIELD_NAME, errorCollection);

        assertEquals(makeTranslation("admin.errors.you.must.specify.a.workflow.name"),
                errorCollection.getErrors().get(FIELD_NAME));
        assertFalse(check);
    }

    @Test
    public void shouldReturnFalseWhenNameIsBlank()
    {
        final boolean check = WorkflowUtil.isAcceptableName("   ", FIELD_NAME, errorCollection);

        assertEquals(makeTranslation("admin.errors.you.must.specify.a.workflow.name"),
                errorCollection.getErrors().get(FIELD_NAME));
        assertFalse(check);
    }

    @Test
    public void shouldReturnFalseWhenNameContainsNonASCIILikeCharacters()
    {
        final boolean check = WorkflowUtil.isAcceptableName("workflow \u4321", FIELD_NAME, errorCollection);

        assertEquals(makeTranslation("admin.errors.please.use.only.ascii.characters"),
                errorCollection.getErrors().get(FIELD_NAME));
        assertFalse(check);
    }

    @Test
    public void shouldReturnFalseWhenNameContainsTrailingWhitespaces()
    {
        final boolean check = WorkflowUtil.isAcceptableName("workflowName ", FIELD_NAME, errorCollection);

        assertEquals(makeTranslation("admin.errors.workflow.name.cannot.contain.leading.or.trailing.whitespaces"),
                errorCollection.getErrors().get(FIELD_NAME));
        assertFalse(check);
    }

    @Test
    public void shouldReturnTrueAndAddNoErrorWheNameIsOK()
    {
        final boolean check = WorkflowUtil.isAcceptableName("workflowName", FIELD_NAME, errorCollection);

        assertNull(errorCollection.getErrors().get(FIELD_NAME));
        assertTrue(check);
    }

    @SuppressWarnings ("ConstantConditions")
    @Test (expected = NullPointerException.class)
    public void shouldReturnThrowExceptionWhenErrorCollectionIsNull()
    {
        WorkflowUtil.isAcceptableName(null, FIELD_NAME, null);
    }

    @SuppressWarnings ("ConstantConditions")
    @Test (expected = NullPointerException.class)
    public void shouldReturnThrowExceptionWhenFieldNameIsNull()
    {
        WorkflowUtil.isAcceptableName(null, null, errorCollection);
    }

    @Test
    public void testAddtoExistingInputs()
    {
        List<String> existingList = new ArrayList<String>();
        existingList.add("item1");
        existingList.add("item2");

        Map<String, Object> inputs = Maps.newHashMap();
        inputs.put("Test Key", existingList);

        List<String> toAdd = new ArrayList<String>();
        toAdd.add("item3");
        toAdd.add("item4");

        WorkflowUtil.addToExistingTransientArgs(inputs, "Test Key", toAdd);
        existingList.addAll(toAdd);
        assertEquals(existingList, inputs.get("Test Key"));
    }

    @Test
    public void testAddtoEmtpyListInputs()
    {
        Map<String, Object> inputs = new HashMap<String, Object>();
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("item3");
        toAdd.add("item4");

        WorkflowUtil.addToExistingTransientArgs(inputs, "Test Key", toAdd);
        assertEquals(toAdd, inputs.get("Test Key"));
    }

    @Test
    public void testGetWorkflowDisplayNameWithNullWorkflow()
    {
        String displayName = WorkflowUtil.getWorkflowDisplayName(null);
        assertNull(displayName);
    }

    @Test
    public void testGetWorkflowDisplayNameNormalWorkflow()
    {
        when(workflow.isDraftWorkflow()).thenReturn(false);
        when(workflow.getName()).thenReturn("Gregory");

        String displayName = WorkflowUtil.getWorkflowDisplayName(workflow);
        assertEquals("Gregory", displayName);
    }

    @Test
    public void testGetWorkflowDisplayNameDraftWorkflow()
    {
        when(workflow.isDraftWorkflow()).thenReturn(true);
        when(workflow.getName()).thenReturn("Gregory");

        String displayName = WorkflowUtil.getWorkflowDisplayName(workflow);
        final String expectedName = String.format("Gregory (%s)", makeTranslation("common.words.draft"));
        assertEquals(expectedName, displayName);
    }

    @SuppressWarnings ("deprecation")
    @Test
    public void testInterpolateProjectKey() throws Exception
    {
        GenericValue gvProject = new MockGenericValue("Project", 12L);
        gvProject.setString("key", "TST");
        assertEquals(null, WorkflowUtil.interpolateProjectKey(null, null));
        assertEquals(null, WorkflowUtil.interpolateProjectKey(gvProject, null));
        assertEquals("blah", WorkflowUtil.interpolateProjectKey(gvProject, "blah"));
        assertEquals("blahTST", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}"));
        assertEquals("TSTblah", WorkflowUtil.interpolateProjectKey(gvProject, "${pkey}blah"));
        assertEquals("blahTSTblah", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}blah"));
        // Only works for first
        assertEquals("blahTSTblah${pkey}", WorkflowUtil.interpolateProjectKey(gvProject, "blah${pkey}blah${pkey}"));

        // Actually this is broken?
        assertEquals("blahTST", WorkflowUtil.interpolateProjectKey(gvProject, "blah${SOME RUBBISH}"));
    }

    @SuppressWarnings ("deprecation")
    @Test
    public void testReplaceProjectKey() throws Exception
    {
        Project project = new MockProject(12L, "TST");
        assertEquals(null, WorkflowUtil.replaceProjectKey(null, null));
        assertEquals(null, WorkflowUtil.replaceProjectKey(project, null));
        assertEquals("blah", WorkflowUtil.replaceProjectKey(project, "blah"));
        assertEquals("blahTST", WorkflowUtil.replaceProjectKey(project, "blah${pkey}"));
        assertEquals("TSTblah", WorkflowUtil.replaceProjectKey(project, "${pkey}blah"));
        assertEquals("blahTSTblah", WorkflowUtil.replaceProjectKey(project, "blah${pkey}blah"));
        // Only works for first
        assertEquals("blahTSTblah${pkey}", WorkflowUtil.replaceProjectKey(project, "blah${pkey}blah${pkey}"));

        assertEquals("blah${SOME RUBBISH}", WorkflowUtil.replaceProjectKey(project, "blah${SOME RUBBISH}"));
    }

    @Test
    public void isReservedKey()
    {
        assertThat(WorkflowUtil.isReservedKey("jira.reserved"), Matchers.equalTo(true));
        assertThat(WorkflowUtil.isReservedKey(" jira.reserved "), Matchers.equalTo(true));

        for (String s : JiraWorkflow.JIRA_META_ATTRIBUTE_ALLOWED_LIST)
        {
            assertThat(WorkflowUtil.isReservedKey(s), Matchers.equalTo(false));
            assertThat(WorkflowUtil.isReservedKey(s + ".extra"), Matchers.equalTo(false));
        }

        assertThat(WorkflowUtil.isReservedKey("other"), Matchers.equalTo(false));
        assertThat(WorkflowUtil.isReservedKey("<>"), Matchers.equalTo(false));
        assertThat(WorkflowUtil.isReservedKey(null), Matchers.equalTo(false));
    }

    @Test
    public void getCallerKeyGetsCallerKeyFromWorkflowContext()
    {
        final String test_user_key = "test_user_key";
        final WorkflowContext workflowContext = mock(WorkflowContext.class);
        when(workflowContext.getCaller()).thenReturn(test_user_key);
        final ImmutableMap<String, WorkflowContext> transientVars = ImmutableMap.of("context", workflowContext);

        final String actualUserKey = WorkflowUtil.getCallerKey(transientVars);
        assertEquals(test_user_key, actualUserKey);
    }

    @Test
    public void getCallerKeyReturnsNullWhenTransientVarsAreNull()
    {
        assertNull(WorkflowUtil.getCallerKey(null));
    }

    @Test
    public void getCallerKeyReturnsNullWhenContextIsNull()
    {
        final Map<String, WorkflowContext> transientVars = Maps.newHashMap();
        transientVars.put("context", null);
        assertNull(WorkflowUtil.getCallerKey(transientVars));
    }
}
