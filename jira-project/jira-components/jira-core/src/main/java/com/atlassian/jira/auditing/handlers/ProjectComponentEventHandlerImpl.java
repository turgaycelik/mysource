package com.atlassian.jira.auditing.handlers;

import java.util.List;
import java.util.Locale;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AffectedProject;
import com.atlassian.jira.auditing.AffectedUser;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentCreatedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentDeletedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentMergedEvent;
import com.atlassian.jira.event.bc.project.component.ProjectComponentUpdatedEvent;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.auditing.handlers.HandlerUtils.requestIfThereAreAnyValues;

/**
 * @since v6.3
 */
public class ProjectComponentEventHandlerImpl implements ProjectComponentEventHandler
{
    private final I18nHelper.BeanFactory i18n;
    private final ProjectManager projectManager;

    public ProjectComponentEventHandlerImpl(final I18nHelper.BeanFactory i18n, final ProjectManager projectManager)
    {
        this.i18n = i18n;
        this.projectManager = projectManager;
    }

    @Override
    public RecordRequest onProjectComponentCreatedEvent(final ProjectComponentCreatedEvent event)
    {
        final ProjectComponent component = event.getProjectComponent();

        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.component.created")
                .forObject(AssociatedItem.Type.PROJECT_COMPONENT, component.getName(), component.getId().toString())
                .withAssociatedItems(getAssociatedItems(component))
                .withChangedValues(buildChangedValues(component));
    }

    @Override
    public Option<RecordRequest> onProjectComponentUpdatedEvent(final ProjectComponentUpdatedEvent event)
    {
        final ProjectComponent oldComponent = event.getOldProjectComponent();
        final ProjectComponent component = event.getProjectComponent();

        return requestIfThereAreAnyValues(buildChangedValues(oldComponent, component),
                new Function<List<ChangedValue>, RecordRequest>()
                {
                    @Override
                    public RecordRequest apply(final List<ChangedValue> changedValues)
                    {
                        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.component.updated")
                                .forObject(AssociatedItem.Type.PROJECT_COMPONENT, component.getName(), component.getId().toString())
                                .withAssociatedItems(getAssociatedItems(oldComponent, component))
                                .withChangedValues(changedValues);
                    }
                });
    }

    @Override
    public RecordRequest onProjectComponentMergedEvent(final ProjectComponentMergedEvent event)
    {
        final ProjectComponent component = event.getProjectComponent();
        final ProjectComponent mergedComponent = event.getMergedProjectComponent();

        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.component.merged")
                .forObject(AssociatedItem.Type.PROJECT_COMPONENT, component.getName(), component.getId().toString())
                .withAssociatedItems(new AffectedProject(getProjectOfComponent(component)))
                .withChangedValues(buildChangedValues(mergedComponent, component));
    }

    @Override
    public RecordRequest onProjectComponentDeletedEvent(final ProjectComponentDeletedEvent event)
    {
        final ProjectComponent component = event.getProjectComponent();

        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.component.deleted")
                .forObject(AssociatedItem.Type.PROJECT_COMPONENT, component.getName(), component.getId().toString())
                .withAssociatedItems(new AffectedProject(getProjectOfComponent(component)));
    }

    private ImmutableList<AssociatedItem> getAssociatedItems(ProjectComponent component)
    {
        return getAssociatedItems(null, component);
    }

    private ImmutableList<AssociatedItem> getAssociatedItems(ProjectComponent oldComponent, ProjectComponent component)
    {
        ImmutableList.Builder<AssociatedItem> associated = new ImmutableList.Builder<AssociatedItem>();

        associated.add(new AffectedProject(getProjectOfComponent(component)));
        associated.addAll(getAffectedUsers(oldComponent, component));

        return associated.build();
    }

    private Project getProjectOfComponent(ProjectComponent component)
    {
        return projectManager.getProjectObj(component.getProjectId());
    }

    private ImmutableList<AssociatedItem> getAffectedUsers(ProjectComponent oldComponent, ProjectComponent component)
    {
        final Project project = getProjectOfComponent(component);
        long assigneeType = component.getAssigneeType();

        ApplicationUser componentLead = null;
        ApplicationUser defaultAssignee = null;

        // component lead affected?
        if (oldComponent == null || (oldComponent != null && !Objects.equal(oldComponent.getLead(), component.getLead())))
        {
            componentLead = component.getComponentLead();
        }

        // if default assignee is set to either project lead or project default, it means that component default
        // assignee is in both cases project lead, since project default assignee can be only unassigned or project lead
        if (oldComponent == null || assigneeType != oldComponent.getAssigneeType())
        {
            if (ComponentAssigneeTypes.isComponentLead(assigneeType))
            {
                defaultAssignee = component.getComponentLead();
            }
            else if (ComponentAssigneeTypes.isProjectLead(assigneeType) ||
                    (ComponentAssigneeTypes.isProjectDefault(assigneeType) && ProjectAssigneeTypes.isProjectLead(project.getAssigneeType())))
            {
                defaultAssignee = project.getProjectLead();
            }
        }

        // prepare return list
        ImmutableList.Builder<AssociatedItem> affected = new ImmutableList.Builder<AssociatedItem>();

        if (componentLead != null)
        {
            affected.add(new AffectedUser(componentLead));

            if (defaultAssignee != null && !componentLead.equals(defaultAssignee))
            {
                affected.add(new AffectedUser(defaultAssignee));
            }
        }
        else if (defaultAssignee != null)
        {
            affected.add(new AffectedUser(defaultAssignee));
        }

        return affected.build();
    }

    private List<ChangedValue> buildChangedValues(final ProjectComponent component)
    {
        return buildChangedValues(null, component);
    }

    private List<ChangedValue> buildChangedValues(final ProjectComponent oldComponent, final ProjectComponent component)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();

        changedValues.addIfDifferent("common.words.name",
                oldComponent == null ? null : oldComponent.getName(), component.getName());

        changedValues.addIfDifferent("common.concepts.description",
                oldComponent == null ? null : oldComponent.getDescription(), component.getDescription());

        changedValues.addIfDifferent("admin.projects.component.lead",
                oldComponent == null ? null : (oldComponent.getComponentLead() == null ? null : oldComponent.getComponentLead().getUsername()),
                component.getComponentLead() == null ? null : component.getComponentLead().getUsername());

        changedValues.addIfDifferent("admin.projects.default.assignee",
                oldComponent == null ? null : pre(oldComponent.getAssigneeType()), pre(component.getAssigneeType()));

        return changedValues.build();
    }

    private String pre(final Long assigneeType)
    {
        return getI18n().getText(ComponentAssigneeTypes.getPrettyAssigneeType(assigneeType));
    }

    protected I18nHelper getI18n()
    {
        // you must not cache I18nHelper
        return i18n.getInstance(Locale.ENGLISH);
    }
}
