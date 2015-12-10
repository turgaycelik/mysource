package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.AffectedProject;
import com.atlassian.jira.auditing.AffectedUser;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.event.ProjectDeletedEvent;
import com.atlassian.jira.event.ProjectUpdatedEvent;
import com.atlassian.jira.event.project.ProjectAvatarUpdateEvent;
import com.atlassian.jira.event.project.ProjectCategoryChangeEvent;
import com.atlassian.jira.event.project.ProjectCategoryUpdateEvent;
import com.atlassian.jira.event.role.ProjectRoleDeletedEvent;
import com.atlassian.jira.event.role.ProjectRoleUpdatedEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;


import static com.atlassian.jira.auditing.handlers.HandlerUtils.requestIfThereAreAnyValues;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @since v6.2
 */
public class ProjectEventHandlerImpl implements ProjectEventHandler
{

    private final I18nHelper.BeanFactory i18n;
    private final ProjectManager projectManager;

    public ProjectEventHandlerImpl(final I18nHelper.BeanFactory i18n, final ProjectManager projectManager)
    {
        this.i18n = i18n;
        this.projectManager = projectManager;
    }

    @Override
    public RecordRequest onProjectCreatedEvent(final ProjectCreatedEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.created")
                .forObject(new AffectedProject(event.getProject()))
                .withAssociatedItems(getUserByProjectLeadChange(event.getProject()))
                .withChangedValues(buildChangedValues(event.getProject()));
    }

    @Override
    public Option<RecordRequest> onProjectUpdatedEvent(final ProjectUpdatedEvent event)
    {
        return requestIfThereAreAnyValues(
                buildChangedValues(event.getOldProject(), event.getProject()),
                new Function<List<ChangedValue>, RecordRequest>()
                {
                    @Override
                    public RecordRequest apply(final List<ChangedValue> changedValues)
                    {
                        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.updated")
                                .forObject(new AffectedProject(event.getProject()))
                                .withAssociatedItems(getUserByProjectLeadChange(event.getOldProject(), event.getProject()))
                                .withChangedValues(changedValues);
                    }
                });
    }

    @Override
    public RecordRequest onProjectDeletedEvent(final ProjectDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.deleted")
                .forObject(new AffectedProject(event.getProject()));
    }

    @Override
    public RecordRequest onProjectCategoryChangeEvent(final ProjectCategoryChangeEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.category.changed")
                .forObject(AssociatedItem.Type.PROJECT, event.getProject().getName(), event.getProject().getId().toString())
                .withChangedValues(buildChangedValuesForCategoryChange(event.getOldProjectCategory(), event.getNewProjectCategory()));
    }

    @Override
    public RecordRequest onProjectAvatarUpdateEvent(ProjectAvatarUpdateEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.avatar.changed")
                .forObject(AssociatedItem.Type.PROJECT, event.getProject().getName(), event.getProject().getId().toString());
    }

    @Override
    public RecordRequest onProjectRoleUpdatedEvent(final ProjectRoleUpdatedEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.roles.changed")
                .forObject(AssociatedItem.Type.PROJECT_ROLE, event.getProjectRole().getName(), event.getProjectRole().getId().toString())
                .withAssociatedItem(AssociatedItem.Type.PROJECT, event.getProject().getName(), event.getProject().getId().toString())
                .withChangedValues(computeChangedValues(event.getOriginalRoleActors(), event.getRoleActors()));
    }

    @Override
    public RecordRequest onProjectRoleDeletedEvent(final ProjectRoleDeletedEvent event)
    {
        return new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.role.deleted")
                .forObject(AssociatedItem.Type.PROJECT_ROLE, event.getProjectRole().getName(), event.getProjectRole().getId().toString());
    }

    @Nonnull
    private List<ChangedValue> computeChangedValues(@Nonnull final ProjectRoleActors originalRoleActors, @Nonnull final ProjectRoleActors roleActors)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
        final ListMultimap<String, String> originalActors = getCategorisedActors(originalRoleActors);
        final List<String> originalUsers = originalActors.get(UserRoleActorFactory.TYPE);
        final List<String> originalGroups = originalActors.get(GroupRoleActorFactory.TYPE);

        final ListMultimap<String, String> newActors = getCategorisedActors(roleActors);
        final List<String> newUsers = newActors.get(UserRoleActorFactory.TYPE);
        final List<String> newGroups = newActors.get(GroupRoleActorFactory.TYPE);

