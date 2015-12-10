package com.atlassian.jira.workflow;

import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * @since v5.2
 */
class DefaultWorkflowScheme implements AssignableWorkflowScheme
{
    private static final Map<String,String> WORKFLOW_MAP = Collections.singletonMap(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME);
    private final JiraAuthenticationContext ctx;

    DefaultWorkflowScheme(JiraAuthenticationContext ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public Long getId()
    {
        return null;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return ctx.getI18nHelper().getText("admin.schemes.workflows.default");
    }

    @Override
    public String getDescription()
    {
        return ctx.getI18nHelper().getText("admin.schemes.workflows.default.desc");
    }

    @Override
    public boolean isDraft()
    {
        return false;
    }

    @Override
    public boolean isDefault()
    {
        return true;
    }

    @Nonnull
    @Override
    public String getActualWorkflow(String issueTypeId)
    {
        return JiraWorkflow.DEFAULT_WORKFLOW_NAME;
    }

    @Nonnull
    @Override
    public String getActualDefaultWorkflow()
    {
        return JiraWorkflow.DEFAULT_WORKFLOW_NAME;
    }

    @Nonnull
    @Override
    public Map<String, String> getMappings()
    {
        return WORKFLOW_MAP;
    }

    @Override
    public String getConfiguredDefaultWorkflow()
    {
        return JiraWorkflow.DEFAULT_WORKFLOW_NAME;
    }

    @Override
    public String getConfiguredWorkflow(String issueTypeId)
    {
        return null;
    }

    @Nonnull
    @Override
    public Builder builder()
    {
        return new AssignableWorkflowSchemeBuilder(this);
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode()
    {
        return 0;
    }
}
