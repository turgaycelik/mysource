package com.atlassian.jira.bc.config;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryChangedAnalyticsEvent;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @since v6.1
 */
public class DefaultStatusService implements StatusService
{
    private final StatusManager statusManager;
    private final I18nHelper i18nHelper;
    private final ConstantsManager constantsManager;
    private final PermissionManager permissionManager;
    private final WorkflowManager workflowManager;
    private final EventPublisher eventPublisher;
    private final StatusCategoryManager statusCategoryManager;

    public DefaultStatusService(final StatusManager statusManager, final I18nHelper i18nHelper, final ConstantsManager constantsManager, final PermissionManager permissionManager, final WorkflowManager workflowManager, final EventPublisher eventPublisher, final StatusCategoryManager statusCategoryManager)
    {
        this.statusManager = statusManager;
        this.i18nHelper = i18nHelper;
        this.constantsManager = constantsManager;
        this.permissionManager = permissionManager;
        this.workflowManager = workflowManager;
        this.eventPublisher = eventPublisher;
        this.statusCategoryManager = statusCategoryManager;
    }

    @Override
    public ServiceOutcome<Status> createStatus(ApplicationUser user, String name, String description, String iconUrl, final StatusCategory statusCategory)
    {
        name = StringUtils.trimToNull(name);
        description = StringUtils.trimToNull(description);
        iconUrl = StringUtils.trimToNull(iconUrl);

        ServiceResult validation = validateCreateStatus(user, name, description, iconUrl, statusCategory);
        final Status status;
        if (validation.isValid())
        {
            // status category can be null when status lozenges are disabled - then use default one
            if (statusCategory == null)
            {
                status = statusManager.createStatus(name, description, iconUrl, statusCategoryManager.getDefaultStatusCategory());
            }
            else
            {
                status = statusManager.createStatus(name, description, iconUrl, statusCategory);
            }
        }
        else
        {
            status = null;
        }
        return ServiceOutcomeImpl.from(validation.getErrorCollection(), status);
    }

    @Override
    public ServiceOutcome<Status> editStatus(ApplicationUser user, final Status status, String name, String description, String iconUrl, final StatusCategory statusCategory)
    {
        name = StringUtils.trimToNull(name);
        description = StringUtils.trimToNull(description);
        iconUrl = StringUtils.trimToNull(iconUrl);

        ServiceResult validation = validateEditStatus(user, status, name, description, iconUrl, statusCategory);
        if (validation.isValid())
        {
            final boolean categoriesPresent = status.getStatusCategory() != null && statusCategory != null;

            String oldCategoryKey = null;
            String newCategoryKey = null;
            String oldStatusName = null;

            if (categoriesPresent)
            {
                oldCategoryKey = status.getStatusCategory().getKey();
                newCategoryKey = statusCategory.getKey();
                oldStatusName = status.getName();
            }

            // status category can be null when status lozenges are disabled - then don't override category
            if (statusCategory == null)
            {
                statusManager.editStatus(status, name, description, iconUrl);
            }
            else
            {
                statusManager.editStatus(status, name, description, iconUrl, statusCategory);
            }

            // publish event if possible
            if (categoriesPresent && !newCategoryKey.equals(oldCategoryKey))
            {
                eventPublisher.publish(new StatusCategoryChangedAnalyticsEvent(
                        oldCategoryKey,
                        newCategoryKey,
                        oldStatusName,
                        status.getName()
                ));
            }
        }

        return ServiceOutcomeImpl.from(validation.getErrorCollection(), status);
    }

    @Override
    public ServiceResult validateCreateStatus(ApplicationUser user, final String name, final String description, final String iconUrl, final StatusCategory statusCategory)
    {
        final SimpleErrorCollection ec = genericPermissionCheck(user, "admin.errors.not.allowed.to.create.status");
        if (ec.hasAnyErrors())
        {
            return new ServiceResultImpl(ec);
        }

        return validateCommon(user, null, name, description, iconUrl, statusCategory);
    }

    @Override
    public ServiceResult validateEditStatus(ApplicationUser user, final Status status, String name, String description, String iconUrl, final StatusCategory statusCategory)
    {
        final SimpleErrorCollection ec = genericPermissionCheck(user, "admin.errors.not.allowed.to.edit.status");
        if (ec.hasAnyErrors())
        {
            return new ServiceResultImpl(ec);
        }

        final ServiceResult serviceResult = validateCommon(user, status, name, description, iconUrl, statusCategory);
        if (status == null)
        {
            serviceResult.getErrorCollection().addErrorMessage(i18nHelper.getText("admin.errors.must.specify.valid.status.object"));
        }
        return serviceResult;
    }

