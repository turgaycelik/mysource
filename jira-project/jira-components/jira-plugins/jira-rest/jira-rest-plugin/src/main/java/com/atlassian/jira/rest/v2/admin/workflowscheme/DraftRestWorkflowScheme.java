package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.workflow.WorkflowSchemeService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;

import java.util.Map;

/**
 * @since 5.2
 */
public class DraftRestWorkflowScheme extends RestWorkflowScheme
{
    private final WorkflowSchemeService workflowSchemeService;
    private final ApplicationUser user;

    private AssignableWorkflowScheme parent;
    private DraftWorkflowScheme child;

    public DraftRestWorkflowScheme(WorkflowSchemeService workflowSchemeService, ApplicationUser user,
            WorkflowSchemeBeanFactory beanFactory, AssignableWorkflowScheme parent, DraftWorkflowScheme child)
    {
        super(beanFactory);
        this.workflowSchemeService = workflowSchemeService;
        this.user = user;
        this.parent = parent;
        this.child = child;
    }

    @Override
    WorkflowScheme getScheme()
    {
        return child;
    }

    WorkflowSchemeBean asBean()
    {
        return beanFactory.asBean(parent, child);
    }

    @Override
    ServiceOutcome<Void> delete()
    {
        return workflowSchemeService.deleteWorkflowScheme(user, child);
    }

    @Override
    ServiceOutcome<? extends RestWorkflowScheme> update(WorkflowSchemeBean bean)
    {
        final AssignableWorkflowScheme.Builder parentBuilder = parent.builder();
        if (bean.isNameSet())
        {
            parentBuilder.setName(bean.getName());
        }
        if (bean.isDescriptionSet())
        {
            parentBuilder.setDescription(bean.getDescription());
        }

        AssignableWorkflowScheme newParent = parentBuilder.build();
        ServiceOutcome<Void> validateOutcome = workflowSchemeService.validateUpdateWorkflowScheme(user, newParent);
        if (!validateOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(validateOutcome);
        }

        Map<String, String> map = getWorkflowMap(child, bean);
        final DraftWorkflowScheme newChild = child.builder().setMappings(map).build();
        ServiceOutcome<DraftWorkflowScheme> draftSchemeOutcome = workflowSchemeService.updateWorkflowScheme(user, newChild);
        if (!draftSchemeOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(draftSchemeOutcome);
        }
        child = draftSchemeOutcome.getReturnedValue();

        ServiceOutcome<AssignableWorkflowScheme> parentOutcome = workflowSchemeService.updateWorkflowScheme(user, newParent);
        if (!parentOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(parentOutcome);
        }
        parent = parentOutcome.getReturnedValue();
        return ServiceOutcomeImpl.ok(this);
    }

    ServiceOutcome<DraftRestWorkflowScheme> deleteWorkflow(String workflowName)
    {
        final DraftWorkflowScheme build = child.builder().removeWorkflow(workflowName).build();
        return doUpdateMappings(build);
    }

    ServiceOutcome<DraftRestWorkflowScheme> updateWorkflowMappings(WorkflowMappingBean bean)
    {
        final DraftWorkflowScheme.Builder builder = child.builder();
        mergeWorkflowMappings(builder, bean);

        return doUpdateMappings(builder.build());
    }

    ServiceOutcome<? extends RestWorkflowScheme> removeIssueType(String issueType)
    {
        final DraftWorkflowScheme.Builder builder = child.builder();
        builder.removeMapping(issueType);

        return doUpdateMappings(builder.build());
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateIssueTypeMappings(IssueTypeMappingBean bean)
    {
        final DraftWorkflowScheme.Builder builder = child.builder();
        mergeIssueTypeMapping(builder, bean);

        return doUpdateMappings(builder.build());
    }

    ServiceOutcome<? extends RestWorkflowScheme> removeDefault()
    {
        return doUpdateMappings(child.builder().removeDefault().build());
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateDefault(DefaultBean bean)
    {
        final DraftWorkflowScheme.Builder builder = child.builder();
        setDefaultMapping(builder, bean);

        return doUpdateMappings(builder.build());
    }

    private ServiceOutcome<DraftRestWorkflowScheme> doUpdateMappings(DraftWorkflowScheme build)
    {
        if (build.getMappings().equals(child.getMappings()))
        {
            return ServiceOutcomeImpl.ok(this);
        }

        final ServiceOutcome<DraftWorkflowScheme> outcome =
                workflowSchemeService.updateWorkflowScheme(user, build);
        if (!outcome.isValid())
        {
            return ServiceOutcomeImpl.error(outcome);
        }
        else
        {
            child = outcome.getReturnedValue();
            return ServiceOutcomeImpl.ok(this);
        }
    }
}
