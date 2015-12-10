package com.atlassian.jira.workflow;

import java.util.Map;

import com.google.common.collect.Maps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v5.2
 */
public class TestAbstractWorkflowScheme
{
    @Test
    public void withExplictDefault()
    {
        final String defaultScheme = "Default";
        final String otherScheme = "Something";
        final String issueType = "one";

        Map<String, String> maps = Maps.newHashMap();
        maps.put(null, defaultScheme);
        maps.put(issueType, otherScheme);

        final TestWorkflowScheme testWorkflowScheme = new TestWorkflowScheme(maps);
        assertEquals(defaultScheme, testWorkflowScheme.getActualWorkflow(null));
        assertEquals(otherScheme, testWorkflowScheme.getActualWorkflow(issueType));
        assertEquals(defaultScheme, testWorkflowScheme.getActualWorkflow(issueType + "Extra"));

        assertEquals(defaultScheme, testWorkflowScheme.getActualDefaultWorkflow());
    }

    @Test
    public void getWorkflowWithImplicitDefault()
    {
        final String otherScheme = "Something";
        final String issueType = "one";

        Map<String, String> maps = Maps.newHashMap();
        maps.put(issueType, otherScheme);

        final TestWorkflowScheme testWorkflowScheme = new TestWorkflowScheme(maps);
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, testWorkflowScheme.getActualWorkflow(null));
        assertEquals(otherScheme, testWorkflowScheme.getActualWorkflow(issueType));
        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, testWorkflowScheme.getActualWorkflow(issueType + "Extra"));

        assertEquals(JiraWorkflow.DEFAULT_WORKFLOW_NAME, testWorkflowScheme.getActualDefaultWorkflow());
    }

    private static class TestWorkflowScheme extends AbstractWorkflowScheme
    {
        TestWorkflowScheme(Map<String, String> workflowMap)
        {
            super(1000L, workflowMap);
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public boolean isDraft()
        {
            return false;
        }

        @Override
        public boolean isDefault()
        {
            return false;
        }
    }
}
