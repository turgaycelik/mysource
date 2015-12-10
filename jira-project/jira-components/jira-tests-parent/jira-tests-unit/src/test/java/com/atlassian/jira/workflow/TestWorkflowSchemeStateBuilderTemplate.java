package com.atlassian.jira.workflow;

import org.junit.Test;

import static com.atlassian.jira.workflow.WorkflowSchemeAssertions.assertSetMapping;

/**
 * @since v5.2
 */
public class TestWorkflowSchemeStateBuilderTemplate
{
    @Test
    public void mapping()
    {
        BuilderForTest builder = new BuilderForTest();
        assertSetMapping(builder);
    }

    private static class BuilderForTest extends WorkflowSchemeStateBuilderTemplate<BuilderForTest>
    {
        @Override
        BuilderForTest getThis()
        {
            return this;
        }
    }
}
