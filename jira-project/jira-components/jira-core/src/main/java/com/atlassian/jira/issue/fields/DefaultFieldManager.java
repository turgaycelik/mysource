
package com.atlassian.jira.issue.fields;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;

public class DefaultFieldManager implements FieldManager
{
    private static final Logger LOG = Logger.getLogger(DefaultFieldManager.class);
    private static final Predicate<Field> ORDERABLE = new IsOrderable();

    // WARNING!
    // If you add a column here - some tests will fail.  Make sure you run tests before you check in!
    // The order *is* significant!
    private static final List<Class<? extends Field>> SYSTEM_FIELD_CLASSES = ImmutableList.<Class<? extends Field>>builder()
            .add(ProjectSystemField.class)  // Note the special cases for this one...
            .add(KeySystemField.class)
            .add(SummarySystemField.class)
            .add(IssueTypeSystemField.class)
            .add(StatusSystemField.class)
            .add(PrioritySystemField.class)
            .add(ResolutionSystemField.class)
            .add(AssigneeSystemField.class)
            .add(ReporterSystemField.class)
            .add(CreatorSystemField.class)
            .add(CreatedSystemField.class)
            .add(LastViewedSystemField.class)
            .add(UpdatedSystemField.class)
            .add(ResolutionDateSystemField.class)
            .add(AffectedVersionsSystemField.class)
            .add(FixVersionsSystemField.class)
            .add(ComponentsSystemField.class)
            .add(DueDateSystemField.class)
            .add(VotesSystemField.class)
            .add(WatchesSystemField.class)
            .add(ThumbnailSystemField.class)
            .add(OriginalEstimateSystemField.class)
            .add(TimeEstimateSystemField.class)
            .add(TimeSpentSystemField.class)
            .add(WorkRatioSystemField.class)
            .add(SubTaskSystemField.class)
            .add(IssueLinksSystemField.class)
            .add(AttachmentSystemField.class)
            .add(EnvironmentSystemField.class)
            .add(DescriptionSystemField.class)
            .add(TimeTrackingSystemField.class)
            .add(SecurityLevelSystemField.class)
            .add(CommentSystemField.class)
            .add(ProgressBarSystemField.class)
            .add(AggregateProgressBarSystemField.class)
            .add(AggregateTimeSpentSystemField.class)
            .add(AggregateEstimateSystemField.class)
            .add(AggregateOriginalEstimateSystemField.class)
            .add(LabelsSystemField.class)
            .add(WorklogSystemField.class)
            .build();

    // Local references to the components so that we don't have to churn on the component accessor too heavily
    private volatile CustomFieldManager customFieldManager;
    private volatile FieldLayoutManager fieldLayoutManager;

    // These are all immutable with a stable/predictable iteration order
    private final Map<String, Field> fields;
    private final Collection<OrderableField> orderableFields;
    private final Collection<NavigableField> navigableFields;
    private final Collection<SearchableField> searchableFields;

    // It may look like there is a lot of "work" in this constructor, but what we are actually doing
    // here is hydrating all the system field components, which do not get registered directly in
    // the Pico container.  This really isn't any different from if they were listed as dependencies
    // and injected to this class.
    public DefaultFieldManager()
    {
        this.fields = buildSystemFieldMap();

        // Special case: ProjectSystemField is not orderable, even though it implements OrderableField
        this.orderableFields = ImmutableSet.copyOf(filter(filter(fields.values(), ORDERABLE), OrderableField.class));
        this.navigableFields = ImmutableSet.copyOf(filter(fields.values(), NavigableField.class));
        this.searchableFields = ImmutableSet.copyOf(filter(fields.values(), SearchableField.class));
    }

    private static Map<String, Field> buildSystemFieldMap()
    {
        final ImmutableMap.Builder<String, Field> fieldsBuilder = ImmutableMap.builder();
        for (Class<? extends Field> fieldClass : SYSTEM_FIELD_CLASSES)
        {
            final Field field = JiraUtils.loadComponent(fieldClass);
            fieldsBuilder.put(field.getId(), field);
        }
        return fieldsBuilder.build();
    }


