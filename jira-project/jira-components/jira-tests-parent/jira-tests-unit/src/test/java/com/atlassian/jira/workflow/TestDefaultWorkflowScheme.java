package com.atlassian.jira.workflow;

import java.util.Collections;
import java.util.Locale;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.util.NoopI18nHelper.makeTranslation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.2
 */
public class TestDefaultWorkflowScheme
{
    private DefaultWorkflowScheme workflowScheme;

    @Before
    public void setUp() throws Exception
    {
        MockSimpleAuthenticationContext authCtx = new MockSimpleAuthenticationContext(new MockUser("something"), Locale.ENGLISH, new NoopI18nHelper());
        workflowScheme = new DefaultWorkflowScheme(authCtx);
    }

    @Test
    public void getName()
    {
        assertEquals(makeTranslation("admin.schemes.workflows.default"), workflowScheme.getName());
    }

    @Test
    public void getDescription()
    {
        assertEquals(makeTranslation("admin.schemes.workflows.default.desc"), workflowScheme.getDescription());
    }

    @Test
    public void getId()
    {
        assertNull(workflowScheme.getId());
    }

    @Test
    public void getMappings()
    {
        assertEquals(Collections.singletonMap(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME), workflowScheme.getMappings());
    }

    @Test
    public void getWorkflow()
    {
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, workflowScheme.getActualWorkflow(null));
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, workflowScheme.getActualWorkflow(""));
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, workflowScheme.getActualWorkflow("antghrerj"));
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, workflowScheme.getActualWorkflow("skdlaksdlasd"));
    }

    @Test
    public void getDefault()
    {
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, workflowScheme.getActualDefaultWorkflow());
    }

    @Test
    public void isDefault()
    {
        assertTrue(workflowScheme.isDefault());
    }

    @Test
    public void isDraft()
    {
        assertFalse(workflowScheme.isDraft());
    }

    @Test
    public void builder()
    {
        AssignableWorkflowScheme.Builder newBuilder = workflowScheme.builder();
        AssignableWorkflowScheme scheme = newBuilder.build();

        assertEquals(makeTranslation("admin.schemes.workflows.default"), scheme.getName());
        assertEquals(makeTranslation("admin.schemes.workflows.default.desc"), scheme.getDescription());
        assertFalse(scheme.isDefault());
        assertFalse(scheme.isDraft());
        assertEquals(Collections.singletonMap(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME), scheme.getMappings());
    }
}