        changedValues.addIfDifferent("admin.common.words.users", defaultIfEmpty(join(originalUsers, ", "), null), defaultIfEmpty(join(newUsers, ", "), null));
        changedValues.addIfDifferent("common.words.groups", defaultIfEmpty(join(originalGroups, ", "), null), defaultIfEmpty(join(newGroups, ", "), null));

        return changedValues.build();
    }




    @Nonnull
    private ListMultimap<String, String> getCategorisedActors(final ProjectRoleActors originalRoleActors)
    {
        final ListMultimap<String, String> result = LinkedListMultimap.create(2);
        for(RoleActor actor : originalRoleActors.getRoleActors())
        {
            result.put(actor.getType(), actor.getParameter());
        }
        return result;

    }

    @Override
    public Option<RecordRequest> onProjectCategoryUpdateEvent(final ProjectCategoryUpdateEvent event)
    {
        final Collection<Project> projectsFromProjectCategory = projectManager.getProjectsFromProjectCategory(event.getOldProjectCategory());

        if (!projectsFromProjectCategory.isEmpty())
        {
            final RecordRequest recordRequest = new RecordRequest(AuditingCategory.PROJECTS, "jira.auditing.project.category.updated")
                    .forObject(AssociatedItem.Type.PROJECT_CATEGORY, event.getNewProjectCategory().getName())
                    .withChangedValues(buildChangedValuesForCategoryUpdate(event.getOldProjectCategory(), event.getNewProjectCategory()))
                    .withAssociatedItems(Iterables.transform(projectsFromProjectCategory, new Function<Project, AssociatedItem>()
                    {
                        @Override
                        public AssociatedItem apply(final Project project)
                        {
                            return new AffectedProject(project);
                        }
                    }));
            return Option.option(recordRequest);
        }
        else
        {
            return Option.none();
        }
    }

    private ImmutableList<AssociatedItem> getUserByProjectLeadChange(Project project)
    {
        return getUserByProjectLeadChange(null, project);
    }

    private ImmutableList<AssociatedItem> getUserByProjectLeadChange(Project oldProject, Project project)
    {
        if (oldProject == null || !project.getProjectLead().equals(oldProject.getProjectLead()))
        {
            return ImmutableList.<AssociatedItem>of(new AffectedUser(project.getProjectLead()));
        }
        else
        {
            return ImmutableList.of();
        }
    }

    private List<ChangedValue> buildChangedValues(Project project)
    {
        return buildChangedValues(null, project);
    }

    private List<ChangedValue> buildChangedValues(final Project originalProject, final Project currentProject)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
        changedValues.addIfDifferent("common.words.name", originalProject == null ? null : originalProject.getName(), currentProject.getName());
        changedValues.addIfDifferent("common.words.key", originalProject == null ? null : originalProject.getKey(), currentProject.getKey());
        changedValues.addIfDifferent("common.concepts.description", originalProject == null ? null : originalProject.getDescription(), currentProject.getDescription());
        changedValues.addIfDifferent("common.concepts.url", originalProject == null ? null : originalProject.getUrl(), currentProject.getUrl());
        changedValues.addIfDifferent("common.concepts.projectlead", originalProject == null ? null : originalProject.getLeadUserName(), currentProject.getLeadUserName());
        changedValues.addIfDifferent("admin.projects.default.assignee", originalProject == null ? null : pre(originalProject.getAssigneeType()), pre(currentProject.getAssigneeType()));
        return changedValues.build();
    }


    private List<ChangedValue> buildChangedValuesForCategoryChange(final ProjectCategory oldProjectCategory, final ProjectCategory newProjectCategory)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
        return changedValues.addIfDifferent("common.concepts.category", getNameOrNone(oldProjectCategory), getNameOrNone(newProjectCategory)).build();
    }

    private List<ChangedValue> buildChangedValuesForCategoryUpdate(final ProjectCategory oldProjectCategory, final ProjectCategory newProjectCategory)
    {
        final ChangedValuesBuilder changedValues = new ChangedValuesBuilder();
        changedValues.addIfDifferent("common.concepts.name", oldProjectCategory.getName(), newProjectCategory.getName());
        changedValues.addIfDifferent("common.concepts.description", oldProjectCategory.getDescription(), newProjectCategory.getDescription());
        return  changedValues.build();
    }

    private String pre(final Long assigneeType)
    {
        return getI18n().getText(ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType));
    }

    protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return i18n.getInstance(Locale.ENGLISH);
    }

    private String getNameOrNone(final ProjectCategory category)
    {
        return (category != null) ? category.getName() : "None";
    }
}