    @Override
    public Field getField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        return fields.get(id);
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project in the system.
     * <p/>
     * NOTE: This method is used in the Admin interface, as admins should be able to configure the default ColumnLayouts
     * irrespective of their permissions. They should be able to see all fields that are not hidden in at least one
     * FieldLayout in the system
     *
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    @Override
    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException
    {
        try
        {
            // Include custom fields (irrespective of scope) and exclude fields that should not be available (e.g. timetracking if it is turned off)
            final Set<NavigableField> allAvailableFields = getAvailableNavigableFields();

            // Retrieve all unique FieldLayouts in the system
            final Set<FieldLayout> uniqueSchemes = getAllFieldLayouts();

            // Go through the list of available fields and see of the field is NOT hidden in at least one scheme
            return getAvailableFields(allAvailableFields, uniqueSchemes);
        }
        catch (DataAccessException e)
        {
            final String message = "Error retrieving field layout.";
            LOG.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see (has
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for).
     * <p/>
     * The returned set of fields contains all custom fields that are not hidden in AT LEAST one FieldLayout that the
     * user can see.
     * <p/>
     * NOTE: This method is primarily used for configuring user's ColumnLayout, as the user should be able to add any
     * field (including custom field) to it that they can see in the system. THe scope of custom fields is ignored here
     * as the user configures the ColumnLayout outside of scope.
     *
     * @param remoteUser the remote user
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    @Override
    public Set<NavigableField> getAvailableNavigableFields(final User remoteUser) throws FieldException
    {
        final Set<NavigableField> availableFields = new HashSet<NavigableField>();
        try
        {
            final Set<FieldLayout> uniqueSchemes = getUniqueSchemes(Collections.<Long>emptyList(), Collections.<String>emptyList(), remoteUser);
            // JRA-26070: For efficiency we only want to check browse permission on each project once.
            final Set<String> browsableProjects = getBrowsableProjectKeys(remoteUser);

            // Include custom fields and exclude fields that should not be available (e.g. timetracking it is turned off)
            final Set<NavigableField> allAvailableFields = getAvailableNavigableFields();

            // Go through the list of available fields and see if the field is NOT hidden in at least one scheme
            for (final NavigableField field : allAvailableFields)
            {
                if (!isFieldHidden(uniqueSchemes, field))
                {
                    // if the field is a project custom field ensure that the user can see the project
                    if (isCustomField(field))
                    {
                        // Check if the user has permission to the associated projects
                        final CustomField customField = getCustomField(field.getId());
                        if (userHasPermissionToCustomFieldProjects(customField, browsableProjects))
                        {
                            availableFields.add(field);
                        }
                    }
                    else
                    {
                        // The field is not a custom field and is is not hidden, add it to the list
                        availableFields.add(field);
                    }
                }
            }
            return availableFields;
        }
        catch (DataAccessException e)
        {
            final String message = "Error retrieving field layout.";
            LOG.error(message, e);
            throw new FieldException(message, e);
        }
    }

    private static boolean userHasPermissionToCustomFieldProjects(final CustomField customField, final Set<String> browsableProjects)
    {
        if (customField.isAllProjects())
        {
            return true;
        }
        else
        {
            final List<GenericValue> projects = customField.getAssociatedProjects();
            if (projects == null)
            {
                return false;
            }

            for (final GenericValue project : projects)
            {
                if (browsableProjects.contains(project.getString("key")))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static Set<String> getBrowsableProjectKeys(final User remoteUser)
    {
        final Collection<Project> browsableProjectObjects = getBrowsableProjectObjects(remoteUser);
        final Set<String> browsableProjectKeys = new HashSet<String>(browsableProjectObjects.size());
        for (final Project project : browsableProjectObjects)
        {
            browsableProjectKeys.add(project.getKey());
        }
        return browsableProjectKeys;
    }

    @Override
    public Set<SearchableField> getAllSearchableFields()
    {
        final Set<SearchableField> fields = new LinkedHashSet<SearchableField>(searchableFields);
        //All custom fields are SearchableFields so we don't have to filter them.
        fields.addAll(getCustomFieldManager().getCustomFieldObjects());
        return fields;
    }

    @Override
    public Set<SearchableField> getSystemSearchableFields()
    {
        return new LinkedHashSet<SearchableField>(searchableFields);
    }

    // --------------------------------------------------------------------------------------------- Convenience Methods
    @Override
    public IssueTypeField getIssueTypeField()
    {
        return (IssueTypeField) getField(IssueFieldConstants.ISSUE_TYPE);
    }

    @Override
    public ProjectField getProjectField()
    {
        return (ProjectField) getField(IssueFieldConstants.PROJECT);
    }

    @Override
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User user) throws FieldException
    {
        return getAvailableNavigableFieldsWithScope(user, Collections.<Long>emptyList(), Collections.<String>emptyList());
    }

    @Override
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User remoteUser, final QueryContext queryContext)
            throws FieldException
    {
        final Set<NavigableField> allFields = new LinkedHashSet<NavigableField>();
        for (final QueryContext.ProjectIssueTypeContexts context : queryContext.getProjectIssueTypeContexts())
        {
            final Set<NavigableField> availableFields = getAvailableNavigableFieldsWithScope(remoteUser, context.getProjectIdInList(), context.getIssueTypeIds());

            // Union all the visible fields for each project/issue type context
            allFields.addAll(availableFields);
        }
        return allFields;
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see (has
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for).
     * <p/>
     * NOTE: This method is used when actually showing the results (e.g. in Issue Navigator) to determine if the field
     * (column) should be actually shown.
     *
     * @param remoteUser the remote user.
     * @param projectIds a List of Longs.
     * @param issueTypes Issue types
     * @return a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see.
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    private Set<NavigableField> getAvailableNavigableFieldsWithScope(final User remoteUser, final List<Long> projectIds, final List<String> issueTypes)
            throws FieldException
    {
        final Set<NavigableField> availableFields = new LinkedHashSet<NavigableField>();
        try
        {
            // Get the projects Field Layout
            final Set<FieldLayout> schemes = getUniqueSchemes(projectIds, issueTypes, remoteUser);

            final Set<NavigableField> allAvailableFields = new LinkedHashSet<NavigableField>();

            allAvailableFields.addAll(navigableFields);

            // Exclude fields that should not be available (e.g. timetracking is turned off)
            allAvailableFields.removeAll(getUnavailableFields());

            // Add all standard (non-custom) available fields
            availableFields.addAll(getAvailableFields(allAvailableFields, schemes));

            // Add all the custom fields that are in scope
            availableFields.addAll(getAvailableCustomFieldsWithScope(remoteUser, projectIds, issueTypes));

            return availableFields;
        }
        catch (DataAccessException e)
        {
            final String message = "Error retrieving field layout.";
            LOG.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Checks that the fields in the fieldsToCheck collection are actually available in AT LEAST ONE FieldLayout present
     * in the given schemes set.
     *
     * @param fieldsToCheck Superset of all Fields. We will return a subset of these.
     * @param schemes Schemes used to find Visible fields.
     * @return the fields that are available in AT LEAST ONE FieldLayout
     */
    private <F extends Field> Set<F> getAvailableFields(final Collection<F> fieldsToCheck, final Set<FieldLayout> schemes)
    {
        final Set<F> availableFields = new LinkedHashSet<F>(fieldsToCheck.size());

        // Go through the list of available fields and see of the field is NOT hidden in at least one scheme
        for (final F field : fieldsToCheck)
        {
            if (!isFieldHidden(schemes, field))
            {
                availableFields.add(field);
            }
        }

        return availableFields;
    }

    @Override
    public Set<CustomField> getAvailableCustomFields(final User remoteUser, final Issue issue) throws FieldException
    {
        final Project project = issue.getProjectObject();
        // TODO: Do we really want to do this null check? It was just copied from the old GenericValue version of this method.
        final List<Long> projectList = (project == null) ? Collections.<Long>emptyList() : CollectionBuilder.list(project.getId());
        return getAvailableCustomFieldsWithScope(remoteUser, projectList, CollectionBuilder.list(issue.getIssueTypeObject().getId()));
    }

    /**
     * Returns a set of {@link CustomField}s that are in scope.
     *
     * @param remoteUser Remote User
     * @param projectIds List of Project IDs. Can be empty, in which case means "any project" (acts like a wildcard). It can not be null.
     * @param issueTypes List of Issue Types. Can be empty, in which case means "any issue type" (acts like a wildcard). It can not be null.
     * @return a set of {@link CustomField}s that are in scope.
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     * @see {@link CustomFieldManager#getCustomFieldObjects(Long, java.util.List)} for a full description on how wildcards are treated.
     */
    private Set<CustomField> getAvailableCustomFieldsWithScope(final User remoteUser, final List<Long> projectIds, final List<String> issueTypes)
            throws FieldException
    {
        final CustomFieldManager customFieldManager = getCustomFieldManager();
        try
        {
            // Retrieve all the unique FieldLayout schemes
            final Set<FieldLayout> schemes = getUniqueSchemes(projectIds, issueTypes, remoteUser);

            Collection<CustomField> existingCustomFields = new HashSet<CustomField>();
            // Only get custom fields and exclude fields that should not be available (e.g. timetracking it is turned off)
            for (final Long projectId : projectIds)
            {
                final List<CustomField> existingCustomFieldsForProject = customFieldManager.getCustomFieldObjects(projectId, issueTypes);
                if (existingCustomFieldsForProject != null)
                {
                    existingCustomFields.addAll(existingCustomFieldsForProject);
                }
            }

            if (projectIds.isEmpty())
            {
                existingCustomFields = customFieldManager.getCustomFieldObjects(null, issueTypes);
            }

            // Go through the list of existing custom fields and see if the field is NOT hidden in at least one scheme
            return getAvailableFields(existingCustomFields, schemes);
        }
        catch (DataAccessException e)
        {
            final String message = "Error retrieving field layout for " + (projectIds != null && !projectIds.isEmpty() ? "projects '" + projectIds + "'." : "null project.");
            LOG.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Retrieves all the unique FieldLayouts that the user should be able to see for project/issuetype pairs.
     *
     * @param projectIds a List of Longs.
     * @param issueTypes Issue types
     * @param remoteUser the remote user.
     * @return all the unique FieldLayouts that the user should be able to see for project/issuetype pairs.
     */
    private Set<FieldLayout> getUniqueSchemes(final List<Long> projectIds, final List<String> issueTypes, final User remoteUser)
    {
        if (projectIds.isEmpty() && issueTypes.isEmpty())
        {
            // JRA-19426 - we need to be more efficient about how we get all unique field layouts for a project
            // If no project and no specific issue types have been specified retrieve all schemes
            final Collection<Project> projects = getBrowsableProjectObjects(remoteUser);
            final Set<FieldLayout> fieldLayoutSet = new HashSet<FieldLayout>();
            for (final Project project : projects)
            {
                fieldLayoutSet.addAll(getFieldLayoutManager().getUniqueFieldLayouts(project));
            }
            return fieldLayoutSet;
        }
        else if (projectIds.isEmpty() && !issueTypes.isEmpty())
        {
            // If the project has NOT been specified, but issue types have been, we need to retrieve
            // unique schemes for all projects that the user can see, but only for the specified issue types
            return findVisibleFieldLayouts(getBrowsableProjectObjects(remoteUser), issueTypes);
        }
        else if (!projectIds.isEmpty() && issueTypes.isEmpty())
        {
            // If we have a project specified but no issue types, we need to retrieve all unique schemes
            // for all issue types but only the ones for that project
            return getVisibleFieldLayouts(ComponentAccessor.getProjectManager().convertToProjects(projectIds), getAllIssueTypes());
        }
        else
        {
            // We have a project as well as issue types. Retrieve unique schemes only for those project/issuetype pairs
            return getVisibleFieldLayouts(ComponentAccessor.getProjectManager().convertToProjects(projectIds), issueTypes);
        }
    }

    @Override
    public boolean isFieldHidden(final User remoteUser, final String fieldId)
    {
        return isFieldHidden(remoteUser, getField(fieldId));
    }

    @Override
    public boolean isFieldHidden(final User remoteUser, final Field field)
    {
        final Set<FieldLayout> visibleFieldLayouts = getVisibleFieldLayouts(remoteUser);
        return isFieldHidden(visibleFieldLayouts, field);
    }

    @Override
    public Set<FieldLayout> getVisibleFieldLayouts(final User user)
    {
        return getUniqueSchemes(Collections.<Long>emptyList(), Collections.<String>emptyList(), user);
    }

    @Override
    public boolean isFieldHidden(final Set<FieldLayout> fieldLayouts, final Field field)
    {
        // Only orderable fields can be hidden
        return isOrderableField(field) && isFieldHidden(fieldLayouts, (OrderableField)field);
    }

    private static boolean isFieldHidden(final Set<FieldLayout> fieldLayouts, final OrderableField field)
    {
        for (final FieldLayout fieldLayout : fieldLayouts)
        {
            final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);
            if (!fieldLayoutItem.isHidden())
            {
                return false;
            }
        }

        // The field is hidden in all field layouts
        return true;
    }

    private Set<FieldLayout> getAllFieldLayouts()
    {
        // Retrieve the list of all projects
        return getVisibleFieldLayouts(ComponentAccessor.getProjectManager().getProjects(), getAllIssueTypes());
    }

    /**
     * Retrive a list of all issue types in the system.
     *
     * @return all issue types in the system.
     */
    private static List<String> getAllIssueTypes()
    {
        final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        return constantsManager.expandIssueTypeIds(ImmutableList.of(ConstantsManager.ALL_ISSUE_TYPES));
    }

    private static Collection<Project> getBrowsableProjectObjects(final User remoteUser)
    {
        return ComponentAccessor.getPermissionManager().getProjectObjects(Permissions.BROWSE, remoteUser);
    }

    /**
     * Retrives unique FieldLayouts for the given projects and issuetype pairs.
     */
    private Set<FieldLayout> findVisibleFieldLayouts(final Collection<Project> projects, final List<String> issueTypes)
    {
        // Get the field layout schemes of each project/issuetype pair and work out all the unique schemes
        // remember that more than one project can have the same field layout (scheme)
        final Set<FieldLayout> uniqueSchemes = new HashSet<FieldLayout>();
        for (final Project project : projects)
        {

            for (final String issueTypeId : issueTypes)
            {
                final FieldLayout fieldLayout = getFieldLayoutManager().getFieldLayout(project, issueTypeId);
                uniqueSchemes.add(fieldLayout);
            }
        }
        return uniqueSchemes;
    }

    /**
     * Retrives unique FieldLayouts for the given projects and issuetype pairs.
     */
    private Set<FieldLayout> getVisibleFieldLayouts(final Collection<GenericValue> projects, final List<String> issueTypes)
    {
        // Get the field layout schemes of each project/issuetype pair and work out all the unique schemes
        // remember that more than one project can have the same field layout (scheme)
        final Set<FieldLayout> uniqueSchemes = new HashSet<FieldLayout>();
        for (final GenericValue project : projects)
        {

            for (final String issueTypeId : issueTypes)
            {
                final FieldLayout fieldLayout = getFieldLayoutManager().getFieldLayout(project, issueTypeId);
                uniqueSchemes.add(fieldLayout);
            }
        }
        return uniqueSchemes;
    }

    @Override
    public boolean isCustomField(final String key)
    {
        return getCustomFieldManager().exists(key);
    }

    @Override
    public boolean isCustomField(final Field field)
    {
        return (field instanceof CustomField);
    }

    @Override
    public CustomField getCustomField(final String key)
    {
        final CustomField customFieldObject = getCustomFieldManager().getCustomFieldObject(key);
        if (customFieldObject == null)
        {
            throw new IllegalArgumentException("Custom field with id '" + key + "' does not exist.");
        }
        else
        {
            return customFieldObject;
        }
    }

    @Override
    public boolean isHideableField(final String id)
    {
        return isCustomField(id) || fields.get(id) instanceof HideableField;
    }

    @Override
    public boolean isHideableField(final Field field)
    {
        // CustomField implements HideableField, so no need to check it separately
        return field instanceof HideableField;
    }

    @Override
    public HideableField getHideableField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        final Field field = fields.get(id);
        if (field instanceof HideableField)
        {
            return (HideableField)field;
        }
        throw new IllegalArgumentException("The field with id '" + id + "' is not a HideableField.");
    }

    @Override
    public boolean isOrderableField(final String id)
    {
        return isCustomField(id) || ORDERABLE.apply(fields.get(id));
    }

    @Override
    public boolean isOrderableField(final Field field)
    {
        return ORDERABLE.apply(field);
    }

    @Nullable
    @Override
    public OrderableField getOrderableField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }

        final Field field = fields.get(id);
        return ORDERABLE.apply(field) ? (OrderableField)field : null;
    }

    @Nullable
    @Override
    public ConfigurableField getConfigurableField(final String id)
    {
        final OrderableField field = getOrderableField(id);
        if (field instanceof ConfigurableField)
        {
            return (ConfigurableField)field;
        }

        if (LOG.isInfoEnabled() && field != null)
        {
            LOG.info("Field found for " + id + " but was not a ConfigurableField. Type is " + field.getClass().getName() + " : " + field);
        }
        return null;
    }

    @Override
    public Set<OrderableField> getOrderableFields()
    {
        return Collections.unmodifiableSet(getAvailableOrderableFields());
    }

    @Override
    public Set<NavigableField> getNavigableFields()
    {
        return Collections.unmodifiableSet(getAvailableNavigableFields());
    }

    @Override
    public boolean isNavigableField(final String id)
    {
        return isCustomField(id) || isNavigableField(fields.get(id));
    }

    @Override
    public boolean isNavigableField(final Field field)
    {
        // CustomField implements NavigableField, so checking for it would be redundant
        return field instanceof NavigableField;
    }

    @Override
    public NavigableField getNavigableField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }

        final Field field = fields.get(id);
        if (field instanceof NavigableField)
        {
            return (NavigableField)field;
        }
        throw new IllegalArgumentException("The field with id '" + id + "' is not a NavigableField.");
    }

    @Override
    public boolean isRequirableField(final String id)
    {
        return isCustomField(id) || fields.get(id) instanceof RequirableField;
    }

    @Override
    public boolean isRequirableField(final Field field)
    {
        // CustomField implements RequirableField, so checking for it would be redundant
        return field instanceof RequirableField;
    }

    @Override
    public boolean isMandatoryField(final String id)
    {
        return !isCustomField(id) && isMandatoryField(fields.get(id));
    }

    @Override
    public boolean isMandatoryField(final Field field)
    {
        return field instanceof MandatoryField;
    }

    @Override
    public boolean isRenderableField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id).isRenderable();
        }
        return fields.get(id) instanceof RenderableField;
    }

    @Override
    public boolean isRenderableField(final Field field)
    {
        if (isCustomField(field))
        {
            return ((CustomField) field).isRenderable();
        }
        return (field instanceof RenderableField);
    }

    @Override
    public boolean isUnscreenableField(final String id)
    {
        return !isCustomField(id) && fields.get(id) instanceof UnscreenableField;
    }

    @Override
    public boolean isUnscreenableField(final Field field)
    {
        return !isCustomField(field) && field instanceof UnscreenableField;
    }

    @Override
    public RequirableField getRequiredField(final String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }

        final Field field = fields.get(id);
        if (field instanceof RequirableField)
        {
            return (RequirableField)field;
        }
        throw new IllegalArgumentException("The field with id '" + id + "' is not a RequirableField.");
    }

    /**
     * Breaks circular dependency betweeen FieldManager and CustomFieldManager.
     *
     * @return CustomFieldManager component
     */
    private CustomFieldManager getCustomFieldManager()
    {
        if (customFieldManager == null)  // volatile read
        {
            customFieldManager = ComponentAccessor.getCustomFieldManager();  // volatile write
        }
        return customFieldManager;
    }

    /**
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    @Override
    public FieldLayoutManager getFieldLayoutManager()
    {
        //this is really a circular design.
        //all clients of this method should declare their dep. on fieldlayoutmanager
        //and let pico resolve them
        if (fieldLayoutManager == null)  // volatile read
        {
            fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();  // volatile write
        }
        return fieldLayoutManager;
    }

    /**
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    @Override
    public ColumnLayoutManager getColumnLayoutManager()
    {
        return ComponentAccessor.getColumnLayoutManager();
    }

    @Override
    public void refresh()
    {
        refreshSearchersAndIndexers();

        // Refresh the FieldLayoutManager (due to its caches of field layouts)
        getFieldLayoutManager().refresh();

        // Refresh the ColumnLayoutManager (due to its caches of column layouts)
        getColumnLayoutManager().refresh();
    }

    @Override
    public boolean isTimeTrackingOn()
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    private static boolean isVotingOn()
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_VOTING);
    }

    private static boolean isWatchingOn()
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    private static boolean isSubTasksOn()
    {
        return ComponentAccessor.getSubTaskManager().isSubTasksEnabled();
    }

    /**
     * Returns all navigable fields (including Custom Fields) minus the unavailable fields.
     *
     * @return All navigable fields (including Custom Fields) minus the unavailable fields.
     */
    @SuppressWarnings ("unchecked")
    private Set<NavigableField> getAvailableNavigableFields()
    {
        return getAvailableFields(navigableFields);
    }

    /**
     * Returns all Orderable fields (including Custom Fields) minus the unavailable fields.
     *
     * @return All Orderable fields (including Custom Fields) minus the unavailable fields.
     */
    @SuppressWarnings ("unchecked")
    private Set<OrderableField> getAvailableOrderableFields()
    {
        return getAvailableFields(orderableFields);
    }

    /**
     * Returns the list of available fields with all custom fields irrespective of scope. Takes the given list of
     * fields, adds custom fields, then removes Unavailable fields.
     *
     * @param allFields all Fields
     * @return the list of available fields with all custom fields irrespective of scope.
     */
    @SuppressWarnings("unchecked")
    private <T extends Field> Set<T> getAvailableFields(final Collection<T> allFields)
    {
        // Add custom fields that are relevant to the project and issue types
        final Set<T> availableFields = new HashSet<T>(allFields);
        availableFields.addAll((Collection<T>)getCustomFieldManager().getCustomFieldObjects());
        availableFields.removeAll(getUnavailableFields());
        return availableFields;
    }

    @Override
    public Set<Field> getUnavailableFields()
    {
        final Set<Field> unavailableFields = new HashSet<Field>();
        if (!isTimeTrackingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.TIMETRACKING));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_SPENT));
            unavailableFields.add(fields.get(IssueFieldConstants.PROGRESS));
            unavailableFields.add(fields.get(IssueFieldConstants.WORKLOG));
        }
        if (!isSubTasksOn() || !isTimeTrackingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_SPENT));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_PROGRESS));
        }
        if (!isVotingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.VOTES));
        }
        if (!isWatchingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.WATCHES));
        }
        if (!isSubTasksOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.SUBTASKS));
        }

        return unavailableFields;
    }

    private void refreshSearchersAndIndexers()
    {
        // @todo These must be statically called since otherwise a cyclic dependency will occur. There really needs to be a CacheManager that handles all these dependent caches
        ComponentAccessor.getComponent(FieldConfigSchemeManager.class).init();

        // Resets the issue search manager
        final IssueSearcherManager issueSearcherManager = ComponentAccessor.getComponentOfType(IssueSearcherManager.class);
        issueSearcherManager.refresh();

        final FieldIndexerManager fieldIndexerManager = ComponentAccessor.getComponentOfType(FieldIndexerManager.class);
        fieldIndexerManager.refresh();
    }


    static class IsOrderable implements Predicate<Field>
    {
        @Override
        public boolean apply(final Field field)
        {
            // Special case: ProjectSystemField is not orderable, even though it implements OrderableField
            return field instanceof OrderableField && !(field instanceof ProjectSystemField);
        }
    }
}
