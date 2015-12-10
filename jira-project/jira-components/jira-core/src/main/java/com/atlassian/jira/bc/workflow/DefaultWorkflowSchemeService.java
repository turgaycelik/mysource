package com.atlassian.jira.bc.workflow;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.SchemeIsBeingMigratedException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.atlassian.jira.bc.ServiceOutcomeImpl.from;
import static com.atlassian.jira.bc.ServiceOutcomeImpl.ok;
import static com.atlassian.jira.util.ErrorCollection.Reason;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultWorkflowSchemeService implements WorkflowSchemeService
{
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18Factory;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final WorkflowManager workflowManager;
    private final IssueTypeManager issueTypeManager;

    public DefaultWorkflowSchemeService(PermissionManager permissionManager, I18nHelper.BeanFactory i18Factory,
            WorkflowSchemeManager workflowSchemeManager, WorkflowManager workflowManager,
            IssueTypeManager issueTypeManager)
    {
        this.permissionManager = permissionManager;
        this.i18Factory = i18Factory;
        this.workflowSchemeManager = workflowSchemeManager;
        this.workflowManager = workflowManager;
        this.issueTypeManager = issueTypeManager;
    }

    @Override
    public AssignableWorkflowScheme.Builder assignableBuilder()
    {
        return workflowSchemeManager.assignableBuilder();
    }

    @Override
    public DraftWorkflowScheme.Builder draftBuilder(AssignableWorkflowScheme parent)
    {
        notNull("parent", parent);
        notNull("parent.id", parent.getId());

        return workflowSchemeManager.draftBuilder(parent);
    }

    @Override
    @Nonnull
    public ServiceOutcome<AssignableWorkflowScheme> createScheme(ApplicationUser creator, @Nonnull AssignableWorkflowScheme scheme)
    {
        notNull("scheme", scheme);

        ServiceOutcome<AssignableWorkflowScheme> outcome = checkEditPermission(creator);
        if (!outcome.isValid())
        {
            return outcome;
        }

        outcome = validateAssignable(creator, scheme, true);
        if (!outcome.isValid())
        {
            return outcome;
        }

        return ok(workflowSchemeManager.createScheme(scheme));
    }

    @Override
    public ServiceOutcome<DraftWorkflowScheme> createDraft(ApplicationUser creator, long parentId)
    {
        ServiceOutcome<AssignableWorkflowScheme> parentOutcome = validateCreateDraft(creator, parentId);
        if (!parentOutcome.isValid())
        {
            return ServiceOutcomeImpl.error(parentOutcome);
        }

        DraftWorkflowScheme draft = workflowSchemeManager.createDraftOf(creator, parentOutcome.getReturnedValue());
        return ok(draft);
    }

    @Override
    public ServiceOutcome<DraftWorkflowScheme> createDraft(ApplicationUser creator, DraftWorkflowScheme draftWorkflowScheme)
    {
        notNull("workflowScheme", draftWorkflowScheme);
        AssignableWorkflowScheme parentScheme = draftWorkflowScheme.getParentScheme();
        notNull("workflowScheme.parentScheme", parentScheme);
        notNull("workflowScheme.parentScheme.id", parentScheme.getId());

        ServiceOutcome<AssignableWorkflowScheme> parentOutcome = validateCreateDraft(creator, parentScheme.getId());
        if (!parentOutcome.isValid())
        {
           return ServiceOutcomeImpl.error(parentOutcome);
        }

        ServiceOutcome<DraftWorkflowScheme> outcome = validateMappings(creator, draftWorkflowScheme);
        if (!outcome.isValid())
        {
            return outcome;
        }

        return ok(workflowSchemeManager.createDraft(creator, draftWorkflowScheme));
    }

    private ServiceOutcome<AssignableWorkflowScheme> validateCreateDraft(ApplicationUser creator, long parentId)
    {
        ServiceOutcome<AssignableWorkflowScheme> outcome = checkEditPermission(creator);
        if (!outcome.isValid())
        {
            return outcome;
        }

        //Make sure the scheme exists.
        outcome = getWorkflowSchemeNoPermissionCheck(creator, parentId);
        if (!outcome.isValid())
        {
            return from(outcome.getErrorCollection(), null);
        }
        final AssignableWorkflowScheme workflowScheme = outcome.getReturnedValue();

        if (!workflowSchemeManager.isActive(workflowScheme))
        {
            return getError(creator, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.not.active");
        }

        if (workflowSchemeManager.hasDraft(workflowScheme))
        {
            return getError(creator, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.has.draft");
        }

        return outcome;
    }

    @Override
    public ServiceOutcome<AssignableWorkflowScheme> getWorkflowScheme(ApplicationUser user, long id)
    {
        final ServiceOutcome<AssignableWorkflowScheme> outcome = checkViewPermission(user);
        if (!outcome.isValid())
        {
            return outcome;
        }

        return getWorkflowSchemeNoPermissionCheck(user, id);
    }

    @Override
    public ServiceOutcome<DraftWorkflowScheme> getDraftWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme parentScheme)
    {
        Assertions.notNull("scheme", parentScheme);

        return ok(!parentScheme.isDefault() ? workflowSchemeManager.getDraftForParent(parentScheme) : null);
    }

    @Override
    public ServiceOutcome<DraftWorkflowScheme> getDraftWorkflowSchemeNotNull(ApplicationUser user, @Nonnull AssignableWorkflowScheme parentScheme)
    {
        ServiceOutcome<DraftWorkflowScheme> result = getDraftWorkflowScheme(user, parentScheme);

        if (!result.isValid() || result.getReturnedValue() != null)
        {
            return result;
        }

        return getError(user, Reason.NOT_FOUND, "admin.workflowschemes.service.error.no.draft");
    }

    @Override
    public ServiceOutcome<Void> deleteWorkflowScheme(ApplicationUser user, @Nonnull WorkflowScheme scheme)
    {
        Assertions.notNull("scheme", scheme);

        ServiceOutcome<Void> outcome = checkEditPermission(user);
        if (!outcome.isValid())
        {
            return outcome;
        }

        ServiceOutcome<?> existsOutcome;
        if (scheme.isDraft())
        {
            existsOutcome = getDraftSchemeNoPermissionCheck(user, scheme.getId());
        }
        else
        {
            existsOutcome = getWorkflowSchemeNoPermissionCheck(user, scheme.getId());
        }

        if (!existsOutcome.isValid())
        {
            return convert(existsOutcome);
        }

        if (scheme.isDefault())
        {
            return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.cant.delete.default");
        }

        if (isActive(scheme))
        {
            return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.delete.active");
        }

        boolean deleteResult;
        try
        {
            deleteResult = workflowSchemeManager.deleteWorkflowScheme(scheme);
        }
        catch (SchemeIsBeingMigratedException e)
        {
            return getSchemeIsBeingMigratedError(user);
        }

        if (!deleteResult)
        {
            return getError(user, Reason.SERVER_ERROR, "admin.workflowschemes.service.error.delete.error");
        }
        else
        {
            return ok(null);
        }
    }

    @Override
    public boolean isActive(WorkflowScheme workflowScheme)
    {
        return workflowSchemeManager.isActive(notNull("workflowScheme", workflowScheme));
    }

    @Override
    public ServiceOutcome<AssignableWorkflowScheme> getSchemeForProject(ApplicationUser user, @Nonnull Project project)
    {
        if (!hasPermissionToEditProject(user, project))
        {
            return getError(user, Reason.FORBIDDEN, "admin.workflowschemes.service.error.no.permission.project");
        }
        return ok(workflowSchemeManager.getWorkflowSchemeObj(project));
    }

    @Override
    public ServiceOutcome<AssignableWorkflowScheme> updateWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme scheme)
    {
        notNull("scheme", scheme);
        notNull("scheme.id", scheme.getId());

        final ServiceOutcome<Void> outcome = validateUpdateWorkflowScheme(user, scheme);
        if (!outcome.isValid())
        {
            return error(outcome);
        }

        try
        {
            return ok(workflowSchemeManager.updateWorkflowScheme(scheme));
        }
        catch (SchemeIsBeingMigratedException e)
        {
            return getSchemeIsBeingMigratedError(user);
        }
     }

    @Override
    public ServiceOutcome<Void> validateUpdateWorkflowScheme(ApplicationUser user, @Nonnull AssignableWorkflowScheme scheme)
    {
        ServiceOutcome<AssignableWorkflowScheme> outcome = checkEditPermission(user);
        if (!outcome.isValid())
        {
            return error(outcome);
        }

        outcome = getWorkflowSchemeNoPermissionCheck(user, scheme.getId());
        if (!outcome.isValid())
        {
            return error(outcome);
        }

        AssignableWorkflowScheme currentScheme = outcome.getReturnedValue();
        outcome = validateAssignable(user, scheme, false);
        if (!outcome.isValid())
        {
            return error(outcome);
        }

        if (isActive(currentScheme) && !Objects.equal(scheme.getMappings(), currentScheme.getMappings()))
        {
            return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.change.active");
        }
        return error(outcome);
    }

    private static <T> ServiceOutcome<T> error(ServiceOutcome<?> outcome)
    {
        return ServiceOutcomeImpl.from(outcome.getErrorCollection(), null);
    }

    @Override
    public ServiceOutcome<DraftWorkflowScheme> updateWorkflowScheme(final ApplicationUser user, @Nonnull DraftWorkflowScheme scheme)
    {
        Assertions.notNull("scheme", scheme);

        ServiceOutcome<DraftWorkflowScheme> outcome = checkEditPermission(user);
        if (!outcome.isValid())
        {
            return outcome;
        }

        outcome = getDraftSchemeNoPermissionCheck(user, scheme.getId());
        if (!outcome.isValid())
        {
            return outcome;
        }

        outcome = validateMappings(user, scheme);
        if (!outcome.isValid())
        {
            return outcome;
        }

        try
        {
            return ok(workflowSchemeManager.updateDraftWorkflowScheme(user, scheme));
        }
        catch (SchemeIsBeingMigratedException e)
        {
            return getSchemeIsBeingMigratedError(user);
        }
    }

    private <T> ServiceOutcome<T> getSchemeIsBeingMigratedError(ApplicationUser user)
    {
        return getError(user, Reason.FORBIDDEN, "admin.workflowschemes.manager.migration.in.progress");
    }

    @Override
    public int getUsageCount(@Nonnull AssignableWorkflowScheme assignableWorkflowScheme)
    {
        return workflowSchemeManager.getProjectsUsing(assignableWorkflowScheme).size();
    }

    @Override
    public boolean isUsingDefaultScheme(@Nonnull Project project)
    {
        return workflowSchemeManager.isUsingDefaultScheme(project);
    }

    boolean hasPermissionToEditProject(ApplicationUser user, Project project)
    {
        return ProjectAction.EDIT_PROJECT_CONFIG.hasPermission(permissionManager, user, project);
    }

    private <T> ServiceOutcome<T> getError(ApplicationUser user, Reason reason, String key)
    {
        final String text = i18Factory.getInstance(user).getText(key);
        return outcomeForReason(reason, text);
    }

    private <T> ServiceOutcome<T> getError(ApplicationUser user, Reason reason, String key, String arg)
    {
        final String text = i18Factory.getInstance(user).getText(key, arg);
        return outcomeForReason(reason, text);
    }

    private <T> ServiceOutcome<T> outcomeForReason(Reason reason, String text)
    {
        final SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        simpleErrorCollection.addErrorMessage(text);
        simpleErrorCollection.addReason(reason);

        return from(simpleErrorCollection, null);
    }

    private boolean hasAdminPermission(ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    private ServiceOutcome<AssignableWorkflowScheme> validateAssignable(ApplicationUser user,
            AssignableWorkflowScheme scheme, boolean creating)
    {
        if (scheme.getName() == null)
        {
            return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.scheme.must.have.name");
        }
        else if (scheme.getName().length() > 255)
        {
            return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.scheme.name.too.big");
        }
        else
        {
            AssignableWorkflowScheme oldScheme = workflowSchemeManager.getWorkflowSchemeObj(scheme.getName());
            if (oldScheme != null && (creating || !oldScheme.getId().equals(scheme.getId())))
            {
                return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.scheme.name.duplicate", scheme.getName());
            }
        }

        return validateMappings(user, scheme);
    }

    private <T extends WorkflowScheme> ServiceOutcome<T> validateMappings(ApplicationUser user, WorkflowScheme scheme)
    {
        for (Map.Entry<String, String> stringEntry : scheme.getMappings().entrySet())
        {
            String issueType = stringEntry.getKey();
            String workflowName = stringEntry.getValue();

            if (issueType != null && issueTypeManager.getIssueType(issueType) == null)
            {
                return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.invalid.issue.type", issueType);
            }

            if (workflowName == null || workflowManager.getWorkflow(workflowName) == null)
            {
                return getError(user, Reason.VALIDATION_FAILED, "admin.workflowschemes.service.error.bad.workflow", workflowName == null ? "<null>" : workflowName);
            }
        }
        return ok(null);
    }

    private <T> ServiceOutcome<T> checkEditPermission(ApplicationUser user)
    {
        if (!hasAdminPermission(user))
        {
            return getError(user, Reason.FORBIDDEN, "admin.workflowschemes.service.error.no.admin.permission");
        }
        else
        {
            return ok(null);
        }
    }

    private <T> ServiceOutcome<T> checkViewPermission(ApplicationUser user)
    {
        if (!hasAdminPermission(user))
        {
            return getError(user, Reason.FORBIDDEN, "admin.workflowschemes.service.error.no.view.permission");
        }
        else
        {
            return ok(null);
        }
    }

    private ServiceOutcome<AssignableWorkflowScheme> getWorkflowSchemeNoPermissionCheck(ApplicationUser user, long id)
    {
        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(id);
        if (workflowScheme == null)
        {
            return getError(user, Reason.NOT_FOUND, "admin.workflowschemes.service.error.does.not.exist");
        }

        return ok(workflowScheme);
    }

    private ServiceOutcome<DraftWorkflowScheme> getDraftSchemeNoPermissionCheck(ApplicationUser user, long id)
    {
        DraftWorkflowScheme workflowScheme = workflowSchemeManager.getDraft(id);
        if (workflowScheme == null)
        {
            return getError(user, Reason.NOT_FOUND, "admin.workflowschemes.service.error.draft.does.not.exist");
        }

        return ok(workflowScheme);
    }

    private static <T> ServiceOutcome<T> convert(ServiceOutcome<?> errors)
    {
        return ServiceOutcomeImpl.from(errors.getErrorCollection(), null);
    }
}
