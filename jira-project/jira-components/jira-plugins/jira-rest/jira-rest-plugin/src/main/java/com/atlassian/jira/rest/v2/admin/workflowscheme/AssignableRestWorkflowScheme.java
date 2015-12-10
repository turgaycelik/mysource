package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.workflow.WorkflowSchemeService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.google.common.base.Function;
import com.google.common.base.Objects;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @since 5.2
 */
public class AssignableRestWorkflowScheme extends RestWorkflowScheme
{
    private final WorkflowSchemeService workflowSchemeService;
    private final ApplicationUser user;

    private AssignableWorkflowScheme scheme;

    AssignableRestWorkflowScheme(WorkflowSchemeService workflowSchemeService, ApplicationUser user, WorkflowSchemeBeanFactory beanFactory, AssignableWorkflowScheme scheme)
    {
        super(beanFactory);
        this.workflowSchemeService = workflowSchemeService;
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    WorkflowScheme getScheme()
    {
        return scheme;
    }

    @Override
    WorkflowSchemeBean asBean()
    {
        return beanFactory.asBean(scheme, null);
    }

    @Override
    ServiceOutcome<Void> delete()
    {
        return workflowSchemeService.deleteWorkflowScheme(user, scheme);
    }

    @Override
    ServiceOutcome<? extends RestWorkflowScheme> update(WorkflowSchemeBean bean)
    {
        final AssignableWorkflowScheme.Builder parentBuilder = scheme.builder();
        if (bean.isNameSet())
        {
            parentBuilder.setName(bean.getName());
        }
        if (bean.isDescriptionSet())
        {
            parentBuilder.setDescription(bean.getDescription());
        }

        boolean magicDraft = asBoolean(bean.isUpdateDraftIfNeeded());
        DraftWorkflowScheme child = magicDraft ? getDraft() : null;
        Map<String, String> map = getWorkflowMap(scheme, bean);
        if (child == null && (!workflowSchemeService.isActive(scheme) || Objects.equal(map, scheme.getMappings()) || !magicDraft))
        {
            ServiceOutcome<AssignableWorkflowScheme> parentOutcome = workflowSchemeService.updateWorkflowScheme(user, parentBuilder.setMappings(map).build());
            if (!parentOutcome.isValid())
            {
               return ServiceOutcomeImpl.error(parentOutcome);
            }
            scheme = parentOutcome.getReturnedValue();
            return ServiceOutcomeImpl.ok(this);
        }
        else
        {
            AssignableWorkflowScheme newParent = parentBuilder.build();
            ServiceOutcome<Void> validateOutcome = workflowSchemeService.validateUpdateWorkflowScheme(user, newParent);
            if (!validateOutcome.isValid())
            {
                return ServiceOutcomeImpl.error(validateOutcome);
            }

            ServiceOutcome<DraftWorkflowScheme> draftSchemeOutcome;
            if (child == null)
            {
                draftSchemeOutcome = workflowSchemeService.createDraft(user, scheme.getId());
                if (!draftSchemeOutcome.isValid())
                {
                    return ServiceOutcomeImpl.error(draftSchemeOutcome);
                }
                else
                {
                    child = draftSchemeOutcome.getReturnedValue();
                }
            }

            DraftRestWorkflowScheme draft = asDraft(child);
            return draft.update(bean);
        }
    }

    RestWorkflowScheme getDraftMaybe()
    {
        DraftWorkflowScheme draft = getDraft();
        return draft == null ? this : asDraft(draft);
    }

    private DraftWorkflowScheme getDraft()
    {
        ServiceOutcome<DraftWorkflowScheme> draftWorkflowScheme = workflowSchemeService.getDraftWorkflowScheme(user, scheme);
        if (!draftWorkflowScheme.isValid() || draftWorkflowScheme.getReturnedValue() == null)
        {
            return null;
        }
        else
        {
            return draftWorkflowScheme.getReturnedValue();
        }
    }

    ServiceOutcome<DraftRestWorkflowScheme> getDraftScheme()
    {
        ServiceOutcome<DraftWorkflowScheme> draftWorkflowScheme = workflowSchemeService.getDraftWorkflowSchemeNotNull(user, scheme);
        if (!draftWorkflowScheme.isValid())
        {
            return ServiceOutcomeImpl.error(draftWorkflowScheme);
        }
        else
        {
            return ServiceOutcomeImpl.ok(asDraft(draftWorkflowScheme.getReturnedValue()));
        }
    }

    ServiceOutcome<DraftRestWorkflowScheme> createDraftScheme()
    {
        ServiceOutcome<DraftWorkflowScheme> draftWorkflowScheme = workflowSchemeService.createDraft(user, scheme.getId());
        if (!draftWorkflowScheme.isValid())
        {
            return ServiceOutcomeImpl.error(draftWorkflowScheme);
        }
        else
        {
            return ServiceOutcomeImpl.ok(asDraft(draftWorkflowScheme.getReturnedValue()));
        }
    }

    private DraftRestWorkflowScheme asDraft(DraftWorkflowScheme draft)
    {
        return new DraftRestWorkflowScheme(workflowSchemeService, user, beanFactory, scheme, draft);
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateDraft(WorkflowSchemeBean bean)
    {
        DraftWorkflowScheme child = getDraft();
        if (child != null)
        {
            return asDraft(child).update(bean);
        }

        final AssignableWorkflowScheme.Builder parentBuilder = scheme.builder();
        if (bean.isNameSet())
        {
            parentBuilder.setName(bean.getName());
        }
        if (bean.isDescriptionSet())
        {
            parentBuilder.setDescription(bean.getDescription());
        }

        AssignableWorkflowScheme newParent = parentBuilder.build();
        ServiceOutcome<?> outcome = workflowSchemeService.validateUpdateWorkflowScheme(user, newParent);
        if (!outcome.isValid())
        {
            return ServiceOutcomeImpl.error(outcome);
        }

        ServiceOutcome<DraftWorkflowScheme> draftSchemeOutcome;
        DraftWorkflowScheme.Builder builder = workflowSchemeService.draftBuilder(scheme);
        builder.setMappings(getWorkflowMap(scheme, bean));

        draftSchemeOutcome = workflowSchemeService.createDraft(user, builder.build());
        if (!draftSchemeOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(draftSchemeOutcome);
        }

        ServiceOutcome<AssignableWorkflowScheme> parentOutcome = workflowSchemeService.updateWorkflowScheme(user, newParent);
        if (!parentOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(outcome);
        }
        else
        {
            scheme = parentOutcome.getReturnedValue();
        }

        return ServiceOutcomeImpl.ok(asDraft(draftSchemeOutcome.getReturnedValue()));
    }

    ServiceOutcome<? extends RestWorkflowScheme> deleteWorkflow(final String workflowName, boolean updateDraftIfNeeded)
    {
        return doUpdateMappings(scheme.builder().removeWorkflow(workflowName).build(), updateDraftIfNeeded,
                new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
                {
                    @Override
                    public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
                    {
                        return input.deleteWorkflow(workflowName);
                    }
                });
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateWorkflowMappings(final WorkflowMappingBean bean)
    {
        boolean magicDraft = asBoolean(bean.isUpdateDraftIfNeeded());

        final AssignableWorkflowScheme.Builder builder = scheme.builder();
        mergeWorkflowMappings(builder, bean);

        return doUpdateMappings(builder.build(), magicDraft, new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.updateWorkflowMappings(bean);
            }
        });
    }

    ServiceOutcome<? extends RestWorkflowScheme> removeIssueType(final String issueType, boolean updateDraftIfNeeded)
    {
        final AssignableWorkflowScheme.Builder builder = scheme.builder();
        builder.removeMapping(issueType);

        return doUpdateMappings(builder.build(), updateDraftIfNeeded, new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.removeIssueType(issueType);
            }
        });
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateIssueTypeMappings(final IssueTypeMappingBean bean)
    {
        boolean magicDraft = asBoolean(bean.getUpdateDraftIfNeeded());

        final AssignableWorkflowScheme.Builder builder = scheme.builder();
        mergeIssueTypeMapping(builder, bean);

        return doUpdateMappings(builder.build(), magicDraft, new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.updateIssueTypeMappings(bean);
            }
        });
    }

    ServiceOutcome<? extends RestWorkflowScheme> removeDefault(boolean updateDraftIfNeeded)
    {
        final AssignableWorkflowScheme newScheme = scheme.builder().removeDefault().build();
        return doUpdateMappings(newScheme, updateDraftIfNeeded, new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.removeDefault();
            }
        });
    }

    ServiceOutcome<? extends RestWorkflowScheme> updateDefault(final DefaultBean bean)
    {
        final AssignableWorkflowScheme.Builder builder = scheme.builder();
        setDefaultMapping(builder, bean);

        boolean magicDraft = asBoolean(bean.getUpdateDraftIfNeeded());
        return doUpdateMappings(builder.build(), magicDraft, new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.updateDefault(bean);
            }
        });
    }

    private ServiceOutcome<? extends RestWorkflowScheme> doUpdateMappings(AssignableWorkflowScheme newScheme, boolean updateDraftIfNeeded,
            Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>> draftUpdateFunction)
    {
        DraftWorkflowScheme child = updateDraftIfNeeded ? getDraft() : null;
        if (child != null)
        {
            return draftUpdateFunction.apply(asDraft(child));
        }

        if (scheme.getMappings().equals(newScheme.getMappings()))
        {
            return ServiceOutcomeImpl.ok(this);
        }
        else if (!workflowSchemeService.isActive(scheme) || !updateDraftIfNeeded)
        {
            ServiceOutcome<AssignableWorkflowScheme> parentOutcome = workflowSchemeService.updateWorkflowScheme(user, newScheme);
            if (!parentOutcome.isValid())
            {
                return ServiceOutcomeImpl.error(parentOutcome);
            }
            scheme = parentOutcome.getReturnedValue();
            return ServiceOutcomeImpl.ok(this);
        }
        else
        {
            ServiceOutcome<DraftWorkflowScheme> draftSchemeOutcome;
            draftSchemeOutcome = workflowSchemeService.createDraft(user, scheme.getId());
            if (!draftSchemeOutcome.isValid())
            {
                return ServiceOutcomeImpl.error(draftSchemeOutcome);
            }
            else
            {
                child = draftSchemeOutcome.getReturnedValue();
            }
            return draftUpdateFunction.apply(asDraft(child));
        }

    }

    @Component
    public static class Factory
    {
        private final WorkflowSchemeService workflowSchemeService;
        private final JiraAuthenticationContext jac;
        private final WorkflowSchemeBeanFactory beanFactory;

        @Autowired
        public Factory(WorkflowSchemeService workflowSchemeService, JiraAuthenticationContext jac, WorkflowSchemeBeanFactory beanFactory)
        {
            this.workflowSchemeService = workflowSchemeService;
            this.jac = jac;
            this.beanFactory = beanFactory;
        }

        ServiceOutcome<AssignableRestWorkflowScheme> getById(long id)
        {
            ApplicationUser user = getUser();
            return respone(user, workflowSchemeService.getWorkflowScheme(user, id));
        }

        ServiceOutcome<AssignableRestWorkflowScheme> create(WorkflowSchemeBean bean)
        {
            final AssignableWorkflowScheme.Builder builder = workflowSchemeService.assignableBuilder();
            builder.setName(bean.getName());
            builder.setDescription(bean.getDescription());
            builder.setMappings(getWorkflowMap(null, bean));

            ApplicationUser user = getUser();
            return respone(user, workflowSchemeService.createScheme(user, builder.build()));
        }

        private ServiceOutcome<AssignableRestWorkflowScheme> respone(ApplicationUser user, ServiceOutcome<AssignableWorkflowScheme> workflowScheme)
        {
            if (workflowScheme.isValid())
            {
                return ServiceOutcomeImpl.ok(new AssignableRestWorkflowScheme(workflowSchemeService, user, beanFactory, workflowScheme.getReturnedValue()));
            }
            else
            {
                return ServiceOutcomeImpl.error(workflowScheme);
            }
        }

        private ApplicationUser getUser()
        {
            return ApplicationUsers.from(jac.getLoggedInUser());
        }
    }
}
