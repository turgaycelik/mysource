package com.atlassian.jira.issue.fields;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.event.ComponentCreatedInlineEvent;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.rest.ComponentsRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.ComponentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ComponentSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ComponentStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.jira.web.bean.DefaultBulkMoveHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A field implementation to render {@link com.atlassian.jira.bc.project.component.ProjectComponent} values.
 */
public class ComponentsSystemField extends AbstractOrderableNavigableFieldImpl
        implements HideableField, RequirableField, ComponentsField, RestAwareField, RestFieldOperations
{
    private static final Logger log = Logger.getLogger(ComponentsSystemField.class);
    private static final String COMPONENTS_NAME_KEY = "issue.field.components";
    private static final Long UNKNOWN_COMPONENTS_ID = -1L;

    private final ProjectComponentManager projectComponentManager;
    private final ComponentStatisticsMapper componentStatisticsMapper;
    private final ProjectManager projectManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final EventPublisher eventPublisher;

    /**
     * true if the long id is a valid component id
     */
    private final Predicate<Long> validComponentId = new Predicate<Long>()
    {
        @Override
        public boolean apply(Long componentId)
        {
            try
            {
                if (UNKNOWN_COMPONENTS_ID.equals(componentId))
                {
                    return true;
                }
                ProjectComponent component = projectComponentManager.find(componentId);
                return true;
            }
            catch (EntityNotFoundException e)
            {
                return false;
            }
        }
    };

    public ComponentsSystemField(VelocityTemplatingEngine templatingEngine, ProjectComponentManager projectComponentManager, ApplicationProperties applicationProperties,
            PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, ComponentStatisticsMapper componentStatisticsMapper,
            ComponentSearchHandlerFactory componentSearchHandlerFactory, final ProjectManager projectManager, JiraBaseUrls jiraBaseUrls, EventPublisher eventPublisher)
    {
        super(IssueFieldConstants.COMPONENTS, COMPONENTS_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, componentSearchHandlerFactory);
        this.projectComponentManager = projectComponentManager;
        this.componentStatisticsMapper = componentStatisticsMapper;
        this.projectManager = projectManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.eventPublisher = eventPublisher;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        LongIdsValueHolder vh = LongIdsValueHolder.fromFieldValuesHolder(getId(), operationContext.getFieldValuesHolder());
        if (vh != null)
        {
            vh.validateIds(validComponentId); // remove any remaining invalid ids from the list
        }

        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Currently selected Components for this issue.
        velocityParams.put("currentComponents", vh);
        velocityParams.put("frotherInputText", vh == null ? null : vh.getInputText());
        // List of possible Components for the project
        velocityParams.put("components", projectComponentManager.convertToGenericValues(getComponents(issue.getProjectObject())));
        velocityParams.put("unknownComponentId", UNKNOWN_COMPONENTS_ID);
        if (fieldLayoutItem != null)
        {
            velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
        }
        velocityParams.put("createPermission", getPermissionManager().hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), authenticationContext.getUser()));

        return renderTemplate("components-edit.vm", velocityParams);
    }

    /**
     * Returns HTML that should be shown when components are being bulk edited.
     * <p/>
     * The HTML displayed for Bulk Move of Components needs to allow the user to specify mappings for each old component
     * present in the currently selected issues.
     */
    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);
            final BulkMoveHelper bulkMoveHelper = new DefaultBulkMoveHelper();
            final Function<Object, String> componentNameResolver = new Function<Object, String>()
            {
                public String get(final Object input)
                {
                    try
                    {
                        return projectComponentManager.find((Long) input).getName();
                    }
                    catch (EntityNotFoundException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
            final Function<Issue, Collection<Object>> issueValueResolver = new Function<Issue, Collection<Object>>()
            {
                public Collection<Object> get(final Issue input)
                {
                    final Map fieldValuesHolder = new LinkedHashMap();
                    populateFromIssue(fieldValuesHolder, input);
                    return (Collection<Object>) fieldValuesHolder.get(getId());
                }
            };
            final Map<Long, BulkMoveHelper.DistinctValueResult> distinctComponentValues = bulkMoveHelper.getDistinctValuesForMove(bulkEditBean, this, issueValueResolver, componentNameResolver);

            final Issue issue = bulkEditBean.getFirstTargetIssueObject();
            final Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

            // the distinct values which need to be mapped
            velocityParams.put("valuesToMap", distinctComponentValues);
            velocityParams.put("bulkMoveHelper", bulkMoveHelper);

            // List of possible Components for the project
            velocityParams.put("components", projectComponentManager.convertToGenericValues(getComponents(issue.getProjectObject())));
            velocityParams.put("unknownComponentId", UNKNOWN_COMPONENTS_ID);
            if (fieldLayoutItem != null)
            {
                velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
            }
            return renderTemplate("components-bulkmove.vm", velocityParams);
        }
        else
        {
            return super.getBulkEditHtml(operationContext, action, bulkEditBean, displayParameters);
        }
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("components", issue.getComponents());
        velocityParams.put("projectManager", projectManager);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("components", value);
        velocityParams.put("projectManager", projectManager);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("components-view.vm", velocityParams);
    }

    private Collection<ProjectComponent> getComponents(Project project)
    {
        return getComponents(project.getId());
    }

    private Collection<ProjectComponent> getComponents(Long id)
    {
        return projectComponentManager.findAllForProject(id);
    }


    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        final LongIdsValueHolder componentIds = LongIdsValueHolder.fromFieldValuesHolder(getId(), fieldValuesHolder);
        final Project project = issue.getProjectObject();
        if (componentIds != null && componentIds.size() > 1)
        {
            for (Object componentId : componentIds)
            {
                Long l = (Long) componentId;

                if (UNKNOWN_COMPONENTS_ID.equals(l))
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.noneselectedwithother"), Reason.VALIDATION_FAILED);
                    return;
                }
            }
        }

        if (validateForRequiredField(errorCollectionToAddTo, i18n, fieldScreenRenderLayoutItem, componentIds, project))
        {
            // only do this validation if they are valid Ids            
            validateComponentForProject(errorCollectionToAddTo, i18n, componentIds, project);
        }

        if (componentIds != null)
        {
            if (getPermissionManager().hasPermission(ADMINISTER_PROJECTS, issue.getProjectObject(), authenticationContext.getUser()))
            {
                for (String componentName : componentIds.getValuesToAdd())
                {
                    if (StringUtils.isBlank(componentName))
                    {
                        errorCollectionToAddTo.addError(getId(), i18n.getText("admin.projects.component.namenotset"), Reason.VALIDATION_FAILED);
                    }
                    else
                    {
                        final ProjectComponent component = projectComponentManager.findByComponentName(project.getId(), componentName);
                        if (component != null)
                        {
                            errorCollectionToAddTo.addError(getId(), i18n.getText("admin.projects.component.namenotunique", component), Reason.VALIDATION_FAILED);
                        }
                    }
                }
            }
            else
            {
                final String bad = componentIds.getInputText();
                if (StringUtils.isNotBlank(bad))
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.components.does.not.exist", bad), Reason.VALIDATION_FAILED);
                }
            }
        }
    }

    @VisibleForTesting
    boolean validateForRequiredField(ErrorCollection errorCollectionToAddTo, I18nHelper i18n, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, LongIdsValueHolder componentIds, Project project)
    {
        // The check for 'Unknown' id is needed for bulk-edit
        boolean noExistingComponentsSpecified = componentIds == null || componentIds.isEmpty() || componentIds.contains(UNKNOWN_COMPONENTS_ID);
        boolean noNewComponentsSpecified = componentIds != null && componentIds.getValuesToAdd().isEmpty();

        if (fieldScreenRenderLayoutItem.isRequired() && noExistingComponentsSpecified && noNewComponentsSpecified)
        {
            final Collection<ProjectComponent> components = getComponents(project);
            // Check if we have components configured in the Project:
            if (components.isEmpty())
            {
                errorCollectionToAddTo.addErrorMessage(i18n.getText("createissue.error.components.required", i18n.getText(getNameKey()), project.getName()), Reason.VALIDATION_FAILED);
            }
            else
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())), Reason.VALIDATION_FAILED);
            }
            return false;
        }
        return true;
    }

    private void validateComponentForProject(ErrorCollection errorCollectionToAddTo, I18nHelper i18n, LongIdsValueHolder componentIds, Project project)
    {
        if (componentIds != null)
        {
            StringBuilder sb = null;
            for (Long componentId : componentIds)
            {
                if (componentId == -1)
                {
                    // Unkown should have already been validated
                    return;
                }
                try
                {
                    final ProjectComponent component = projectComponentManager.find(componentId);
                    if (!component.getProjectId().equals(project.getId()))
                    {
                        if (sb == null)
                        {
                            sb = new StringBuilder(component.getName()).append("(").append(component.getId()).append(")");
                        }
                        else
                        {
                            sb.append(", ").append(component.getName()).append("(").append(component.getId()).append(")");
                        }

                    }
                }
                catch (EntityNotFoundException e)
                {
                    componentIds.addBadId(componentId);
                    return;
                }
            }
            if (sb != null)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.components.not.valid.for.project", sb.toString(), project.getName()), Reason.VALIDATION_FAILED);
            }
        }
    }

    protected Object getRelevantParams(Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        LongIdsValueHolder vh = new LongIdsValueHolder(value);
        vh.validateIds(validComponentId);
        return vh;
    }

    public Object getValueFromParams(Map params)
    {
        List<ProjectComponent> components = getComponentsFromParams(params);
        // existing behaviour for no value:
        if (components == null)
        {
            return Collections.emptyList();
        }
        // Convert component objects to GenericValues for backward compatibility:
        return projectComponentManager.convertToGenericValues(components);
    }

    /**
     * Returns the list of components contained in the given parameters Map, or null if not contained.
     * <p/>
     * This is used by the DefaultAssigneeResolver to find components about to be set to an issue.
     *
     * @param params the map of parameters.
     * @return the list of components contained in the given parameters Map, or null if not contained.
     */
    public List<ProjectComponent> getComponentsFromParams(Map params)
    {
        List<Long> componentIds = LongIdsValueHolder.fromFieldValuesHolder(getId(), params);
        if (componentIds == null)
        {
            // by contract this returns null because DefaultAssigneeResolver needs to know if a value is in the map or not.
            return null;
        }
        if (componentIds.isEmpty() || componentIds.contains(UNKNOWN_COMPONENTS_ID))
        {
            return Collections.emptyList();
        }
        else
        {
            try
            {
                return projectComponentManager.getComponents(componentIds);
            }
            catch (EntityNotFoundException e)
            {
                throw new FieldValidationException("Trying to retrieve non existant component");
            }
        }
    }

    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), new LongIdsValueHolder(getComponentIds(issue, stringValue)));
    }

    private List<Long> getComponentIds(Issue issue, String stringValue) throws FieldValidationException
    {
        // Use a set to ensure that there are no duplicate component ids.
        final Set<Long> components = new HashSet<Long>();
        final Project project = issue.getProjectObject();

        // Check if the components were provided
        if (TextUtils.stringSet(stringValue))
        {
            // If so set the values
            String[] componentParams = StringUtils.split(stringValue, ",");
            for (String componentParam : componentParams)
            {
                try
                {
                    components.add(Long.valueOf(componentParam));
                }
                catch (NumberFormatException e)
                {
                    // Try getting the version by name
                    final ProjectComponent component = projectComponentManager.findByComponentName(project.getId(), componentParam);
                    if (component != null)
                    {
                        components.add(component.getId());
                    }
                    else
                    {
                        throw new FieldValidationException("Invalid component name '" + componentParam + "'.");
                    }
                }
            }
        }

        return new ArrayList<Long>(components);
    }

    /**
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Collection currentComponents = (Collection) modifiedValue.getOldValue();
        Collection newComponents = (Collection) modifiedValue.getNewValue();
        if (currentComponents == null || currentComponents.isEmpty())
        {
            if (newComponents != null)
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, newComponents));
            }
        }
        else
        {
            if (!compareIdSets(newComponents, currentComponents))
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, newComponents));
            }
        }
    }

    /**
     * Compare the two genericValue collections and make sure they both contain the same set of ids
     *
     * @param newComponentGVs collection of old {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @param currentComponentGVs collection of new {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @return true if they have the same set of ids or if they are both null, false otherwise
     */
    protected boolean compareIdSets(Collection /*<GenericValue>*/ newComponentGVs, Collection /*<GenericValue>*/ currentComponentGVs)
    {
        if (newComponentGVs != null && currentComponentGVs != null)
        {
            Collection /*<Long>*/ newComponentIds = CollectionUtils.collect(newComponentGVs, JiraEntityUtils.GV_TO_ID_TRANSFORMER);
            Collection /*<Long>*/ currentComponentIds = CollectionUtils.collect(currentComponentGVs, JiraEntityUtils.GV_TO_ID_TRANSFORMER);
            return valuesEqual(new HashSet(newComponentIds), new HashSet(currentComponentIds));
        }
        return newComponentGVs == null && currentComponentGVs == null;
    }

    private List updateIssueValue(Issue issue, Object value)
    {
        try
        {
            return JiraEntityUtils.updateDependentEntitiesCheckId(issue.getGenericValue(), (Collection) value, IssueRelationConstants.COMPONENT, "Component");
        }
        catch (GenericEntityException e)
        {
            log.error("Error while saving components '" + value + "' for issue with id '" + issue.getLong("id") + "'.");
        }

        return Collections.EMPTY_LIST;
    }

    public void createValue(Issue issue, Object value)
    {
        updateIssueValue(issue, value);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        //JRA-13011: The default for this field needs to be the empty list. We need this to ensure so that invalid
        //components are not kept on the issue when it moves between projects. This is really only a problem when moving an issue's
        //sub-tasks during a move operation since there is no GUI to configure new components for sub-taks.
        fieldValuesHolder.put(getId(), new LongIdsValueHolder(Collections.<Long>emptyList()));
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        Collection<GenericValue> components = issue.getComponents();
        if (components != null)
        {
            List<Long> componentIds = new LinkedList<Long>();
            for (GenericValue componentGV : components)
            {
                componentIds.add(componentGV.getLong("id"));
            }

            fieldValuesHolder.put(getId(), new LongIdsValueHolder(componentIds));
        }
        else
        {
            fieldValuesHolder.put(getId(), null);
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return Collections.EMPTY_LIST;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        final LongIdsValueHolder components = LongIdsValueHolder.fromFieldValuesHolder(getId(), fieldValueHolder);
        if (components != null)
        {
            final Set<String> componentNames = components.getValuesToAdd();
            final List<ProjectComponent> allComponents = newArrayList();
            for (String componentName : componentNames)
            {
                final ProjectComponent newComponent = projectComponentManager.create(componentName, null, null, AssigneeTypes.PROJECT_DEFAULT, issue.getProjectId());
                allComponents.add(newComponent);
                eventPublisher.publish(new ComponentCreatedInlineEvent(newComponent));
            }
            allComponents.addAll(getComponentsFromParams(fieldValueHolder));

            //if we created new components replace the newly added component labes with ids in the field values holder.
            //This is so that bulk edits will not try to re-create the same components over and over.
            if (!components.getValuesToAdd().isEmpty())
            {
                final Iterable<Long> componentIds = transform(allComponents, new com.google.common.base.Function<ProjectComponent, Long>()
                {
                    @Override
                    public Long apply(final ProjectComponent input)
                    {
                        return input.getId();
                    }
                });
                fieldValueHolder.put(getId(), new LongIdsValueHolder(newArrayList(componentIds)));
            }
            issue.setComponentObjects(allComponents);
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Issue originalIssue : (Collection<Issue>) originalIssues)
        {
            // If the projects are different then need to ask user to specify new component
            if (projectMoved(originalIssue, targetIssue))
            {
                if (originalIssue.getComponents().isEmpty())
                {
                    // If no components are set only need to ask the user if the target field layout has components as required
                    if (targetFieldLayoutItem.isRequired())
                    {
                        return new MessagedResult(true);
                    }
                }
                else
                {
                    return new MessagedResult(true);
                }
            }
            else
            {
                // Same project (different issue type) - need to see if the field is required in the target field layout
                if (originalIssue.getComponents().isEmpty() && targetFieldLayoutItem.isRequired())
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    private boolean projectMoved(final Issue originalIssue, final Issue targetIssue)
    {
        // JRA-20184: Should only check the ID, the other fields can change.
        // Don't do any null checks - a null Project or ID is an unrecoverable error.
        final Long originalProjectId = originalIssue.getProjectObject().getId();
        final Long targetProjectId = targetIssue.getProjectObject().getId();
        return !originalProjectId.equals(targetProjectId);
    }

    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {

        // Preselect components with the same name
        final Collection<String> currentComponentNames = getComponentNames(originalIssue.getComponents());
        final Collection<ProjectComponent> possibleComponents = getComponents(targetIssue.getProjectObject());
        final List<Long> componentIds = new LinkedList<Long>();

        for (ProjectComponent possibleComponent : possibleComponents)
        {
            if (currentComponentNames.contains(possibleComponent.getName()))
            {
                componentIds.add(possibleComponent.getId());
            }
        }

        fieldValuesHolder.put(getId(), new LongIdsValueHolder(componentIds));
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setComponents(Collections.EMPTY_LIST);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        Collection components = issue.getComponents();
        return (components != null && !components.isEmpty());
    }

    private Collection<String> getComponentNames(Collection<GenericValue> components)
    {
        Collection<String> componentNames = new HashSet<String>();
        for (final GenericValue componentGV : components)
        {
            componentNames.add(componentGV.getString("name"));
        }

        return componentNames;
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Can bulk-edit this field only if all selected issue belong to one project
        if (bulkEditBean.isMultipleProjects())
        {
            // Let the user know that selected issues belong to more than one project so the action is not available
            return "bulk.edit.unavailable.multipleprojects";
        }

        final Project project = bulkEditBean.getSingleProject();
        // Ensure that the project has components
        if (getComponents(project.getId()).isEmpty())
        {
            return "bulk.edit.unavailable.nocomponents";
        }

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (FieldLayout fieldLayout : (Collection<FieldLayout>) bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
        for (Issue issue : (List<Issue>) bulkEditBean.getSelectedIssues())
        {
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    //////////////////////////////////////////// NavigableField implementation ////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.components";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return componentStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("components", issue.getComponents());
        return renderTemplate("components-columnview.vm", velocityParams);
    }

    // this is a method that allows the AssigneeSystemField to find out about modified components before they
    // have been committed to the issue object
    public Collection getComponents(Issue issue, Map fieldValuesHolder)
    {
        Collection valuesFromMap = (Collection) getValueFromParams(fieldValuesHolder);
        if (valuesFromMap == null || valuesFromMap.isEmpty())
        {
            return issue.getComponents();
        }
        return valuesFromMap;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get all options for the config
        Collection<ProjectComponent> components = getComponents(fieldTypeInfoContext.getIssueContext().getProjectObject());
        return new FieldTypeInfo(components, null);
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.COMPONENT_TYPE, getId());
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new ComponentsRestFieldOperationsHandler(projectComponentManager, authenticationContext.getI18nHelper());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        Collection<ProjectComponent> components = issue.getComponentObjects();
        Collection<ComponentJsonBean> beans = ComponentJsonBean.shortBeans(components, jiraBaseUrls);
        return new FieldJsonRepresentation(new JsonData(beans));
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }

}