    private ServiceResult validateCommon(ApplicationUser user, final Status status, String name, String description, String iconUrl, final StatusCategory statusCategory)
    {
        name = StringUtils.trimToNull(name);
        iconUrl = StringUtils.trimToNull(iconUrl);
        SimpleErrorCollection ec = new SimpleErrorCollection();

        // name cannot be empty
        if (StringUtils.isBlank(name))
        {
            ec.addError("name", i18nHelper.getText("admin.errors.must.specify.name"));
        }
        if (StringUtils.length(name) > MAX_STATUS_LENGTH)
        {
            ec.addError("name", i18nHelper.getText("admin.errors.status.length", MAX_STATUS_LENGTH));
        }

        // name has to be unique
        Status existingStatus = constantsManager.getStatusByNameIgnoreCase(name);
        if (existingStatus != null && (status == null || !existingStatus.getId().equals(status.getId())))
        {
            ec.addError("name", i18nHelper.getText("admin.errors.status.with.name.exists"));
        }

        // icon must be defined
        if (StringUtils.isBlank(iconUrl))
        {
            ec.addError("iconurl", i18nHelper.getText("admin.errors.must.specify.url.for.status.edit"));
        }

        // status category must to be defined if status lozenges are enabled
        if (statusCategory == null && isStatusAsLozengeEnabled())
        {
            ec.addError("statusCategory", i18nHelper.getText("admin.errors.must.specify.status.category"));
        }
        return new ServiceResultImpl(ec);
    }

    @Override
    public Status getStatusById(ApplicationUser user, final String id)
    {
        return statusManager.getStatus(id);
    }

    public ServiceResult validateRemoveStatus(ApplicationUser user, final Status status)
    {
        final SimpleErrorCollection ec = genericPermissionCheck(user, "admin.errors.not.allowed.to.remove.status");
        if (ec.hasAnyErrors())
        {
            return new ServiceResultImpl(ec);
        }

        final Status systemStatus = statusManager.getStatus(status.getId());
        if (systemStatus == null || !systemStatus.equals(status))
        {
            ec.addErrorMessage(i18nHelper.getText("admin.error.given.status.does.not.exist"));
        }

        if (hasAssociatedWorkflows(status))
        {
            ec.addErrorMessage(i18nHelper.getText("admin.error.status.is.associated.with.workflow"));
        }

        return new ServiceResultImpl(ec);
    }

    @Override
    public ServiceResult removeStatus(ApplicationUser user, final Status status)
    {

        ServiceResult validationResult = validateRemoveStatus(user, status);
        if (!validationResult.isValid())
        {
            return validationResult;
        }


        final SimpleErrorCollection ec = new SimpleErrorCollection();
        try
        {
            statusManager.removeStatus(Preconditions.checkNotNull(status).getId());
        }
        catch (IllegalArgumentException e)
        {
            ec.addErrorMessage(e.getLocalizedMessage());
        }
        return new ServiceResultImpl(ec);
    }

    private SimpleErrorCollection genericPermissionCheck(ApplicationUser user, String errorMessage){
        final SimpleErrorCollection ec = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            ec.addErrorMessage(i18nHelper.getText(errorMessage));
        }
        return ec;
    }

    @Override
    public ServiceOutcome<List<JiraWorkflow>> getAssociatedWorkflows(ApplicationUser user, final Status status){

        List<JiraWorkflow> allWorkflows = workflowManager.getWorkflowsIncludingDrafts();
        List<JiraWorkflow>  workflows = ImmutableList.copyOf(Iterables.filter(allWorkflows, containsStatus(status)));
        return ServiceOutcomeImpl.ok(workflows);
    }

    @Override
    public ServiceResult moveStatusUp(final ApplicationUser user, final String id)
    {
        final SimpleErrorCollection ec = genericPermissionCheck(user, "admin.errors.not.allowed.to.edit.status");
        if (ec.hasAnyErrors())
        {
            return new ServiceResultImpl(ec);
        }

        statusManager.moveStatusUp(id);

        return new ServiceResultImpl(ec);
    }

    @Override
    public ServiceResult moveStatusDown(final ApplicationUser user, final String id)
    {
        final SimpleErrorCollection ec = genericPermissionCheck(user, "admin.errors.not.allowed.to.edit.status");
        if (ec.hasAnyErrors())
        {
            return new ServiceResultImpl(ec);
        }

        statusManager.moveStatusDown(id);

        return new ServiceResultImpl(ec);
    }


    private boolean hasAssociatedWorkflows(final Status status)
    {
        List<JiraWorkflow> workflows = workflowManager.getWorkflowsIncludingDrafts();
        return Iterables.any(workflows, containsStatus(status));
    }

    public static Predicate<JiraWorkflow> containsStatus(final Status status)
    {
        return new Predicate<JiraWorkflow>()
        {
            @Override
            public boolean apply(@Nullable final JiraWorkflow input)
            {
                return input.getLinkedStatusObjects().contains(status);
            }
        };
    }

    public boolean isStatusAsLozengeEnabled()
    {
        return statusCategoryManager.isStatusAsLozengeEnabled();
    }
}
